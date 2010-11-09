/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.umo.UMOEventContext;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.SystemEvent;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;

public class JavaScriptUtil {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private static ScriptableObject sealedSharedScope;
    private static final int SOURCE_CODE_LINE_WRAPPER = 5;

    // singleton pattern
    private static JavaScriptUtil instance = null;

    private JavaScriptUtil() {

    }

    public static JavaScriptUtil getInstance() {
        synchronized (JavaScriptUtil.class) {
            if (instance == null)
                instance = new JavaScriptUtil();

            return instance;
        }
    }

    public Context getContext() {
        Context context = Context.enter();
        // MIRTH-1627 - Run in interpreted mode
        context.setOptimizationLevel(-1);

        if (sealedSharedScope == null) {
            String importScript = getJavascriptImportScript();
            sealedSharedScope = new ImporterTopLevel(context);
            JavaScriptScopeUtil.buildScope(sealedSharedScope);
            Script script = context.compileString(importScript, UUIDGenerator.getUUID(), 1, null);
            script.exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        }

        return context;
    }

    public String getJavascriptImportScript() {
        StringBuilder script = new StringBuilder();

        // add #trim() function to JS String
        script.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");

        script.append("importPackage(Packages.com.mirth.connect.server.util);\n");
        script.append("importPackage(Packages.com.mirth.connect.model.converters);\n");
        script.append("regex = new RegExp('');\n");
        script.append("xml = new XML('');\n");
        script.append("xmllist = new XMLList();\n");
        script.append("namespace = new Namespace();\n");
        script.append("qname = new QName();\n");

        /*
         * Ignore whitespace so blank lines are removed when deleting elements.
         * This also involves changing XmlProcessor.java in Rhino to account for
         * Rhino issue 369394 and MIRTH-1405
         */
        script.append("XML.ignoreWhitespace=true;");
        // Setting prettyPrinting to true causes HL7 to break when converting
        // back from HL7.
        script.append("XML.prettyPrinting=false;");

        return script.toString();
    }

    public Scriptable getScope() {
        Scriptable scope = getContext().newObject(sealedSharedScope);
        scope.setPrototype(sealedSharedScope);
        scope.setParentScope(null);
        return scope;
    }

    /**
     * Executes the global and channel preprocessor scripts in order, building
     * up the necessary scope for the global preprocessor and adding the result
     * back to it for the channel preprocessor.
     * 
     */
    public String executePreprocessorScripts(String message, UMOEventContext muleContext, String channelId) {
        String result = message;

        Scriptable scope = getScope();
        Logger scriptLogger = Logger.getLogger(ScriptController.PREPROCESSOR_SCRIPT_KEY.toLowerCase());

        JavaScriptScopeUtil.buildScopeForPreprocessor(scope, message, channelId, muleContext, scriptLogger);

        try {
            // Execute the global preprocessor and check the result
            Object globalResult = executeScript(ScriptController.PREPROCESSOR_SCRIPT_KEY, scope);

            if (globalResult != null) {
                String processedMessage = (String) Context.jsToJava(globalResult, java.lang.String.class);

                if (processedMessage != null) {
                    result = processedMessage;

                    // Put the new message in the scope before the channel
                    // preprocessor is executed
                    scope.put("message", scope, result);
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, null, e);
        }

        try {
            // Execute the channel preprocessor using the result of the global
            // preprocessor (if any)
            Object channelResult = executeScript(channelId + "_" + ScriptController.PREPROCESSOR_SCRIPT_KEY, scope);

            if (channelResult != null) {
                String processedMessage = (String) Context.jsToJava(channelResult, java.lang.String.class);

                if (processedMessage != null) {
                    result = processedMessage;
                }
            }
        } catch (Exception e) {
            logScriptError(ScriptController.PREPROCESSOR_SCRIPT_KEY, channelId, e);
        }

        return result;
    }

    /**
     * Executes the channel postprocessor, followed by the global postprocessor.
     * 
     * @param messageObject
     */
    public void executePostprocessorScripts(MessageObject messageObject) {
        Scriptable scope = getScope();
        Logger scriptLogger = Logger.getLogger(ScriptController.POSTPROCESSOR_SCRIPT_KEY.toLowerCase());
        JavaScriptScopeUtil.buildScope(scope, messageObject, scriptLogger);

        try {
            executeScript(messageObject.getChannelId() + "_" + ScriptController.POSTPROCESSOR_SCRIPT_KEY, scope);
        } catch (Exception e) {
            logScriptError(ScriptController.POSTPROCESSOR_SCRIPT_KEY, messageObject.getChannelId(), e);
        }

        try {
            executeScript(ScriptController.POSTPROCESSOR_SCRIPT_KEY, scope);
        } catch (Exception e) {
            logScriptError(ScriptController.POSTPROCESSOR_SCRIPT_KEY, null, e);
        }
    }

    /**
     * Executes channel level deploy or shutdown scripts.
     * 
     * @param scriptId
     * @param scriptType
     * @param channelId
     */
    public void executeChannelDeployOrShutdownScript(String scriptId, String scriptType, String channelId) {
        try {
            Scriptable scope = getScope();
            Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());
            JavaScriptScopeUtil.buildScope(scope, channelId, scriptLogger);

            executeScript(scriptId, scope);
        } catch (Exception e) {
            logScriptError(scriptId, channelId, e);
        }
    }

