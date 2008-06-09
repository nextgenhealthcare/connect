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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.MuleProperties;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.VariableFilenameParser;
import org.mule.transformers.simple.ByteArrayToSerializable;
import org.mule.transformers.simple.SerializableToByteArray;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.Utility;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.SystemLogger;

/**
 * <code>FileConnector</code> is used for setting up listeners on a directory
 * and for writing files to a directory. The connecotry provides support for
 * defining file output patterns and filters for receiving files.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.14 $
 */

public class FileConnector extends AbstractServiceEnabledConnector {
	/**
	 * logger used by this class
	 */
	private static transient Log logger = LogFactory.getLog(FileConnector.class);

	// These are properties that can be overridden on the Receiver by the
	// endpoint
	// declarations
    public static final String PROPERTY_POLLING_TYPE = "pollingType";
    public static final String PROPERTY_POLLING_TIME = "pollingTime";
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILE_AGE = "fileAge";
	public static final String PROPERTY_FILE_FILTER = "fileFilter";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern";
	public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory";
	public static final String PROPERTY_MOVE_TO_ERROR_DIRECTORY = "moveToErrorDirectory";
	public static final String PROPERTY_DELETE_ON_READ = "autoDelete";
	public static final String PROPERTY_DIRECTORY = "directory";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_SORT_ATTRIBUTE = "sortAttribute";
	public static final String PROPERTY_BATCH_PROCESS = "processBatchFiles";
	public static final String PROPERTY_BINARY = "binary";

	public static final String SORT_NAME = "name";
	public static final String SORT_DATE = "date";
	public static final String SORT_SIZE = "size";
	
    public static final String POLLING_TYPE_INTERVAL = "interval";
    public static final String POLLING_TYPE_TIME = "time";
    
	public static final long DEFAULT_POLLING_FREQUENCY = 1000;

