/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mule.MuleManager;
import org.mule.config.ConfigurationException;
import org.mule.config.builders.MuleXmlConfigurationBuilder;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ChannelStatisticsController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ExtensionController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MigrationController;
import com.webreach.mirth.server.controllers.SystemLogger;
import com.webreach.mirth.server.controllers.UserController;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.GlobalVariableStore;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.VMRegistry;
import com.webreach.mirth.util.PropertyLoader;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class Mirth extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean running = false;
	private Properties mirthProperties = null;
	private Properties versionProperties = null;
	private MuleManager muleManager = null;
	private HttpServer webServer = null;
	private CommandQueue commandQueue = CommandQueue.getInstance();
	private SystemLogger systemLogger = SystemLogger.getInstance();
	private MirthManager manager = new MirthManager();
	private ConfigurationController configurationController = ConfigurationController.getInstance();
	private ChannelController channelController = ChannelController.getInstance();
	private UserController userController = UserController.getInstance();
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private ChannelStatisticsController channelStatisticsController = ChannelStatisticsController.getInstance();
	private ExtensionController extensionController =  ExtensionController.getInstance();
    private MigrationController migrationController =  MigrationController.getInstance();
    
	public static void main(String[] args) {
		Mirth mirth = new Mirth();
		mirth.run();
	}

	public MirthManager getManager() {
		return this.manager;
	}

	public void run() {
		if (initResources()) {
			logger.info("starting mirth server...");
			running = true;
			startWebServer();
			
			// initialize controllers
			messageObjectController.initialize();
			configurationController.initialize();
            migrationController.initialize();
            extensionController.initialize();
			channelController.initialize();
			userController.initialize();
            
            extensionController.startPlugins();
			// add the start command to the queue
			CommandQueue.getInstance().clear();
			CommandQueue.getInstance().addCommand(new Command(Command.Operation.START));
			
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());

			// pulls commands off of the command queue
			while (running) {
				Command command = commandQueue.getCommand();

				if (command.getOperation().equals(Command.Operation.START)) {
					startMule();
					printSplashScreen();
				} else if (command.getOperation().equals(Command.Operation.STOP)) {
					stopMule();
				} else if (command.getOperation().equals(Command.Operation.RESTART)) {
					restartMule();
				} else if (command.getOperation().equals(Command.Operation.SHUTDOWN)) {
					shutdown();
				}
			}
		} else {
			logger.error("could not initialize resources");
		}
	}

	/**
	 * Returns true if the resources required by the server have been
	 * sucessfully loaded
	 * 
	 * @return true if the resources required by the server have been
	 *         sucessfully loaded
	 */
	public boolean initResources() {
		mirthProperties = PropertyLoader.loadProperties("mirth");
		versionProperties = PropertyLoader.loadProperties("version");

		return (mirthProperties != null);
	}

	/**
	 * Shuts down the server.
	 * 
	 */
	public void shutdown() {
		logger.info("shutting down mirth due to normal request");
		stopMule();
		channelStatisticsController.updateAllStatistics(); // do one last
		// update to the
		// stats table
		stopWebServer();
        extensionController.stopPlugins();
		running = false;
	}

	/**
	 * Restarts the Mule server. This is accomplished by stopping and starting
	 * the server.
	 * 
	 */
	private void restartMule() {
		logger.debug("retarting mule");
		stopMule();
		startMule();
	}

	/**
	 * Starts the Mule server.
	 * 
	 */
	private void startMule() {
		configurationController.setEngineStarting(true);

		try {
			String configurationFilePath = configurationController.getLatestConfiguration().getAbsolutePath();
			logger.debug("starting mule with configuration file: " + configurationFilePath);

			// disables validation of Mule configuration files
			System.setProperty("org.mule.xml.validate", "false");
			VMRegistry.getInstance().rebuild();
			MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
            
			// clear global map and do channel deploy scripts if the user specified to
            if(configurationController.getServerProperties().getProperty("clearGlobal") == null || configurationController.getServerProperties().getProperty("clearGlobal").equals("1"))
                GlobalVariableStore.getInstance().globalVariableMap.clear();
            
            List<Channel> channels = channelController.getChannel(null);
            configurationController.compileScripts(channels);
            
            configurationController.executeGlobalDeployScript();
            configurationController.executeChannelDeployScripts(channelController.getChannel(null));
            
			muleManager = (MuleManager) builder.configure(configurationFilePath);
            
		} catch (ConfigurationException e) {
			logger.warn("Error deploying channels.", e);

			// if deploy fails, log to system events
			SystemEvent event = new SystemEvent("Error deploying channels.");
			event.setLevel(SystemEvent.Level.HIGH);
			event.setDescription(StackTracePrinter.stackTraceToString(e));
			systemLogger.logSystemEvent(event);

			// remove the errant configuration
			configurationController.deleteLatestConfiguration();
		} catch (ControllerException e) {
			logger.warn("Could not retrieve latest configuration.", e);
		} catch (Exception e) {
			logger.error("Could not start Mule.", e);
		}
        
        configurationController.setEngineStarting(false);
	}

	/**
	 * Stops the Mule server.
	 * 
	 */
	private void stopMule() {
		logger.debug("stopping mule");

		if (muleManager != null) {
			try {
				if (muleManager.isStarted()) {
                    configurationController.executeChannelShutdownScripts(channelController.getChannel(null));
                    configurationController.executeGlobalShutdownScript();
                    muleManager.stop();
				}
			} catch (Exception e) {
				logger.error(e);
			} finally {
				logger.debug("disposing mule instance");
				muleManager.dispose();
			}
		}
	}

	/**
	 * Starts the Jetty web server.
	 * 
	 */
	private void startWebServer() {
		logger.debug("starting jetty web server");

		try {
			// this disables validaiton of the web.xml file
			// which causes exceptions when Mirth is run
			// behind a firewall and the resources cannot be
			// accessed
			System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");
			// this disables a "form too large" error for occuring by setting
			// form size to infinite
			System.setProperty("org.mortbay.http.HttpRequest.maxFormContentSize", "0");

			webServer = new HttpServer();

			// add HTTPS listener
			SslListener sslListener = new SslListener();
			sslListener.setPort(Integer.valueOf(mirthProperties.getProperty("https.port")).intValue());
			sslListener.setKeystore(ConfigurationController.mirthHomeDir + System.getProperty("file.separator") + mirthProperties.getProperty("https.keystore"));
			sslListener.setPassword(mirthProperties.getProperty("https.password"));
			sslListener.setKeyPassword(mirthProperties.getProperty("https.keypassword"));
			webServer.addListener(sslListener);

			// add HTTP listener
			SocketListener listener = new SocketListener();
			listener.setPort(Integer.valueOf(mirthProperties.getProperty("http.port")).intValue());
			webServer.addListener(listener);
			
			// Create the lib context
			HttpContext libContext = new HttpContext();
			libContext.setContextPath("/client-lib/");
			webServer.addContext(libContext);
			
			// Serve static content from the lib context
			File connectors = new File(ClassPathResource.getResourceURI("connectors"));
            File plugins = new File(ClassPathResource.getResourceURI("plugins"));
			String libPath = ConfigurationController.mirthHomeDir + System.getProperty("file.separator") + "client-lib";

			libContext.setResourceBase(libPath);
			libContext.addHandler(new ResourceHandler());

			// Create the connectors context
			HttpContext connectorsContext = new HttpContext();
			connectorsContext.setContextPath("/connectors/");
			webServer.addContext(connectorsContext);
			
			// Serve static content from the connectors context
			String connectorsPath = connectors.getPath(); //ConfigurationController.mirthHomeDir + System.getProperty("file.separator") + "extensions" + System.getProperty("file.separator") + "connectors";
			connectorsContext.setResourceBase(connectorsPath);
			connectorsContext.addHandler(new ResourceHandler());
            
		    // Create the connectors context
            HttpContext pluginsContext = new HttpContext();
            pluginsContext.setContextPath("/plugins/");
            webServer.addContext(pluginsContext);
            
            // Serve static content from the connectors context
            String pluginsPath = plugins.getPath();
            pluginsContext.setResourceBase(pluginsPath);
            pluginsContext.addHandler(new ResourceHandler());
            
		    // Create the public_html context
            HttpContext publicContext = new HttpContext();
            publicContext.setContextPath("/");
            webServer.addContext(publicContext);
            
            String publicPath = ConfigurationController.mirthHomeDir + System.getProperty("file.separator") + "public_html";
            publicContext.setResourceBase(publicPath);
            publicContext.addHandler(new ResourceHandler());
            
			// Create a servlet container
			ServletHandler servlets = new ServletHandler();
			HttpContext servletContext = new HttpContext();
			servletContext.setContextPath("/");
			servletContext.addHandler(servlets);
			webServer.addContext(servletContext);

			// Map a servlet onto the container
			servlets.addServlet("Alerts", "/alerts", "com.webreach.mirth.server.servlets.AlertServlet");
			servlets.addServlet("Channels", "/channels", "com.webreach.mirth.server.servlets.ChannelServlet");
			servlets.addServlet("ChannelStatistics", "/channelstatistics", "com.webreach.mirth.server.servlets.ChannelStatisticsServlet");
			servlets.addServlet("ChannelStatus", "/channelstatus", "com.webreach.mirth.server.servlets.ChannelStatusServlet");
			servlets.addServlet("Configuration", "/configuration", "com.webreach.mirth.server.servlets.ConfigurationServlet");
			servlets.addServlet("MessageObject", "/messages", "com.webreach.mirth.server.servlets.MessageObjectServlet");
            servlets.addServlet("Extensions", "/extensions", "com.webreach.mirth.server.servlets.ExtensionServlet");
			servlets.addServlet("SystemEvent", "/events", "com.webreach.mirth.server.servlets.SystemEventServlet");
			servlets.addServlet("Users", "/users", "com.webreach.mirth.server.servlets.UserServlet");
			servlets.addServlet("WebStart", "/webstart.jnlp", "com.webreach.mirth.server.servlets.WebStartServlet");
			servlets.addServlet("Activation", "/activation.jnlp", "com.webreach.mirth.server.servlets.ActivationServlet");			

			// start the web server
			webServer.start();

			logger.debug("started jetty web server on ports: " + listener.getPort() + ", " + sslListener.getPort());
		} catch (Exception e) {
			logger.warn("Could not start web server.", e);
		}
	}

	/**
	 * Stops the Jetty web server.
	 * 
	 */
	private void stopWebServer() {
		logger.debug("stopping jetty web server");

		try {
			webServer.stop();
		} catch (Exception e) {
			logger.warn("Could not stop web server.", e);
		}
	}

	private class ShutdownHook extends Thread {
		public void run() {
			shutdown();
		}
	}

	/**
	 * Displays the splash screen information, including the server version and
	 * build date, to the system console.
	 * 
	 */
	private void printSplashScreen() {
		String version = versionProperties.getProperty("mirth.version");
		String buildDate = versionProperties.getProperty("mirth.date");
		System.out.println("Mirth " + version + " (" + buildDate + ") server successfully started: " + (new Date()).toString());
		System.out.println("This product includes software developed by SymphonySoft Limited (http://www.symphonysoft.com) and its contributors.");
		System.out.println("Running Java " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ")");
	}

}
