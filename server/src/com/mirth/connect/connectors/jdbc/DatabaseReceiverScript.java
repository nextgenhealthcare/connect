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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.connectors.js.MirthScopeProvider;
import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.codetemplates.ContextType;
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
    List<String> contextFactoryIdList = new ArrayList<String>();
    private MirthMain debugger;
    private Boolean debug;
    private Boolean update = false;
    private boolean ignoreBreakpoints = false;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    

    public DatabaseReceiverScript(DatabaseReceiver connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws ConnectorTaskException {
        Channel channel = connector.getChannel();
        DebugOptions debugOptions = channel.getDebugOptions();

        this.debug  = debugOptions != null && debugOptions.isSourceConnectorScripts();
        connectorProperties = (DatabaseReceiverProperties) connector.getConnectorProperties();
        selectScriptId = UUID.randomUUID().toString() + "_Select";
        MirthContextFactory contextFactory;

        try {
            
        	if (debug) {
                contextFactory = contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), selectScriptId);
                contextFactory.setContextType(ContextType.SOURCE_RECEIVER);
                contextFactory.setScriptText(connectorProperties.getSelect());
                contextFactory.setDebugType(true);
                debugger = JavaScriptUtil.getDebugger(contextFactory, scopeProvider, channel, selectScriptId);
        		
        	} else {
        		contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
        	}
        	
        	contextFactoryId = contextFactory.getId();
            contextFactoryIdList.add(contextFactoryId);
        	JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, selectScriptId, connectorProperties.getSelect(), ContextType.SOURCE_RECEIVER, null, null);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error compiling select script " + selectScriptId + ".", e);
        }

        if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
            updateScriptId = UUID.randomUUID().toString() + "_Update";

            try {
                JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, updateScriptId, connectorProperties.getUpdate(), ContextType.SOURCE_RECEIVER, null, null);
            } catch (Exception e) {
                throw new ConnectorTaskException("Error compiling update script " + updateScriptId + ".", e);
            }
        }
    }

    @Override
    public void start() throws ConnectorTaskException {
        ignoreBreakpoints = false;
        if (debug && debugger != null) {
            debugger.enableDebugging();
        }
    }

    @Override
    public void stop() throws ConnectorTaskException {
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
    }

    @Override
    public void undeploy() {
    	if (selectScriptId != null) {
    		JavaScriptUtil.removeScriptFromCache(selectScriptId);
    	}
        if (updateScriptId != null) {
            JavaScriptUtil.removeScriptFromCache(updateScriptId);
            
        }
        if (debug && debugger != null) {
            debugger.detach();
            contextFactoryController.removeDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), selectScriptId);
            contextFactoryController.removeDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), updateScriptId);
            debugger.dispose();
            debugger = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object poll() throws DatabaseReceiverException, InterruptedException {
        Object finalResult = null;
        int attempts = 0;
        String channelId = connector.getChannelId();
        String channelName = connector.getChannel().getName();
        int maxRetryCount = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryCount(), channelId, channelName), 0);
        int retryInterval = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRetryInterval(), channelId, channelName), 0);
        boolean done = false;

        while (!done && !connector.isTerminated()) {
            try {
            	update = false;
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
            	update = true;
                JavaScriptUtil.execute(new UpdateTask(getContextFactory(), resultMap, mergedConnectorMessage));
            } catch (Exception e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    @Override
    public void runAggregatePostProcess(List<Map<String, Object>> resultsList, ConnectorMessage mergedConnectorMessage) throws DatabaseReceiverException, InterruptedException {
        try {
        	update = true;
            JavaScriptUtil.execute(new UpdateTask(getContextFactory(), resultsList, mergedConnectorMessage));
        } catch (Exception e) {
            throw new DatabaseReceiverException(e);
        }
    }

    @Override
    public void afterPoll() throws InterruptedException, DatabaseReceiverException {
        if (connectorProperties.getUpdateMode() == DatabaseReceiverProperties.UPDATE_ONCE && !connectorProperties.isAggregateResults()) {
            try {
            	update = true;
                JavaScriptUtil.execute(new UpdateTask(getContextFactory(), null, null, null));
            } catch (Exception e) {
                throw new DatabaseReceiverException(e);
            }
        }
    }

    private class SelectTask extends JavaScriptTask<Object> {

        public SelectTask(MirthContextFactory contextFactory) {
            super(contextFactory, connector.getConnectorProperties().getName() + " Select", connector);
        }

        @Override
        public Object doCall() throws Exception {
            try {
                Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId(), connector.getChannel().getName());
                
                if (debug) {
                    scopeProvider.setScope(scope);

                    if (debugger != null && !ignoreBreakpoints) {
                        debugger.doBreak();
                        
                        if (!debugger.isVisible()) {
                            debugger.setVisible(true);
                        }
                    }
                }

                return JavaScriptUtil.executeScript(this, selectScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }

    private class UpdateTask extends JavaScriptTask<Object> {
        private Map<String, Object> resultMap;
        private List<Map<String, Object>> resultsList;
        private ConnectorMessage mergedConnectorMessage;

        public UpdateTask(MirthContextFactory contextFactory, Map<String, Object> resultMap, ConnectorMessage mergedConnectorMessage) {
            this(contextFactory, resultMap, null, mergedConnectorMessage);
        }

        public UpdateTask(MirthContextFactory contextFactory, List<Map<String, Object>> resultsList, ConnectorMessage mergedConnectorMessage) {
            this(contextFactory, null, resultsList, mergedConnectorMessage);
        }

        public UpdateTask(MirthContextFactory contextFactory, Map<String, Object> resultMap, List<Map<String, Object>> resultsList, ConnectorMessage mergedConnectorMessage) {
            super(contextFactory, connector.getConnectorProperties().getName() + " Update", connector);
            this.resultMap = resultMap;
            this.resultsList = resultsList;
            this.mergedConnectorMessage = mergedConnectorMessage;
        }

        @Override
        public Object doCall() throws Exception {
            try {
                Scriptable scope = null;

                if (mergedConnectorMessage == null) {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId(), connector.getChannel().getName());
                } else {
                    scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), scriptLogger, connector.getChannelId(), new ImmutableConnectorMessage(mergedConnectorMessage, true, connector.getDestinationIdMap()));
                }

                if (resultMap != null) {
                    scope.put("resultMap", scope, Context.javaToJS(resultMap, scope));
                }
                if (resultsList != null) {
                    scope.put("results", scope, Context.javaToJS(resultsList, scope));
                }

                if (debug) {
                    scopeProvider.setScope(scope);

                    if (debugger != null && !ignoreBreakpoints) {
                        debugger.doBreak();
                        
                        if (!debugger.isVisible()) {
                            debugger.setVisible(true);
                        }
                    }
                }

                return JavaScriptUtil.executeScript(this, updateScriptId, scope, connector.getChannelId(), "Source");
            } finally {
                Context.exit();
            }
        }
    }

    private MirthContextFactory getContextFactory() throws Exception {
    	
    	MirthContextFactory contextFactory = debug ? contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), selectScriptId) : contextFactoryController.getContextFactory(connector.getResourceIds()); 

        if (!contextFactoryIdList.contains(contextFactory.getId())) {
            synchronized (this) {
                contextFactory = debug ? contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), selectScriptId) : contextFactoryController.getContextFactory(connector.getResourceIds());

                if (!contextFactoryIdList.contains(contextFactory.getId())) {
                    JavaScriptUtil.recompileGeneratedScript(contextFactory, selectScriptId);
                    if (connectorProperties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
                        JavaScriptUtil.recompileGeneratedScript(contextFactory, updateScriptId);
                    }
                    contextFactoryIdList.add(contextFactory.getId());
                }
            }
        }
    	

        return contextFactory;
    }
}
