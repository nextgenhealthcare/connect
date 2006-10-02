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
package org.mule.providers.ftp;

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
import org.mule.providers.file.FilenameParser;
import org.mule.providers.file.SimpleFilenameParser;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageReceiver;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.7 $
 */
public class FtpConnector extends AbstractServiceEnabledConnector {

	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	public static final String PROPERTY_TEMPLATE = "template";

	/**
	 * Time in milliseconds to poll. On each poll the poll() method is called
	 */
	private long pollingFrequency = 0;

	private String outputPattern = null;

	private String template = null;

	private FilenameParser filenameParser = new SimpleFilenameParser();

	private Map pools = new HashMap();

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
		}
		if (polling <= 0) {
			polling = 1000;
		}
		logger.debug("set polling frequency to: " + polling);
		return serviceDescriptor.createMessageReceiver(this, component, endpoint, new Object[] { new Long(polling) });
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

	public FTPClient getFtp(UMOEndpointURI uri) throws Exception {
		ObjectPool pool = getFtpPool(uri);
		return (FTPClient) pool.borrowObject();
	}

	public void releaseFtp(UMOEndpointURI uri, FTPClient client) throws Exception {
		if (isCreateDispatcherPerRequest()) {
			destroyFtp(uri, client);
			UMOMessageDispatcher dispatcher = getDispatcher(uri.toString());
		} else {
			if (client != null && client.isConnected()) {
				ObjectPool pool = getFtpPool(uri);
				pool.returnObject(client);
			}
		}
	}

	public void destroyFtp(UMOEndpointURI uri, FTPClient client) throws Exception {
		if (client != null && client.isConnected()) {
			ObjectPool pool = getFtpPool(uri);
			pool.invalidateObject(client);
		}
	}

	protected synchronized ObjectPool getFtpPool(UMOEndpointURI uri) {
		String key = uri.getUsername() + ":" + uri.getPassword() + "@" + uri.getHost() + ":" + uri.getPort();
		ObjectPool pool = (ObjectPool) pools.get(key);
		if (pool == null) {
			pool = new GenericObjectPool(new FtpConnectionFactory(uri));
			pools.put(key, pool);
		}
		return pool;
	}

	/**
	 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
	 */
	protected class FtpConnectionFactory implements PoolableObjectFactory {
		private UMOEndpointURI uri;

		public FtpConnectionFactory(UMOEndpointURI uri) {
			this.uri = uri;
		}

		public Object makeObject() throws Exception {
			FTPClient client = new FTPClient();
			try {
				if (uri.getPort() > 0) {
					client.connect(uri.getHost(), uri.getPort());
				} else {
					client.connect(uri.getHost());
				}
				if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
					throw new IOException("Ftp error: " + client.getReplyCode());
				}
				if (!client.login(uri.getUsername(), uri.getPassword())) {
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
			FTPClient client = (FTPClient) obj;
			client.logout();
			client.disconnect();
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
}
