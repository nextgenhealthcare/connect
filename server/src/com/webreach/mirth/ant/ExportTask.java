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
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * an ant task to export stuff from Mirth
 * 
 * @author andrzej@coalese.com
 */

public class ExportTask extends AbstractMirthTask {
	private final String TYPE_CHANNEL = "channel";
	private final String TYPE_CONFIG = "config";
	private final String TYPE_SCRIPT = "script";

	private final String SCRIPT_DEPLOY = "Deploy";
	private final String SCRIPT_PRE = "Preprocessor";
	private final String SCRIPT_POST = "Postprocessor";
	private final String SCRIPT_SHUTDOWN = "Shutdown";

	protected String type = TYPE_CHANNEL;
	protected String script = "";
	protected String filename = "";
	protected String selector = "";

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param channel
	 */
	public void setChannel(String selector) {
		this.selector = selector;
	}

	/**
	 * @param script
	 */
	public void setScript(String script) {
		this.script = script;
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
				if (type.equalsIgnoreCase(TYPE_CHANNEL)) {
					commandExportChannel();
				} else if (type.equalsIgnoreCase(TYPE_CONFIG)) {
					commandExportConfig();
				} else if (type.equalsIgnoreCase(TYPE_SCRIPT)) {
					if (script.length() > 0) {
						commandExportScript();
					} else {
						throw (new BuildException("No script type specified"));
					}
				} else {
					throw (new BuildException("Invalid Export Type specified: " + type));
				}
			} else {
				throw (new BuildException("No filename specified"));
			}

		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandExportChannel() throws ClientException, BuildException {
		connectClient();

		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		List<Channel> channels = client.getChannel(null);

		if (selector.equals("*")) {
			for (Channel channel : channels) {
				try {
					File fXml = new File(filename + channel.getName() + ".xml");
					System.out.println("Exporting " + channel.getName());
					String channelXML = serializer.toXML(channel);
					writeFile(fXml, channelXML);
				} catch (IOException e) {
					throw (new BuildException("Unable to write file to" + filename + ": " + e));
				}
			}

			System.out.println("Export Complete.");

		} else {
			File fXml = new File(filename);

			for (Channel channel : channels) {
				if (selector.equalsIgnoreCase(channel.getName()) != selector.equalsIgnoreCase(channel.getId())) {
					System.out.println("Exporting " + channel.getName());
					String channelXML = serializer.toXML(channel);
					try {
						writeFile(fXml, channelXML);
					} catch (IOException e) {
						throw (new BuildException("Unable to write file " + filename + ": " + e));
					}
					System.out.println("Export Complete.");
				}
			}
		}

		disconnectClient();
	}

	private void commandExportConfig() throws ClientException, BuildException {
		connectClient();

		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		try {
			ServerConfiguration configuration = client.getServerConfiguration();
			File fXml = new File(filename);

			System.out.println("Exporting Configuration");
			String configurationXML = serializer.toXML(configuration);
			writeFile(fXml, configurationXML);
		} catch (IOException e) {
			throw (new BuildException("Unable to write file " + filename + ": " + e));
		}

		System.out.println("Configuration Export Complete.");

		disconnectClient();
	}

	private void commandExportScript() throws ClientException, BuildException {
		connectClient();

		if (script.equalsIgnoreCase(SCRIPT_DEPLOY)) {
			script = SCRIPT_DEPLOY;
		} else if (script.equalsIgnoreCase(SCRIPT_PRE)) {
			script = SCRIPT_PRE;
		} else if (script.equalsIgnoreCase(SCRIPT_POST)) {
			script = SCRIPT_POST;
		} else if (script.equalsIgnoreCase(SCRIPT_SHUTDOWN)) {
			script = SCRIPT_SHUTDOWN;
		} else {
			throw (new BuildException("Invalid Script Type specified: " + script));
		}

		try {
			Map<String, String> scripts = client.getGlobalScripts();

			String global = scripts.get(script);

			File fXml = new File(filename);

			System.out.println("Exporting " + script + " script");

			writeFile(fXml, global);
		} catch (IOException e) {
			throw (new BuildException("Unable to write file " + filename + ": " + e));
		}

		System.out.println("" + script + "Script Export Complete.");

		disconnectClient();
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

}
