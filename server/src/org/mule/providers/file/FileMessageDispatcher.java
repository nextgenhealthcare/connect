/* 
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileMessageDispatcher.java,v 1.8 2005/11/12 20:55:57 lajos Exp $
 * $Revision: 1.8 $
 * $Date: 2005/11/12 20:55:57 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package org.mule.providers.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.MuleException;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.Utility;

import com.webreach.mirth.server.mule.util.GlobalVariableStore;

/**
 * <code>FileMessageDispatcher</code> is used to read/write files to the
 * filesystem and
 * 
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class FileMessageDispatcher extends AbstractMessageDispatcher {
	private static final String TEMPLATE_REPLACE_PATTERN = "\\$\\{[^\\}]*\\}";
	private FileConnector connector;

	public FileMessageDispatcher(FileConnector connector) {
		super(connector);
		this.connector = connector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#dispatch(org.mule.umo.UMOEvent)
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		try {
			String endpoint = event.getEndpoint().getEndpointURI().getAddress();
			Object data = event.getTransformedMessage();
			String filename = (String) event
					.getProperty(FileConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String outPattern = (String) event
						.getProperty(FileConnector.PROPERTY_OUTPUT_PATTERN);
				if (outPattern == null) {
					outPattern = connector.getOutputPattern();
				}
				filename = generateFilename(event, outPattern);
			}

			if (filename == null) {
				throw new IOException("Filename is null");
			}

			File file = Utility.createFile(endpoint + "/" + filename);
			String template = connector.getTemplate();
			byte[] buf;
			if (data instanceof byte[]) {
				buf = (byte[]) data;
			} else if (data instanceof HashMap) {
				HashMap map = (HashMap) data;
				template = replaceValues(template, map);
				//Check for Mirth global map
				//template = replaceValues(template, globalMap);
				//localmap takes precendence of globalmap, so run it first

				buf = template.getBytes();
			} else {
				buf = data.toString().getBytes();
			}
			//Hackish way to append a new line
			//TODO: find where newlines are stripped in config
			if (connector.isOutputAppend()) {
				buf = (new String(buf) + "\r\n").getBytes();
			}
			logger.info("Writing file to: " + file.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(file, connector
					.isOutputAppend());
			try {

				fos.write(buf);
			} finally {
				fos.close();
			}
		} catch (Exception e) {
			getConnector().handleException(e);
		}

	}

	private String replaceValues(String template, HashMap map) throws Exception {
		if ((template == null) || !(template.length() > 0)) {
			return "";
		}
		Pattern p = Pattern.compile(TEMPLATE_REPLACE_PATTERN);
		Matcher m = p.matcher(template);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group();
			m.appendReplacement(sb, getTemplateValue(key, map));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private String getTemplateValue(String name, HashMap map) {
		String value = "";
		try {
			if (map.containsKey(name)) {
				//Assign the value to the value in the hash with the key we're looking for (name)
				value = map.get(name).toString();
			} else if (GlobalVariableStore.getInstance().containsKey(name)) {
				//Try to get Mirth global hash
				value = GlobalVariableStore.getInstance().get(name).toString();
			}
		} catch (Exception ignored) {

		}
		return value;
	}

	/*Deprecated v.1.1.2
	 private String replaceValues(String template, HashMap map) throws Exception {
	 if ((template == null) || !(template.length() > 0)) {
	 return "";
	 }
	 
	 for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
	 Entry entry = (Entry) it.next();
	 String key = entry.getKey().toString();
	 String value = entry.getValue().toString();
	 template = template.replaceAll("\\$\\{" + key + "\\}", value.replace("\\","\\\\"));
	 }
	 
	 return template;
	 }
	 */
	/**
	 * There is no associated session for a file connector
	 * 
	 * @return
	 * @throws UMOException
	 */
	public Object getDelegateSession() throws UMOException {
		return null;
	}

	/**
	 * Will attempt to do a receive from a directory, if the endpointUri
	 * resolves to a file name the file will be returned, otherwise the first
	 * file in the directory according to the filename filter configured on the
	 * connector.
	 * 
	 * @param endpointUri a path to a file or directory
	 * @param timeout this is ignored when doing a receive on this dispatcher
	 * @return a message containing file contents or null if there was notthing
	 *         to receive
	 * @throws Exception
	 */
	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout)
			throws Exception {
		File file = new File(endpointUri.getAddress());
		File result = null;
		FilenameFilter filenameFilter = null;
		String filter = (String) endpointUri.getParams().get("filter");
		if (filter != null) {
			filter = URLDecoder.decode(filter, MuleManager.getConfiguration()
					.getEncoding());
			filenameFilter = new FilenameWildcardFilter(filter);
		}
		if (file.exists()) {
			if (file.isFile()) {
				result = file;
			} else if (file.isDirectory()) {
				result = getNextFile(endpointUri.getAddress(), filenameFilter);
			}
			if (result != null) {
				boolean checkFileAge = connector.getCheckFileAge();
				if (checkFileAge) {
					long fileAge = connector.getFileAge();
					long lastMod = result.lastModified();
					long now = (new java.util.Date()).getTime();
					if ((now - lastMod) < fileAge) {
						return null;
					}
				}

				MuleMessage message = new MuleMessage(connector
						.getMessageAdapter(result));
				if (connector.getMoveToDirectory() != null) {
					{
						File destinationFile = new File(connector
								.getMoveToDirectory(), result.getName());
						if (!result.renameTo(destinationFile)) {
							logger.error("Failed to move file: "
									+ result.getAbsolutePath() + " to "
									+ destinationFile.getAbsolutePath());
						}
					}
				}
				result.delete();
				return message;
			}
		}
		return null;
	}

	private File getNextFile(String dir, FilenameFilter filter)
			throws UMOException {
		File[] files = new File[] {};
		File file = new File(dir);
		File result = null;
		try {
			if (file.exists()) {
				if (file.isFile()) {
					result = file;
				} else if (file.isDirectory()) {
					if (filter != null) {
						files = file.listFiles(filter);
					} else {
						files = file.listFiles();
					}
					if (files.length > 0) {
						result = files[0];
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#send(org.mule.umo.UMOEvent)
	 */
	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#getConnector()
	 */
	public UMOConnector getConnector() {
		return connector;
	}

	private String generateFilename(UMOEvent event, String pattern) {
		if (pattern == null) {
			pattern = connector.getOutputPattern();
		}
		return connector.getFilenameParser().getFilename(event.getMessage(),
				pattern);
	}

	public void doDispose() {
	}

}
