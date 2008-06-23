/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpConnector.java,v 1.7 2005/10/23 15:22:46 holger Exp $
 * $Revision: 1.7 $
 * $Date: 2005/10/23 15:22:46 $
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageReceiver;

import com.webreach.mirth.connectors.file.FilenameParser;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.SystemLogger;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.7 $
 */
public class FtpConnector extends AbstractServiceEnabledConnector {
    public static final String PROPERTY_POLLING_TYPE = "pollingType";
    public static final String PROPERTY_POLLING_TIME = "pollingTime";
	public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_BINARY = "binary";
	public static final String PROPERTY_FILE_AGE = "fileAge";
	public static final String PROPERTY_FILE_FILTER = "fileFilter";
	public static final String PROPERTY_MOVE_TO_PATTERN = "moveToPattern";
	public static final String PROPERTY_MOVE_TO_DIRECTORY = "moveToDirectory";
	public static final String PROPERTY_MOVE_TO_ERROR_DIRECTORY = "moveToErrorDirectory";
	public static final String PROPERTY_DELETE_ON_READ = "autoDelete";
	public static final String PROPERTY_DIRECTORY = "directory";
	public static final String PROPERTY_SORT_ATTRIBUTE = "sortAttribute";
	public static final String PROPERTY_BATCH_PROCESS = "processBatchFiles";
	public static final String SORT_NAME = "name";;
	public static final String SORT_DATE = "date";
	public static final String SORT_SIZE = "size";
	public static final long DEFAULT_POLLING_FREQUENCY = 1000;
    
    public static final String POLLING_TYPE_INTERVAL = "interval";
    public static final String POLLING_TYPE_TIME = "time";
    
