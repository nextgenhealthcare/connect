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

package com.webreach.mirth.server.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;

public class Shell {
	private Client client;

	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.run(args);
	}

	private void run(String[] args) {
		Option serverOption = OptionBuilder.withArgName("address").hasArg().withDescription("server address").create("a");
		Option userOption = OptionBuilder.withArgName("user").hasArg().withDescription("user login").create("u");
		Option passwordOption = OptionBuilder.withArgName("password").hasArg().withDescription("user password").create("p");
		Option scriptOption = OptionBuilder.withArgName("script").hasArg().withDescription("script file").create("s");
		Option helpOption = new Option("h", "help");

		Options options = new Options();
		options.addOption(serverOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(helpOption);
		options.addOption(scriptOption);

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("a") && line.hasOption("u") && line.hasOption("p") && line.hasOption("s")) {
				String server = line.getOptionValue("a");
				String user = line.getOptionValue("u");
				String password = line.getOptionValue("p");
				String script = line.getOptionValue("s");

				try {
					client = new Client(server);

					if (client.login(user, password)) {
						System.out.println("Connected to Mirth server @ " + server + " (" + client.getVersion() + ")");

						BufferedReader reader = new BufferedReader(new FileReader(script));
						String statement = null;

						try {
							while ((statement = reader.readLine()) != null) {
								System.out.println("Executing statement: " + statement);
								executeStatement(statement);
							}
						} finally {
							reader.close();
						}
					} else {
						System.out.println("Error: Could not login to server.");
					}

					client.logout();
					System.out.println("Disconnected from server.");
				} catch (ClientException ce) {
					ce.printStackTrace();
				} catch (IOException ioe) {
					System.out.println("Error: Could not load script file.");
				}
			} else if (line.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Shell", options);
			}
		} catch (ParseException e) {
			System.err.println("Error: Could not parse input arguments.");
		}
	}

	private void executeStatement(String statement) {
		try {
			String[] arguments = statement.split(" ");
			
			if (arguments.length > 0) {
				String command = arguments[0].toLowerCase();

				if (command.equalsIgnoreCase("start") || command.equalsIgnoreCase("stop")) {
					List<Channel> channels = client.getChannels();

					for (Iterator iter = channels.iterator(); iter.hasNext();) {
						Channel channel = (Channel) iter.next();

						if (command.equals("start")) {
							client.startChannel(channel.getId());
						} else if (command.equals("stop")) {
							client.stopChannel(channel.getId());
						}
					}
				} else if (command.equalsIgnoreCase("clear")) {
					List<Channel> channels = client.getChannels();

					for (Iterator iter = channels.iterator(); iter.hasNext();) {
						Channel channel = (Channel) iter.next();

						client.clearStatistics(channel.getId());
						client.clearMessages(channel.getId());
					}
				} else {
					System.out.println("Error: Bad command: " + statement);
				}
			}
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
}
