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

package com.webreach.mirth;

import org.apache.log4j.Logger;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.umo.manager.UMOManager;

/**
 * Instantiate a Mirth server that listens for commands from the CommandQueue.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class Mirth {
	private Logger logger = Logger.getLogger(Mirth.class);
	private boolean running = false;

	private UMOManager muleManager = null;
	private Server webServer = null;
	private CommandQueue commandQueue = CommandQueue.getInstance();
	private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

	public static void main(String[] args) {
		Mirth mirth = new Mirth();
		mirth.start();
	}

	public Mirth() {}

	public void start() {
		running = true;
		startWebServer();
		commandQueue.addCommand(new Command(Command.CMD_START_MULE));

		// pulls commands off of the command queue
		while (running) {
			System.out.println("waiting for command...");
			Command command = commandQueue.getCommand();

			switch (command.getCommand()) {
				case Command.CMD_START_MULE:
					startMule();
					break;
				case Command.CMD_STOP_MULE:
					stopMule();
					break;
				case Command.CMD_RESTART_MULE:
					restartMule();
					break;
				case Command.CMD_SHUTDOWN:
					stopMule();
					stopWebServer();
					running = false;
					break;
				default:
					break;
			}
		}
	}

	// restarts mule
	private void restartMule() {
		logger.debug("retarting mule");

		stopMule();
		startMule();
	}

	// starts mule
	private void startMule() {
		logger.debug("starting mule with configuration file: " + configurationManager.getLatestConfiguration().getAbsolutePath());

		try {
			// disables validation of Mule configuration files
			System.setProperty("org.mule.xml.validate", "false");
			MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
			muleManager = builder.configure(configurationManager.getLatestConfiguration().getAbsolutePath());
		} catch (Exception e) {
			logger.error(e);
		}
	}

	// stops mule
	private void stopMule() {
		logger.debug("stopping mule");

		try {
			if (muleManager.isInitialised()) {
				muleManager.stop();
				System.out.println("stopped mule");
			} else {
				commandQueue.addCommand(new Command(Command.CMD_STOP_MULE, Command.PRIORITY_HIGH));
			}
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
			SocketListener listener = new SocketListener();
			listener.setPort(8080);
			webServer.addListener(listener);
			webServer.addWebApplication("/", "./jetty/webapps/axis/");
			webServer.start();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	// stops the Jetty web server
	private void stopWebServer() {
		logger.debug("stopping jetty web server");

		try {
			webServer.stop();
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}
}
