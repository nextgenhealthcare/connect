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
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mule.MuleManager;
import org.mule.impl.MuleDescriptor;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.routing.UMOOutboundRouter;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.builders.MuleManagerBuilder;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ChannelStatisticsController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.EventController;
import com.webreach.mirth.server.controllers.ExtensionController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MigrationController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.UserController;
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
    private UMOManager umoManager = null;
    private HttpServer httpServer = null;
    private HttpServer servletContainer = null;
    private CommandQueue commandQueue = CommandQueue.getInstance();
    private MirthManager manager = new MirthManager();

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private ChannelStatisticsController channelStatisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private MigrationController migrationController = ControllerFactory.getFactory().createMigrationController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    public static void main(String[] args) {
        Mirth mirth = new Mirth();
        mirth.run();
        System.exit(0);
    }

    public MirthManager getManager() {
        return this.manager;
    }

    public void run() {
        if (initResources()) {
            logger.info("starting mirth server...");

            // Check the ports to see if they are already in use
            boolean httpPort = testPort(PropertyLoader.getProperty(mirthProperties, "http.port"), "http.port");
            boolean httpsPort = testPort(PropertyLoader.getProperty(mirthProperties, "https.port"), "https.port");
            boolean jmxPort = testPort(PropertyLoader.getProperty(mirthProperties, "jmx.port"), "jmx.port");

            if (!httpPort || !httpsPort || !jmxPort) {
                return;
            }

            running = true;

            // add the start command to the queue
            CommandQueue.getInstance().clear();
            CommandQueue.getInstance().addCommand(new Command(Command.Operation.START_SERVER));

            Runtime.getRuntime().addShutdownHook(new ShutdownHook());

            // pulls commands off of the command queue
            while (running) {
                Command command = commandQueue.getCommand();

                if (command.getOperation().equals(Command.Operation.START_SERVER)) {
                    startup();
                } else if (command.getOperation().equals(Command.Operation.SHUTDOWN_SERVER)) {
                    shutdown();
                } else if (command.getOperation().equals(Command.Operation.START_ENGINE)) {
                    startMule();
                } else if (command.getOperation().equals(Command.Operation.STOP_ENGINE)) {
                    stopMule();
                } else if (command.getOperation().equals(Command.Operation.RESTART_ENGINE)) {
                    restartMule();
                } else if (command.getOperation().equals(Command.Operation.DEPLOY_CHANNELS)) {
                    deployChannels((List<Channel>) command.getParameter());
                } else if (command.getOperation().equals(Command.Operation.UNDEPLOY_CHANNELS)) {
                    undeployChannels((List<String>) command.getParameter());
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
     * Starts up the server.
     * 
     */
    public void startup() {
        startWebServer();
        messageObjectController.removeAllFilterTables();
        eventController.removeAllFilterTables();
        extensionController.uninstallExtensions();
        migrationController.migrate();
        migrationController.migrateExtensions();
        channelStatisticsController.start();
        channelController.loadChannelCache();
        configurationController.loadEncryptionKey();
        userController.resetUserStatus();
        monitoringController.initPlugins();
        extensionController.startPlugins();

        // disable the velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");
        startMule();
        printSplashScreen();
    }

    /**
     * Shuts down the server.
     * 
     */
    public void shutdown() {
        logger.info("shutting down mirth due to normal request");
        stopMule();
        channelStatisticsController.updateAllStatistics();
        // do one last update to the stats table
        stopWebServer();
        extensionController.stopPlugins();
        channelStatisticsController.shutdown();
        stopDatabase();
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

    private void deployChannels(List<Channel> channels) {
        try {
            // unregister existing channels
            for (Channel channel : channels) {
                MuleDescriptor oldDescriptor = (MuleDescriptor) umoManager.getModel().getDescriptor(channel.getId());

                if (oldDescriptor != null) {
                    channelController.getChannelCache().remove(channel);
                    umoManager.getModel().unregisterComponent(oldDescriptor);
                    MuleManagerBuilder.unregisterTransformers(umoManager, oldDescriptor.getInboundRouter().getEndpoints());
                    UMOOutboundRouter outboundRouter = (UMOOutboundRouter) oldDescriptor.getOutboundRouter().getRouters().iterator().next();
                    MuleManagerBuilder.unregisterTransformers(umoManager, outboundRouter.getEndpoints());
                }
            }

            // TODO: delete old scripts from script table

            // update the manager with the new classes
            MuleManagerBuilder managerBuilder = new MuleManagerBuilder();
            managerBuilder.getConfiguration(umoManager, channels, extensionController.getConnectorMetaData());
            configurationController.executeChannelDeployScripts(channelController.getChannel(null));
        } catch (Exception e) {
            logger.error("Error deploying channels.", e);
        }
    }

    private void undeployChannels(List<String> channelIds) {
        try {
            for (String channelId : channelIds) {
                MuleDescriptor oldDescriptor = (MuleDescriptor) umoManager.getModel().getDescriptor(channelId);
                channelController.getChannelCache().remove(channelController.getChannelCache().get(channelId));
                umoManager.getModel().unregisterComponent(oldDescriptor);
            }

            // TODO: delete old scripts from script table
        } catch (Exception e) {
            logger.error("Error un-deploying channels.", e);
        }
    }

    /**
     * Starts the Mule server.
     * 
     */
    private void startMule() {
        try {
            // clear global map and do channel deploy scripts if the user
            // specified to
            if (configurationController.getServerProperties().getProperty("server.resetglobalvariables") == null || configurationController.getServerProperties().getProperty("server.resetglobalvariables").equals("1")) {
                GlobalVariableStore.getInstance().globalVariableMap.clear();
            }
        } catch (Exception e) {
            logger.warn("Could not clear the global map.", e);
        }

        configurationController.setEngineStarting(true);

        try {
            // disables validation of Mule configuration files
            System.setProperty("org.mule.xml.validate", "false");
            VMRegistry.getInstance().rebuild();
            List<Channel> channels = channelController.getChannel(null);
            configurationController.compileScripts(channels);
            configurationController.executeGlobalDeployScript();
            umoManager = MuleManager.getInstance();
            MuleManagerBuilder managerBuilder = new MuleManagerBuilder();
            managerBuilder.loadDefaultConfiguration(umoManager);
            deployChannels(channelController.getChannel(null));
            umoManager.start();
        } catch (Exception e) {
            logger.warn("Error deploying channels.", e);
            // if deploy fails, log to system events
            SystemEvent event = new SystemEvent("Error deploying channels.");
            event.setLevel(SystemEvent.Level.HIGH);
            event.setDescription(StackTracePrinter.stackTraceToString(e));
            eventController.logSystemEvent(event);
        }

        configurationController.setEngineStarting(false);
    }

    /**
     * Stops the Mule server.
     * 
     */
    private void stopMule() {
        logger.debug("stopping mule");

        if (umoManager != null) {
            try {
                if (umoManager.isStarted()) {
                    configurationController.executeChannelShutdownScripts(channelController.getChannel(null));
                    configurationController.executeGlobalShutdownScript();

                    umoManager.stop();
                }
            } catch (Exception e) {
                logger.error(e);
            } finally {
                logger.debug("disposing mule instance");
                umoManager.dispose();
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

            httpServer = new HttpServer();
            servletContainer = new HttpServer();

            // add HTTPS listener
            SslListener sslListener = new SslListener();

            String ciphers = PropertyLoader.getProperty(mirthProperties, "https.ciphers");
            if (ciphers != null && !ciphers.equals("")) {
                sslListener.setCipherSuites(ciphers.split(","));
            }

            sslListener.setPort(Integer.valueOf(PropertyLoader.getProperty(mirthProperties, "https.port")).intValue());
            sslListener.setKeystore(ControllerFactory.getFactory().createConfigurationController().getBaseDir() + System.getProperty("file.separator") + PropertyLoader.getProperty(mirthProperties, "https.keystore"));
            sslListener.setPassword(PropertyLoader.getProperty(mirthProperties, "https.password"));
            sslListener.setKeyPassword(PropertyLoader.getProperty(mirthProperties, "https.keypassword"));
            sslListener.setAlgorithm(PropertyLoader.getProperty(mirthProperties, "https.algorithm"));
            sslListener.setKeystoreType(PropertyLoader.getProperty(mirthProperties, "https.keystoretype"));
            servletContainer.addListener(sslListener);

            // add HTTP listener
            SocketListener listener = new SocketListener();
            listener.setPort(Integer.valueOf(PropertyLoader.getProperty(mirthProperties, "http.port")).intValue());
            httpServer.addListener(listener);

            // Load the context path property and remove the last char if it is
            // a '/'.
            String contextPath = PropertyLoader.getProperty(mirthProperties, "context.path");
            if (contextPath.lastIndexOf('/') == (contextPath.length() - 1)) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }

            // Create the lib context
            HttpContext libContext = new HttpContext();
            libContext.setContextPath(contextPath + "/client-lib/");
            httpServer.addContext(libContext);

            // Serve static content from the lib context
            File extensions = new File(ExtensionController.getExtensionsPath());
            String libPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + System.getProperty("file.separator") + "client-lib";

            libContext.setResourceBase(libPath);
            libContext.addHandler(new ResourceHandler());

            // Create the extensions context
            HttpContext extensionsContext = new HttpContext();
            extensionsContext.setContextPath(contextPath + "/extensions/");
            httpServer.addContext(extensionsContext);

            // Serve static content from the extensions context
            String extensionsPath = extensions.getPath();
            extensionsContext.setResourceBase(extensionsPath);
            extensionsContext.addHandler(new ResourceHandler());

            // Create the public_html context
            HttpContext publicContext = new HttpContext();
            publicContext.setContextPath(contextPath + "/");
            httpServer.addContext(publicContext);

            String publicPath = ControllerFactory.getFactory().createConfigurationController().getBaseDir() + System.getProperty("file.separator") + "public_html";
            publicContext.setResourceBase(publicPath);
            publicContext.addHandler(new ResourceHandler());

            // Create a normal servlet container
            ServletHandler servlets = new ServletHandler();
            HttpContext servletContext = new HttpContext();
            servletContext.setContextPath(contextPath + "/");
            servletContext.addHandler(servlets);
            httpServer.addContext(servletContext);
            servlets.addServlet("WebStart", "/webstart.jnlp", "com.webreach.mirth.server.servlets.WebStartServlet");
            // Servlets for backwards compatibility
            servlets.addServlet("WebStart", "/webstart", "com.webreach.mirth.server.servlets.WebStartServlet");

            // Create a secure servlet container
            ServletHandler secureServlets = new ServletHandler();
            HttpContext secureServletContext = new HttpContext();
            secureServletContext.setContextPath(contextPath + "/");
            secureServletContext.addHandler(secureServlets);
            servletContainer.addContext(secureServletContext);
            // Map a servlet onto the container
            secureServlets.addServlet("Alerts", "/alerts", "com.webreach.mirth.server.servlets.AlertServlet");
            secureServlets.addServlet("Channels", "/channels", "com.webreach.mirth.server.servlets.ChannelServlet");
            secureServlets.addServlet("ChannelStatistics", "/channelstatistics", "com.webreach.mirth.server.servlets.ChannelStatisticsServlet");
            secureServlets.addServlet("ChannelStatus", "/channelstatus", "com.webreach.mirth.server.servlets.ChannelStatusServlet");
            secureServlets.addServlet("CodeTemplates", "/codetemplates", "com.webreach.mirth.server.servlets.CodeTemplateServlet");
            secureServlets.addServlet("Configuration", "/configuration", "com.webreach.mirth.server.servlets.ConfigurationServlet");
            secureServlets.addServlet("MessageObject", "/messages", "com.webreach.mirth.server.servlets.MessageObjectServlet");
            secureServlets.addServlet("Extensions", "/extensions", "com.webreach.mirth.server.servlets.ExtensionServlet");
            secureServlets.addServlet("SystemEvent", "/events", "com.webreach.mirth.server.servlets.SystemEventServlet");
            secureServlets.addServlet("Users", "/users", "com.webreach.mirth.server.servlets.UserServlet");

            // start the web server
            httpServer.start();
            servletContainer.start();

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
            httpServer.stop();
            servletContainer.stop();
        } catch (Exception e) {
            logger.warn("Could not stop web server.", e);
        }
    }

    private void stopDatabase() {
        String database = PropertyLoader.getProperty(mirthProperties, "database");

        if (database.equals("derby")) {
            boolean gotException = false;

            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (SQLException sqle) {
                if ((sqle.getSQLState() != null) && sqle.getSQLState().equals("XJ015")) {
                    gotException = true;
                }
            }

            if (gotException) {
                System.out.println("Database shut down normally.");
            }
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
        String version = PropertyLoader.getProperty(versionProperties, "mirth.version");
        String buildDate = PropertyLoader.getProperty(versionProperties, "mirth.date");
        System.out.println("Mirth Connect " + version + " (" + buildDate + ") server successfully started: " + (new Date()).toString());
        System.out.println("This product was developed by Mirth Corporation (http://www.mirthcorp.com) and its contributors (c)2005-" + Calendar.getInstance().get(Calendar.YEAR) + ".");
        System.out.println("Running Java " + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ") with charset " + Charset.defaultCharset() + ".");
    }

    /**
     * Test a port to see if it is already in use.
     * 
     * @param port
     *            The port to test.
     * @param name
     *            A friendly name to display in case of an error.
     * @return
     */
    private boolean testPort(String port, String name) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(Integer.parseInt(port));
        } catch (NumberFormatException ex) {
            logger.error(name + " port is invalid: " + port);
            return false;
        } catch (IOException ex) {
            logger.error(name + " port is already in use: " + port);
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Could not close test socket for " + name + ": " + port);
                    return false;
                }
            }
        }
        return true;
    }

}
