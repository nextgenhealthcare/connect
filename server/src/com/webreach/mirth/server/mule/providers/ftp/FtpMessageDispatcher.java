/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpMessageDispatcher.java,v 1.5 2005/10/17 14:52:55 rossmason Exp $
 * $Revision: 1.5 $
 * $Date: 2005/10/17 14:52:55 $
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
package com.webreach.mirth.server.mule.providers.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.mule.MuleManager;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import sun.misc.BASE64Decoder;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.providers.file.filters.FilenameWildcardFilter;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class FtpMessageDispatcher extends AbstractMessageDispatcher {
	protected FtpConnector connector;

	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = new AlertController();

	public FtpMessageDispatcher(FtpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		FTPClient client = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}

		try {
			String filename = (String) event.getProperty(FtpConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String pattern = (String) event.getProperty(FtpConnector.PROPERTY_OUTPUT_PATTERN);

				if (pattern == null) {
					pattern = connector.getOutputPattern();
				}

				filename = generateFilename(event, pattern, messageObject);
			}

			if (filename == null) {
				throw new IOException("Filename is null");
			}

			String template = replacer.replaceValues(connector.getTemplate(), messageObject);
			byte[] buffer = null;
			if (connector.isBinary()) {
				BASE64Decoder base64 = new BASE64Decoder();
				buffer = base64.decodeBuffer(template);
			} else {
				buffer = template.getBytes();
				// TODO: Add support for Charset encodings in 1.4.1
			}
			client = connector.getFtp(uri);
			try {
				if (!client.changeWorkingDirectory(uri.getPath())) {
					throw new IOException("Ftp error: " + client.getReplyCode() + client.getReplyString());
				}
			} catch (Exception exception) {
				connector.destroyFtp(uri, client);
				client = connector.getFtp(uri);
				if (!client.changeWorkingDirectory(uri.getPath())) {
					throw new IOException("Ftp error: " + client.getReplyCode() + client.getReplyString());
				}
			}

			if (!client.storeFile(filename, new ByteArrayInputStream(buffer))) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}

			// update the message status to sent
			messageObjectController.setSuccess(messageObject, "File successfully written: " + filename);

		} catch (Exception e) {
			alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, null, e);
			
			if (client != null) {
				messageObjectController.setError(messageObject, Constants.ERROR_405, "Error writing to FTP: " + client.getReplyCode() + client.getReplyString(), e);
			} else {
				messageObjectController.setError(messageObject, Constants.ERROR_405, "Error writing to FTP", e);
			}
			connector.handleException(e);
		} finally {
			try {
				connector.releaseFtp(uri, client);
			} catch (Exception e) {
				logger.debug("Could not release FTP connection.", e);
				connector.destroyFtp(uri, client);
			}
		}
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		FTPClient client = null;

		try {
			client = connector.getFtp(endpointUri);

			if (!client.changeWorkingDirectory(endpointUri.getPath())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}

			FilenameFilter filenameFilter = null;
			String filter = (String) endpointUri.getParams().get("filter");
			if (filter != null) {
				filter = URLDecoder.decode(filter, MuleManager.getConfiguration().getEncoding());
				filenameFilter = new FilenameWildcardFilter(filter);
			}
			FTPFile[] files = client.listFiles();
			if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			if (files == null || files.length == 0) {
				return null;
			}
			List fileList = new ArrayList();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					if (filenameFilter == null || filenameFilter.accept(null, files[i].getName())) {
						fileList.add(files[i]);
						// only read the first one
						break;
					}
				}
			}
			if (fileList.size() == 0)
				return null;

			FTPFile file = (FTPFile) fileList.get(0);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (!client.retrieveFile(file.getName(), baos)) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			return new MuleMessage(connector.getMessageAdapter(baos.toByteArray()));

		} finally {
			connector.releaseFtp(endpointUri, client);
		}
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public void doDispose() {}

	private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
		if (connector.getFilenameParser() instanceof VariableFilenameParser) {
			VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
			filenameParser.setMessageObject(messageObject);
			return filenameParser.getFilename(event.getMessage(), pattern);
		} else {
			return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
		}
	}

}