	// ast: encoding Charset
	public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
	public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
	public static final String DEFAULT_CHARSET_ENCODING = System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());

	public static final String PROPERTY_VALIDATE_CONNECTION = "validateConnections";
	/**
	 * Time in milliseconds to poll. On each poll the poll() method is called
	 */
    private String pollingType = POLLING_TYPE_INTERVAL;
    private String pollingTime = "12:00 AM";
	private long pollingFrequency = 0;
	private String outputPattern = null;
	private String template = null;
	private FilenameParser filenameParser = new VariableFilenameParser();
	private Map pools = new HashMap();
	private String username;
	private String password;
	private boolean binary;
	private String moveToPattern = null;
	private String writeToDirectoryName = null;
	private String moveToDirectory = null;
	private String moveToErrorDirectory = null;
	private String sortAttribute = SORT_NAME;
	private boolean outputAppend = false;
	private boolean autoDelete = true;
	private boolean checkFileAge = false;
	private String fileFilter = "*.*";
	private long fileAge = 0;
	private FileOutputStream outputStream = null;
	private boolean serialiseObjects = false;
	private UMOMessageReceiver receiver = null;
	private boolean processBatchFiles = true;
	private boolean validateConnections = true;
	// ast: encoding charset
	private String charsetEncoding = DEFAULT_CHARSET_ENCODING;
	private String channelId;
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	public FtpConnector() {
		filenameParser = new VariableFilenameParser();
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

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public boolean isCheckFileAge() {
		return checkFileAge;
	}

	public void setCheckFileAge(boolean checkFileAge) {
		this.checkFileAge = checkFileAge;
	}

	public long getFileAge() {
		return fileAge;
	}

	public void setFileAge(long fileAge) {
		this.fileAge = fileAge;
	}

	public String getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
	}

	public String getMoveToDirectory() {
		return moveToDirectory;
	}

	public void setMoveToDirectory(String moveToDirectoryName) {
		this.moveToDirectory = moveToDirectoryName;
	}

	public String getMoveToErrorDirectory() {
		return moveToErrorDirectory;
	}

	public void setMoveToErrorDirectory(String moveToErrorDirectory) {
		this.moveToErrorDirectory = moveToErrorDirectory;
	}

	public String getMoveToPattern() {
		return moveToPattern;
	}

	public void setMoveToPattern(String moveToPattern) {
		this.moveToPattern = moveToPattern;
	}

	public boolean isOutputAppend() {
		return outputAppend;
	}

	public void setOutputAppend(boolean outputAppend) {
		this.outputAppend = outputAppend;
	}

	public FileOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(FileOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public boolean isProcessBatchFiles() {
		return processBatchFiles;
	}

	public void setProcessBatchFiles(boolean processBatchFiles) {
		this.processBatchFiles = processBatchFiles;
	}

	public String getSortAttribute() {
		return sortAttribute;
	}

	public void setSortAttribute(String sortAttribute) {
		this.sortAttribute = sortAttribute;
	}

	public String getWriteToDirectoryName() {
		return writeToDirectoryName;
	}

	public void setWriteToDirectoryName(String writeToDirectoryName) {
		this.writeToDirectoryName = writeToDirectoryName;
	}

	public String getProtocol() {
		return "ftp";
	}

	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		long polling = pollingFrequency;
		Map props = endpoint.getProperties();
		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
			if (tempPolling != null) {
				polling = Long.parseLong(tempPolling);
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
			polling = 1000;
		}
		logger.debug("set polling frequency to: " + polling);
		return serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { new Long(polling) });
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public FTPClient getFtp(UMOEndpointURI uri, MessageObject messageObject) throws Exception {
		ObjectPool pool = getFtpPool(uri, messageObject);
		return (FTPClient) pool.borrowObject();
	}

	public void releaseFtp(UMOEndpointURI uri, FTPClient client, MessageObject messageObject) throws Exception {
		if (isCreateDispatcherPerRequest()) {
			destroyFtp(uri, client, messageObject);
			UMOMessageDispatcher dispatcher = getDispatcher(uri.toString());
		} else {
			if (client != null && client.isConnected()) {
				ObjectPool pool = getFtpPool(uri, messageObject);
				pool.returnObject(client);
			}
		}
	}

	public void destroyFtp(UMOEndpointURI uri, FTPClient client, MessageObject messageObject) throws Exception {
		if (client != null) {
			ObjectPool pool = getFtpPool(uri, messageObject);
			pool.invalidateObject(client);
		}
	}

	protected synchronized ObjectPool getFtpPool(UMOEndpointURI uri, MessageObject messageObject) {
		String username = getUsername();
		String password = getPassword();
		if (messageObject == null){
			username = replacer.replaceValuesFromGlobal(username, true);
			password = replacer.replaceValuesFromGlobal(password, true);
		}else{
			if (username.indexOf('$') > -1)
				username = replacer.replaceValues(username, messageObject);
			if (password.indexOf('$') > -1)
				password = replacer.replaceValues(password, messageObject);
		}
		String key = username + ":" + password + "@" + uri.getHost() + ":" + uri.getPort();
		ObjectPool pool = (ObjectPool) pools.get(key);
		if (pool == null) {
			GenericObjectPool.Config config = new GenericObjectPool.Config();
			if (isValidateConnections()) {
				config.testOnBorrow = true;
				config.testOnReturn = true;
			}
			pool = new GenericObjectPool(new FtpConnectionFactory(uri.getHost(), uri.getPort(), username, password), config);

			pools.put(key, pool);
		}
		return pool;
	}

	/**
	 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
	 */
	protected class FtpConnectionFactory implements PoolableObjectFactory {
		private String username;
		private String password;
		private String host;
		int port;
		public FtpConnectionFactory(String host, int port, String username, String password) {			
			this.username = username;
			this.password = password;
			this.port = port;
			this.host = host;
		}

		public Object makeObject() throws Exception {
			FTPClient client = new FTPClient();
			try {
				if (port > 0) {
					client.connect(host, port);
				} else {
					client.connect(host);
				}
				if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}
				if (!client.login(username, password)) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}
				if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
					throw new IOException("Ftp error");
				}
			} catch (Exception e) {
				if (client.isConnected()) {
					client.disconnect();
				}
				throw e;
			}
			return client;
		}

		public void destroyObject(Object obj) throws Exception {
			try{
				FTPClient client = (FTPClient) obj;
				client.logout();
				client.disconnect();
			} catch (Exception e){
				logger.debug(e);
			}
		}

		public boolean validateObject(Object obj) {
			FTPClient client = (FTPClient) obj;
			try {
				client.sendNoOp();
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public void activateObject(Object obj) throws Exception {
			FTPClient client = (FTPClient) obj;
			client.setReaderThread(true);
		}

		public void passivateObject(Object obj) throws Exception {
			FTPClient client = (FTPClient) obj;
			client.setReaderThread(false);
		}
	}

	protected void doStop() throws UMOException {
		try {
			for (Iterator it = pools.values().iterator(); it.hasNext();) {
				ObjectPool pool = (ObjectPool) it.next();
				pool.close();
			}
		} catch (Exception e) {
			throw new ConnectorException(new Message(Messages.FAILED_TO_STOP_X, "FTP Connector"), this, e);
		}
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
	 * @return Returns the filenameParser.
	 */
	public FilenameParser getFilenameParser() {
		return filenameParser;
	}

	/**
	 * @param filenameParser
	 *            The filenameParser to set.
	 */
	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public boolean isValidateConnections() {
		return validateConnections;
	}

	public void setValidateConnections(boolean validateConnections) {
		this.validateConnections = validateConnections;
	}
	
    public String getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(String pollingTime) {
        this.pollingTime = pollingTime;
    }

    public String getPollingType() {
        return pollingType;
    }

    public void setPollingType(String pollingType) {
        this.pollingType = pollingType;
    }

    
}
