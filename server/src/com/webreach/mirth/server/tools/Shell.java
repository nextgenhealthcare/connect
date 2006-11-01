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
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;

public class Shell {
	private Client client;
	private static boolean running;

	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.run(args);
	}

	private void run(String[] args) {
		Option serverOption = OptionBuilder.withArgName("address").hasArg().withDescription("server").create("s");
		Option userOption = OptionBuilder.withArgName("user").hasArg().withDescription("user login").create("u");
		Option passwordOption = OptionBuilder.withArgName("password").hasArg().withDescription("user password").create("p");
		Option helpOption = new Option("h", "help");

		Options options = new Options();
		options.addOption(serverOption);
		options.addOption(userOption);
		options.addOption(passwordOption);
		options.addOption(helpOption);

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("s") && line.hasOption("u") && line.hasOption("p")) {
				String server = line.getOptionValue("s");
				String user = line.getOptionValue("u");
				String password = line.getOptionValue("p");

				client = new Client(server);

				try {
					if (client.login(user, password)) {
						System.out.println("Connected to Mirth server @ " + server + " (" + client.getVersion() + ")");

						running = true;

						while (running) {
							System.out.print(user + "@mirth> ");

							BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
							String input = reader.readLine();
							
							processInput(input);
						}
					} else {
						System.out.println("Error: Could not login to server.");
					}
				} catch (ClientException ce) {
					ce.printStackTrace();
				} catch (IOException ioe) {
					System.out.println("Error: Could not read command input.");
					System.exit(1);
				}
			} else if (line.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Shell", options);
			}
		} catch (ParseException e) {
			System.err.println("Error: Could not parse input arguments.");
		}
	}
	
	private void processInput(String input) {
		try {
			String[] arguments = input.split(" ");
			String command = arguments[0].toLowerCase();

			if (command.equals("channels")) {
				List<Channel> channels = client.getChannels();
				
				for (Iterator iter = channels.iterator(); iter.hasNext();) {
					Channel channel = (Channel) iter.next();
					
					StringBuffer row = new StringBuffer();
					row.append("[" + channel.getId() + "]");
					row.append(" ");
					row.append(channel.getName());
					row.append(" (");
					row.append(channel.getDirection().toString().toLowerCase());
					row.append(" ");
					row.append(channel.getMode().toString().toLowerCase());
					row.append(")");
					System.out.println(row.toString());
				}
			} else if (command.equals("status")) {
				List<ChannelStatus> channelStatusList = client.getChannelStatusList();
				
				for (Iterator iter = channelStatusList.iterator(); iter.hasNext();) {
					ChannelStatus status = (ChannelStatus) iter.next();
					ChannelStatistics statistics = client.getStatistics(status.getChannelId());
					
					StringBuffer row = new StringBuffer();
					row.append("[" + status.getChannelId() + "]");
					row.append(" ");
					row.append(status.getName());
					row.append(" (");
					row.append(status.getState().toString().toLowerCase());
					row.append(") [");
					row.append(statistics.getReceivedCount());
					row.append(" received, ");
					row.append(statistics.getSentCount());
					row.append(" sent, ");
					row.append(statistics.getErrorCount());
					row.append(" errors]");
					System.out.println(row.toString());
				}
			} else if (command.equals("start") || command.equals("stop") || command.equals("resume") || command.equals("pause")) {
				try {
					String channelId = arguments[1];
					
					if (isValidChannel(channelId)) {
						if (command.equals("start")) {
							client.startChannel(channelId);
							System.out.println("Channel " + channelId + " successfully started.");
						} else if (command.equals("stop")) {
							client.stopChannel(channelId);
							System.out.println("Channel " + channelId + " successfully stopped.");
						} else if (command.equals("pause")) {
							client.pauseChannel(channelId);
							System.out.println("Channel " + channelId + " successfully paused.");
						} else if (command.equals("resume")) {
							client.resumeChannel(channelId);
							System.out.println("Channel " + channelId + " successfully resumed.");
						}
					} else {
						System.out.println("Error: Channel with id " + channelId + " does not exist");
					}
				} catch (NumberFormatException e) {
					System.out.println("Error: Invalid channel id." );
				}
			} else if (command.equals("quit") || command.equals("exit")) {
				System.out.println("Quitting.");
				running = false;
				System.exit(0);
			} else if (command.equals("help")) {
				System.out.println("\tchannels - List all channels.");
				System.out.println("\tstatus - List status of all channels.");
				System.out.println("\tstart <id | all> - Start channel with specified id, or all channels.");
				System.out.println("\tstop <id | all> - Stop channel with specified id, or all channels.");
				System.out.println("\tpause <id | all> - Pause channel with specified id, or all channels.");
				System.out.println("\tresume <id | all> - Resume channel with specified id, or all channels.");
				System.out.println("\thelp - Display help.");
				System.out.println("\tquit - Exit the Mirth shell.");
			} else {
				System.out.println("Error: Bad command: " + input);
			}
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isValidChannel(String channelId) throws ClientException {
		List<Channel> channels = client.getChannels();
		boolean isVerifiedChannelId = false;
		
		for (Iterator iter = channels.iterator(); iter.hasNext();) {
			Channel channel = (Channel) iter.next();
			
			if (channelId.equals(channel.getId())) {
				isVerifiedChannelId = true;
			}
		}
		
		return isVerifiedChannelId;
	}
}
