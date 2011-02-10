/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ListHandlerException;
import com.mirth.connect.client.core.EventListHandler;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.filters.EventFilter;

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

			for (Channel channel : client.getChannel(null)) {
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

			EventListHandler eventListHandler = client.getEventListHandler(new EventFilter(), 20, false);
			try {
				List<Event> events = eventListHandler.getFirstPage();

				while (events.size() != 0) {
					for (Iterator<Event> iter = events.iterator(); iter.hasNext();) {
						Event event = iter.next();
						writer.write(event.getId() + ", " + event.getName() + ", " + formatDate(event.getDate()) + ", " + event.getLevel() + "\n");
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
