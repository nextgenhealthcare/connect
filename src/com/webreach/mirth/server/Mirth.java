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

import org.apache.log4j.Logger;
import org.mortbay.http.SslListener;
import org.mortbay.jetty.Server;
import org.mule.config.ConfigurationException;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.umo.manager.UMOManager;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.SystemLogger;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class Mirth {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean running = false;

	private UMOManager muleManager = null;
	private Server webServer = null;
	private CommandQueue commandQueue = CommandQueue.getInstance();
	private SystemLogger systemLogger = new SystemLogger();

	public static void main(String[] args) {
		Mirth mirth = new Mirth();
		mirth.start();
	}

	public Mirth() {

	}

	public void start() {
		running = true;
		startWebServer();
		commandQueue.addCommand(new Command(Command.Operation.START));
		
        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

		// pulls commands off of the command queue
		while (running) {
			System.out.println("waiting for command...");
			Command command = commandQueue.getCommand();

			if (command.getOperation().equals(Command.Operation.START)) {
				startMule();
			} else if (command.getOperation().equals(Command.Operation.STOP)) {
				stopMule();
			} else if (command.getOperation().equals(Command.Operation.RESTART)) {
				restartMule();
			} else if (command.getOperation().equals(Command.Operation.SHUTDOWN)) {
				shutdown();
			}
		}
	}
	
	public void shutdown() {
		System.out.println("shutting down mirth");
		stopMule();
		stopWebServer();
		running = false;
	}

	// restarts mule
	private void restartMule() {
		logger.debug("retarting mule");
		stopMule();
		startMule();
	}

	// starts mule
	private void startMule() {
		ConfigurationController configurationController = new ConfigurationController();

		try {
			String configurationFilePath = configurationController.getLatestConfiguration().getAbsolutePath();
			logger.debug("starting mule with configuration file: " + configurationFilePath);

			// disables validation of Mule configuration files
			System.setProperty("org.mule.xml.validate", "false");
			MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
			muleManager = builder.configure(configurationFilePath);
		} catch (ConfigurationException e) {
			logger.warn("Error deploying channels.", e);

			// if deploy fails, log to system events
			SystemEvent event = new SystemEvent("Error deploying channels.");
			event.setLevel(SystemEvent.Level.HIGH);
			event.setDescription(e.getMessage());
			systemLogger.logSystemEvent(event);

			configurationController.deleteLatestConfiguration();
			commandQueue.addCommand(new Command(Command.Operation.START));
		} catch (ControllerException e) {
			logger.warn("Could not retrieve latest configuration.", e);
		}
	}

	// stops mule
	private void stopMule() {
		logger.debug("stopping mule");

		try {
			muleManager.stop();
			System.out.println("stopped mule");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug("disposing mule");
			muleManager.dispose();
		}
	}

	// starts the Jetty web server
	private void startWebServer() {
		logger.debug("starting jetty web server");

		try {
			// this disables validaiton of the web.xml file
			// which causes exceptions when Mirth is run
			// behind a firewall and the resources cannot be
			// accessed
			System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");

			webServer = new Server();

			SslListener sslListener = new SslListener();
			sslListener.setPort(8443);
			sslListener.setKeystore("keystore");
			sslListener.setPassword("abc12345");
			sslListener.setKeyPassword("abc12345");

			webServer.addListener(sslListener);
			webServer.addWebApplication("/", "./web/webapps/mirth.war");
			webServer.start();
		} catch (Exception e) {
			logger.warn("Could not start web server.", e);
		}
	}

	// stops the Jetty web server
	private void stopWebServer() {
		logger.debug("stopping jetty web server");

		try {
			webServer.stop();
		} catch (InterruptedException e) {
			logger.warn("Could not stop web server.", e);
		}
	}

	private class ShutdownHook extends Thread {
		public void run() {
			shutdown();
		}
	}
}
