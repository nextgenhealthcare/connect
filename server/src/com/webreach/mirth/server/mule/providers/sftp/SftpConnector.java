package com.webreach.mirth.server.mule.providers.sftp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageReceiver;

import com.jcraft.jsch.ChannelSftp;
import com.webreach.mirth.server.mule.providers.file.FilenameParser;

public class SftpConnector extends AbstractServiceEnabledConnector {
	public static final String PROPERTY_ORIGINAL_FILENAME = "originalFilename";
	public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
	public static final String PROPERTY_FILENAME = "filename";
	public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_BINARY = "binary";
	
	private String username;
	private String password;
	private long pollingFrequency = 0;
	private String outputPattern = null;
	private String template = null;
	private FilenameParser filenameParser = new VariableFilenameParser();
	private Map pools = new HashMap();
	private boolean binary;

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

	public ChannelSftp getClient(UMOEndpointURI uri) throws Exception {
		ObjectPool pool = getClientPool(uri);
		return (ChannelSftp) pool.borrowObject();
	}

	public void releaseClient(UMOEndpointURI uri, ChannelSftp client) throws Exception {
		if (isCreateDispatcherPerRequest()) {
			destroyClient(uri, client);
			UMOMessageDispatcher dispatcher = getDispatcher(uri.toString());
		} else {
			if (client != null && client.isConnected()) {
				ObjectPool pool = getClientPool(uri);
				pool.returnObject(client);
			}
		}
	}

	public void destroyClient(UMOEndpointURI uri, ChannelSftp client) throws Exception {
		if ((client != null) && (client.isConnected())) {
			ObjectPool pool = getClientPool(uri);
			pool.invalidateObject(client);
		}
	}

	protected synchronized ObjectPool getClientPool(UMOEndpointURI uri) {
		String key = uri.getUsername() + ":" + uri.getPassword() + "@" + uri.getHost() + ":" + uri.getPort();
		ObjectPool pool = (ObjectPool) pools.get(key);
		
		if (pool == null) {
			pool = new GenericObjectPool(new SftpConnectionFactory(uri, username, password));
			pools.put(key, pool);
		}
		
		return pool;
	}

	protected void doStop() throws UMOException {
		try {
			for (Iterator iter = pools.values().iterator(); iter.hasNext();) {
				ObjectPool pool = (ObjectPool) iter.next();
				pool.close();
			}
		} catch (Exception e) {
			throw new ConnectorException(new Message(Messages.FAILED_TO_STOP_X, "FTP Connector"), this, e);
		}
	}

	public String getProtocol() {
		return "sftp";
	}

	public FilenameParser getFilenameParser() {
		return this.filenameParser;
	}

	public void setFilenameParser(FilenameParser filenameParser) {
		this.filenameParser = filenameParser;
	}

	public String getOutputPattern() {
		return this.outputPattern;
	}

	public void setOutputPattern(String outputPattern) {
		this.outputPattern = outputPattern;
	}

	public long getPollingFrequency() {
		return this.pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
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

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	
}
