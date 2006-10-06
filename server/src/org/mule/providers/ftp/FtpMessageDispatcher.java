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
package org.mule.providers.ftp;

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
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.model.MessageObject;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class FtpMessageDispatcher extends AbstractMessageDispatcher {
	protected FtpConnector connector;

	public FtpMessageDispatcher(FtpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispose() {}

	public void doDispatch(UMOEvent event) throws Exception {
		FTPClient client = null;
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		
		try {
			String filename = (String) event.getProperty(FtpConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String outPattern = (String) event.getProperty(FtpConnector.PROPERTY_OUTPUT_PATTERN);
				if (outPattern == null) {
					outPattern = connector.getOutputPattern();
				}
				filename = generateFilename(event, outPattern);
			}
			if (filename == null) {
				throw new IOException("Filename is null");
			}

			String template = connector.getTemplate();
			Object data = event.getTransformedMessage();

			byte[] buf;

			if (data instanceof byte[]) {
				buf = (byte[]) data;
			} else if (data instanceof MessageObject) {
				MessageObject messageObject = (MessageObject) data;
				template = replacer.replaceValues(template, messageObject, filename);
				buf = template.getBytes();
			} else {
				buf = data.toString().getBytes();
			}

			client = connector.getFtp(uri);
			if (!client.changeWorkingDirectory(uri.getPath())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			if (!client.storeFile(filename, new ByteArrayInputStream(buf))) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}

		} finally {
			connector.releaseFtp(uri, client);
		}
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
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

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	private String generateFilename(UMOEvent event, String pattern) {
		if (pattern == null) {
			pattern = connector.getOutputPattern();
		}
		return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
	}

}
