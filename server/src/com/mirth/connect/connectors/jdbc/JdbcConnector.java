/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
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

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;

public class JdbcConnector extends AbstractServiceEnabledConnector {
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
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
    private boolean processResultsInOrder = true;
    private boolean useAck;
    private Map queries;
    private boolean useScript;
    private String scriptId;
    private String ackScriptId;
    private String channelId;

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getProtocol() {
        return "jdbc";
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getdriver() {
        return this.driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        this.URL = url;
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

    public long getPollingFrequency() {
        return pollingFrequency;
    }

    public void setPollingFrequency(long pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    public Map getQueries() {
        return queries;
    }

    public void setQueries(Map queries) {
        this.queries = queries;
    }

    public String getScriptId() {
        return this.scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public boolean isProcessResultsInOrder() {
        return processResultsInOrder;
    }

    public void setProcessResultsInOrder(boolean processResultsInOrder) {
        this.processResultsInOrder = processResultsInOrder;
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

    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        String[] params = {};

        if (!useScript) {
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

    public String[] getReadAndAckStatements(UMOEndpointURI endpointUri, UMOEndpoint endpoint) {
        String str = null;

        // find read statement
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
            throw new IllegalArgumentException("Read statement should not be NULL");
        }

        if (!readStmt.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Read statement should be a SELECT sql statement");
        }

        if (ackStmt != null) {
            if (!ackStmt.toLowerCase().startsWith("insert") && !ackStmt.toLowerCase().startsWith("update") && !ackStmt.toLowerCase().startsWith("delete")) {
                throw new IllegalArgumentException("Ack statement should be an INSERT, UPDATE, or DELETE SQL statement");
            }
        }

        return new String[] { readStmt, ackStmt };
    }

    public String getQuery(UMOEndpoint endpoint, String stmt) {
        Object query = null;

        if ((endpoint != null) && (endpoint.getProperties() != null)) {
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

    /*
     * This method gets called when the JDBC connector is initialized. It
     * compiles the JavaScript and adds it to the cache
     */
    protected synchronized void initFromServiceDescriptor() throws InitialisationException {
        super.initFromServiceDescriptor();

        setCreateMultipleTransactedReceivers(false);

        Context context = Context.enter();

        try {
            if (scriptId != null) {
                String databaseScript = scriptController.getScript(channelId, scriptId);

                if (databaseScript != null) {
                    String generatedDatabaseScript = generateDatabaseScript(databaseScript, false);
                    logger.debug("compiling database script");
                    Script compiledDatabaseScript = context.compileString(generatedDatabaseScript, scriptId, 1, null);
                    compiledScriptCache.putCompiledScript(scriptId, compiledDatabaseScript, generatedDatabaseScript);
                }
            }

            if (ackScriptId != null) {
                String ackScript = scriptController.getScript(channelId, ackScriptId);

                if (ackScript != null) {
                    String generatedDatabaseScript = generateDatabaseScript(ackScript, true);
                    logger.debug("compiling database ack script");
                    Script compiledDatabaseScript = context.compileString(generatedDatabaseScript, ackScriptId, 1, null);
                    compiledScriptCache.putCompiledScript(ackScriptId, compiledDatabaseScript, generatedDatabaseScript);
                }
            }
        } catch (Exception e) {
            throw new InitialisationException(e, this);
        } finally {
            Context.exit();
        }
    }

    // Generates the JavaScript based on the script which the user enters
    private String generateDatabaseScript(String databaseScript, boolean ack) {
        logger.debug("generating database script");
        StringBuilder script = new StringBuilder();
        script.append("importPackage(Packages.com.mirth.connect.server.util);\n");
        
        // start function
        script.append("function $(string) { ");

        if (ack) {
            script.append("if (resultMap.containsKey(string)) { return resultMap.get(string) }\n else ");
        }

        script.append("if (globalChannelMap.containsKey(string)) { return globalChannelMap.get(string); }\n");
        script.append("else if (globalMap.containsKey(string)) { return globalMap.get(string); }\n");
        script.append("else { return ''; } }");
        // end function
        
        script.append("function $g(key, value) {");
        script.append("if (arguments.length == 1) { return globalMap.get(key); }");
        script.append("else if (arguments.length == 2) { globalMap.put(key, value); } }");
        
        script.append("function $gc(key, value) {");
        script.append("if (arguments.length == 1) { return globalChannelMap.get(key); }");
        script.append("else if (arguments.length == 2) { globalChannelMap.put(key, value); } }");
        
        try {
            for (CodeTemplate template : ControllerFactory.getFactory().createCodeTemplateController().getCodeTemplate(null)) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    script.append(template.getCode());
                }
            }
        } catch (ControllerException e) {
            logger.error("Could load code templates.", e);
        }

        script.append("function doDatabaseScript() {");
        script.append(databaseScript + "\n");
        script.append("}\n");
        script.append("doDatabaseScript();\n");
        return script.toString();
    }

    public Connection getConnection(MessageObject messageObject) throws Exception {
        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();

        if (tx != null) {
            if (tx.hasResource(URL)) {
                logger.debug("Retrieving connection from current transaction");
                return (Connection) tx.getResource(URL);
            }
        }

        Connection connection = null;

        /*
         * The database source is NULL because the username, password, or URL
         * have replaceable values
         */
        if (dataSource == null) {
            if (messageObject != null) {
                username = replacer.replaceValues(username, messageObject);
                password = replacer.replaceValues(password, messageObject);
                URL = replacer.replaceValues(URL, messageObject);
            } else {
                username = replacer.replaceValues(username, channelId);
                password = replacer.replaceValues(password, channelId);
                URL = replacer.replaceValues(URL, channelId);
            }

            setupDataSource(URL, driver, username, password);
        }

        logger.debug("Retrieving new connection from datasource: " + URL + " using driver: " + driver);
        connection = dataSource.getConnection();

        if (tx != null) {
            logger.debug("Binding connection to current transaction");
            try {
                tx.bindResource(URL, connection);
            } catch (TransactionException e) {
                throw new RuntimeException("Could not bind connection to current transaction", e);
            }
        }

        return connection;
    }

    private void setupDataSource(String address, String driver, String username, String password) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driver);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setUrl(address);
        dataSource = basicDataSource;
    }

    private void shutdownDataSource() throws Exception {
        BasicDataSource bds = (BasicDataSource) dataSource;
        bds.close();
    }

    @Override
    public void doConnect() throws Exception {
        super.doConnect();

        // if we don't plan on replacing the values, setup the datasource
        if (!TemplateValueReplacer.hasReplaceableValues(URL) && !TemplateValueReplacer.hasReplaceableValues(username) && !TemplateValueReplacer.hasReplaceableValues(password)) {
            setupDataSource(URL, driver, username, password);
        }
    }

    @Override
    public void doDisconnect() throws Exception {
        super.doDisconnect();
        shutdownDataSource();
    }

}
