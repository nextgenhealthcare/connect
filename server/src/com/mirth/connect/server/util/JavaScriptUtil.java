/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Event;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptCompileException;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class JavaScriptUtil {
    private static Logger logger = Logger.getLogger(JavaScriptUtil.class);
    private static CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private static JavaScriptExecutor<Object> jsExecutor = new JavaScriptExecutor<Object>();
    private static final int SOURCE_CODE_LINE_WRAPPER = 5;

    public static String executeAttachmentScript(final String message, final String channelId, final List<Attachment> attachments) throws InterruptedException, JavaScriptExecutorException {
        String processedMessage = message;
        Object result = null;

        try {
            result = jsExecutor.execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(ScriptController.ATTACHMENT_SCRIPT_KEY.toLowerCase());
                    Scriptable scope = JavaScriptScopeUtil.getAttachmentScope(scriptLogger, channelId, message, attachments);
                    return JavaScriptUtil.executeScript(this, ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channelId), scope, null, null);
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
     * Executes the global and channel preprocessor scripts in order, building
     * up the necessary scope for the global preprocessor and adding the result
     * back to it for the channel preprocessor.
     * 
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     * 
     */
    public static String executePreprocessorScripts(final ConnectorMessage message, final String channelId) throws InterruptedException, JavaScriptExecutorException {
        if (channelId == null) {
            System.err.println("channelId is null");
        }

        // TODO compare performance of this code compared to having a separate PreProcessorTask class that is only instantiated once
        return (String) jsExecutor.execute(new JavaScriptTask<Object>() {
            @Override
            public Object call() throws Exception {
                String processedMessage = null;
                String globalResult = message.getRaw().getContent();
                Logger scriptLogger = Logger.getLogger(ScriptController.PREPROCESSOR_SCRIPT_KEY.toLowerCase());
                Scriptable scope = JavaScriptScopeUtil.getPreprocessorScope(scriptLogger, channelId, message.getRaw().getContent(), message);
                
                try {
                    // Execute the global preprocessor and check the result
                    Object result = JavaScriptUtil.executeScript(this, ScriptController.PREPROCESSOR_SCRIPT_KEY, scope, null, null);

                    if (result != null) {
                        String resultString = (String) Context.jsToJava(result, java.lang.String.class);

                        // Set the processed message in case something goes wrong in the channel processor. Also update the global result so the channel processor uses the updated message
                        if (resultString != null) {
                            processedMessage = resultString;
                            globalResult = processedMessage;
                        }
                    }
                } catch (Exception e) {
                    logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId, e);
                    throw e;
                }

                // Update the scope with the result from the global processor
                scope = JavaScriptScopeUtil.getPreprocessorScope(scriptLogger, channelId, globalResult, message);

                try {
                    // Execute the channel preprocessor and check the result
                    Object result = JavaScriptUtil.executeScript(this, ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId), scope, null, null);

                    if (result != null) {
                        String resultString = (String) Context.jsToJava(result, java.lang.String.class);

                        // Set the processed message if there was a result.
                        if (resultString != null) {
                            processedMessage = resultString;
                        }
                    }
                } catch (Exception e) {
                    logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId, e);
                    throw e;
                }

                return processedMessage;
            }
        });
    }

    private static Response executePostprocessorScript(JavaScriptTask<Object> task, Message message, String channelId, boolean isGlobal, Response initialResponse) throws Exception {
        Response response = null;

        Logger scriptLogger = Logger.getLogger(ScriptController.POSTPROCESSOR_SCRIPT_KEY.toLowerCase());
        Scriptable scope = null;
        if (!isGlobal) {
            scope = JavaScriptScopeUtil.getPostprocessorScope(scriptLogger, channelId, message);
        } else {
            scope = JavaScriptScopeUtil.getPostprocessorScope(scriptLogger, channelId, message, initialResponse);
        }

        Object result = null;
        try {
            if (!isGlobal) {
                result = executeScript(task, ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId), scope, null, null);
            } else {
                if (compiledScriptCache.getCompiledScript(ScriptController.POSTPROCESSOR_SCRIPT_KEY) == null) {
                    // The script doesn't exist, so assume the global script is the default and use the channel result
                    result = initialResponse;
                } else {
                    result = executeScript(task, ScriptController.POSTPROCESSOR_SCRIPT_KEY, scope, null, null);
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.POSTPROCESSOR_SCRIPT_KEY, channelId, e);
            throw e;
        }

        // Convert result of JavaScript execution to Response object
        if (result instanceof Response) {
            response = (Response) result;
        } else if (result instanceof NativeJavaObject) {
            Object object = ((NativeJavaObject) result).unwrap();

            if (object instanceof Response) {
                response = (Response) object;
            } else {
                // Assume it's a string, and return a successful response
                // TODO: is it okay that we use Status.SENT here?
                response = new Response(Status.SENT, result.toString());
            }
        } else if ((result != null) && !(result instanceof Undefined)) {
            // This branch will catch all objects that aren't Response, NativeJavaObject, Undefined, or null
            // Assume it's a string, and return a successful response
            // TODO: is it okay that we use Status.SENT here?
            response = new Response(Status.SENT, result.toString());
        }

        return response;
    }

    /**
     * Executes the channel postprocessor, followed by the global postprocessor.
     * 
     * @param messageObject
     * @throws InterruptedException
     * @throws JavaScriptExecutorException
     */
    public static Response executePostprocessorScripts(final Message message) throws JavaScriptExecutorException, InterruptedException {
        // TODO compare performance of this code compared to having a separate PostProcessorTask class that is only instantiated once
        return (Response) jsExecutor.execute(new JavaScriptTask<Object>() {
            @Override
            public Object call() throws Exception {
                Response channelResult = executePostprocessorScript(this, message, message.getChannelId(), false, null);
                return executePostprocessorScript(this, message, message.getChannelId(), true, channelResult);
            }
        });
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
            jsExecutor.execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());
                    Scriptable scope = JavaScriptScopeUtil.getDeployScope(scriptLogger, channelId);
                    JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                    return null;
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
            jsExecutor.execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());
                    Scriptable scope = JavaScriptScopeUtil.getShutdownScope(scriptLogger, channelId);
                    JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                    return null;
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
            jsExecutor.execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptId.toLowerCase());
                    Scriptable scope = JavaScriptScopeUtil.getDeployScope(scriptLogger);
                    JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                    return null;
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
            jsExecutor.execute(new JavaScriptTask<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger scriptLogger = Logger.getLogger(scriptId.toLowerCase());
                    Scriptable scope = JavaScriptScopeUtil.getShutdownScope(scriptLogger);
                    JavaScriptUtil.executeScript(this, scriptId, scope, null, null);
                    return null;
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

        Event event = new Event(error);
        event.setLevel(Event.Level.ERROR);
        event.getAttributes().put(Event.ATTR_EXCEPTION, ExceptionUtils.getStackTrace(t));
        eventController.addEvent(event);
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
        } finally {
            Context.exit();
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
        return compileScript(context, script, UUIDGenerator.getUUID());
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

        Context context = JavaScriptScopeUtil.getContext();
        boolean scriptInserted = false;
        String generatedScript = null;

        try {
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
