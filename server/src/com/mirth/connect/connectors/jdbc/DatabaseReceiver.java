/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class DatabaseReceiver extends PollConnector {
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private String readStmt;
    private String ackStmt;
    private List<String> readParams = new ArrayList<String>();
    private List<String> ackParams = new ArrayList<String>();
//    private Map jdbcMap;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.READER;
    private Connection connection = null;

    private DataSource dataSource = null;
    private String queryScriptId = null;
    private String ackScriptId = null;
    private Logger logger = Logger.getLogger(getClass());
    private DatabaseReceiverProperties connectorProperties;
    private JavaScriptExecutor<Object> jsExecutor = new JavaScriptExecutor<Object>();

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (DatabaseReceiverProperties) getConnectorProperties();

        if (connectorProperties.isUseScript()) {
            String queryScriptId = UUID.randomUUID().toString();

            try {
                JavaScriptUtil.compileAndAddScript(queryScriptId, connectorProperties.getQuery(), null, null);
            } catch (Exception e) {
                throw new DeployException("Error compiling " + connectorProperties.getName() + " query script " + queryScriptId + ".", e);
            }

            this.queryScriptId = queryScriptId;

            if (connectorProperties.isUseAck()) {
                String ackScriptId = UUID.randomUUID().toString();

                try {
                    JavaScriptUtil.compileAndAddScript(ackScriptId, connectorProperties.getAck(), null, null);
                } catch (Exception e) {
                    throw new DeployException("Error compiling " + connectorProperties.getName() + " update script " + ackScriptId + ".", e);
                }

                this.ackScriptId = ackScriptId;
            }
        } else {
            readStmt = connectorProperties.getQuery();

            if (readStmt == null) {
                throw new DeployException("Read statement should not be NULL");
            }

            if (!readStmt.toLowerCase().startsWith("select")) {
                throw new DeployException("Read statement should be a SELECT sql statement");
            }

            this.readStmt = JdbcUtils.parseStatement(readStmt, this.readParams);

            if (connectorProperties.isUseAck()) {
                ackStmt = connectorProperties.getAck();

                if (ackStmt != null) {
                    if (!ackStmt.toLowerCase().startsWith("insert") && !ackStmt.toLowerCase().startsWith("update") && !ackStmt.toLowerCase().startsWith("delete")) {
                        throw new DeployException("Ack statement should be an INSERT, UPDATE, or DELETE SQL statement");
                    }
                }

                this.ackStmt = JdbcUtils.parseStatement(ackStmt, this.ackParams);
            }

        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() throws UndeployException {
        if (queryScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(queryScriptId);
        }

        if (ackScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(queryScriptId);
        }
    }

    @Override
    public void onStart() throws StartException {
        initializeDataSource(connectorProperties);
        if (!connectorProperties.isUseScript()) {
            try {
                connection = dataSource.getConnection();
            } catch (Exception e) {
                logger.error(e);
                throw new StartException("Error creating connection to database", e);
            }
        }
    }

    @Override
    public void onStop() throws StopException {
        if (!connectorProperties.isUseScript()) {
            try {
                JdbcUtils.close(connection);
                shutdownDataSource();
            } catch (SQLException e) {
                throw new StopException("Error shutting down connection to database", e);
            }
        }
    }

    @Override
    protected void poll() throws InterruptedException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.CONNECTED);

        try {
            List<Map<String, Object>> result = null;

            if (connectorProperties.isUseScript()) {
                Object scriptResult = null;
                
                try {
                    scriptResult = jsExecutor.execute(new DatabaseReceiverTask(queryScriptId, null));
                } catch (JavaScriptExecutorException e) {
                    logger.error("Error executing " + connectorProperties.getName() + " script " + queryScriptId + ".", e.getCause());
                    alertController.sendAlerts(getChannelId(), Constants.ERROR_406, null, e.getCause());
                }

                if (scriptResult instanceof NativeJavaObject) {
                    Object javaRetVal = ((NativeJavaObject) scriptResult).unwrap();

                    if (javaRetVal instanceof CachedRowSet) {
                        MapListHandler handler = new MapListHandler();
                        Object rows = handler.handle((CachedRowSet) javaRetVal);
                        result = (List<Map<String, Object>>) rows;
                    } else if (javaRetVal instanceof RowSet) {
                        MapListHandler handler = new MapListHandler();
                        Object rows = handler.handle((RowSet) javaRetVal);
                        result = (List<Map<String, Object>>) rows;
                    } else if (javaRetVal instanceof List) {
                        result = (List<Map<String, Object>>) javaRetVal;
                    } else {
                        logger.error("Got a result of: " + javaRetVal.toString());
                    }
                } else {
                    logger.error("Got a result of: " + scriptResult.toString());
                }
            } else {
                if (connection.isClosed()) {
                    try {
                        connection = dataSource.getConnection();
                    } catch (Exception e) {
                        logger.error("Error trying to establish a connection to the datatabase receiver in channel: " + getChannelId(), e);
                        return;
                    }
                }

                try {
                    result = new QueryRunner().query(connection, readStmt, new MapListHandler(), JdbcUtils.getParams(readParams, null));
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
                            connection = dataSource.getConnection();
                            result = new QueryRunner().query(connection, readStmt, new MapListHandler(), JdbcUtils.getParams(readParams, null));
                        } catch (SQLException e2) {
                            e = e2;
                        }
                    }

                    if (result == null) {
                        throw e;
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(result)) {
                for (Map<String, Object> message : result) {
                    try {
                        if (isTerminated()) {
                            return;
                        }
                        
                        processMessage(message);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            alertController.sendAlerts(getChannelId(), Constants.ERROR_406, null, e);
//            throw e;
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }
    }

    private void processMessage(Map<String, Object> row) throws InterruptedException {
        try {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY);
            
            String messageString = ResultMapToXML.doTransform(row);
            
            // TODO: When should this be run?  Before or after the ack script?
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = dispatchRawMessage(new RawMessage(messageString));
            } finally {
                finishDispatch(dispatchResult);
            }

            if (connectorProperties.isUseScript() && connectorProperties.isUseAck()) {
                try {
                    jsExecutor.execute(new DatabaseReceiverTask(ackScriptId, row));
                } catch (JavaScriptExecutorException e) {
                    logger.error("Error executing " + connectorProperties.getName() + " script " + ackScriptId + ".", e);
                }
            } else {
                Exception ackException = null;

                try {
                    if (connectorProperties.isUseAck() && ackStmt != null) {
                        int numRows = new QueryRunner().update(connection, ackStmt, JdbcUtils.getParams(ackParams, row));

                        if (numRows != 1) {
                            logger.warn("Row count for ack should be 1 and not " + numRows);
                        }
                    }
                } catch (Exception ue) {
                    logger.error("Error in the ACK sentence of the JDBC connection, but the message is being sent anyway, " + ue);
                    ackException = ue;
                }

                if (ackException != null) {
                    throw ackException;
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error in channel: " + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName(), ExceptionUtils.getRootCause(e));
            alertController.sendAlerts(getChannelId(), Constants.ERROR_406, null, e);
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }
    }

    private void initializeDataSource(DatabaseReceiverProperties databaseReceiverProperties) {
        // If a data source already exists for the current properties, do nothing
        if (dataSource != null) {
            BasicDataSource bds = (BasicDataSource) dataSource;
            if (databaseReceiverProperties.getDriver().equals(bds.getDriverClassName()) && databaseReceiverProperties.getUsername().equals(bds.getUsername()) && databaseReceiverProperties.getPassword().equals(bds.getPassword()) && databaseReceiverProperties.getUrl().equals(bds.getUrl())) {
                // Do Nothing
                return;
            }
        }

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(databaseReceiverProperties.getDriver());
        basicDataSource.setUsername(databaseReceiverProperties.getUsername());
        basicDataSource.setPassword(databaseReceiverProperties.getPassword());
        basicDataSource.setUrl(databaseReceiverProperties.getUrl());

        dataSource = basicDataSource;
    }

    private void shutdownDataSource() throws SQLException {
        BasicDataSource bds = (BasicDataSource) dataSource;
        bds.close();
    }

    private class DatabaseReceiverTask extends JavaScriptTask<Object> {
        private String scriptId;
        private Map<String, Object> row;
        
        public DatabaseReceiverTask(String scriptId, Map<String, Object> row) {
            this.scriptId = scriptId;
            this.row = row;
        }
        
        @Override
        public Object call() throws Exception {
            Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, getChannelId());
            
            if (row != null) {
                scope.put("resultMap", scope, row);
            }
            
            // TODO: Needed?
//            scope.put("dbMap", scope, jdbcMap);
//          scope.put("responseMap", scope, messageObject.getResponseMap());

            return JavaScriptUtil.executeScript(scriptId, scope, getChannelId(), "Source");
        }
    }

	@Override
	public void handleRecoveredResponse(DispatchResult dispatchResult) {
		//TODO add cleanup code?
		finishDispatch(dispatchResult);
	}
}
