/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
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
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

public class DatabaseReceiverScript implements DatabaseReceiverDelegate {
    private DatabaseReceiver connector;
    private String selectScriptId;
    private String updateScriptId;
    private DatabaseReceiverProperties connectorProperties;
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private String contextFactoryId;

    public DatabaseReceiverScript(DatabaseReceiver connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws ConnectorTaskException {
        connectorProperties = (DatabaseReceiverProperties) connector.getConnectorProperties();
        selectScriptId = UUID.randomUUID().toString();
        MirthContextFactory contextFactory;

        try {
            contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
            contextFactoryId = contextFactory.getId();
            JavaScriptUtil.compileAndAddScript(contextFactory, selectScriptId, connectorProperties.getSelect(), ContextType.MESSAGE_CONTEXT, null, null);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error compiling select script " + selectScriptId + ".", e);
        }

        if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
            updateScriptId = UUID.randomUUID().toString();

            try {
                JavaScriptUtil.compileAndAddScript(contextFactory, updateScriptId, connectorProperties.getUpdate(), ContextType.MESSAGE_CONTEXT, null, null);
            } catch (Exception e) {
                throw new ConnectorTaskException("Error compiling update script " + updateScriptId + ".", e);
            }
        }
    }

    @Override
    public void start() throws ConnectorTaskException {}

    @Override
    public void stop() throws ConnectorTaskException {}

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
                Object result = JavaScriptUtil.execute(new SelectTask(getContextFactory()));

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
            } catch (Exception e) {
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
                JavaScriptUtil.execute(new UpdateTask(getContextFactory(), resultMap, mergedConnectorMessage));
            } catch (Exception e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    @Override
    public void afterPoll() throws InterruptedException, DatabaseReceiverException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_ONCE) {
            try {
                JavaScriptUtil.execute(new UpdateTask(getContextFactory(), null, null));
            } catch (Exception e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    private class SelectTask extends JavaScriptTask<Object> {

        public SelectTask(MirthContextFactory contextFactory) {
            super(contextFactory);
        }

        @Override
        public Object call() throws Exception {
            try {
                Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId());
                return JavaScriptUtil.executeScript(this, selectScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }

    private class UpdateTask extends JavaScriptTask<Object> {
        private Map<String, Object> resultMap;
        private ConnectorMessage mergedConnectorMessage;

        public UpdateTask(MirthContextFactory contextFactory, Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) {
            super(contextFactory);
            this.resultMap = resultMap;
            this.mergedConnectorMessage = mergedConnectorMessage;
        }

        @Override
        public Object call() throws Exception {
            try {
                Scriptable scope = null;

                if (mergedConnectorMessage == null) {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId());
                } else {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId(), new ImmutableConnectorMessage(mergedConnectorMessage, true, connector.getDestinationIdMap()));
                }

                if (resultMap != null) {
                    scope.put("resultMap", scope, Context.javaToJS(resultMap, scope));
                }

                return JavaScriptUtil.executeScript(this, updateScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }

    private MirthContextFactory getContextFactory() throws Exception {
        MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());

        if (!contextFactoryId.equals(contextFactory.getId())) {
            JavaScriptUtil.recompileGeneratedScript(contextFactory, selectScriptId);
            if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
                JavaScriptUtil.recompileGeneratedScript(contextFactory, updateScriptId);
            }
            contextFactoryId = contextFactory.getId();
        }

        return contextFactory;
    }
}
