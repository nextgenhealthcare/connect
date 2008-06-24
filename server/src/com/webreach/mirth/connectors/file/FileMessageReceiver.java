/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.PollingMessageReceiver;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.routing.RoutingException;
import org.mule.util.Utility;

import sun.misc.BASE64Encoder;

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
 * <code>FileMessageReceiver</code> is a polling listener that reads files
 * from a directory.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */

public class FileMessageReceiver extends PollingMessageReceiver {
	private String readDir = null;
	private String moveDir = null;
	private String errorDir = null;
	private File readDirectory = null;
	private File moveDirectory = null;
	private File errorDirectory = null;
	private String moveToPattern = null;
	private FilenameFilter filenameFilter = null;
	private boolean routingError = false;

	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.READER;

	public FileMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readDir, String moveDir, String moveToPattern, String errorDir, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.readDir = replacer.replaceValuesFromGlobal(readDir, true);
		this.moveDir = replacer.replaceValuesFromGlobal(moveDir, true);
		this.moveToPattern = replacer.replaceValuesFromGlobal(moveToPattern, true);
		this.errorDir = replacer.replaceValuesFromGlobal(errorDir, true);

		if (((FileConnector) connector).getPollingType().equals(FileConnector.POLLING_TYPE_TIME))
			setTime(((FileConnector) connector).getPollingTime());
		else
			setFrequency(((FileConnector) connector).getPollingFrequency());

		filenameFilter = new FilenameWildcardFilter(replacer.replaceValuesFromGlobal(((FileConnector) connector).getFileFilter(), true));
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doConnect() throws Exception {
		if (readDir != null) {
			readDirectory = Utility.openDirectory(readDir);
			if (!(readDirectory.canRead())) {
				throw new ConnectException(new Message(Messages.FILE_X_DOES_NOT_EXIST, readDirectory.getAbsolutePath()), this);
			} else {
				logger.debug("Listening on endpointUri: " + readDirectory.getAbsolutePath());
			}
		}

		if (moveDir != null) {
			moveDirectory = Utility.openDirectory((moveDir));
			if (!(moveDirectory.canRead()) || !moveDirectory.canWrite()) {
				throw new ConnectException(new Message("file", 5), this);
			}
		}

		if (errorDir != null) {
			errorDirectory = Utility.openDirectory((errorDir));
			if (!(errorDirectory.canRead()) || !errorDirectory.canWrite()) {
				throw new ConnectException(new Message("file", 5), this);
			}
		}

	}

	public void doDisconnect() throws Exception {}

