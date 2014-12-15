/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.sun.rowset.CachedRowSetImpl;

public class DatabaseReceiverQuery implements DatabaseReceiverDelegate {
    private PreparedStatement selectStatement;
    private PreparedStatement updateStatement;
    private List<String> selectParams = new ArrayList<String>();
    private List<String> updateParams = new ArrayList<String>();
    private Connection selectConnection;
    private Connection updateConnection;
    private DatabaseReceiver connector;
    private DatabaseReceiverProperties connectorProperties;
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private CustomDriver customDriver;

    public DatabaseReceiverQuery(DatabaseReceiver connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws ConnectorTaskException {
        connectorProperties = (DatabaseReceiverProperties) connector.getConnectorProperties();

        if (connectorProperties.getSelect() == null) {
            throw new ConnectorTaskException("A query has not been defined");
        }

        try {
            Class.forName(connectorProperties.getDriver());
        } catch (ClassNotFoundException e) {
            try {
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
                if (CollectionUtils.isNotEmpty(contextFactory.getResourceIds())) {
                    customDriver = new CustomDriver(contextFactory.getApplicationClassLoader(), connectorProperties.getDriver());
                } else {
                    throw new ConnectorTaskException(e);
                }
            } catch (Exception e2) {
                throw new ConnectorTaskException(e2);
            }
        }
    }

    @Override
    public void start() throws ConnectorTaskException {
        // if the keepConnectionOpen option is enabled, we open the database connection(s) here and they remain open until undeploy()
        if (connectorProperties.isKeepConnectionOpen()) {
            initConnection();
        }
    }

    private void initConnection() throws ConnectorTaskException {
        int attempts = 0;
        int maxRetryCount = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId()), 0);
        int retryInterval = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryInterval(), connector.getChannelId()), 0);
        boolean done = false;

        while (!done) {
            try {
                initSelectConnection();

                if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
                    initUpdateConnection();
                }

                done = true;
            } catch (SQLException e) {
                // close all of the connections/statements in case some of them did initialize successfully
                closeSelectConnection();
                closeUpdateConnection();

                if (attempts++ < maxRetryCount) {
                    logger.error("An error occurred while initializing the connection, retrying after " + retryInterval + " ms...", e);

                    // Wait the specified amount of time before retrying
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        throw new ConnectorTaskException("Thread interrupted while trying to initialize database connection", e);
                    }
                } else {
                    throw new ConnectorTaskException("Failed to initialize database connection", e);
                }
            }
        }
    }

    @Override
    public void stop() throws ConnectorTaskException {
        if (connectorProperties.isKeepConnectionOpen()) {
            closeSelectConnection();

            if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
                closeUpdateConnection();
            }
        }
    }

    @Override
    public void undeploy() {
        if (customDriver != null) {
            try {
                customDriver.dispose();
            } catch (SQLException e) {
            }
        }
    }

    @Override
    public Object poll() throws DatabaseReceiverException, InterruptedException {
        ResultSet resultSet = null;
        int attempts = 0;
        int maxRetryCount = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId()), 0);
        int retryInterval = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryInterval(), connector.getChannelId()), 0);
        boolean done = false;

        while (!done && !connector.isTerminated()) {
            CachedRowSet cachedRowSet = null;

            try {
                /*
                 * If the keepConnectionOpen option is not enabled, we open the database
                 * connection(s) here. They will be closed in afterPoll().
                 */
                if (!connectorProperties.isKeepConnectionOpen()) {
                    initSelectConnection();

                    if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_EACH) {
                        initUpdateConnection();
                    }
                }

                int objectIndex = 1;

                /*
                 * Using the list of placeholder keys found in the select statement (selectParams),
                 * get the corresponding values from JdbcUtils.getParameters() which uses a
                 * TemplateValueReplacer to to look up values from a default context based on the
                 * given channel id
                 */
                for (Object param : JdbcUtils.getParameters(selectParams, connector.getChannelId(), null, null, null)) {
                    selectStatement.setObject(objectIndex++, param);
                }

                resultSet = selectStatement.executeQuery();

                // if we are not caching the ResultSet, return it immediately
                if (connectorProperties.isCacheResults()) {
                    // if we are caching the ResultSet, convert it into a CachedRowSet and return it
                    cachedRowSet = new CachedRowSetImpl();
                    cachedRowSet.populate(resultSet);
                    DbUtils.closeQuietly(resultSet);
                    resultSet = cachedRowSet;
                }

                done = true;
            } catch (SQLException e) {
                DbUtils.closeQuietly(resultSet);
                DbUtils.closeQuietly(cachedRowSet);

                if (attempts++ < maxRetryCount && !connector.isTerminated()) {
                    logger.error("An error occurred while polling for messages, retrying after " + retryInterval + " ms...", e);

                    // Wait the specified amount of time before retrying
                    Thread.sleep(retryInterval);

                    if (connectorProperties.isKeepConnectionOpen() && !JdbcUtils.isValidConnection(selectConnection)) {
                        try {
                            initSelectConnection();
                        } catch (SQLException e1) {
                        }
                    }
                } else {
                    throw new DatabaseReceiverException(e);
                }
            }
        }

        return resultSet;
    }

    @Override
    public void runPostProcess(Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) throws DatabaseReceiverException, InterruptedException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_EACH) {
            try {
                runUpdateStatement(NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId())), resultMap, mergedConnectorMessage);
            } catch (SQLException e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    @Override
    public void afterPoll() throws DatabaseReceiverException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_ONCE) {
            try {
                initUpdateConnection();
                runUpdateStatement(NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId())), null, null);
            } catch (SQLException e) {
                throw new DatabaseReceiverException(e);
            }
        }

        if (!connectorProperties.isKeepConnectionOpen()) {
            closeSelectConnection();
            closeUpdateConnection();
        }
    }

    private void runUpdateStatement(int retryCount, Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) throws SQLException {
        try {
            int i = 1;

            /*
             * Using the list of placeholder keys found in the update statement (updateParams), get
             * the corresponding values from JdbcUtils.getParameters() which uses a
             * TemplateValueReplacer to look up values from the given resultMap or merged
             * ConnectorMessage
             */
            for (Object param : JdbcUtils.getParameters(updateParams, connector.getChannelId(), mergedConnectorMessage, resultMap, null)) {
                updateStatement.setObject(i++, param);
            }

            /*
             * We do not use Statement.executeUpdate() here because it could prevent users from
             * executing a stored procedure. Executing a stored procedure in Postgres (and possibly
             * other databases) is done via SELECT myprocedure(), which breaks executeUpdate() since
             * it returns a result, even if the procedure itself returns void.
             */
            updateStatement.execute();
        } catch (SQLException e) {
            if (retryCount < 1) {
                throw e;
            }

            if (!JdbcUtils.isValidConnection(updateConnection)) {
                initUpdateConnection();
            }

            logger.error("An error occurred while executing the post-process SQL, retrying", e);
            runUpdateStatement(retryCount - 1, resultMap, mergedConnectorMessage);
        }
    }

    private void initSelectConnection() throws SQLException {
        closeSelectConnection();

        String url = replacer.replaceValues(connectorProperties.getUrl(), connector.getChannelId());
        String username = replacer.replaceValues(connectorProperties.getUsername(), connector.getChannelId());
        String password = replacer.replaceValues(connectorProperties.getPassword(), connector.getChannelId());

        selectConnection = DriverManager.getConnection(url, username, password);
        selectConnection.setAutoCommit(true);

        /*
         * Before preparing the select statement, we extract the Apache velocity variables from the
         * statement into selectParams and replace them with ? placeholders. Prior to executing the
         * select statement, we use selectParams along with a TemplateValueReplacer to determine
         * what values to set on the prepared statement (see JdbcUtils.getParameters()).
         */
        selectParams.clear();
        selectStatement = selectConnection.prepareStatement(JdbcUtils.extractParameters(connectorProperties.getSelect(), selectParams));

        if (!connectorProperties.isCacheResults()) {
            selectStatement.setFetchSize(NumberUtils.toInt(replacer.replaceValues(connectorProperties.getFetchSize(), connector.getChannelId())));
        }
    }

    private void closeSelectConnection() {
        DbUtils.closeQuietly(selectStatement);

        try {
            if (selectConnection != null && !selectConnection.isClosed()) {
                selectConnection.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }

    private void initUpdateConnection() throws SQLException {
        closeUpdateConnection();

        String url = replacer.replaceValues(connectorProperties.getUrl(), connector.getChannelId());
        String username = replacer.replaceValues(connectorProperties.getUsername(), connector.getChannelId());
        String password = replacer.replaceValues(connectorProperties.getPassword(), connector.getChannelId());

        updateConnection = DriverManager.getConnection(url, username, password);
        updateConnection.setAutoCommit(true);

        /*
         * Before preparing the update statement, we extract the Apache velocity variables from the
         * statement into updateParams and replace them with ? placeholders. Prior to executing the
         * update statement, we use updateParams along with a TemplateValueReplacer to determine
         * what values to set on the prepared statement (see JdbcUtils.getParameters()).
         */
        updateParams.clear();
        updateStatement = updateConnection.prepareStatement(JdbcUtils.extractParameters(connectorProperties.getUpdate(), updateParams));
    }

    private void closeUpdateConnection() {
        DbUtils.closeQuietly(updateStatement);

        try {
            if (updateConnection != null && !updateConnection.isClosed()) {
                updateConnection.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close database connection", e);
        }
    }
}
