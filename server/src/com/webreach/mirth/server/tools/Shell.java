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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;

public class Shell {
	private Client client;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SS");

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

	private void executeStatement(String command) {
		try {
			String[] arguments = command.split(" ");
			
			if (arguments.length >= 1) {
				String arg1 = arguments[0];

				if (arg1.equalsIgnoreCase("start") || arg1.equalsIgnoreCase("stop")) {
					List<Channel> channels = client.getChannels();

					for (Iterator iter = channels.iterator(); iter.hasNext();) {
						Channel channel = (Channel) iter.next();

						if (arg1.equals("start")) {
							client.startChannel(channel.getId());
						} else if (arg1.equals("stop")) {
							client.stopChannel(channel.getId());
						}
					}
				} else if (arg1.equalsIgnoreCase("clear")) {
					List<Channel> channels = client.getChannels();

					for (Iterator iter = channels.iterator(); iter.hasNext();) {
						Channel channel = (Channel) iter.next();

						client.clearStatistics(channel.getId());
						client.clearMessages(channel.getId());
					}
				} else if (arg1.equalsIgnoreCase("dump")) {
					if (arguments.length >= 2) {
						String arg2 = arguments[1];	

						if (arg2.equalsIgnoreCase("stats")) {
							String dumpFilename = arguments[2];
							dumpFilename = replaceValues(dumpFilename);

							StringBuilder builder = new StringBuilder();
							builder.append("Mirth Channel Statistics Dump: " + (new Date()).toString() + "\n");
							builder.append("Name, Received, Sent, Error\n");

							List<Channel> channels = client.getChannels();
							
							for (Iterator iter = channels.iterator(); iter.hasNext();) {
								Channel channel = (Channel) iter.next();
								ChannelStatistics stats = client.getStatistics(channel.getId());
								builder.append(channel.getName() + ", " + stats.getReceivedCount() + ", " + stats.getSentCount() + ", " + stats.getErrorCount());
							}

							File dumpFile = new File(dumpFilename);

							try {
								writeFile(dumpFile, builder.toString());	
							} catch (IOException e) {
								System.out.println("Error: Could not write file: " + dumpFile.getAbsolutePath());
							}
						} else if (arg2.equals("events")) {
							String dumpFilename = arguments[2];
							dumpFilename = replaceValues(dumpFilename);

							StringBuilder builder = new StringBuilder();
							builder.append("Mirth Event Log Dump: " + (new Date()).toString() + "\n");
							builder.append("Id, Event, Date, Description, Level\n");

							List<SystemEvent> events = client.getSystemEvents(new SystemEventFilter());
							
							for (Iterator iter = events.iterator(); iter.hasNext();) {
								SystemEvent event = (SystemEvent) iter.next();
								builder.append(event.getId() +", " + event.getEvent() + ", " + formatDate(event.getDate()) + ", " + event.getDescription() + ", " + event.getLevel()  + "\n");
							}
							
							File dumpFile = new File(dumpFilename);

							try {
								writeFile(dumpFile, builder.toString());	
							} catch (IOException e) {
								System.out.println("Error: Could not write file: " + dumpFile.getAbsolutePath());
							}
						} else {
							System.out.println("Error: Unknown dump command: " + arg2);
						}
					} else {
						System.out.println("Error: Missing dump commands.");
					}
				} else {
					System.out.println("Error: Unknown command: " + command);
				}
			}
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	
	private String replaceValues(String source) {
		source = source.replaceAll("\\$\\{date\\}", getTimeStamp());
		return source;
	}
	
	private void writeFile(File file, String data) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		try {
			writer.write(data);
			writer.flush();
		} finally {
			writer.close();
		}
	}
	
    private String getTimeStamp() {
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }
    
    private String formatDate(Calendar date) {
    	return String.format("%1$tY-%1$tm-%1$td 00:00:00", date);
    }
}

