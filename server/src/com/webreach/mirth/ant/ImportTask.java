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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.util.PropertyVerifier;

/**
 * an ant task to import stuff into Mirth
 * 
 * @author andrzej@coalese.com
 */

public class ImportTask extends AbstractMirthTask {
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
	protected boolean force = false;

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
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

	/**
	 * @param force
	 */
	public void setForce(boolean force) {
		this.force = force;
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
					commandImportChannel();
				} else if (type.equalsIgnoreCase(TYPE_CONFIG)) {
					commandImportConfig();
				} else if (type.equalsIgnoreCase(TYPE_SCRIPT)) {
					if (script.length() > 0) {
						commandImportScript();
					} else {
						throw (new BuildException("No script type specified"));
					}
				} else {
					throw (new BuildException("Invalid Import Type specified: " + type));
				}
			} else {
				throw (new BuildException("No filename specified"));
			}

		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandImportChannel() throws ClientException, BuildException {
		connectClient();

		File fXml = new File(filename);

		if (!fXml.exists()) {
			throw (new BuildException("" + filename + " not found"));
		} else if (!fXml.canRead()) {
			throw (new BuildException("cannot read " + filename));
		} else {
			doImportChannel(fXml, force);
		}

		disconnectClient();
	}

	private void commandImportConfig() throws ClientException, BuildException {
		connectClient();

		File fXml = new File(filename);

		if (!fXml.exists()) {
			throw (new BuildException("" + filename + " not found"));
		} else if (!fXml.canRead()) {
			throw (new BuildException("cannot read " + filename));
		} else {
			ObjectXMLSerializer serializer = new ObjectXMLSerializer();
			try {
				client.setServerConfiguration((ServerConfiguration) serializer.fromXML(readFile(fXml)));
			} catch (IOException e) {
				throw (new BuildException("cannot read " + filename));
			}
		}

		System.out.println("Configuration Import Complete.");

		disconnectClient();
	}

	private void commandImportScript() throws ClientException, BuildException {
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

		File fXml = new File(filename);

		if (!fXml.exists()) {
			throw (new BuildException("" + filename + " not found"));
		} else if (!fXml.canRead()) {
			throw (new BuildException("Cannot read " + filename));
		} else {
			doImportScript(script, fXml);
		}

		System.out.println(script + " script import complete");

		disconnectClient();
	}

	private void doImportScript(String name, File scriptFile) throws ClientException, BuildException {
		String script = "";

		try {
			script = readFile(scriptFile);
		} catch (Exception e) {
			throw (new BuildException("Invalid script file."));
		}

		Map<String, String> scriptMap = new HashMap<String, String>();

		scriptMap.put(name, script);
		client.setGlobalScripts(scriptMap);
	}

	private void doImportChannel(File importFile, boolean force) throws ClientException, BuildException {
		String channelXML = "";

		try {
			channelXML = ImportConverter.convertChannelFile(importFile);
		} catch (Exception e1) {
			throw (new BuildException("Invalid channel file."));
		}

		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
		Channel importChannel;

		try {
			importChannel = (Channel) serializer.fromXML(channelXML.replaceAll("\\&\\#x0D;\\n", "\n").replaceAll("\\&\\#x0D;", "\n"));
			PropertyVerifier.checkChannelProperties(importChannel);
			PropertyVerifier.checkConnectorProperties(importChannel, client.getConnectorMetaData());

		} catch (Exception e) {
			throw (new BuildException("invalid channel file."));
		}

		String channelName = importChannel.getName();
		String tempId = client.getGuid();

		// Check to see that the channel name doesn't already exist.
		if (!checkChannelName(channelName, tempId)) {
			if (!force) {
				importChannel.setRevision(0);
				importChannel.setName(importChannel.getId());
				importChannel.setId(tempId);
			} else {
				for (Channel channel : client.getChannel(null)) {
					if (channel.getName().equalsIgnoreCase(channelName)) {
						importChannel.setId(channel.getId());
					}
				}
			}
		}
		// If the channel name didn't already exist, make sure the id doesn't exist either.
        else if (!checkChannelId(importChannel.getId())) {
        	importChannel.setId(tempId);
        }

		importChannel.setVersion(client.getVersion());
		client.updateChannel(importChannel, true);

		System.out.println("Channel '" + channelName + "' imported successfully.");
	}

	public boolean checkChannelId(String id) throws ClientException {
		for (Channel channel : client.getChannel(null)) {
			if (channel.getId().equalsIgnoreCase(id)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean checkChannelName(String name, String id) throws ClientException {
		if (name.equals("")) {
			System.out.println("Channel name cannot be empty.");
			return false;
		}

		if (name.length() > 40) {
			System.out.println("Channel name cannot be longer than 40 characters.");
			return false;
		}

        Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z_0-9\\-\\s]*$");
        Matcher matcher = alphaNumericPattern.matcher(name);
        
        if (!matcher.find()) {
        	System.out.println("Channel name cannot have special characters besides hyphen, underscore, and space.");
            return false;
        }

		for (Channel channel : client.getChannel(null)) {
			if (channel.getName().equalsIgnoreCase(name) && !channel.getId().equals(id)) {
				System.out.println("Channel \"" + name + "\" already exists.");
				return false;
			}
		}
		return true;
	}

	public static String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder contents = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				contents.append(line + "\n");
			}
		} finally {
			reader.close();
		}

		return (contents.toString());
	}

}
