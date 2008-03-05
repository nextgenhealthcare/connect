/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpMessageReceiver.java,v 1.10 2005/09/27 16:21:40 aperepel Exp $
 * $Revision: 1.10 $
 * $Date: 2005/09/27 16:21:40 $
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

import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.routing.RoutingException;

import sun.misc.BASE64Encoder;

import com.webreach.mirth.connectors.file.FileConnector;
import com.webreach.mirth.connectors.file.filters.FilenameWildcardFilter;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;
import com.webreach.mirth.server.util.BatchMessageProcessor;
import com.webreach.mirth.server.util.StackTracePrinter;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.10 $
 */
public class FtpMessageReceiver extends PollingMessageReceiver {
	protected Set currentFiles = Collections.synchronizedSet(new HashSet());
	protected FtpConnector connector;
	private FilenameFilter filenameFilter = null;
	private boolean routingError = false;
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
	private ConnectorType connectorType = ConnectorType.READER;

	public FtpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.connector = (FtpConnector) connector;

		if (((FtpConnector) connector).getPollingType().equals(FtpConnector.POLLING_TYPE_TIME))
			setTime(((FtpConnector) connector).getPollingTime());
		else
			setFrequency(((FtpConnector) connector).getPollingFrequency());

		filenameFilter = new FilenameWildcardFilter(this.connector.getFileFilter());
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void poll() {
		try {
			monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
			FTPFile[] files = listFiles();
			sortFiles(files);
			routingError = false;
			
			for (int i = 0; i < files.length; i++) {
				final FTPFile file = files[i];
				if (!currentFiles.contains(file.getName())) {

					try {
						monitoringController.updateStatus(connector, connectorType, Event.BUSY);
						currentFiles.add(file.getName());
						if (!routingError) {
							processFile(file);
						}
					} catch (Exception e) {
						alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, null, e);
						connector.handleException(e);
					} finally {
						monitoringController.updateStatus(connector, connectorType, Event.DONE);
						currentFiles.remove(file.getName());
					}
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, null, e);
			handleException(e);
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	public void sortFiles(FTPFile[] files) {
		String sortAttribute = connector.getSortAttribute();

		if (sortAttribute.equals(FileConnector.SORT_DATE)) {
			Arrays.sort(files, new Comparator<FTPFile>() {
				public int compare(FTPFile file1, FTPFile file2) {
					return file1.getTimestamp().compareTo(file2.getTimestamp());
				}
			});
		} else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
			Arrays.sort(files, new Comparator<FTPFile>() {
				public int compare(FTPFile file1, FTPFile file2) {
					return Float.compare(file1.getSize(), file2.getSize());
				}
			});
		} else {
			Arrays.sort(files, new Comparator<FTPFile>() {
				public int compare(FTPFile file1, FTPFile file2) {
					return file1.getName().compareToIgnoreCase(file2.getName());
				}
			});
		}
	}

	protected FTPFile[] listFiles() throws Exception {
		FTPClient client = null;
		UMOEndpointURI uri = endpoint.getEndpointURI();
		try {
			client = connector.getFtp(uri, null);
			if (!client.changeWorkingDirectory(uri.getPath())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			FTPFile[] files = client.listFiles();
			if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			if (files == null || files.length == 0) {
				return files;
			}
			List v = new ArrayList();
			for (int i = 0; i < files.length; i++) {
				if ((files[i] != null) && files[i].isFile()) {
					if (filenameFilter == null || filenameFilter.accept(null, files[i].getName())) {
						v.add(files[i]);
					}
				}
			}
			return (FTPFile[]) v.toArray(new FTPFile[v.size()]);

		} finally {
			try {
				connector.releaseFtp(uri, client, null);
			} catch (Exception e) {
				logger.debug("Could not release FTP connection.", e);
				connector.destroyFtp(uri, client, null);
			}
		}
	}

	protected void processFile(FTPFile file) throws Exception {
		boolean checkFileAge = connector.isCheckFileAge();
		String originalFilename = file.getName();

		if (checkFileAge) {
			long fileAge = connector.getFileAge();
			long lastMod = file.getTimestamp().getTimeInMillis();
			long now = (new java.util.Date()).getTime();
			if ((now - lastMod) < fileAge)
				return;
		}

		UMOEndpointURI uri = endpoint.getEndpointURI();
		UMOMessageAdapter adapter = connector.getMessageAdapter(file);
		adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
		String destinationFile = null;
		boolean resultOfFileMoveOperation = false;
		FTPClient client = null;
		String moveDir = connector.getMoveToDirectory();
		String errorDir = connector.getMoveToErrorDirectory();
		
		try {
			client = connector.getFtp(uri, null);

			if (moveDir != null) {
				if (connector.getMoveToPattern() != null) {
					destinationFile = connector.getFilenameParser().getFilename(adapter, connector.getMoveToPattern());
				}

				destinationFile = destinationFile.replaceAll("//", "/");
			}
			
			Exception fileProcesedException = null;
			
			try {
				if (!client.changeWorkingDirectory(endpoint.getEndpointURI().getPath())) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				if (!client.retrieveFile(file.getName(), baos)) {
					// this might happen if another thread already processed
					// file
					throw new IOException("Ftp error: " + client.getReplyCode() + " error retrieving file: " + file.getName() + ". Might be thread race condition.");
				}

				byte[] contents = baos.toByteArray();

				if (connector.isProcessBatchFiles()) {
					List<String> messages = new BatchMessageProcessor().processHL7Messages(new String(contents, connector.getCharsetEncoding()));
					
					for (Iterator iter = messages.iterator(); iter.hasNext() && (fileProcesedException == null);) {
						String message = (String) iter.next();
						adapter = connector.getMessageAdapter(message.getBytes(connector.getCharsetEncoding()));
						adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
						UMOMessage umoMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
						postProcessor.doPostProcess(umoMessage.getPayload());
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
					UMOMessage umoMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
					postProcessor.doPostProcess(umoMessage.getPayload());
				}
			} catch (RoutingException e) {
				logger.error("Unable to route. " + StackTracePrinter.stackTraceToString(e));
				
				// routingError is reset to false at the beginning of the poll method
				routingError = true;
				
				if (errorDir != null) {
					moveDir = errorDir;
				}
			} catch (Exception e) {
				fileProcesedException = new MuleException(new Message(Messages.FAILED_TO_READ_PAYLOAD, file.getName()));;
			}

			// move the file if needed
			if (destinationFile != null) {
				if (!client.changeWorkingDirectory(moveDir)) {
					if (!client.makeDirectory(moveDir)) {
						if (moveDir.startsWith("/")) {
							moveDir = moveDir.substring(1);
						}

						String[] dirs = moveDir.split("/");

						if (dirs.length > 0) {
							for (int i = 0; i < dirs.length; i++) {
								if (!client.changeWorkingDirectory(dirs[i])) {
									logger.debug("Making directory: " + dirs[i]);
									if (!client.makeDirectory(dirs[i])) {
										throw new Exception("Unable to make destination directory: " + dirs[i]);
									}
									if (!client.changeWorkingDirectory(dirs[i])) {
										throw new Exception("Unable to change to destination directory: " + dirs[i]);
									}
								}
							}
						}
					}
				}

				try {
					client.deleteFile(destinationFile);
				} catch (Exception e) {
					logger.info("Unable to delete destination file");
				}

				if (!client.changeWorkingDirectory(uri.getPath())) {
					throw new Exception("Unable to change to directory: " + uri.getPath().substring(1) + "/");
				}

				resultOfFileMoveOperation = client.rename((file.getName()).replaceAll("//", "/"), (moveDir + "/" + destinationFile).replaceAll("//", "/"));
				if (!resultOfFileMoveOperation) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}
			}

			if (connector.isAutoDelete()) {
				adapter.getPayloadAsBytes();
				// no moveTo directory
				if (destinationFile == null) {
					resultOfFileMoveOperation = client.deleteFile(file.getName());
					if (!resultOfFileMoveOperation) {
						throw new IOException("Ftp error: " + client.getReplyCode());
					}
				}
			}
			
			if (fileProcesedException != null) {
				throw fileProcesedException;
			}
		} catch (Exception e) {
			alertController.sendAlerts(((FtpConnector) connector).getChannelId(), Constants.ERROR_405, "", e);
			handleException(e);
		} finally {
			try {
				connector.releaseFtp(uri, client, null);
			} catch (Exception e) {
				logger.debug("Could not release FTP connection.", e);
				connector.destroyFtp(uri, client, null);
			}
		}
	}

	public void doConnect() throws Exception {
		FTPClient client = connector.getFtp(getEndpointURI(), null);
		connector.releaseFtp(getEndpointURI(), client, null);
	}

	public void doDisconnect() throws Exception {

	}

}
