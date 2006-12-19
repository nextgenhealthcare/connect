package org.mule.providers.sftp;

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
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
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
		MessageObject messageObject = null;
		try {
			Object data = event.getTransformedMessage();
			if (data == null) {
				return;
			} else if (data instanceof MessageObject) {
				messageObject = (MessageObject) data;
				
				if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)){
					return;
				}
				if (messageObject.getCorrelationId() == null){
					//If we have no correlation id, this means this is the original message
					//so let's copy it and assign a new id and set the proper correlationid
					MessageObject clone = (MessageObject) messageObject.clone();
					clone.setId(UUIDGenerator.getUUID());
					clone.setDateCreated(Calendar.getInstance());
					clone.setCorrelationId(messageObject.getId());
					clone.setConnectorName(new ChannelController().getDestinationName(this.getConnector().getName()));
					messageObject = clone;
				}
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
				byte[] buffer = template.getBytes();
				client = connector.getClient(uri);
				
				// TODO: have this mode be set by the connector
				int mode = ChannelSftp.OVERWRITE;
				
				client.put(new ByteArrayInputStream(buffer), ".", mode);
				
				// update the message status to sent
				messageObject.setStatus(MessageObject.Status.SENT);
				messageObjectController.updateMessage(messageObject);
			} else {
				logger.warn("received data is not of expected type");
			}
		} catch (Exception e) {
			if (messageObject != null){
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + "Error writing to SFTP server\n" +  StackTracePrinter.stackTraceToString(e));
				messageObjectController.updateMessage(messageObject);
			}
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
