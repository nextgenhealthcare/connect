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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.umo.manager.UMOManager;

import com.webreach.mirth.managers.ConfigurationManager;
import com.webreach.mirth.managers.Database;

/**
 * Instantiate a Mirth server that listens for commands from the
 * MirthCommandQueue.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class Mirth {
	protected transient Log logger = LogFactory.getLog(Mirth.class);
	private boolean running = true;

	private UMOManager muleManager = null;
	private Server webServer = null;
	private MirthCommandQueue commandQueue = MirthCommandQueue.getInstance();

	public static void main(String[] args) {
		Mirth mirth = new Mirth();
		
		if (args.length != 0) {
			mirth.start(args[0]);	
		} else {
			mirth.start(null);
		}
	}

	public Mirth() {}

	/**
	 * Starts the Mirth service.
	 * 
	 * @param bootConfigFile
	 *            the Mirth configuration file.
	 */
	public void start(String bootConfigFile) {
		startWebServer();

		File muleConfigFile;
		
		if (bootConfigFile != null) {
			// if a config file was specified in the arguments
			muleConfigFile = new File(bootConfigFile);
		} else {
			// load the existing config file
			muleConfigFile = new File(ConfigurationManager.MULE_CONFIG_FILE);
		}
		
		// if the mule-config.xml file hasnt been created yet
		if (!muleConfigFile.exists() || !(muleConfigFile.length() > 0)) {
			// start using the mule-boot.xml
			muleConfigFile = new File(ConfigurationManager.MULE_BOOT_FILE);
		}
		
		// boot-strap mule
		commandQueue.addCommand(new MirthCommand(MirthCommand.CMD_START_MULE, muleConfigFile.getAbsolutePath()));

		// pulls commands off of the command queue
		while (running) {
			System.out.println("waiting for command...");
			MirthCommand command = commandQueue.getCommand();

			switch (command.getCommand()) {
				case MirthCommand.CMD_START_MULE:
					startMule((String) command.getParameter());
					break;
				case MirthCommand.CMD_STOP_MULE:
					stopMule();
					break;
				case MirthCommand.CMD_RESTART_MULE:
					restartMule((String) command.getParameter());
					break;
				case MirthCommand.CMD_SHUTDOWN:
					stopMule();
					stopDatabase();
					stopWebServer();
					running = false;
					break;
				default:
					break;
			}
		}
	}

	// restarts mule
	private void restartMule(String configuration) {
		logger.debug("retarting mule");
		stopMule();
		startMule(configuration);
	}

	// starts mule
	private void startMule(String configuration) {
		logger.debug("starting mule with configuration file: " + configuration);

		try {
			// disables validation of Mule configuration files
			System.setProperty("org.mule.xml.validate", "false");
			
			MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
			muleManager = builder.configure(configuration);
		} catch (Exception e) {
			e.printStackTrace();
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
				commandQueue.addCommand(new MirthCommand(MirthCommand.CMD_STOP_MULE, MirthCommand.PRIORITY_HIGH));
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			logger.debug("disposing mule");
			muleManager.dispose();
		}
	}

	// starts the Jetty web server
	private void startWebServer() {
		logger.debug("starting jetty web/jsp server");

		try {
			// this disables validaiton of the web.xml file, which causes exceptions 
			// when Mirth is run behind a firewall and the resources cannot be accessed
			System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");
			
			webServer = new Server();
			SocketListener listener = new SocketListener();
			listener.setPort(8080);
			webServer.addListener(listener);
			webServer.addWebApplication("/", "./jetty/webapps/root/");
			webServer.start();
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	// stops the Jetty web server
	private void stopWebServer() {
		try {
			webServer.stop();
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}
	}
	
	private void stopDatabase() {
		try {
			Database database = new Database("mirth");
			database.shutdown();
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
}
