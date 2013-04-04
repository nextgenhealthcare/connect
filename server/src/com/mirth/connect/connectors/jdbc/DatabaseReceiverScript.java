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
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class DatabaseReceiverScript implements DatabaseReceiverDelegate {
    private DatabaseReceiver connector;
    private String selectScriptId;
    private String updateScriptId;
    private JavaScriptExecutor<Object> javaScriptExecutor = new JavaScriptExecutor<Object>();
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
    public void undeploy() {
        JavaScriptUtil.removeScriptFromCache(selectScriptId);

        if (updateScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(updateScriptId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object poll() throws DatabaseReceiverException, InterruptedException {
        return poll(NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), connector.getChannelId())));
    }

    /*
     * If an error occurs, this method will be called recursively until it succeeds or the
     * retryCount is reached
     */
    private Object poll(int retryCount) throws DatabaseReceiverException, InterruptedException {
        Object result = null;

        try {
            result = javaScriptExecutor.execute(new SelectTask());
        } catch (JavaScriptExecutorException e) {
            if (retryCount > 0) {
                logger.error("An error occurred while polling for messages, retrying", e);
                return poll(retryCount - 1);
            }

            throw new DatabaseReceiverException("Error executing script " + selectScriptId + ".", e.getCause());
        }

        if (result instanceof NativeJavaObject) {
            Object unwrappedResult = ((NativeJavaObject) result).unwrap();

            if (unwrappedResult instanceof ResultSet) {
                return (ResultSet) unwrappedResult;
            }

            if (unwrappedResult instanceof List) {
                return (List<Map<String, Object>>) unwrappedResult;
            }

            throw new DatabaseReceiverException("Unrecognized value returned from script in channel \"" + ChannelController.getInstance().getDeployedChannelById(connector.getChannelId()).getName() + "\", expected ResultSet or List<Map<String, Object>>: " + unwrappedResult.toString());
        }

        throw new DatabaseReceiverException("Unrecognized value returned from script in channel \"" + ChannelController.getInstance().getDeployedChannelById(connector.getChannelId()).getName() + "\", expected ResultSet or List<Map<String, Object>>: " + result.toString());
    }

    @Override
    public void runPostProcess(Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) throws DatabaseReceiverException, InterruptedException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_EACH) {
            try {
                javaScriptExecutor.execute(new UpdateTask(resultMap, mergedConnectorMessage));
            } catch (JavaScriptExecutorException e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    @Override
    public void afterPoll() throws InterruptedException, DatabaseReceiverException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_ONCE) {
            try {
                javaScriptExecutor.execute(new UpdateTask(null, null));
            } catch (JavaScriptExecutorException e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    private class SelectTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId());
            return JavaScriptUtil.executeScript(this, selectScriptId, scope, connector.getChannelId(), "Source");
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
            Scriptable scope = null;

            if (mergedConnectorMessage == null) {
                scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId());
            } else {
                scope = JavaScriptScopeUtil.getMessageReceiverScope(scriptLogger, connector.getChannelId(), mergedConnectorMessage);
            }

            if (resultMap != null) {
                scope.put("resultMap", scope, resultMap);
            }

            return JavaScriptUtil.executeScript(this, updateScriptId, scope, connector.getChannelId(), "Source");
        }
    }
}
