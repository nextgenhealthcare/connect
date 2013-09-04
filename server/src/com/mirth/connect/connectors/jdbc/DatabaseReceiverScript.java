/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.userutil.ImmutableConnectorMessage;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class DatabaseReceiverScript implements DatabaseReceiverDelegate {
    private DatabaseReceiver connector;
    private String selectScriptId;
    private String updateScriptId;
    private DatabaseReceiverProperties connectorProperties;
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private Logger logger = Logger.getLogger(getClass());

    public DatabaseReceiverScript(DatabaseReceiver connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws DeployException {
        connectorProperties = (DatabaseReceiverProperties) connector.getConnectorProperties();
        selectScriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(selectScriptId, connectorProperties.getSelect(), null, null);
        } catch (Exception e) {
            throw new DeployException("Error compiling select script " + selectScriptId + ".", e);
        }

        if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
            updateScriptId = UUID.randomUUID().toString();

            try {
                JavaScriptUtil.compileAndAddScript(updateScriptId, connectorProperties.getUpdate(), null, null);
            } catch (Exception e) {
                throw new DeployException("Error compiling update script " + updateScriptId + ".", e);
            }
        }
    }

    @Override
    public void start() throws StartException {}

    @Override
    public void stop() throws StopException {}

    @Override
    public void undeploy() {
        JavaScriptUtil.removeScriptFromCache(selectScriptId);

        if (updateScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(updateScriptId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object poll() throws DatabaseReceiverException, InterruptedException {
        Object finalResult = null;
        int attempts = 0;
        int maxRetryCount = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId()), 0);
        int retryInterval = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryInterval(), connector.getChannelId()), 0);
        boolean done = false;

        while (!done && !connector.isTerminated()) {
            try {
                Object result = JavaScriptUtil.execute(new SelectTask());

                if (result instanceof NativeJavaObject) {
                    Object unwrappedResult = ((NativeJavaObject) result).unwrap();

                    if (unwrappedResult instanceof ResultSet) {
                        finalResult = (ResultSet) unwrappedResult;
                    } else if (unwrappedResult instanceof List) {
                        finalResult = (List<Map<String, Object>>) unwrappedResult;
                    } else {
                        throw new DatabaseReceiverException("Unrecognized value returned from script in channel \"" + ChannelController.getInstance().getDeployedChannelById(connector.getChannelId()).getName() + "\", expected ResultSet or List<Map<String, Object>>: " + unwrappedResult.toString());
                    }

                    done = true;
                } else {
                    throw new DatabaseReceiverException("Unrecognized value returned from script in channel \"" + ChannelController.getInstance().getDeployedChannelById(connector.getChannelId()).getName() + "\", expected ResultSet or List<Map<String, Object>>: " + result.toString());
                }
            } catch (JavaScriptExecutorException e) {
                if (attempts++ < maxRetryCount && !connector.isTerminated()) {
                    logger.error("An error occurred while polling for messages, retrying after " + retryInterval + " ms...", e);

                    // Wait the specified amount of time before retrying
                    Thread.sleep(retryInterval);
                } else {
                    throw new DatabaseReceiverException("Error executing script " + selectScriptId + ".", e.getCause());
                }
            }
        }

        return finalResult;
    }

    @Override
    public void runPostProcess(Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) throws DatabaseReceiverException, InterruptedException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_EACH) {
            try {
                JavaScriptUtil.execute(new UpdateTask(resultMap, mergedConnectorMessage));
            } catch (JavaScriptExecutorException e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    @Override
    public void afterPoll() throws InterruptedException, DatabaseReceiverException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_ONCE) {
            try {
                JavaScriptUtil.execute(new UpdateTask(null, null));
            } catch (JavaScriptExecutorException e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    private class SelectTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            try {
                Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId());
                return JavaScriptUtil.executeScript(this, selectScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }

    private class UpdateTask extends JavaScriptTask<Object> {
        private Map<String, Object> resultMap;
        private ConnectorMessage mergedConnectorMessage;

        public UpdateTask(Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) {
            this.resultMap = resultMap;
            this.mergedConnectorMessage = mergedConnectorMessage;
        }

        @Override
        public Object call() throws Exception {
            try {
                Scriptable scope = null;

                if (mergedConnectorMessage == null) {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId());
                } else {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId(), new ImmutableConnectorMessage(mergedConnectorMessage, true, connector.getDestinationNameMap()));
                }

                if (resultMap != null) {
                    scope.put("resultMap", scope, resultMap);
                }

                return JavaScriptUtil.executeScript(this, updateScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }
}
