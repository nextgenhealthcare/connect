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
 * The Original Code is Mirth-Ant.
 *
 * The Initial Developer of the Original Code is
 * Coalese Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Andrzej Taramina <andrzej@coalese.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.SystemEventListHandler;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;

/**
 * an ant task to dump stats or events
 * 
 * @author andrzej@coalese.com
 */

public class DumpTask extends AbstractMirthTask {
	private final String TYPE_STATS = "stats";
	private final String TYPE_EVENTS = "events";

	protected String type = TYPE_STATS;
	protected String filename = "";

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			if (filename.length() > 0) {
				if (type.equalsIgnoreCase(TYPE_STATS)) {
					commandDumpStats();
				} else if (type.equalsIgnoreCase(TYPE_EVENTS)) {
					commandDumpEvents();
				} else {
					throw (new BuildException("Invalid Dump Type specified: " + type));
				}
			} else {
				throw (new BuildException("No filename specified"));
			}

		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandDumpStats() throws ClientException {
		BufferedWriter writer = null;

		connectClient();

		try {
			File dumpFile = new File(filename);

			writer = new BufferedWriter(new FileWriter(dumpFile));

			writer.write(" Mirth Channel Statistics Dump: " + (new Date()).toString() + "\n");
			writer.write(" Name, Received, Filtered, Sent, Error\n");

			List<Channel> channels = client.getChannel(null);

			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				ChannelStatistics stats = client.getStatistics(channel.getId());
				writer.write(channel.getName() + ", " + stats.getReceived() + ", " + stats.getFiltered() + ", " + stats.getSent() + ", " + stats.getError() + "\n");
			}

			writer.flush();

			System.out.println("Stats written to " + filename);
		} catch (IOException e) {
			throw (new BuildException("Could not write file: " + filename));
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}

		disconnectClient();
	}

	private void commandDumpEvents() throws ClientException {
		BufferedWriter writer = null;

		connectClient();

		try {

			File dumpFile = new File(filename);

			writer = new BufferedWriter(new FileWriter(dumpFile, false));

			writer.write("Mirth Event Log Dump: " + (new Date()).toString() + "\n");
			writer.write("Id, Event, Date, Description, Level\n");

			SystemEventListHandler eventListHandler = client.getSystemEventListHandler(new SystemEventFilter(), 20, false);
			try {
				List<SystemEvent> events = eventListHandler.getFirstPage();

				while (events.size() != 0) {
					for (Iterator iter = events.iterator(); iter.hasNext();) {
						SystemEvent event = (SystemEvent) iter.next();
						writer.write(event.getId() + ", " + event.getEvent() + ", " + formatDate(event.getDate()) + ", " + event.getDescription() + ", " + event.getLevel() + "\n");
					}

					writer.flush();

					events = eventListHandler.getNextPage();
				}

			} catch (ListHandlerException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Events written to " + filename);
		} catch (IOException e) {
			throw (new BuildException("Could not write file: " + filename));
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}

		disconnectClient();

	}

	private String formatDate(Calendar date) {
		return (String.format("%1$tY-%1$tm-%1$td 00:00:00", date));
	}

}
