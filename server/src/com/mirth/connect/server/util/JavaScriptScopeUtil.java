/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.Reader;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.converters.delimited.DelimitedProperties;
import com.mirth.connect.server.util.javascript.StoppableContextFactory;
import com.mirth.connect.util.PropertyLoader;

public class JavaScriptScopeUtil {
    private static Logger logger = Logger.getLogger(JavaScriptScopeUtil.class);
    private static ScriptableObject sealedSharedScope = null;
    private static Integer rhinoOptimizationLevel = null;
    private static StoppableContextFactory contextFactory = new StoppableContextFactory();
    
    private static void initialize() {
        if (rhinoOptimizationLevel == null) {
            rhinoOptimizationLevel = -1;

            /*
             * Checks mirth.properties for the rhino.optimizationlevel property.
             * Setting it to -1 runs it in interpretive mode.
             * See MIRTH-1627 for more information.
             */
            Properties properties = PropertyLoader.loadProperties("mirth");

            if (MapUtils.isNotEmpty(properties) && properties.containsKey("rhino.optimizationlevel")) {
                rhinoOptimizationLevel = Integer.valueOf(properties.getProperty("rhino.optimizationlevel")).intValue();
                logger.debug("set Rhino context optimization level: " + rhinoOptimizationLevel);
            } else {
                logger.debug("using defualt Rhino context optimization level (-1)");
            }
        }
    }

    // Retrieves the Context for the current Thread; only initializes the shared scope if necessary
    public static Context getContext() {
        initialize();
        Context context = Context.getCurrentContext();
        if (context == null)
            context = Context.enter();
        context.setOptimizationLevel(rhinoOptimizationLevel);

        if (sealedSharedScope == null) {
            sealedSharedScope = new ImporterTopLevel(context);
            Script script = JavaScriptUtil.getCompiledGlobalSealedScript(context);
            script.exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        }

        return context;
    }
    
    public static Context getContext(ContextFactory contextFactory) {
        initialize();
        Context context = contextFactory.enterContext();
        context.setOptimizationLevel(rhinoOptimizationLevel);

        if (sealedSharedScope == null) {
            sealedSharedScope = new ImporterTopLevel(context);
            Script script = JavaScriptUtil.getCompiledGlobalSealedScript(context);
            script.exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        }

        return context;
    }

    // Creates a new global scope within the current Context
    private static Scriptable getScope(Context context) {
        Scriptable scope = context.newObject(sealedSharedScope);
        scope.setPrototype(sealedSharedScope);
        scope.setParentScope(null);
        return scope;
    }

    /*
     * Private Scope Builders
     */

    // RawMessage Builder
    private static void addRawMessage(Scriptable scope, String message) {
        // TODO: Change this to RawMessage object?
        scope.put("message", scope, message);
    }

    // Message Builder
    private static void addMessage(Scriptable scope, Message message) {
        // TODO: Change this to Message read-only API
        scope.put("message", scope, new ImmutableMessage(message));
    }

    // ConnectorMessage Builder
    private static void addConnectorMessage(Scriptable scope, ConnectorMessage message) {
        // TODO: Change this to ConnectorMessage read-only API
        scope.put("messageObject", scope, new ImmutableConnectorMessage(message));
        scope.put("message", scope, message.getTransformed().getContent());

        scope.put("connectorMap", scope, message.getConnectorMap());
        scope.put("channelMap", scope, message.getChannelMap());
        scope.put("responseMap", scope, message.getResponseMap());
        scope.put("connector", scope, message.getConnectorName());
    }

    // Router Builder
    private static void addRouter(Scriptable scope) {
        scope.put("router", scope, new VMRouter());
    }

    // Global Map Builder
    private static void addGlobalMap(Scriptable scope) {
        scope.put("globalMap", scope, GlobalVariableStore.getInstance());
    }

    // Channel Builder
    private static void addChannel(Scriptable scope, String channelId) {
        scope.put("alerts", scope, new AlertSender(channelId));
        scope.put("channelId", scope, channelId);
        scope.put("globalChannelMap", scope, GlobalChannelVariableStoreFactory.getInstance().get(channelId));
    }

    // Logger builder
    private static void addLogger(Scriptable scope, Object logger) {
        scope.put("logger", scope, logger);
    }

    /*
     * Private Basic Scopes
     */

    private static Scriptable getBasicScope(Context context) {
        Scriptable scope = getScope(context);
        addRouter(scope);
        addGlobalMap(scope);
        return scope;
    }

