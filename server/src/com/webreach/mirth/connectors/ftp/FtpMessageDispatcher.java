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
package com.webreach.mirth.connectors.ftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.mule.impl.endpoint.MuleEndpointURI;
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
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class FtpMessageDispatcher extends AbstractMessageDispatcher {
	protected FtpConnector connector;

	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.WRITER;
	public FtpMessageDispatcher(FtpConnector connector) {
		super(connector);
		this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doDispatch(UMOEvent event) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		
		FTPClient client = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}
		
		try {
			uri = new MuleEndpointURI(replacer.replaceURLValues(uri.toString(), messageObject));
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
			client = connector.getFtp(uri, messageObject);
			try {
				if (!client.changeWorkingDirectory(uri.getPath())) {
					throw new IOException("Ftp error: " + client.getReplyCode() + client.getReplyString());
				}
			} catch (Exception exception) {
				connector.destroyFtp(uri, client, messageObject);
				client = connector.getFtp(uri, messageObject);
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
			if (client != null) {
				alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, "Error writing to FTP: " + client.getReplyCode() + client.getReplyString(), e);
				messageObjectController.setError(messageObject, Constants.ERROR_405, "Error writing to FTP: " + client.getReplyCode() + client.getReplyString(), e);
			} else {
				alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, "Error writing to FTP", e);
				messageObjectController.setError(messageObject, Constants.ERROR_405, "Error writing to FTP", e);
			}
			connector.handleException(e);
		} finally {
			try {
				connector.releaseFtp(uri, client, messageObject);
			} catch (Exception e) {
				logger.debug("Could not release FTP connection.", e);
				connector.destroyFtp(uri, client, messageObject);
			}
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
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

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