	// ast: encoding Charset
	public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
	public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
	public static final String DEFAULT_CHARSET_ENCODING = System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());

	/**
	 * Time in milliseconds to poll. On each poll the poll() method is called
	 */
    private String pollingType = POLLING_TYPE_INTERVAL;
    private String pollingTime = "12:00 AM";
	private long pollingFrequency = 0;
	private String moveToPattern = null;
	private String writeToDirectoryName = null;
	private String moveToDirectory = null;
	private String moveToErrorDirectory = null;
	private String outputPattern = null;
	private String sortAttribute = SORT_NAME;
	private boolean outputAppend = false;
	private boolean autoDelete = true;
	private boolean checkFileAge = false;
	private String fileFilter = "*";
	private long fileAge = 0;
	private String template = null;
	private FileOutputStream outputStream = null;
	private boolean serialiseObjects = false;
	public FilenameParser filenameParser = new VariableFilenameParser();
	private UMOMessageReceiver receiver = null;
	private boolean processBatchFiles = true;
	// ast: encoding charset
	private String charsetEncoding = DEFAULT_CHARSET_ENCODING;
	private boolean binary = false;
	private String channelId;
	private Map protocolProperties;
	private String inboundProtocol;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.AbstractConnector#doInitialise()
	 */
	public FileConnector() {
		filenameParser = new VariableFilenameParser();
		// ast: try to set the default encoding
		this.setCharsetEncoding(DEFAULT_CHARSET_ENCODING);
	}

	protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint) {
		// if(endpoint.getFilter()!=null) {
		// return endpoint.getEndpointURI().getAddress() + "/" +
		// ((FilenameWildcardFilter)endpoint.getFilter()).getPattern();
		// }
		// TODO: Fix later -cl
		return endpoint.getEndpointURI().getAddress();
	}

	/**
	 * Registers a listener for a particular directory The following properties
	 * can be overriden in the endpoint declaration
	 * <ul>
	 * <li>moveToDirectory</li>
	 * <li>filterPatterns</li>
	 * <li>filterClass</li>
	 * <li>pollingFrequency</li>
	 * </ul>
	 */
	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		String readDir = endpoint.getEndpointURI().getAddress();
		long polling = this.pollingFrequency;

		String moveTo = moveToDirectory;
		Map props = endpoint.getProperties();
		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String move = (String) props.get(PROPERTY_MOVE_TO_DIRECTORY);
			if (move != null) {
				moveTo = move;
			}
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
			}
			Long tempFileAge = (Long) props.get(PROPERTY_FILE_AGE);
			if (tempFileAge != null) {
				setFileAge(tempFileAge.longValue());
			}
            String pollingType = (String) props.get(PROPERTY_POLLING_TYPE);
            if (pollingType != null) {
                setPollingType(pollingType);
            }
            String pollingTime = (String) props.get(PROPERTY_POLLING_TIME);
            if (pollingTime != null) {
                setPollingTime(pollingTime);
            }
		}
		if (polling <= 0) {
			polling = DEFAULT_POLLING_FREQUENCY;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("set polling frequency to: " + polling);
		}
		try {
			receiver = serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { readDir, moveTo, moveToPattern, moveToErrorDirectory, new Long(polling) });
			return receiver;
		} catch (Exception e) {
			throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X_WITH_X, "Message Receiver", serviceDescriptor.getMessageReceiver()), e, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#stop()
	 */
	protected synchronized void doStop() throws UMOException {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.warn("Failed to close file output stream on stop: " + e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#stop()
	 */
	protected synchronized void doStart() throws UMOException {
		if (receiver != null) {
			((FileMessageReceiver) receiver).setRoutingError(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#getProtocol()
	 */
	public String getProtocol() {
		return "FILE";
	}

	public FilenameParser getFilenameParser() {
		return filenameParser;
	}

	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.AbstractConnector#doDispose()
	 */
	protected void doDispose() {
		try {
			doStop();
		} catch (UMOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @return Returns the moveToDirectoryName.
	 */
	public String getMoveToDirectory() {
		return moveToDirectory;
	}

	/**
	 * @param dir
	 *            The moveToDirectoryName to set.
	 */
	public void setMoveToDirectory(String dir) throws IOException {
		this.moveToDirectory = dir;
	}

	public String getMoveToErrorDirectory() {
		return moveToErrorDirectory;
	}

	public void setMoveToErrorDirectory(String dir) {
		this.moveToErrorDirectory = dir;
	}

	/**
	 * @return Returns the outputAppend.
	 */
	public boolean isOutputAppend() {
		return outputAppend;
	}

	/**
	 * @param outputAppend
	 *            The outputAppend to set.
	 */
	public void setOutputAppend(boolean outputAppend) {
		this.outputAppend = outputAppend;
	}

	/**
	 * @return Returns the outputPattern.
	 */
	public String getOutputPattern() {
		return outputPattern;
	}

	/**
	 * @param outputPattern
	 *            The outputPattern to set.
	 */
	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
	}

	/**
	 * @return Returns the outputStream.
	 */
	public FileOutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * @param outputStream
	 *            The outputStream to set.
	 */
	public void setOutputStream(FileOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	/**
	 * @return Returns the pollingFrequency.
	 */
	public long getPollingFrequency() {
		return pollingFrequency;
	}

	/**
	 * @param pollingFrequency
	 *            The pollingFrequency to set.
	 */
	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	/**
	 * @return Returns the fileAge.
	 */
	public long getFileAge() {
		return fileAge;
	}

	public boolean getCheckFileAge() {
		return checkFileAge;
	}

	/**
	 * @param fileAge
	 *            The fileAge in seconds to set.
	 */
	public void setFileAge(long fileAge) {
		this.fileAge = fileAge;
		this.checkFileAge = true;
	}

	/**
	 * @return Returns the writeToDirectory.
	 */
	public String getWriteToDirectory() {
		return writeToDirectoryName;
	}

	/**
	 * @return Contents
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * 
	 * @param val =
	 *            template to set
	 */
	public void setTemplate(String val) {
		template = val;
	}

	/**
	 * @param dir
	 *            The writeToDirectory to set.
	 */
	public void setWriteToDirectory(String dir) throws IOException {
		this.writeToDirectoryName = dir;
		if (writeToDirectoryName != null) {
			File writeToDirectory = Utility.openDirectory((writeToDirectoryName));
			if (!(writeToDirectory.canRead()) || !writeToDirectory.canWrite()) {
				throw new IOException("Error on initialization, Write To directory does not exist or is not read/write");
			}
		}
	}

	public boolean isSerialiseObjects() {
		return serialiseObjects;
	}

	public void setSerialiseObjects(boolean serialiseObjects) {
		// set serialisable transformers on the connector if this is set
		if (serialiseObjects) {
			if (serviceOverrides == null)
				serviceOverrides = new Properties();
			serviceOverrides.setProperty(MuleProperties.CONNECTOR_INBOUND_TRANSFORMER, ByteArrayToSerializable.class.getName());
			serviceOverrides.setProperty(MuleProperties.CONNECTOR_OUTBOUND_TRANSFORMER, SerializableToByteArray.class.getName());
		}

		this.serialiseObjects = serialiseObjects;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		if (!autoDelete) {
			if (serviceOverrides == null)
				serviceOverrides = new Properties();
			if (serviceOverrides.getProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER) == null)
				serviceOverrides.setProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER, TextLineMessageAdapter.class.getName());
		}
	}

	public String getMoveToPattern() {
		return moveToPattern;
	}

	public void setMoveToPattern(String moveToPattern) {
		this.moveToPattern = moveToPattern;
	}

	public String getSortAttribute() {
		return this.sortAttribute;
	}

	public void setSortAttribute(String sortAttribute) {
		this.sortAttribute = sortAttribute;
	}

	// ast: set the charset Encoding
	public void setCharsetEncoding(String charsetEncoding) {
		if ((charsetEncoding == null) || (charsetEncoding.equals("")) || (charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING")))
			charsetEncoding = DEFAULT_CHARSET_ENCODING;
		logger.debug("FileConnector: trying to set the encoding to " + charsetEncoding);
		try {
			byte b[] = { 20, 21, 22, 23 };
			String k = new String(b, charsetEncoding);
			this.charsetEncoding = charsetEncoding;
		} catch (Exception e) {
			// set the encoding to the default one: this charset can't launch an
			// exception
			this.charsetEncoding = java.nio.charset.Charset.defaultCharset().name();
			logger.error("Impossible to use [" + charsetEncoding + "] as the Charset Encoding: changing to the platform default [" + this.charsetEncoding + "]");
			SystemLogger systemLogger = SystemLogger.getInstance();
			SystemEvent event = new SystemEvent("Exception occured in channel.");
			event.setDescription("Impossible to use [" + charsetEncoding + "] as the Charset Encoding: changing to the platform default [" + this.charsetEncoding + "]");
			systemLogger.logSystemEvent(event);
		}
	}

	// ast: get the charset encoding
	public String getCharsetEncoding() {
		if ((this.charsetEncoding == null) || (this.charsetEncoding.equals("")) || (this.charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))) {
			// Default Charset
			return DEFAULT_CHARSET_ENCODING;
		}
		return (this.charsetEncoding);
	}

	public String getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
	}

	public boolean isProcessBatchFiles() {
		return processBatchFiles;
	}

	public void setProcessBatchFiles(boolean processBatchFiles) {
		this.processBatchFiles = processBatchFiles;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

    public String getPollingTime()
    {
        return pollingTime;
    }

    public void setPollingTime(String pollingTime)
    {
        this.pollingTime = pollingTime;
    }

    public String getPollingType()
    {
        return pollingType;
    }

    public void setPollingType(String pollingType)
    {
        this.pollingType = pollingType;
    }

	public Map getProtocolProperties() {
		return protocolProperties;
	}

	public void setProtocolProperties(Map protocolProperties) {
		this.protocolProperties = protocolProperties;
	}

	public String getInboundProtocol() {
		return inboundProtocol;
	}

	public void setInboundProtocol(String inboundProtocol) {
		this.inboundProtocol = inboundProtocol;
	}
}
