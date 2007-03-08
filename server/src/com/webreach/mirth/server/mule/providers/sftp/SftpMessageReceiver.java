package com.webreach.mirth.server.mule.providers.sftp;

import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.resource.spi.work.Work;

import org.apache.log4j.Logger;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import com.jcraft.jsch.ChannelSftp;

public class SftpMessageReceiver extends PollingMessageReceiver {
	private Logger logger = Logger.getLogger(this.getClass());
	protected SftpConnector connector;
	private FilenameFilter filenameFilter = null;
	protected Set currentFiles = Collections.synchronizedSet(new HashSet());

	public SftpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.connector = (SftpConnector) connector;

		if (endpoint.getFilter() instanceof FilenameFilter) {
			filenameFilter = (FilenameFilter) endpoint.getFilter();
		}
	}

	public void poll() throws Exception {
		List files = listFiles();

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			final ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

			if (!currentFiles.contains(entry.getFilename())) {
				getWorkManager().scheduleWork(new Work() {
					public void run() {
						try {
							currentFiles.add(entry.getFilename());
							processFile(entry);
						} catch (Exception e) {
							connector.handleException(e);
						} finally {
							currentFiles.remove(entry.getFilename());
						}
					}

					public void release() {}
				});
			}
		}
	}

	protected List listFiles() throws Exception {
		ChannelSftp client = null;
		UMOEndpointURI uri = endpoint.getEndpointURI();
		
		try {
			client = connector.getClient(uri);
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

			return files;
		} finally {
			connector.releaseClient(uri, client);
		}
	}

	protected void processFile(ChannelSftp.LsEntry entry) throws Exception {
		ChannelSftp client = null;
		UMOEndpointURI uri = endpoint.getEndpointURI();
		
		try {
			client = connector.getClient(uri);
			logger.debug("processing file: " + entry.getFilename());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			client.get(entry.getFilename(), baos);
			UMOMessage message = new MuleMessage(connector.getMessageAdapter(baos.toByteArray()));
			message.setProperty(SftpConnector.PROPERTY_ORIGINAL_FILENAME, entry.getFilename());
			routeMessage(message);
			client.rm(entry.getFilename());
		} finally {
			connector.releaseClient(uri, client);
		}
	}

	public void doConnect() throws Exception {
		ChannelSftp client = connector.getClient(getEndpointURI());
        connector.releaseClient(getEndpointURI(), client);
	}

	public void doDisconnect() throws Exception {
		
	}
}
