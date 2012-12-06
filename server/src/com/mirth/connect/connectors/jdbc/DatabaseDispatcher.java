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
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class DatabaseDispatcher extends DestinationConnector {
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private Logger scriptLogger = Logger.getLogger("db-connector");

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private DataSource dataSource = null;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private String scriptId = null;
    private Logger logger = Logger.getLogger(getClass());
    private DatabaseDispatcherProperties connectorProperties;
    private JavaScriptExecutor<Object> jsExecutor = new JavaScriptExecutor<Object>();

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (DatabaseDispatcherProperties) getConnectorProperties();

        if (connectorProperties.isUseScript()) {
            String scriptId = UUID.randomUUID().toString();

            try {
                JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getQuery(), null, null);
            } catch (Exception e) {
                throw new DeployException("Error compiling " + connectorProperties.getName() + " query script " + scriptId + ".", e);
            }

            this.scriptId = scriptId;

        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() throws UndeployException {
        if (scriptId != null) {
            JavaScriptUtil.removeScriptFromCache(scriptId);
        }
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStop() throws StopException {
        try {
            shutdownDataSource();
        } catch (SQLException e) {
            throw new StopException("Could not close data source", e);
        }
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        DatabaseDispatcherProperties databaseDispatcherProperties = (DatabaseDispatcherProperties) SerializationUtils.clone(connectorProperties);

        databaseDispatcherProperties.setUrl(replacer.replaceValues(databaseDispatcherProperties.getUrl(), connectorMessage));
        databaseDispatcherProperties.setUsername(replacer.replaceValues(databaseDispatcherProperties.getUsername(), connectorMessage));
        databaseDispatcherProperties.setPassword(replacer.replaceValues(databaseDispatcherProperties.getPassword(), connectorMessage));

        // TODO: Replace variables if useScript?
        if (!databaseDispatcherProperties.isUseScript()) {
            databaseDispatcherProperties.setQuery(replacer.replaceValues(databaseDispatcherProperties.getQuery(), connectorMessage));
        }

        return databaseDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) throws InterruptedException {
        DatabaseDispatcherProperties databaseDispatcherProperties = (DatabaseDispatcherProperties) connectorProperties;

        String info = "URL: " + databaseDispatcherProperties.getUrl();
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, info);

        String responseData = null;
        String responseError = null;
        Status responseStatus = Status.QUEUED;

        initializeDataSource(databaseDispatcherProperties);

        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            // execute the database script if selected
            if (databaseDispatcherProperties.isUseScript()) {
                try {
                    jsExecutor.execute(new DatabaseDispatcherTask());
                } catch (JavaScriptExecutorException e) {
                    logger.error("Error executing " + connectorProperties.getName() + " script " + scriptId + ".", e.getCause());
                }

                responseStatus = Status.SENT;
                responseData = "Database write success";

                /*
                 * The user could write Javascript that sets the response for
                 * this connector if that's the case, then let's save it.
                 */
                String connectorName = getConnectorName();
                if (connectorName != null && connectorMessage.getResponseMap().containsKey(getConnectorName())) {
                    return connectorMessage.getResponseMap().get(getConnectorName());
                }
            } else {
                // otherwise run the SQL insert/update/delete statement
                String writeStmt = databaseDispatcherProperties.getQuery();

                writeStmt = JdbcUtils.stripSqlComments(writeStmt);

                if (writeStmt == null) {
                    throw new IllegalArgumentException("Write statement should not be NULL");
                } else if (!writeStmt.toLowerCase().startsWith("insert") && !writeStmt.toLowerCase().startsWith("update") && !writeStmt.toLowerCase().startsWith("delete")) {
                    throw new IllegalArgumentException("Write statement should be an INSERT, UPDATE, or DELETE SQL statement.");
                }

                List<String> paramNames = new ArrayList<String>();
                writeStmt = JdbcUtils.parseStatement(writeStmt, paramNames);
                Object[] paramValues = JdbcUtils.getParams(paramNames, connectorMessage);

                int numRows = -1;
                try {
                    numRows = new QueryRunner().update(connection, writeStmt, paramValues);
                } catch (SQLException e) {
                    // If the connection was closed, get a new connection and
                    // try again
                    if (connection.isClosed()) {
                        connection = dataSource.getConnection();
                        numRows = new QueryRunner().update(connection, writeStmt, paramValues);
                    } else {
                        throw e;
                    }
                }

                JdbcUtils.commitAndClose(connection);
                responseStatus = Status.SENT;
                responseData = "Database write success, " + numRows + " rows updated";
                logger.debug("Event dispatched succesfuly");
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Error dispatching event", e);
            try {
                JdbcUtils.rollbackAndClose(connection);
            } catch (SQLException e1) {
                // TODO: Close quietly?
            }
            alertController.sendAlerts(getChannelId(), Constants.ERROR_406, "Error writing to database", e);

            // TODO: Error data
//            messageObjectController.setError(messageObject, Constants.ERROR_406, "Error writing to database: ", e, null);
//            connector.handleException(e);
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }

        return new Response(responseStatus, responseData, responseError);
    }
    
    private class DatabaseDispatcherTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, getChannelId());
            return JavaScriptUtil.executeScript(scriptId, scope, getChannelId(), "Source");
        }
    }

    private void initializeDataSource(DatabaseDispatcherProperties databaseDispatcherProperties) {
        // If a data source already exists for the current properties, do nothing
        if (dataSource != null) {
            BasicDataSource bds = (BasicDataSource) dataSource;
            if (databaseDispatcherProperties.getDriver().equals(bds.getDriverClassName()) && databaseDispatcherProperties.getUsername().equals(bds.getUsername()) && databaseDispatcherProperties.getPassword().equals(bds.getPassword()) && databaseDispatcherProperties.getUrl().equals(bds.getUrl())) {
                // Do Nothing
                return;
            }
        }

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(databaseDispatcherProperties.getDriver());
        basicDataSource.setUsername(databaseDispatcherProperties.getUsername());
        basicDataSource.setPassword(databaseDispatcherProperties.getPassword());
        basicDataSource.setUrl(databaseDispatcherProperties.getUrl());

        dataSource = basicDataSource;
    }

    private void shutdownDataSource() throws SQLException {
        if (dataSource != null) {
            BasicDataSource bds = (BasicDataSource) dataSource;
            bds.close();
        }
    }

    private String getConnectorName() {
        List<Connector> destinations = channelController.getCachedChannelById(getChannelId()).getDestinationConnectors();
        for (Connector destination : destinations) {
            if (destination.getMetaDataId() == getMetaDataId()) {
                return destination.getName();
            }
        }

        return null;
    }
}