    private static Scriptable getBasicScope(Context context, Object logger) {
        Scriptable scope = getBasicScope(context);
        addLogger(scope, logger);
        return scope;
    }

    private static Scriptable getBasicScope(Context context, Object logger, String channelId) {
        Scriptable scope = getBasicScope(context, logger);
        addChannel(scope, channelId);
        return scope;
    }

    private static Scriptable getBasicScope(Context context, Object logger, ConnectorMessage message) {
        return getBasicScope(context, logger, message.getChannelId());
    }

    /*
     * Public Phase-specific Scopes
     */

    public static Scriptable getAttachmentScope(ContextFactory contextFactory, Object logger, String channelId, String message, List<Attachment> attachments) {
        Context context = getContext(contextFactory);
        Scriptable scope = getBasicScope(context, logger, channelId);
        addRawMessage(scope, message);
        scope.put("attachments", scope, attachments);
        return scope;
    }

    // TODO: Add attachments
    public static Scriptable getPreprocessorScope(ContextFactory contextFactory, Object logger, String channelId, String message, ConnectorMessage connectorMessage) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addRawMessage(scope, message);
        scope.put("messageObject", scope, new ImmutableConnectorMessage(connectorMessage));
        return scope;
    }

    // TODO: Add attachments
    public static Scriptable getPostprocessorScope(ContextFactory contextFactory, Object logger, String channelId, Message message) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addMessage(scope, message);
        return scope;
    }

    public static Scriptable getPostprocessorScope(ContextFactory contextFactory, Object logger, String channelId, Message message, Response response) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addMessage(scope, message);
        scope.put("response", scope, response);
        return scope;
    }

    public static Scriptable getFilterTransformerScope(ContextFactory contextFactory, Object logger, ConnectorMessage message, String template, String phase) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, message);
        addConnectorMessage(scope, message);
        scope.put("template", scope, template);
        scope.put("phase", scope, phase);
        return scope;
    }

    public static Scriptable getResponseTransformerScope(ContextFactory contextFactory, Object logger, Response response) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger);
        scope.put("response", scope, response);
        return scope;
    }

    public static Scriptable getDeployScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    public static Scriptable getDeployScope(ContextFactory contextFactory, Object logger) {
        return getBasicScope(getContext(contextFactory), logger);
    }

    public static Scriptable getShutdownScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    public static Scriptable getShutdownScope(ContextFactory contextFactory, Object logger) {
        return getBasicScope(getContext(contextFactory), logger);
    }

    public static Scriptable getMessageReceiverScope(ContextFactory contextFactory, Object logger) {
        return getBasicScope(getContext(contextFactory), logger);
    }

    public static Scriptable getMessageReceiverScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    public static Scriptable getMessageDispatcherScope(Object logger, String channelId, ConnectorMessage message) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addConnectorMessage(scope, message);
        return scope;
    }

    public static Scriptable getBatchProcessorScope(ContextFactory contextFactory, Object logger, String channelId, Reader in, DelimitedProperties props, Boolean skipHeader) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger);

        // Provide the reader in the scope
        scope.put("reader", scope, in);

        // Provide the data type properties in the scope (the ones that
        // affect parsing from delimited to XML)
        scope.put("columnDelimiter", scope, props.getColumnDelimiter());
        scope.put("recordDelimiter", scope, props.getRecordDelimiter());
        scope.put("columnWidths", scope, props.getColumnWidths());
        scope.put("quoteChar", scope, props.getQuoteChar());
        scope.put("escapeWithDoubleQuote", scope, props.isEscapeWithDoubleQuote());
        scope.put("quoteEscapeChar", scope, props.getQuoteEscapeChar());
        scope.put("ignoreCR", scope, props.isIgnoreCR());
        if (skipHeader) {
            scope.put("skipRecords", scope, props.getBatchSkipRecords());
        } else {
            scope.put("skipRecords", scope, 0);
        }

        if (channelId != null)
            addChannel(scope, channelId);

        return scope;
    }

    /*
     * Functions to get variables back out of the scope
     */

    public static String getTransformedDataFromScope(Scriptable scope, boolean hasTemplate) {
        String result = null;
        Object transformedData = null;

        if (hasTemplate) {
            transformedData = scope.get("tmp", scope);
        } else {
            transformedData = scope.get("msg", scope);
        }

        if (transformedData != Scriptable.NOT_FOUND) {
            result = Context.toString(transformedData);
        }

        return result;
    }
}