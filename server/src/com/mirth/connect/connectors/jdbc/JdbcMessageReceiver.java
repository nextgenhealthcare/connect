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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.TransactedPollingMessageReceiver;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;

public class JdbcMessageReceiver extends TransactedPollingMessageReceiver {
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private JdbcConnector connector;
    private String readStmt;
    private String ackStmt;
    private List readParams;
    private List ackParams;
    private Map jdbcMap;
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private JavaScriptPostprocessor postprocessor = new JavaScriptPostprocessor();
    private ConnectorType connectorType = ConnectorType.READER;
    private Connection connection = null;

    public JdbcMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint, new Long(((JdbcConnector) connector).getPollingFrequency()));

        if (((JdbcConnector) connector).getPollingType().equals(JdbcConnector.POLLING_TYPE_TIME)) {
            setTime(((JdbcConnector) connector).getPollingTime());
        } else {
            setFrequency(((JdbcConnector) connector).getPollingFrequency());
        }

        this.receiveMessagesInTransaction = ((JdbcConnector) connector).isProcessResultsInOrder();
        this.connector = (JdbcConnector) connector;
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public JdbcMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readStmt, String ackStmt) throws InitialisationException {
        super(connector, component, endpoint, new Long(((JdbcConnector) connector).getPollingFrequency()));

        if (((JdbcConnector) connector).getPollingType().equals(JdbcConnector.POLLING_TYPE_TIME)) {
            setTime(((JdbcConnector) connector).getPollingTime());
        } else {
            setFrequency(((JdbcConnector) connector).getPollingFrequency());
        }

        this.receiveMessagesInTransaction = ((JdbcConnector) connector).isProcessResultsInOrder();
        this.connector = (JdbcConnector) connector;

        // handle read and ack params
        this.readParams = new ArrayList();
        this.readStmt = JdbcUtils.parseStatement(readStmt, this.readParams);
        this.ackParams = new ArrayList();
        this.ackStmt = JdbcUtils.parseStatement(ackStmt, this.ackParams);

        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public void doConnect() throws Exception {
        if (!connector.isUseScript()) {
            try {
                connection = connector.getConnection(null);
            } catch (Exception e) {
                logger.error(e);
                throw new ConnectException(e, this);
            }
        }
    }

    public void doDisconnect() throws ConnectException {
        if (!connector.isUseScript()) {
            try {
                JdbcUtils.close(connection);
            } catch (SQLException e) {
                throw new ConnectException(e, this);
            }
        }
    }

    public void processMessage(Object message) throws Exception {
        try {
            monitoringController.updateStatus(connector, connectorType, Event.BUSY);

            if (connector.isUseScript() && connector.isUseAck()) {
                // dispatch messages
                UMOMessageAdapter msgAdapter = connector.getMessageAdapter(message);
                UMOMessage umoMessage = new MuleMessage(msgAdapter);
                // we should get an MO back (if we're synchronized...)
                umoMessage = routeMessage(umoMessage, endpoint.isSynchronous());

                Context context = Context.enter();
                Scriptable scope = new ImporterTopLevel(context);
                // load variables in JavaScript scope
                JavaScriptScopeUtil.buildScope(scope, connector.getName(), scriptLogger);
                scope.put("dbMap", scope, jdbcMap);
                scope.put("resultMap", scope, message);

                if (umoMessage != null) {
                    MessageObject messageObject = (MessageObject) umoMessage.getPayload();
                    postprocessor.doPostProcess(messageObject);
                    scope.put("responseMap", scope, messageObject.getResponseMap());
                }

                // get the script from the cache and execute it
                Script compiledScript = compiledScriptCache.getCompiledScript(connector.getAckScriptId());

                if (compiledScript == null) {
                    logger.error("Database query update could not be found in cache");
                    throw new Exception("Database query update script could not be found in cache");
                } else {
                    compiledScript.exec(context, scope);
                }
            } else {
                UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
                Exception ackException = null;

                try {
                    try {
                        if (connector.isUseAck() && ackStmt != null) {
                            int numRows = new QueryRunner().update(connection, ackStmt, JdbcUtils.getParams(getEndpointURI(), ackParams, message));

                            if (numRows != 1) {
                                logger.warn("Row count for ack should be 1 and not " + numRows);
                            }
                        }
                    } catch (Exception ue) {
                        logger.error("Error in the ACK sentence of the JDBC connection, but the message is being sent anyway" + ue);
                        ackException = ue;
                    }

                    UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(message);
                    UMOMessage umoMessage = new MuleMessage(msgAdapter);
                    UMOMessage retMessage = routeMessage(umoMessage, tx, tx != null || endpoint.isSynchronous());

                    if (retMessage != null) {
                        // prevents errors if synchronous is not checked
                        postprocessor.doPostProcess(retMessage.getPayload());
                    }

                    if (ackException != null) {
                        throw ackException;
                    }
                } catch (ConnectException ce) {
                    throw new Exception(ce.getCause());
                }
            }
        } catch (Exception e) {
            logger.error("Error in channel: " + ChannelController.getInstance().getDeployedChannelById(connector.getChannelId()).getName(), ExceptionUtils.getRootCause(e));
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_406, null, e);
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    public List getMessages() throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);

        try {
            if (connector.isUseScript()) {
                Context context = Context.enter();
                Scriptable scope = new ImporterTopLevel(context);

                // load variables in JavaScript scope
                JavaScriptScopeUtil.buildScope(scope, connector.getChannelId(), scriptLogger);
                // each time we poll, we want to clear the map.
                // we need to document this
                jdbcMap = new HashMap();
                scope.put("dbMap", scope, jdbcMap);
                // get the script from the cache and execute it
                Script compiledScript = compiledScriptCache.getCompiledScript(connector.getScriptId());

                if (compiledScript == null) {
                    logger.error("Database script could not be found in cache");
                    throw new Exception("Database script could not be found in cache");
                } else {
                    Object result = null;

                    try {
                        result = compiledScript.exec(context, scope);
                    } catch (Exception e) {
                        logger.error(e);
                        alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_406, null, e);
                        return null;
                    }

                    if (result instanceof NativeJavaObject) {
                        Object javaRetVal = ((NativeJavaObject) result).unwrap();

                        if (javaRetVal instanceof CachedRowSet) {
                            MapListHandler handler = new MapListHandler();
                            Object rows = handler.handle((CachedRowSet) javaRetVal);
                            return (List) rows;
                        } else if (javaRetVal instanceof RowSet) {
                            MapListHandler handler = new MapListHandler();
                            Object rows = handler.handle((RowSet) javaRetVal);
                            return (List) rows;
                        } else if (javaRetVal instanceof List) {
                            return (List) javaRetVal;
                        } else {
                            logger.error("Got a result of: " + javaRetVal.toString());
                        }
                    } else {
                        logger.error("Got a result of: " + result.toString());
                    }

                    return null;
                }
            } else {
                if (connection.isClosed()) {
                    try {
                        connection = connector.getConnection(null);
                    } catch (Exception e) {
                        logger.error("Error trying to establish a connection to the datatabase receiver in channel: " + connector.getChannelId(), e);
                        return new ArrayList();
                    }
                }

                try {
                    return new QueryRunner().query(connection, readStmt, new MapListHandler(), JdbcUtils.getParams(getEndpointURI(), readParams, null));
                } catch (SQLException e) {
                    /*
                     * Check if the connection is still valid. Apache pools
                     * throws an unexpected error when calling isValid for some
                     * drivers (i.e. informix), so assume the connection is not
                     * valid if an exception occurs
                     */
                    boolean validConnection = true;
                    try {
                        validConnection = connection.isValid(10000);
                    } catch (Throwable t) {
                        validConnection = false;
                    }

                    /*
                     * If the connection is not valid, then get a new connection
                     * and retry the query now.
                     */
                    if (!validConnection) {
                        try {
                            DbUtils.closeQuietly(connection);
                            connection = connector.getConnection(null);
                            return new QueryRunner().query(connection, readStmt, new MapListHandler(), JdbcUtils.getParams(getEndpointURI(), readParams, null));
                        } catch (SQLException e2) {
                            e = e2;
                        }
                    }

                    throw e;
                }
            }
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_406, null, e);
            throw e;
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

}
