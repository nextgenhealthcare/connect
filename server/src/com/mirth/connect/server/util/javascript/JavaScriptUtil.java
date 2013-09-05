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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptCompileException;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.userutil.Attachment;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.ServerUUIDGenerator;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

public class JavaScriptUtil {
    private static Logger logger = Logger.getLogger(JavaScriptUtil.class);
    private static CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private static final int SOURCE_CODE_LINE_WRAPPER = 5;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static <T> T execute(JavaScriptTask<T> task) throws JavaScriptExecutorException, InterruptedException {
        Future<T> future = executor.submit(task);

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new JavaScriptExecutorException(e.getCause());
        } catch (InterruptedException e) {
            // synchronize with JavaScriptTask.executeScript() so that it will not initialize the context while we are halting the task
            synchronized (task) {
                future.cancel(true);
                Context context = task.getContext();

                if (context != null && context instanceof StoppableContext) {
                    ((StoppableContext) context).setRunning(false);
                }
            }

            // TODO wait for the task thread to complete before exiting?
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    public static String executeAttachmentScript(final String message, final String channelId, final List<Attachment> attachments) throws InterruptedException, JavaScriptExecutorException {
        String processedMessage = message;
        Object result = null;

        try {
            result = execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(ScriptController.ATTACHMENT_SCRIPT_KEY.toLowerCase());
                    try {
                        Scriptable scope = JavaScriptScopeUtil.getAttachmentScope(scriptLogger, channelId, message, attachments);
                        return JavaScriptUtil.executeScript(this, ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId), scope, null, null);
                    } finally {
                        Context.exit();
                    }
                }
            });
        } catch (JavaScriptExecutorException e) {
            logScriptError(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId, e.getCause());
            throw e;
        }

        if (result != null) {
            String resultString = (String) Context.jsToJava(result, java.lang.String.class);

            if (resultString != null) {
                processedMessage = resultString;
            }
        }

        return processedMessage;
    }

    /**
     * Executes the JavaScriptTask associated with the postprocessor, if
     * necessary.
     * 
     * @param task
     * @param channelId
     * @return
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static String executeJavaScriptPreProcessorTask(JavaScriptTask<Object> task, String channelId) throws InterruptedException, JavaScriptExecutorException {
        String channelScriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId);

        // Only execute the task if the channel or global scripts exist
        if (compiledScriptCache.getCompiledScript(channelScriptId) != null || compiledScriptCache.getCompiledScript(ScriptController.PREPROCESSOR_SCRIPT_KEY) != null) {
            return (String) execute(task);
        } else {
            return null;
        }
    }

    /**
     * Executes the global and channel preprocessor scripts in order, building
     * up the necessary scope for the global preprocessor and adding the result
     * back to it for the channel preprocessor.
     * 
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     * 
     */
    public static String executePreprocessorScripts(JavaScriptTask<Object> task, ConnectorMessage message) throws Exception {
        String processedMessage = null;
        String globalResult = message.getRaw().getContent();
        Logger scriptLogger = Logger.getLogger(ScriptController.PREPROCESSOR_SCRIPT_KEY.toLowerCase());

        try {
            // Execute the global preprocessor and check the result
            Object result = null;

            if (compiledScriptCache.getCompiledScript(ScriptController.PREPROCESSOR_SCRIPT_KEY) != null) {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getPreprocessorScope(scriptLogger, message.getChannelId(), message.getRaw().getContent(), new ImmutableConnectorMessage(message, true));
                    result = JavaScriptUtil.executeScript(task, ScriptController.PREPROCESSOR_SCRIPT_KEY, scope, null, null);
                } finally {
                    Context.exit();
                }
            }

            if (result != null) {
                String resultString = (String) Context.jsToJava(result, java.lang.String.class);

                // Set the processed message in case something goes wrong in the channel processor. Also update the global result so the channel processor uses the updated message
                if (resultString != null) {
                    processedMessage = resultString;
                    globalResult = processedMessage;
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, message.getChannelId(), e);
            throw e;
        }

        try {
            // Execute the channel preprocessor and check the result
            Object result = null;
            String scriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, message.getChannelId());

            if (compiledScriptCache.getCompiledScript(scriptId) != null) {
                try {
                    // Update the scope with the result from the global processor
                    Scriptable scope = JavaScriptScopeUtil.getPreprocessorScope(scriptLogger, message.getChannelId(), globalResult, new ImmutableConnectorMessage(message, true));
                    result = JavaScriptUtil.executeScript(task, scriptId, scope, null, null);
                } finally {
                    Context.exit();
                }
            }

            if (result != null) {
                String resultString = (String) Context.jsToJava(result, java.lang.String.class);

                // Set the processed message if there was a result.
                if (resultString != null) {
                    processedMessage = resultString;
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, message.getChannelId(), e);
            throw e;
        }

        return processedMessage;
    }

    /**
     * Executes the JavaScriptTask associated with the postprocessor, if
     * necessary.
     * 
     * @param task
     * @param channelId
     * @return
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static Response executeJavaScriptPostProcessorTask(JavaScriptTask<Object> task, String channelId) throws InterruptedException, JavaScriptExecutorException {
        String channelScriptId = ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId);

        // Only execute the task if the channel or global scripts exist
        if (compiledScriptCache.getCompiledScript(channelScriptId) != null || compiledScriptCache.getCompiledScript(ScriptController.POSTPROCESSOR_SCRIPT_KEY) != null) {
            return (Response) execute(task);
        } else {
            return null;
        }
    }

    /**
     * Executes the channel postprocessor, followed by the global postprocessor.
     * 
     * @param task
     * @param message
     * @return
     * @throws Exception
     */
    public static Response executePostprocessorScripts(JavaScriptTask<Object> task, Message message) throws Exception {
        Logger scriptLogger = Logger.getLogger(ScriptController.POSTPROCESSOR_SCRIPT_KEY.toLowerCase());

        Response channelResponse = null;
        try {
            String scriptId = ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, message.getChannelId());
            if (compiledScriptCache.getCompiledScript(scriptId) != null) {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getPostprocessorScope(scriptLogger, message.getChannelId(), message);
                    channelResponse = getPostprocessorResponse(JavaScriptUtil.executeScript(task, scriptId, scope, null, null));
                } finally {
                    Context.exit();
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.POSTPROCESSOR_SCRIPT_KEY, message.getChannelId(), e);
            throw e;
        }

        Response response = channelResponse;
        try {
            if (compiledScriptCache.getCompiledScript(ScriptController.POSTPROCESSOR_SCRIPT_KEY) != null) {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getPostprocessorScope(scriptLogger, message.getChannelId(), message, new com.mirth.connect.userutil.Response(channelResponse));
                    Response globalResponse = getPostprocessorResponse(JavaScriptUtil.executeScript(task, ScriptController.POSTPROCESSOR_SCRIPT_KEY, scope, null, null));

                    if (globalResponse != null) {
                        response = globalResponse;
                    }
                } finally {
                    Context.exit();
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.POSTPROCESSOR_SCRIPT_KEY, message.getChannelId(), e);
            throw e;
        }

        return response;
    }

    private static Response getPostprocessorResponse(Object result) {
        Response response = null;

        // Convert result of JavaScript execution to Response object
        if (result instanceof com.mirth.connect.userutil.Response) {
            response = convertToDonkeyResponse(result);
        } else if (result instanceof NativeJavaObject) {
            Object object = ((NativeJavaObject) result).unwrap();

            if (object instanceof com.mirth.connect.userutil.Response) {
                response = convertToDonkeyResponse(object);
            } else {
                // Assume it's a string, and return a successful response
                // TODO: is it okay that we use Status.SENT here?
                response = new Response(Status.SENT, object.toString());
            }
        } else if ((result != null) && !(result instanceof Undefined)) {
            // This branch will catch all objects that aren't Response, NativeJavaObject, Undefined, or null
            // Assume it's a string, and return a successful response
            // TODO: is it okay that we use Status.SENT here?
            response = new Response(Status.SENT, result.toString());
        }

        return response;
    }

    public static Response convertToDonkeyResponse(Object response) {
        com.mirth.connect.userutil.Response userResponse = (com.mirth.connect.userutil.Response) response;
        return new Response(convertToDonkeyStatus(userResponse.getStatus()), userResponse.getMessage(), userResponse.getStatusMessage(), userResponse.getError());
    }

    public static Status convertToDonkeyStatus(com.mirth.connect.userutil.Status status) {
        switch (status) {
            case RECEIVED:
                return Status.RECEIVED;
            case FILTERED:
                return Status.FILTERED;
            case TRANSFORMED:
                return Status.TRANSFORMED;
            case SENT:
                return Status.SENT;
            case QUEUED:
                return Status.QUEUED;
            case PENDING:
                return Status.PENDING;
            default:
                return null;
        }
    }

    /**
     * Executes channel level deploy scripts.
     * 
     * @param scriptId
     * @param scriptType
     * @param channelId
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static void executeChannelDeployScript(final String scriptId, final String scriptType, final String channelId) throws InterruptedException, JavaScriptExecutorException {
        try {
            execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());
                    try {
                        Scriptable scope = JavaScriptScopeUtil.getDeployScope(scriptLogger, channelId);
                        JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                        return null;
                    } finally {
                        Context.exit();
                    }
                }
            });
        } catch (JavaScriptExecutorException e) {
            logScriptError(scriptId, channelId, e.getCause());
            throw e;
        }
    }

    /**
     * Executes channel level shutdown scripts.
     * 
     * @param scriptId
     * @param scriptType
     * @param channelId
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static void executeChannelShutdownScript(final String scriptId, final String scriptType, final String channelId) throws InterruptedException, JavaScriptExecutorException {
        try {
            execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());
                    try {
                        Scriptable scope = JavaScriptScopeUtil.getShutdownScope(scriptLogger, channelId);
                        JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                        return null;
                    } finally {
                        Context.exit();
                    }
                }
            });
        } catch (JavaScriptExecutorException e) {
            logScriptError(scriptId, channelId, e.getCause());
            throw e;
        }
    }

    /**
     * Executes global level deploy scripts.
     * 
     * @param scriptId
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static void executeGlobalDeployScript(final String scriptId) throws InterruptedException, JavaScriptExecutorException {
        try {
            execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptId.toLowerCase());
                    try {
                        Scriptable scope = JavaScriptScopeUtil.getDeployScope(scriptLogger);
                        JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                        return null;
                    } finally {
                        Context.exit();
                    }
                }
            });
        } catch (JavaScriptExecutorException e) {
            logScriptError(scriptId, null, e.getCause());
            throw e;
        }
    }

    /**
     * Executes global level shutdown scripts.
     * 
     * @param scriptId
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static void executeGlobalShutdownScript(final String scriptId) throws InterruptedException, JavaScriptExecutorException {
        try {
            execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptId.toLowerCase());
                    try {
                        Scriptable scope = JavaScriptScopeUtil.getShutdownScope(scriptLogger);
                        JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                        return null;
                    } finally {
                        Context.exit();
                    }
                }
            });
        } catch (JavaScriptExecutorException e) {
            logScriptError(scriptId, null, e.getCause());
            throw e;
        }
    }

    /**
     * Logs out a script error with the script type and the script level
     * (channelId or global).
     * 
     * @param scriptType
     * @param channelId
     * @param e
     */
    private static void logScriptError(String scriptType, String channelId, Throwable t) {
        EventController eventController = ControllerFactory.getFactory().createEventController();

        String error = "Error executing " + scriptType + " script from channel: ";

        if (StringUtils.isNotEmpty(channelId)) {
            error += channelId;
        } else {
            error += "Global";
        }

        ServerEvent event = new ServerEvent(error);
        event.setLevel(Level.ERROR);
        event.getAttributes().put(ServerEvent.ATTR_EXCEPTION, ExceptionUtils.getStackTrace(t));
        eventController.dispatchEvent(event);
        logger.error(error, t);
    }

    /**
     * Executes the script with the given scriptId and scope.
     * 
     * @param scriptId
     * @param scope
     * @return
     * @throws Exception
     */
    public static Object executeScript(JavaScriptTask<Object> task, String scriptId, Scriptable scope, String channelId, String connectorName) throws Exception {
        Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

        if (compiledScript == null) {
            return null;
        }

        try {
            logger.debug("executing script: id=" + scriptId);
            return task.executeScript(compiledScript, scope);
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                String script = compiledScriptCache.getSourceScript(scriptId);
                String sourceCode = getSourceCode(script, ((RhinoException) e).lineNumber(), 0);
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, null, sourceCode);
            }

            throw e;
        }
    }

    /*
     * Generates and returns the compiled global scope script.
     */
    public static Script getCompiledGlobalSealedScript(Context context) {
        return compileScript(context, JavaScriptBuilder.generateGlobalSealedScript());
    }

    /*
     * Returns a compiled Script object from a String.
     */
    private static Script compileScript(Context context, String script) {
        return compileScript(context, script, ServerUUIDGenerator.getUUID());
    }

    private static Script compileScript(Context context, String script, String scriptId) {
        return context.compileString(script, scriptId, 1, null);
    }

    public static void compileChannelScripts(Channel channel) throws ScriptCompileException {
        try {
            String deployScriptId = ScriptController.getScriptId(ScriptController.DEPLOY_SCRIPT_KEY, channel.getId());
            String shutdownScriptId = ScriptController.getScriptId(ScriptController.SHUTDOWN_SCRIPT_KEY, channel.getId());
            String preprocessorScriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channel.getId());
            String postprocessorScriptId = ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channel.getId());

            if (channel.isEnabled()) {
                compileAndAddScript(deployScriptId, channel.getDeployScript());
                compileAndAddScript(shutdownScriptId, channel.getShutdownScript());

                // Only compile and run preprocessor if it's not the default
                if (!compileAndAddScript(preprocessorScriptId, channel.getPreprocessingScript())) {
                    logger.debug("removing " + preprocessorScriptId);
                    removeScriptFromCache(preprocessorScriptId);
                }

                // Only compile and run post processor if it's not the default
                if (!compileAndAddScript(postprocessorScriptId, channel.getPostprocessingScript())) {
                    logger.debug("removing " + postprocessorScriptId);
                    removeScriptFromCache(postprocessorScriptId);
                }
            } else {
                removeScriptFromCache(deployScriptId);
                removeScriptFromCache(shutdownScriptId);
                removeScriptFromCache(postprocessorScriptId);
            }
        } catch (Exception e) {
            throw new ScriptCompileException("Failed to compile scripts for channel " + channel.getId() + ".", e);
        }
    }

    public static void compileGlobalScripts(Map<String, String> globalScripts) throws Exception {
        for (Entry<String, String> entry : globalScripts.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                if (!compileAndAddScript(key, value)) {
                    logger.debug("removing global " + key.toLowerCase());
                    removeScriptFromCache(key);
                }
            } catch (Exception e) {
                logger.error("Error compiling global script: " + key, e);
                throw e;
            }
        }
    }

    /*
     * Encapsulates a JavaScript script into the doScript() function,
     * compiles it, and adds it to the compiled script cache.
     */
    public static boolean compileAndAddScript(String scriptId, String script) throws Exception {
        return compileAndAddScript(scriptId, script, null);
    }

    public static boolean compileAndAddScript(String scriptId, String script, Set<String> scriptOptions) throws Exception {
        return compileAndAddScript(scriptId, script, scriptOptions, JavaScriptBuilder.generateDefaultKeyScript(ScriptController.getScriptKey(scriptId), ScriptController.isScriptGlobal(scriptId)));
    }

    public static boolean compileAndAddScript(String scriptId, String script, Set<String> scriptOptions, String defaultScript) throws Exception {
        // Note: If the defaultScript is NULL, this means that the script should
        // always be inserted without being compared.

        boolean scriptInserted = false;
        String generatedScript = null;

        try {
            Context context = JavaScriptScopeUtil.getContext();
            logger.debug("compiling script " + scriptId);
            generatedScript = JavaScriptBuilder.generateScript(script, scriptOptions);
            Script compiledScript = compileScript(context, generatedScript, scriptId);
            String decompiledScript = context.decompileScript(compiledScript, 0);

            String decompiledDefaultScript = null;

            if (defaultScript != null) {
                String generatedDefaultScript = JavaScriptBuilder.generateScript(defaultScript, scriptOptions);
                Script compiledDefaultScript = compileScript(context, generatedDefaultScript, scriptId);
                decompiledDefaultScript = context.decompileScript(compiledDefaultScript, 0);
            }

            if ((defaultScript == null) || !decompiledScript.equals(decompiledDefaultScript)) {
                logger.debug("adding script " + scriptId);
                compiledScriptCache.putCompiledScript(scriptId, compiledScript, generatedScript);
                scriptInserted = true;
            } else {
                compiledScriptCache.removeCompiledScript(scriptId);
            }
        } catch (EvaluatorException e) {
            if (e instanceof RhinoException) {
                String sourceCode = getSourceCode(generatedScript, ((RhinoException) e).lineNumber(), 0);
                MirthJavascriptTransformerException mjte = new MirthJavascriptTransformerException((RhinoException) e, null, null, 0, scriptId, sourceCode);
                throw new Exception(mjte);
            } else {
                throw new Exception(e);
            }
        } finally {
            Context.exit();
        }

        return scriptInserted;
    }

    public static void removeScriptFromCache(String scriptId) {
        if (compiledScriptCache.getCompiledScript(scriptId) != null) {
            compiledScriptCache.removeCompiledScript(scriptId);
        }
    }

    public static void removeChannelScriptsFromCache(String channelId) {
        removeScriptFromCache(ScriptController.getScriptId(ScriptController.DEPLOY_SCRIPT_KEY, channelId));
        removeScriptFromCache(ScriptController.getScriptId(ScriptController.SHUTDOWN_SCRIPT_KEY, channelId));
        removeScriptFromCache(ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId));
        removeScriptFromCache(ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId));
        removeScriptFromCache(ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId));
    }

    /**
     * Utility to get source code from script. Used to generate error report.
     * 
     * @param script
     * @param errorLineNumber
     * @param offset
     * @return
     */
    public static String getSourceCode(String script, int errorLineNumber, int offset) {
        String[] lines = script.split("\n");
        int startingLineNumber = errorLineNumber - offset;

        /*
         * If the starting line number is 5 or less, set it to 6 so that it
         * displays lines 1-11 (0-10 in the array)
         */
        if (startingLineNumber <= SOURCE_CODE_LINE_WRAPPER) {
            startingLineNumber = SOURCE_CODE_LINE_WRAPPER + 1;
        }

        int currentLineNumber = startingLineNumber - SOURCE_CODE_LINE_WRAPPER;
        StringBuilder source = new StringBuilder();

        while ((currentLineNumber < (startingLineNumber + SOURCE_CODE_LINE_WRAPPER)) && (currentLineNumber < lines.length)) {
            source.append(System.getProperty("line.separator") + currentLineNumber + ": " + lines[currentLineNumber - 1]);
            currentLineNumber++;
        }

        return source.toString();
    }

}