    /**
     * Executes global level deploy or shutdown scripts.
     * 
     * @param scriptId
     */
    public void executeGlobalDeployrOrShutdownScript(String scriptId) {
        try {
            Scriptable scope = getScope();
            Logger scriptLogger = Logger.getLogger(scriptId.toLowerCase());
            JavaScriptScopeUtil.buildScope(scope, scriptLogger);

            executeScript(scriptId, scope);
        } catch (Exception e) {
            logScriptError(scriptId, null, e);
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
    private void logScriptError(String scriptType, String channelId, Exception e) {
        EventController systemLogger = ControllerFactory.getFactory().createEventController();

        String error = "Error executing " + scriptType + " script from channel: ";

        if (StringUtils.isNotEmpty(channelId)) {
            error += channelId;
        } else {
            error += "Global";
        }

        SystemEvent event = new SystemEvent(error);
        event.setLevel(SystemEvent.Level.NORMAL);
        event.setDescription(ExceptionUtils.getStackTrace(e));
        systemLogger.logSystemEvent(event);
        logger.error(error, e);
    }

    /**
     * Executes the script with the given scriptId and scope.
     * 
     * @param scriptId
     * @param scope
     * @return
     * @throws Exception
     */
    private Object executeScript(String scriptId, Scriptable scope) throws Exception {
        Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

        if (compiledScript == null) {
            return null;
        }

        try {
            logger.debug("executing script: id=" + scriptId);
            return compiledScript.exec(Context.enter(), scope);
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                String script = compiledScriptCache.getSourceScript(scriptId);
                String sourceCode = JavaScriptUtil.getInstance().getSourceCode(script, ((RhinoException) e).lineNumber(), 0);
                e = new MirthJavascriptTransformerException((RhinoException) e, null, null, 1, null, sourceCode);
            }

            throw e;
        } finally {
            Context.exit();
        }
    }

    public boolean compileAndAddScript(String scriptId, String script, String defaultScript, boolean includeChannelMap, boolean includeGlobalChannelMap, boolean includeMuleContext) throws Exception {
        // Note: If the defaultScript is NULL, this means that the script should
        // always be inserted without being compared.

        Context context = getContext();
        boolean scriptInserted = false;

        try {
            logger.debug("compiling script " + scriptId);
            String generatedScript = generateScript(script, includeChannelMap, includeGlobalChannelMap, includeMuleContext);
            Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
            String decompiledScript = context.decompileScript(compiledScript, 0);

            String decompiledDefaultScript = null;

            if (defaultScript != null) {
                Script compiledDefaultScript = context.compileString(generateScript(defaultScript, includeChannelMap, includeGlobalChannelMap, includeMuleContext), scriptId, 1, null);
                decompiledDefaultScript = context.decompileScript(compiledDefaultScript, 0);
            }

            if ((defaultScript == null) || !decompiledScript.equals(decompiledDefaultScript)) {
                logger.debug("adding script " + scriptId);
                compiledScriptCache.putCompiledScript(scriptId, compiledScript, generatedScript);
                scriptInserted = true;
            }
        } catch (EvaluatorException e) {
            if (e instanceof RhinoException) {
                MirthJavascriptTransformerException mjte = new MirthJavascriptTransformerException((RhinoException) e, null, null, 1, scriptId, null);
                throw new Exception(mjte);
            } else {
                throw new Exception(e);
            }
        } finally {
            Context.exit();
        }

        return scriptInserted;
    }

    public String generateScript(String script, boolean includeChannelMap, boolean includeGlobalChannelMap, boolean includeMuleContext) {
        StringBuilder builtScript = new StringBuilder();
        builtScript.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");
        builtScript.append("function $(string) { ");

        if (includeChannelMap) {
            builtScript.append("if (channelMap.containsKey(string)) { return channelMap.get(string);} else ");
        }

        if (includeGlobalChannelMap) {
            builtScript.append("if (globalChannelMap.containsKey(string)) { return globalChannelMap.get(string);} else ");
        }

        builtScript.append("if (globalMap.containsKey(string)) { return globalMap.get(string);} else ");
        builtScript.append("{ return ''; }}");

        if (includeChannelMap) {
            builtScript.append("function $c(key, value){");
            builtScript.append("if (arguments.length == 1){return channelMap.get(key); }");
            builtScript.append("else if (arguments.length == 2){channelMap.put(key, value); }}");
            builtScript.append("function $co(key, value){");
            builtScript.append("if (arguments.length == 1){return connectorMap.get(key); }");
            builtScript.append("else if (arguments.length == 2){connectorMap.put(key, value); }}");
            builtScript.append("function $r(key, value){");
            builtScript.append("if (arguments.length == 1){return responseMap.get(key); }");
            builtScript.append("else if (arguments.length == 2){responseMap.put(key, value); }}");

            // Helper function to access attachments (returns List<Attachment>)
            builtScript.append("function getAttachments() {");
            builtScript.append("return Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().getAttachmentsByMessage(messageObject);");
            builtScript.append("}");

            // Helper function to set attachment
            builtScript.append("function addAttachment(data, type) {");
            builtScript.append("var attachment = Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().createAttachment(data, type, messageObject);");
            builtScript.append("Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().insertAttachment(attachment); \n");
            builtScript.append("return attachment; }\n");
        }

        if (includeMuleContext) {
            /*
             * If the message object is not available (i.e. in the
             * preprocessor), then add a different version of the addAttachment
             * function that adds the attachment to the mule context for
             * insertion in JavaScriptTransformer#transform.
             */
            builtScript.append("function addAttachment(data, type) {");
            builtScript.append("var attachment = Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().createAttachment(data, type);");
            builtScript.append("muleContext.getProperties().get('attachments').add(attachment); \n");
            builtScript.append("return attachment; }\n");
        }

        if (includeGlobalChannelMap) {
            builtScript.append("function $gc(key, value){");
            builtScript.append("if (arguments.length == 1){return globalChannelMap.get(key); }");
            builtScript.append("else if (arguments.length == 2){globalChannelMap.put(key, value); }}");
        }

        builtScript.append("function $g(key, value){");
        builtScript.append("if (arguments.length == 1){return globalMap.get(key); }");
        builtScript.append("else if (arguments.length == 2){globalMap.put(key, value); }}");

        try {
            for (CodeTemplate template : ControllerFactory.getFactory().createCodeTemplateController().getCodeTemplate(null)) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    if (template.getScope() == CodeTemplate.ContextType.GLOBAL_CONTEXT.getContext() || template.getScope() == CodeTemplate.ContextType.GLOBAL_CHANNEL_CONTEXT.getContext()) {
                        builtScript.append(template.getCode());
                    } else if (includeChannelMap && template.getScope() == CodeTemplate.ContextType.CHANNEL_CONTEXT.getContext()) {
                        builtScript.append(template.getCode());
                    }
                }
            }
        } catch (ControllerException e) {
            logger.error("Could not get user functions.", e);
        }

        builtScript.append("function doScript() {\n" + script + " \n}\n");
        builtScript.append("doScript()\n");
        return builtScript.toString();
    }

    public void removeScriptFromCache(String scriptId) {
        if (compiledScriptCache.getCompiledScript(scriptId) != null) {
            compiledScriptCache.removeCompiledScript(scriptId);
        }
    }

    /**
     * Utility to get source code from script. Used to generate error report.
     * 
     * @param script
     * @param errorLineNumber
     * @param offset
     * @return
     */
    public String getSourceCode(String script, int errorLineNumber, int offset) {
        String[] lines = script.split("\n");
        int startingLineNumber = errorLineNumber - offset;

        if (startingLineNumber < SOURCE_CODE_LINE_WRAPPER) {
            startingLineNumber = SOURCE_CODE_LINE_WRAPPER;
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
