package com.webreach.mirth.connectors.sftp;

import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.routing.RoutingException;

import sun.misc.BASE64Encoder;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.connectors.file.FileConnector;
import com.webreach.mirth.connectors.file.filters.FilenameWildcardFilter;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.util.BatchMessageProcessor;
import com.webreach.mirth.server.util.StackTracePrinter;

public class SftpMessageReceiver extends PollingMessageReceiver {
	private Logger logger = Logger.getLogger(this.getClass());
	protected SftpConnector connector;
	private FilenameFilter filenameFilter = null;
	protected Set currentFiles = Collections.synchronizedSet(new HashSet());
	private AlertController alertController = new AlertController();

	private boolean routingError = false;

	public SftpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.connector = (SftpConnector) connector;
        
        if(((SftpConnector) connector).getPollingType().equals(SftpConnector.POLLING_TYPE_TIME))
            setTime(((SftpConnector) connector).getPollingTime());
        else
            setFrequency(((SftpConnector) connector).getPollingFrequency());
        
		filenameFilter = new FilenameWildcardFilter(this.connector.getFileFilter());
	}

	public void poll() {
		try {
			List files = listFiles();
			sortFiles(files);
			
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				final ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

				if (!currentFiles.contains(entry.getFilename())) {
					getWorkManager().scheduleWork(new Work() {
						public void run() {
							try {
								currentFiles.add(entry.getFilename());
								if (!routingError)
									processFile(entry);
							} catch (Exception e) {
								alertController.sendAlerts(((SftpConnector) connector).getChannelId(), Constants.ERROR_409, null, e);
								connector.handleException(e);
							} finally {
								currentFiles.remove(entry.getFilename());
							}
						}

						public void release() {}
					});
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((SftpConnector) connector).getChannelId(), Constants.ERROR_409, null, e);
			handleException(e);
		}
	}

	protected List<ChannelSftp.LsEntry> listFiles() throws Exception {
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

	public void sortFiles(List<ChannelSftp.LsEntry> files) {
		String sortAttribute = connector.getSortAttribute();
		ChannelSftp.LsEntry[] sftpFiles = new ChannelSftp.LsEntry[] {};
		sftpFiles = files.toArray(sftpFiles);
		if (sortAttribute.equals(FileConnector.SORT_DATE)) {
			Arrays.sort(sftpFiles, new Comparator<ChannelSftp.LsEntry>() {
				public int compare(ChannelSftp.LsEntry file1, ChannelSftp.LsEntry file2) {
					return new Integer(file1.getAttrs().getMTime()).compareTo(file2.getAttrs().getMTime());
				}
			});
		} else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
			Arrays.sort(sftpFiles, new Comparator<ChannelSftp.LsEntry>() {
				public int compare(ChannelSftp.LsEntry file1, ChannelSftp.LsEntry file2) {
					return new Long(file1.getAttrs().getSize()).compareTo(file2.getAttrs().getSize());
				}
			});
		} else {
			Arrays.sort(sftpFiles, new Comparator<ChannelSftp.LsEntry>() {
				public int compare(ChannelSftp.LsEntry file1, ChannelSftp.LsEntry file2) {
					return file1.getFilename().compareToIgnoreCase(file2.getFilename());
				}
			});
		}
	}

	// TODO: The file reader, ftp reader, sftp reader patterns are EXACTLY the
	// same, let's do this in a more intelligent manner
	protected void processFile(ChannelSftp.LsEntry file) throws Exception {
		boolean checkFileAge = connector.isCheckFileAge();
		String originalFilename = file.getFilename();
		if (checkFileAge) {
			long fileAge = connector.getFileAge();
			long lastMod = file.getAttrs().getMTime();
			long now = (new java.util.Date()).getTime();
			if ((now - lastMod) < fileAge)
				return;
		}
		UMOEndpointURI uri = endpoint.getEndpointURI();
		UMOMessageAdapter adapter = connector.getMessageAdapter(file);
		adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
		String destinationFile = null;
		boolean resultOfFileMoveOperation = false;
		ChannelSftp client = null;
		String moveDir = connector.getMoveToDirectory();

		try {
			client = connector.getClient(uri);
			if (moveDir != null) {

				String fileName = file.getFilename();
				if (connector.getMoveToPattern() != null) {
					destinationFile = connector.getFilenameParser().getFilename(adapter, connector.getMoveToPattern());
				}

				destinationFile = destinationFile.replaceAll("//", "/");
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			client.get(file.getFilename(), baos);
			byte[] contents = baos.toByteArray();
			if (connector.isProcessBatchFiles()) {

				List<String> messages = new BatchMessageProcessor().processHL7Messages(new String(contents, connector.getCharsetEncoding()));
				Exception fileProcesedException = null;
				for (Iterator iter = messages.iterator(); iter.hasNext() && (fileProcesedException == null);) {
					String message = (String) iter.next();
					adapter = connector.getMessageAdapter(message.getBytes(connector.getCharsetEncoding()));
					adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
					routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
				}
			} else {
				String message = "";
				if (connector.isBinary()) {
					BASE64Encoder encoder = new BASE64Encoder();
					message = encoder.encode(contents);
					adapter = connector.getMessageAdapter(message.getBytes());
				} else {
					message = new String(contents, connector.getCharsetEncoding());
					adapter = connector.getMessageAdapter(contents);
				}
				adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
				routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
			}
			// move the file if needed
			if (destinationFile != null) {
				try {
					client.cd(moveDir);
				} catch (Exception e) {
					if (moveDir.startsWith("/")) {
						moveDir = moveDir.substring(1);
					}
					String[] dirs = moveDir.split("/");
					if (dirs.length > 0)
						for (int i = 0; i < dirs.length; i++) {
							try {
								client.cd(dirs[i]);
							} catch (Exception ex) {
								logger.debug("Making directory: " + dirs[i]);
								client.mkdir(dirs[i]);
								client.cd(dirs[i]);
							}
						}
				}
				try {
					client.rm(destinationFile);
				} catch (Exception e) {
					logger.info("Unable to delete destination file");
				}
				client.cd(client.getHome());
				client.cd(uri.getPath().substring(1) + "/"); // remove the first slash
				client.rename((file.getFilename()).replaceAll("//", "/"), (moveDir + "/" + destinationFile).replaceAll("//", "/"));
			}
			if (connector.isAutoDelete()) {
				adapter.getPayloadAsBytes();
				// no moveTo directory
				if (destinationFile == null) {
					client.rm(file.getFilename());
				}
			}
		} catch (RoutingException e) {
			logger.error("Unable to route. Stopping Connector: " + StackTracePrinter.stackTraceToString(e));
			// connector.stopConnector();
			// TODO: This was commented out (above). Do we need it?
			routingError = true;
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
