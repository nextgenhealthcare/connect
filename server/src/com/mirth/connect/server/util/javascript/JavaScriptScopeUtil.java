/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.userutil.AlertSender;
import com.mirth.connect.server.userutil.Attachment;
import com.mirth.connect.server.userutil.ChannelMap;
import com.mirth.connect.server.userutil.DestinationSet;
import com.mirth.connect.server.userutil.ImmutableResponse;
import com.mirth.connect.server.userutil.SourceMap;
import com.mirth.connect.server.userutil.VMRouter;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.userutil.ImmutableMessage;
import com.mirth.connect.userutil.Response;
import com.mirth.connect.userutil.ResponseMap;
import com.mirth.connect.userutil.Status;
import com.mirth.connect.util.PropertyLoader;

public class JavaScriptScopeUtil {
    private static Logger logger = Logger.getLogger(JavaScriptScopeUtil.class);
    private static Integer rhinoOptimizationLevel = null;

    static {
        /*
         * Checks mirth.properties for the rhino.optimizationlevel property. Setting it to -1 runs
         * it in interpretive mode. See MIRTH-1627 for more information.
         */
        Properties properties = PropertyLoader.loadProperties("mirth");

        if (MapUtils.isNotEmpty(properties) && properties.containsKey("rhino.optimizationlevel")) {
            logger.debug("set Rhino context optimization level: " + rhinoOptimizationLevel);
            rhinoOptimizationLevel = Integer.valueOf(properties.getProperty("rhino.optimizationlevel")).intValue();
        } else {
            logger.debug("using default Rhino context optimization level (-1)");
            rhinoOptimizationLevel = -1;
        }
    }

    /*
     * Retrieves the Context for the current Thread. The context must be cleaned up with
     * Context.exit() when it is no longer needed.
     */
    protected static Context getContext(ContextFactory contextFactory) {
        Context context = contextFactory.enterContext();
        context.setOptimizationLevel(rhinoOptimizationLevel);
        return context;
    }

    protected static ScriptableObject createSealedSharedScope(ContextFactory contextFactory) {
        Context context = contextFactory.enterContext();

        try {
            context.setOptimizationLevel(rhinoOptimizationLevel);

            ScriptableObject sealedSharedScope = new ImporterTopLevel(context);
            Script script = JavaScriptUtil.getCompiledGlobalSealedScript(context);
            script.exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
            return sealedSharedScope;
        } finally {
            Context.exit();
        }
    }

    // Creates a new global scope within the current Context
    private static Scriptable getScope(Context context) {
        Scriptable scope = context.newObject(((MirthContext) context).getSealedSharedScope());
        scope.setPrototype(((MirthContext) context).getSealedSharedScope());
        scope.setParentScope(null);
        return scope;
    }

    /*
     * Private Scope Builders
     */

    private static void add(String name, Scriptable scope, Object object) {
        scope.put(name, scope, Context.javaToJS(object, scope));
    }

    // Raw Message String Builder
    private static void addRawMessage(Scriptable scope, String message) {
        add("message", scope, message);
    }

    // Message Builder
    private static void addMessage(Scriptable scope, Message message) {
        ImmutableMessage immutableMessage = new ImmutableMessage(message);
        add("message", scope, immutableMessage);

        ConnectorMessage mergedConnectorMessage = message.getMergedConnectorMessage();
        ImmutableConnectorMessage immutableConnectorMessage = new ImmutableConnectorMessage(mergedConnectorMessage);

        add("sourceMap", scope, new SourceMap(immutableConnectorMessage.getSourceMap()));
        add("channelMap", scope, new ChannelMap(immutableConnectorMessage.getChannelMap(), immutableConnectorMessage.getSourceMap()));
        add("responseMap", scope, new ResponseMap(mergedConnectorMessage.getResponseMap(), immutableMessage.getDestinationIdMap()));
    }

    // ConnectorMessage Builder
    private static void addConnectorMessage(Scriptable scope, ImmutableConnectorMessage message) {
        add("connectorMessage", scope, message);
        add("sourceMap", scope, new SourceMap(message.getSourceMap()));
        add("connectorMap", scope, message.getConnectorMap());
        add("channelMap", scope, new ChannelMap(message.getChannelMap(), message.getSourceMap()));
        add("responseMap", scope, new ResponseMap(message.getResponseMap(), message.getDestinationIdMap()));
        add("connector", scope, message.getConnectorName());
        add("alerts", scope, new AlertSender(message));

        if (message.getMetaDataId() == 0) {
            add("destinationSet", scope, new DestinationSet(message));
        }
    }

    private static void addResponse(Scriptable scope, Response response) {
        add("response", scope, new ImmutableResponse(response));
        add("responseStatus", scope, response.getStatus());
        add("responseErrorMessage", scope, response.getError());
        add("responseStatusMessage", scope, response.getStatusMessage());
    }

