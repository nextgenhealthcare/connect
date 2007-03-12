package com.webreach.mirth.server.mule.providers.sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.providers.file.filters.FilenameWildcardFilter;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.UUIDGenerator;

public class SftpMessageDispatcher extends AbstractMessageDispatcher {
	protected SftpConnector connector;
	private MessageObjectController messageObjectController = new MessageObjectController();

	public SftpMessageDispatcher(SftpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();
		ChannelSftp client = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}
		try{
				String filename = (String) event.getProperty(SftpConnector.PROPERTY_FILENAME);

				if (filename == null) {
					String pattern = (String) event.getProperty(SftpConnector.PROPERTY_OUTPUT_PATTERN);
					if (pattern == null) {
						pattern = connector.getOutputPattern();
					}
					filename = generateFilename(event, pattern, messageObject);
				}
				if (filename == null) {
					throw new IOException("Filename is null");
				}

				String template = replacer.replaceValues(connector.getTemplate(), messageObject, filename);
				byte[] buffer = null;
				if (connector.isBinary()) {
					BASE64Decoder base64 = new BASE64Decoder();
					buffer = base64.decodeBuffer(template);
				} else {
					buffer = template.getBytes();
					//TODO: Add support for Charset encodings in 1.4.1
				}
				// TODO: have this mode be set by the connector
				int mode = ChannelSftp.OVERWRITE;
				client = connector.getClient(uri);
				client.put(new ByteArrayInputStream(buffer), filename, mode);
				
				//update the message status to sent
				messageObjectController.setSuccess(messageObject, "File successfully written: " + filename);
		} catch (Exception e) {
			messageObjectController.setError(messageObject, "Error writing to SFTP: ", e);
			connector.handleException(e);
		} finally {
			connector.releaseClient(uri, client);
		}
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		ChannelSftp client = null;

		try {
			client = connector.getClient(endpointUri);

			FilenameFilter filenameFilter = null;
			String filter = (String) endpointUri.getParams().get("filter");

			if (filter != null) {
				filter = URLDecoder.decode(filter, MuleManager.getConfiguration().getEncoding());
				filenameFilter = new FilenameWildcardFilter(filter);
			}

			Vector entries = client.ls(".");
			List<ChannelSftp.LsEntry> files = new ArrayList<ChannelSftp.LsEntry>();

			for (Iterator iter = entries.iterator(); iter.hasNext();) {
				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

				if (!entry.getAttrs().isDir() && !entry.getAttrs().isLink()) {
					if ((filenameFilter == null) || (filenameFilter.accept(null, entry.getFilename()))) {
						files.add(entry);
					}
				}
			}

			if (files.isEmpty()) {
				return null;
			}

			ChannelSftp.LsEntry entry = files.get(0);
			logger.debug("processing file: " + entry.getFilename());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			client.get(entry.getFilename(), baos);
			UMOMessage message = new MuleMessage(connector.getMessageAdapter(baos.toByteArray()));
			message.setProperty(SftpConnector.PROPERTY_FILENAME, entry.getFilename());
			client.rm(entry.getFilename());
			return message;

		} finally {
			connector.releaseClient(endpointUri, client);
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
