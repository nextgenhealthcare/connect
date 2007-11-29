/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcConnector.java,v 1.7 2005/06/23 08:01:29 gnt Exp $
 * $Revision: 1.7 $
 * $Date: 2005/06/23 08:01:29 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.mozilla.javascript.Script;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.TemplateValueReplacer;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.TransactionException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.util.CompiledScriptCache;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.7 $
 */
public class JdbcConnector extends AbstractServiceEnabledConnector {
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ScriptController.getInstance();
	
    public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
    
    public static final String PROPERTY_POLLING_TYPE = "pollingType";
    public static final String PROPERTY_POLLING_TIME = "pollingTime";
    
    public static final String POLLING_TYPE_INTERVAL = "interval";
    public static final String POLLING_TYPE_TIME = "time";
    
    private String pollingType = POLLING_TYPE_INTERVAL;
    private String pollingTime = "12:00 AM";
	private long pollingFrequency = 5000;
	private DataSource dataSource;
	private String driver;
	private String URL;
	private String username;
	private String password;
	private String dataSourceJndiName;
	private Context jndiContext;
	private String jndiInitialFactory;
	private String jndiProviderUrl;
	private boolean useAck;
	private Map providerProperties;
	private Map queries;

	private boolean useScript;
	private String scriptId;
	private String ackScriptId;
	private String channelId;
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	// This method gets called when the JDBC connector is initialized. It
	// compiles the JavaScript and adds it to the cache.
	@Override
	protected synchronized void initFromServiceDescriptor() throws InitialisationException {
		super.initFromServiceDescriptor();
		org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();

		try {
			if (scriptId != null) {
				String databaseScript = scriptController.getScript(scriptId);

				if (databaseScript != null) {
					String generatedDatabaseScript = generateDatabaseScript(databaseScript, false);
					logger.debug("compiling database script");
					Script compiledDatabaseScript = context.compileString(generatedDatabaseScript, scriptId, 1, null);
					compiledScriptCache.putCompiledScript(scriptId, compiledDatabaseScript);
				}
			}

			if (ackScriptId != null) {
				String ackScript = scriptController.getScript(ackScriptId);

				if (ackScript != null) {
					String generatedDatabaseScript = generateDatabaseScript(ackScript, true);
					logger.debug("compiling database ack script");
					Script compiledDatabaseScript = context.compileString(generatedDatabaseScript, ackScriptId, 1, null);
					compiledScriptCache.putCompiledScript(ackScriptId, compiledDatabaseScript);
				}
			}

		} catch (Exception e) {
			throw new InitialisationException(e, this);
		} finally {
			org.mozilla.javascript.Context.exit();
		}
	}

	// Generates the JavaScript based on the script which the user enters
	private String generateDatabaseScript(String databaseScript, boolean ack) {
		logger.debug("generating database script");
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");

		script.append("function $(string) { ");
		if (ack) {
			script.append("if (resultMap.get(string) != null) { return resultMap.get(string) } else ");
		}
		script.append("if (connectorMap.containsKey(string)) { return connectorMap.get(string);} else ");
		script.append("if (channelMap.containsKey(string)) { return channelMap.get(string);} else ");
		script.append("if (globalMap.containsKey(string)) { return globalMap.get(string);} else ");
		script.append("{ return ''; }}");
		script.append("function $g(key, value){");
		script.append("if (arguments.length == 1){return globalMap.get(key); }");
		script.append("else if (arguments.length == 2){globalMap.put(key, value); }}");
		script.append("function $c(key, value){");
		script.append("if (arguments.length == 1){return channelMap.get(key); }");
		script.append("else if (arguments.length == 2){channelMap.put(key, value); }}");
		script.append("function $co(key, value){");
		script.append("if (arguments.length == 1){return connectorMap.get(key); }");
		script.append("else if (arguments.length == 2){connectorMap.put(key, value); }}");
		script.append("function $r(key, value){");
		script.append("if (arguments.length == 1){return responseMap.get(key); }");
		script.append("else if (arguments.length == 2){responseMap.put(key, value); }}");
		script.append("function doDatabaseScript() {");
		script.append(databaseScript + "}\n");
		script.append("doDatabaseScript()\n");
		return script.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnector#getProtocol()
	 */
	public String getProtocol() {
		return "jdbc";
	}

	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		String[] params = {};
		if (!this.useScript) {
			params = getReadAndAckStatements(endpoint.getEndpointURI(), endpoint);
		}
        
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
        
		return getServiceDescriptor().createMessageReceiver(this, component, endpoint, params);
	}

