/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.mule.MuleException;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;

import sun.misc.BASE64Decoder;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.connectors.file.filesystems.FileInfo;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filters.FilenameWildcardFilter;
import com.webreach.mirth.connectors.sftp.SftpConnector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

/**
 * <code>FileMessageDispatcher</code> is used to read/write files to the
 * filesystem and
 * 
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */
public class FileMessageDispatcher extends AbstractMessageDispatcher {
	private FileConnector connector;

	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private ConnectorType connectorType = ConnectorType.WRITER;
	public FileMessageDispatcher(FileConnector connector) {
		super(connector);
		this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#dispatch(org.mule.umo.UMOEvent)
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}

		Object data = null;
		OutputStream os = null;
		FileSystemConnection con = null;
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		try {
			String filename = (String) event.getProperty(FileConnector.PROPERTY_FILENAME);
			if (filename == null) {
				String pattern = (String) event.getProperty(FileConnector.PROPERTY_OUTPUT_PATTERN);

				if (pattern == null) {
					pattern = connector.getOutputPattern();
				}

				filename = generateFilename(event, pattern, messageObject);
			}

			if (filename == null) {
				messageObjectController.setError(messageObject, Constants.ERROR_403, "Filename is null", null);
				throw new IOException("Filename is null");
			}

			String path = generateFilename(event, uri.getAddress(), messageObject);
			String template = replacer.replaceValues(connector.getTemplate(), messageObject);

			// ast: change the output method to allow encoding election
			// if (connector.isOutputAppend())
			// template+=System.getProperty("line.separator");
			// don't automatically include line break
			byte[] buffer = null;
			if (connector.isBinary()) {
				BASE64Decoder base64 = new BASE64Decoder();
				buffer = base64.decodeBuffer(template);
			} else {
				buffer = template.getBytes(connector.getCharsetEncoding());
			}
			//logger.info("Writing file to: " + file.getAbsolutePath());
			con = connector.getConnection(uri, messageObject);
			os = con.writeFile(filename, path, connector.isOutputAppend());
			os.write(buffer);

			// update the message status to sent
			messageObjectController.setSuccess(messageObject, "File successfully written: " + filename);
		} catch (Exception e) {
			alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, "Error writing file", e);
			messageObjectController.setError(messageObject, Constants.ERROR_403, "Error writing file", e);
			connector.handleException(e);
		} finally {
			if (os != null) {
				os.close();
			}
			if (con != null) {
				connector.releaseConnection(uri, con, messageObject);
			}
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	/**
	 * Will attempt to do a receive from a directory, if the endpointUri
	 * resolves to a file name the file will be returned, otherwise the first
	 * file in the directory according to the filename filter configured on the
	 * connector.
	 * 
	 * TODO: This method is not implemented by the FTP or SFTP message
	 * dispatchers. Is it actually used?
	 * 
	 * @param endpointUri
	 *            a path to a file or directory
	 * @param timeout
	 *            this is ignored when doing a receive on this dispatcher
	 * @return a message containing file contents or null if there was nothing
	 *         to receive
	 * @throws Exception
	 */
	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		return null;
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public void doDispose() {
	}

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