	public void poll() {
		monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
		try {
			File[] files = listFiles();

			if (files == null) {
				return;
			}

			// sort files by specified attribute before sorting
			sortFiles(files);
			routingError = false;
			
			for (int i = 0; i < files.length; i++) {
				//
				if (!routingError && !files[i].isDirectory()) {
					monitoringController.updateStatus(connector, connectorType, Event.BUSY);
					processFile(files[i]);
					monitoringController.updateStatus(connector, connectorType, Event.DONE);
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, null, e);
			handleException(e);
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	public void sortFiles(File[] files) {
		String sortAttribute = ((FileConnector) connector).getSortAttribute();

		if (sortAttribute.equals(FileConnector.SORT_DATE)) {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Float.compare(file1.lastModified(), file2.lastModified());
				}
			});
		} else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Float.compare(file1.length(), file2.length());
				}
			});
		} else {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.compareTo(file2);
				}
			});
		}
	}

	public synchronized void processFile(File file) throws UMOException {
		boolean checkFileAge = ((FileConnector) connector).getCheckFileAge();
		if (checkFileAge) {
			long fileAge = ((FileConnector) connector).getFileAge();
			long lastMod = file.lastModified();
			long now = (new java.util.Date()).getTime();
			if ((now - lastMod) < fileAge)
				return;
		}
		FileConnector connector = (FileConnector) this.connector;
		File destinationFile = null;
		String originalFilename = file.getName();
		UMOMessageAdapter adapter = connector.getMessageAdapter(file);
		adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);

		if (moveDir != null) {
			String fileName = file.getName();

			if (moveToPattern != null) {
				fileName = connector.getFilenameParser().getFilename(adapter, moveToPattern);
			}

			destinationFile = new File(moveDir, fileName);
		}

		boolean resultOfFileMoveOperation = false;

		try {
			// Perform some quick checks to make sure file can be processed
			if (file.isDirectory()) {
				// ignore directories
			} else if (!(file.canRead() && file.exists() && file.isFile())) {
				throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getName()));
			} else {
				Exception fileProcesedException = null;
				try {
					// ast: use the user-selected encoding

					if (connector.isProcessBatchFiles()) {
						List<String> messages = new BatchMessageProcessor().processHL7Messages(new InputStreamReader(new FileInputStream(file), connector.getCharsetEncoding()));

						for (Iterator iter = messages.iterator(); iter.hasNext() && (fileProcesedException == null);) {
							String message = (String) iter.next();
							UMOMessageAdapter batchAdapter = connector.getMessageAdapter(message);
							batchAdapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
							UMOMessage umoMessage = routeMessage(new MuleMessage(batchAdapter), endpoint.isSynchronous());
							if (umoMessage != null) {
								postProcessor.doPostProcess(umoMessage.getPayload());
							}
						}
					} else {

						byte[] contents = getBytesFromFile(file);
						String message = "";
						if (connector.isBinary()) {
							BASE64Encoder encoder = new BASE64Encoder();
							message = encoder.encode(contents);
						} else {
							message = new String(contents, connector.getCharsetEncoding());
						}
						adapter = connector.getMessageAdapter(message);
						adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, originalFilename);
						UMOMessage umoMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());
						if (umoMessage != null) {
							postProcessor.doPostProcess(umoMessage.getPayload());
						}
					}
				} catch (RoutingException e) {
					logger.error("Unable to route." + StackTracePrinter.stackTraceToString(e));
					
					// routingError is reset to false at the beginning of the poll method
					routingError = true;
					
					if (errorDir != null) {
						logger.error("Moving file to error directory: " + errorDir);
						destinationFile = new File(errorDir, file.getName());
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					fileProcesedException = new MuleException(new Message(Messages.FAILED_TO_READ_PAYLOAD, file.getName()));
				}

				// move the file if needed
				if (destinationFile != null) {
					try {
						destinationFile.delete();
					} catch (Exception e) {
						logger.info("Unable to delete destination file");
					}

					resultOfFileMoveOperation = file.renameTo(destinationFile);

					if (!resultOfFileMoveOperation) {
						throw new MuleException(new Message("file", 4, file.getAbsolutePath(), destinationFile.getAbsolutePath()));
					}
				}

				if (connector.isAutoDelete()) {
					adapter.getPayloadAsBytes();

					// no moveTo directory
					if (destinationFile == null) {
						resultOfFileMoveOperation = file.delete();

						if (!resultOfFileMoveOperation) {
							throw new MuleException(new Message("file", 3, file.getAbsolutePath()));
						}
					}
				}
				
				if (fileProcesedException != null) {
					throw fileProcesedException;
				}
			}
		} catch (Exception e) {
			alertController.sendAlerts(((FileConnector) connector).getChannelId(), Constants.ERROR_403, "", e);
			handleException(e);
		}
	}

	// Returns the contents of the file in a byte array.
	private byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Exception tolerant roll back method
	 */
	private boolean rollbackFileMove(File sourceFile, String destinationFilePath) {
		boolean result = false;
		try {
			result = sourceFile.renameTo(new File(destinationFilePath));
		} catch (Throwable t) {
			logger.debug("rollback of file move failed: " + t.getMessage());
		}
		return result;
	}

	/**
	 * Get a list of files to be processed.
	 * 
	 * @return a list of files to be processed.
	 * @throws org.mule.MuleException
	 *             which will wrap any other exceptions or errors.
	 */
	File[] listFiles() throws MuleException {
		File[] todoFiles = new File[0];
		try {
			todoFiles = readDirectory.listFiles(filenameFilter);
		} catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
		return todoFiles;
	}

	public boolean isRoutingError() {
		return routingError;
	}

	public void setRoutingError(boolean routingError) {
		this.routingError = routingError;
	}

}