	protected void initJndiContext() throws NamingException {
		if (this.jndiContext == null) {
			Hashtable props = new Hashtable();
			if (this.jndiInitialFactory != null) {
				props.put(Context.INITIAL_CONTEXT_FACTORY, this.jndiInitialFactory);
			}
			if (this.jndiProviderUrl != null) {
				props.put(Context.PROVIDER_URL, jndiProviderUrl);
			}
			if (this.providerProperties != null) {
				props.putAll(this.providerProperties);
			}
			this.jndiContext = new InitialContext(props);
		}
	}

	protected void createDataSource() throws InitialisationException, NamingException {
		Object temp = this.jndiContext.lookup(this.dataSourceJndiName);
		if (temp instanceof DataSource) {
			dataSource = (DataSource) temp;
		} else {
			throw new InitialisationException(new Message(Messages.JNDI_RESOURCE_X_NOT_FOUND, this.dataSourceJndiName), this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#create(java.util.HashMap)
	 */
	public void doInitialise() throws InitialisationException {
		super.doInitialise();
		try {
			// If we have a driver and url, there is no use to use a datasource
			// or initialize
			if ((driver != null) && (URL != null)) {

			} else if (dataSource == null) {
				// If we have a dataSource, there is no need to initialise
				// the JndiContext
				initJndiContext();
				createDataSource();
			}
		} catch (Exception e) {
			throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X, "Jdbc Connector"), e, this);
		}
	}

	public String[] getReadAndAckStatements(UMOEndpointURI endpointUri, UMOEndpoint endpoint) {
		String str;
		// Find read statement
		String readStmt = null;
		if ((str = endpointUri.getParams().getProperty("sql")) != null) {
			readStmt = str;
		} else {
			readStmt = endpointUri.getAddress();
		}
		// Find ack statement
		String ackStmt = null;
		if ((str = endpointUri.getParams().getProperty("ack")) != null) {
			ackStmt = str;
			if ((str = getQuery(endpoint, ackStmt)) != null) {
				ackStmt = str;
			}
		} else {
			ackStmt = "ack";
			if ((str = getQuery(endpoint, ackStmt)) != null) {
				ackStmt = str;
			} else {
				ackStmt = null;
			}
		}
		// Translate both using queries map
		if ((str = getQuery(endpoint, readStmt)) != null) {
			readStmt = str;
		}
		if (readStmt == null) {
			throw new IllegalArgumentException("Read statement should not be null");
		}
		if (!"select".equalsIgnoreCase(readStmt.substring(0, 6))) {
			throw new IllegalArgumentException("Read statement should be a select sql statement");
		}
		if (ackStmt != null) {
			if (!"insert".equalsIgnoreCase(ackStmt.substring(0, 6)) && !"update".equalsIgnoreCase(ackStmt.substring(0, 6)) && !"delete".equalsIgnoreCase(ackStmt.substring(0, 6))) {
				throw new IllegalArgumentException("Ack statement should be an insert / update / delete sql statement");
			}
		}
		return new String[] { readStmt, ackStmt };
	}

	public String getQuery(UMOEndpoint endpoint, String stmt) {
		Object query = null;
		if (endpoint != null && endpoint.getProperties() != null) {
			Object queries = getQueries();
			if (queries instanceof Map) {
				query = ((Map) queries).get(stmt);
			}
		}
		if (query == null) {
			if (this.queries != null) {
				query = this.queries.get(stmt);
			}
		}
		return query == null ? null : query.toString();
	}

	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	public String getdriver() {
		return driver;
	}

	public String getURL() {
		return URL;
	}

	public boolean isUseScript() {
		return this.useScript;
	}

	public void setUseScript(boolean useScript) {
		this.useScript = useScript;
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
	 * @param dataSource
	 *            The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDriver(String _driver) {
		driver = _driver;
	}

	public void setURL(String url) {
		URL = url;
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
	 * @return Returns the queries.
	 */
	public Map getQueries() {
		return queries;
	}

	/**
	 * @param queries
	 *            The queries to set.
	 */
	public void setQueries(Map queries) {
		this.queries = queries;
	}

	public String getScriptId() {
		return this.scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	/**
	 * @return Returns the dataSourceJndiName.
	 */
	public String getDataSourceJndiName() {
		return dataSourceJndiName;
	}

	/**
	 * @param dataSourceJndiName
	 *            The dataSourceJndiName to set.
	 */
	public void setDataSourceJndiName(String dataSourceJndiName) {
		this.dataSourceJndiName = dataSourceJndiName;
	}

	/**
	 * @return Returns the jndiContext.
	 */
	public Context getJndiContext() {
		return jndiContext;
	}

	/**
	 * @param jndiContext
	 *            The jndiContext to set.
	 */
	public void setJndiContext(Context jndiContext) {
		this.jndiContext = jndiContext;
	}

	/**
	 * @return Returns the jndiInitialFactory.
	 */
	public String getJndiInitialFactory() {
		return jndiInitialFactory;
	}

	/**
	 * @param jndiInitialFactory
	 *            The jndiInitialFactory to set.
	 */
	public void setJndiInitialFactory(String jndiInitialFactory) {
		this.jndiInitialFactory = jndiInitialFactory;
	}

	/**
	 * @return Returns the jndiProviderUrl.
	 */
	public String getJndiProviderUrl() {
		return jndiProviderUrl;
	}

	/**
	 * @param jndiProviderUrl
	 *            The jndiProviderUrl to set.
	 */
	public void setJndiProviderUrl(String jndiProviderUrl) {
		this.jndiProviderUrl = jndiProviderUrl;
	}

	/**
	 * @return Returns the providerProperties.
	 */
	public Map getProviderProperties() {
		return providerProperties;
	}

	/**
	 * @param providerProperties
	 *            The providerProperties to set.
	 */
	public void setProviderProperties(Map providerProperties) {
		this.providerProperties = providerProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.TransactionEnabledConnector#getSessionFactory(org.mule.umo.endpoint.UMOEndpoint)
	 */
	public Object getSessionFactory(UMOEndpoint endpoint) throws Exception {
		return dataSource;
	}

	public Connection getConnection(MessageObject messageObject) throws Exception {
		UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
		if (tx != null) {
			if (tx.hasResource(URL)) {
				logger.debug("Retrieving connection from current transaction");
				return (Connection) tx.getResource(URL);
			}
		}
		Connection con;
		if (URL != null && driver != null) {
			if (messageObject == null){
				username = replacer.replaceValuesFromGlobal(username, true);
				password = replacer.replaceValuesFromGlobal(password, true);
				URL = replacer.replaceValuesFromGlobal(URL, true);
			}else{
				if (username.indexOf('$') > -1)
					username = replacer.replaceValues(username, messageObject);
				if (password != null && password.indexOf('$') > -1)
					password = replacer.replaceValues(password, messageObject);
				URL = replacer.replaceValues(URL, messageObject);
			}
			username = replacer.replaceValuesFromGlobal(username, true);
			password = replacer.replaceValuesFromGlobal(password, true);
			
			logger.debug("Retrieving new connection from data source: " + URL + " (" + driver + ")");
			Class.forName(driver);

			// Step 2: Establish the connection to the database.
			con = DriverManager.getConnection(URL, username, password);

		} else {
			logger.debug("Retrieving new connection from data source");
			con = dataSource.getConnection();
		}

		if (tx != null) {
			logger.debug("Binding connection to current transaction");
			try {
				tx.bindResource(URL, con);
			} catch (TransactionException e) {
				throw new RuntimeException("Could not bind connection to current transaction", e);
			}
		}
		return con;
	}

	public boolean isUseAck() {
		return useAck;
	}

	public void setUseAck(boolean useAck) {
		this.useAck = useAck;
	}

	public String getAckScriptId() {
		return ackScriptId;
	}

	public void setAckScriptId(String ackScriptId) {
		this.ackScriptId = ackScriptId;
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

}
