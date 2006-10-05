package org.mule.providers.sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.mule.MuleManager;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.ProviderUtil;
import org.mule.providers.file.filters.FilenameWildcardFilter;
import org.mule.providers.ftp.FtpConnector;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.model.MessageObject;

public class SftpMessageDispatcher extends AbstractMessageDispatcher {
	protected SftpConnector connector;

	public SftpMessageDispatcher(SftpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		ChannelSftp client = null;
		UMOEndpointURI uri = event.getEndpoint().getEndpointURI();

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

			byte[] buffer;

			if (data instanceof byte[]) {
				buffer = (byte[]) data;
			} else if (data instanceof MessageObject) {
				MessageObject messageObject = (MessageObject) data;
				template = ProviderUtil.replaceValues(template, messageObject);
				buffer = template.getBytes();
			} else {
				buffer = data.toString().getBytes();
			}

			client = connector.getClient(uri);
			int mode = ChannelSftp.OVERWRITE;
			client.put(new ByteArrayInputStream(buffer), ".", mode);
		} finally {
			connector.releaseClient(uri, client);
		}
	}

	private String generateFilename(UMOEvent event, String pattern) {
		if (pattern == null) {
			pattern = connector.getOutputPattern();
		}

		return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
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

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public void doDispose() {

	}
}