    // Router Builder
    private static void addRouter(Scriptable scope) {
        add("router", scope, new VMRouter());
    }

    // Replacer
    private static void addReplacer(Scriptable scope) {
        add("replacer", scope, new TemplateValueReplacer());
    }

    // Global Map Builder
    private static void addGlobalMap(Scriptable scope) {
        add("globalMap", scope, GlobalVariableStore.getInstance());
    }

    // Configuration Map Builder
    private static void addConfigurationMap(Scriptable scope) {
        add("configurationMap", scope, ConfigurationController.getInstance().getConfigurationMap());
    }

    // Channel Builder
    private static void addChannel(Scriptable scope, String channelId) {
        add("alerts", scope, new AlertSender(channelId));
        add("channelId", scope, channelId);
        add("globalChannelMap", scope, GlobalChannelVariableStoreFactory.getInstance().get(channelId));
    }

    // Logger builder
    private static void addLogger(Scriptable scope, Object logger) {
        add("logger", scope, logger);
    }

    // Status enum builder
    private static void addStatusValues(Scriptable scope) {
        for (Status status : Status.values()) {
            add(status.toString(), scope, status);
        }
    }

    /*
     * Private Basic Scopes
     */

    private static Scriptable getBasicScope(Context context) {
        Scriptable scope = getScope(context);
        addRouter(scope);
        addReplacer(scope);
        addConfigurationMap(scope);
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

    private static Scriptable getBasicScope(Context context, Object logger, ImmutableConnectorMessage message) {
        return getBasicScope(context, logger, message.getChannelId());
    }

    /*
     * Public Phase-specific Scopes
     */

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getAttachmentScope(ContextFactory contextFactory, Object logger, String channelId, String message, List<Attachment> attachments) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addRawMessage(scope, message);
        add("mirth_attachments", scope, attachments);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPreprocessorScope(ContextFactory contextFactory, Object logger, String channelId, String message, ImmutableConnectorMessage connectorMessage) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addRawMessage(scope, message);
        addConnectorMessage(scope, connectorMessage);

        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPostprocessorScope(ContextFactory contextFactory, Object logger, String channelId, Message message) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addStatusValues(scope);
        addMessage(scope, message);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPostprocessorScope(ContextFactory contextFactory, Object logger, String channelId, Message message, Response response) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addMessage(scope, message);
        addStatusValues(scope);
        add("response", scope, response);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getFilterTransformerScope(ContextFactory contextFactory, Object logger, ImmutableConnectorMessage message, String template, Object phase) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, message);
        addConnectorMessage(scope, message);
        add("template", scope, template);
        add("phase", scope, phase);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getResponseTransformerScope(ContextFactory contextFactory, Object logger, Response response, ImmutableConnectorMessage message, String template) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, message);
        addConnectorMessage(scope, message);
        addResponse(scope, response);
        addStatusValues(scope);
        add("template", scope, template);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getDeployScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getDeployScope(ContextFactory contextFactory, Object logger) {
        return getBasicScope(getContext(contextFactory), logger);
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getUndeployScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getUndeployScope(ContextFactory contextFactory, Object logger) {
        return getBasicScope(getContext(contextFactory), logger);
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageReceiverScope(ContextFactory contextFactory, Object logger, String channelId) {
        return getBasicScope(getContext(contextFactory), logger, channelId);
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageReceiverScope(ContextFactory contextFactory, Object logger, String channelId, ImmutableConnectorMessage message) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addConnectorMessage(scope, message);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageDispatcherScope(ContextFactory contextFactory, Object logger, String channelId, ImmutableConnectorMessage message) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger, channelId);
        addConnectorMessage(scope, message);
        addStatusValues(scope);
        return scope;
    }

    /**
     * Since this method calls getContext(), anything calling it should wrap this method in a
     * try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getBatchProcessorScope(ContextFactory contextFactory, Object logger, String channelId, Map<String, Object> scopeObjects) {
        Scriptable scope = getBasicScope(getContext(contextFactory), logger);

        for (Entry<String, Object> entry : scopeObjects.entrySet()) {
            add(entry.getKey(), scope, entry.getValue());
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

    public static void getResponseDataFromScope(Scriptable scope, Response response) {
        Object status = scope.get("responseStatus", scope);
        Object statusMessage = scope.get("responseStatusMessage", scope);
        Object errorMessage = scope.get("responseErrorMessage", scope);

        response.setStatus((Status) Context.jsToJava(status, Status.class));

        if (statusMessage != null && !(statusMessage instanceof Undefined)) {
            response.setStatusMessage(Context.toString(statusMessage));
        } else {
            response.setStatusMessage(null);
        }

        if (errorMessage != null && !(errorMessage instanceof Undefined)) {
            response.setError(Context.toString(errorMessage));
        } else {
            response.setError(null);
        }
    }
}