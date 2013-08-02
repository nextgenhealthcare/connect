/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableMessage;
import com.mirth.connect.donkey.model.message.ImmutableResponse;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.server.userutil.AlertSender;
import com.mirth.connect.server.userutil.Attachment;
import com.mirth.connect.server.userutil.MessageObject;
import com.mirth.connect.server.userutil.VMRouter;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.PropertyLoader;

public class JavaScriptScopeUtil {
    private static Logger logger = Logger.getLogger(JavaScriptScopeUtil.class);
    private static ScriptableObject sealedSharedScope = null;
    private static Integer rhinoOptimizationLevel = null;

    static {
        ContextFactory.initGlobal(new StoppableContextFactory());
    }

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

    /*
     * Retrieves the Context for the current Thread; only initializes the shared scope if necessary.
     * The context must be cleaned up with Context.exit() when it is no longer needed.
     */
    protected static Context getContext() {
        initialize();
        Context context = ContextFactory.getGlobal().enterContext();
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

    // Raw Message String Builder
    private static void addRawMessage(Scriptable scope, String message) {
        scope.put("message", scope, message);
    }

    // Message Builder
    private static void addMessage(Scriptable scope, Message message) {
        ImmutableMessage immutableMessage = new ImmutableMessage(message);
        scope.put("message", scope, immutableMessage);

        // TODO: Deprecated, Remove in 3.1
        scope.put("messageObject", scope, new MessageObject(immutableMessage.getConnectorMessages().get(0)));

        ConnectorMessage mergedConnectorMessage = message.getMergedConnectorMessage();
        ImmutableConnectorMessage immutableConnectorMessage = new ImmutableConnectorMessage(mergedConnectorMessage);

        scope.put("channelMap", scope, immutableConnectorMessage.getChannelMap());
        scope.put("responseMap", scope, mergedConnectorMessage.getResponseMap());
    }

    // ConnectorMessage Builder
    private static void addConnectorMessage(Scriptable scope, ImmutableConnectorMessage message) {
        // TODO: Deprecated, Remove in 3.1
        scope.put("messageObject", scope, new MessageObject(message));

        scope.put("connectorMessage", scope, message);
        scope.put("connectorMap", scope, message.getConnectorMap());
        scope.put("channelMap", scope, message.getChannelMap());
        scope.put("responseMap", scope, message.getResponseMap());
        scope.put("connector", scope, message.getConnectorName());
        scope.put("alerts", scope, new AlertSender(message));
    }

    private static void addResponse(Scriptable scope, Response response) {
        scope.put("response", scope, new ImmutableResponse(response));
        // Convert java to JS so we can use the == comparator in javascript
        scope.put("responseStatus", scope, Context.javaToJS(response.getStatus(), scope));
        scope.put("responseErrorMessage", scope, response.getError());
        scope.put("responseStatusMessage", scope, response.getStatusMessage());
    }

    // Router Builder
    private static void addRouter(Scriptable scope) {
        scope.put("router", scope, new VMRouter());
    }

    // Replacer
    private static void addReplacer(Scriptable scope) {
        scope.put("replacer", scope, new TemplateValueReplacer());
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

    // Status enum builder
    private static void addStatusValues(Scriptable scope) {
        for (Status status : Status.values()) {
            // Convert java to JS so we can use the == comparator in javascript
            scope.put(status.toString(), scope, Context.javaToJS(status, scope));
        }
    }

    /*
     * Private Basic Scopes
     */

    private static Scriptable getBasicScope(Context context) {
        Scriptable scope = getScope(context);
        addRouter(scope);
        addReplacer(scope);
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
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getAttachmentScope(Object logger, String channelId, String message, List<Attachment> attachments) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addRawMessage(scope, message);
        scope.put("mirth_attachments", scope, attachments);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPreprocessorScope(Object logger, String channelId, String message, ImmutableConnectorMessage connectorMessage) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addRawMessage(scope, message);
        addConnectorMessage(scope, connectorMessage);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPostprocessorScope(Object logger, String channelId, Message message) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addStatusValues(scope);
        addMessage(scope, message);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getPostprocessorScope(Object logger, String channelId, Message message, Response response) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addMessage(scope, message);
        addStatusValues(scope);
        scope.put("response", scope, response);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getFilterTransformerScope(Object logger, ImmutableConnectorMessage message, String template, String phase) {
        Scriptable scope = getBasicScope(getContext(), logger, message);
        addConnectorMessage(scope, message);
        scope.put("template", scope, template);
        scope.put("phase", scope, phase);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getResponseTransformerScope(Object logger, Response response, ImmutableConnectorMessage message, String template) {
        Scriptable scope = getBasicScope(getContext(), logger, message);
        addConnectorMessage(scope, message);
        addResponse(scope, response);
        addStatusValues(scope);
        scope.put("template", scope, template);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getDeployScope(Object logger, String channelId) {
        return getBasicScope(getContext(), logger, channelId);
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getDeployScope(Object logger) {
        return getBasicScope(getContext(), logger);
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getShutdownScope(Object logger, String channelId) {
        return getBasicScope(getContext(), logger, channelId);
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getShutdownScope(Object logger) {
        return getBasicScope(getContext(), logger);
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageReceiverScope(Object logger, String channelId) {
        return getBasicScope(getContext(), logger, channelId);
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageReceiverScope(Object logger, String channelId, ImmutableConnectorMessage message) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addConnectorMessage(scope, message);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getMessageDispatcherScope(Object logger, String channelId, ImmutableConnectorMessage message) {
        Scriptable scope = getBasicScope(getContext(), logger, channelId);
        addConnectorMessage(scope, message);
        addStatusValues(scope);
        return scope;
    }

    /** 
     * Since this method calls getContext(), anything calling it should wrap this method in a try-finally with Context.exit() in the finally block.
     */
    public static Scriptable getBatchProcessorScope(Object logger, String channelId, Map<String, Object> scopeObjects) {
        Scriptable scope = getBasicScope(getContext(), logger);

        for (Entry<String, Object> entry : scopeObjects.entrySet()) {
            scope.put(entry.getKey(), scope, entry.getValue());
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