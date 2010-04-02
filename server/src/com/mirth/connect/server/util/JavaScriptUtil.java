/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.SystemEvent;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
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

    public static JavaScriptUtil getInstance() {
        synchronized (JavaScriptUtil.class) {
            if (instance == null)
                instance = new JavaScriptUtil();

            return instance;
        }
    }

    private Context enterContext() {
		Context context = Context.enter();

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

    private String getJavascriptImportScript() {
        StringBuilder script = new StringBuilder();
        script.append("importPackage(Packages.com.mirth.connect.server.util);\n");
        script.append("importPackage(Packages.com.mirth.connect.model.converters);\n");
        script.append("regex = new RegExp('');\n");
        script.append("xml = new XML('');\n");
        script.append("xmllist = new XMLList();\n");
        script.append("namespace = new Namespace();\n");
        script.append("qname = new QName();\n");
        script.append("XML.ignoreWhitespace = false;\n");
        script.append("XML.prettyPrinting = false;\n");
        return script.toString();
    }

    private Scriptable getScope() {
        Scriptable scope = enterContext().newObject(sealedSharedScope);
        scope.setPrototype(sealedSharedScope);
        scope.setParentScope(null);
        return scope;
    }

    public void executeScript(String scriptId, String scriptType, MessageObject messageObject) {
        try {
            executeScript(scriptId, scriptType, null, messageObject);
        } catch (Exception e) {
            logger.error("Error executing " + scriptType + " script.", e);
        }
    }

    public void executeScript(String scriptId, String scriptType, String channelId) {
        try {
            executeScript(scriptId, scriptType, channelId, null);
        } catch (Exception e) {
            EventController systemLogger = ControllerFactory.getFactory().createEventController();
            SystemEvent event = new SystemEvent("Exception occured in " + scriptType + " script");
            event.setLevel(SystemEvent.Level.NORMAL);
            event.setDescription(StackTracePrinter.stackTraceToString(e));
            systemLogger.logSystemEvent(event);
            logger.error("Error executing " + scriptType + " script from channel: " + channelId, e);
        }
    }

    private void executeScript(String scriptId, String scriptType, String channelId, MessageObject messageObject) throws Exception {
        Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);
        Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());

        if (compiledScript == null) {
            return;
        }

        try {
            Context context = enterContext();
            Scriptable scope = getScope();

            if (messageObject != null) {
                JavaScriptScopeUtil.buildScope(scope, messageObject, scriptLogger);
            } else if ((channelId != null) && (channelId.length() > 0)) {
                JavaScriptScopeUtil.buildScope(scope, channelId, scriptLogger);
            } else {
                JavaScriptScopeUtil.buildScope(scope, scriptLogger);
            }

            logger.debug("executing " + scriptType + " script. id=" + scriptId + ", channelId=" + channelId);
            compiledScript.exec(context, scope);
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                String connectorName = null;
                
                if (messageObject != null) {
                    connectorName = messageObject.getConnectorName();
                }
                
                ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
                String script = scriptController.getScript(channelId, scriptId);
                String sourceCode = JavaScriptUtil.getSourceCode(script, ((RhinoException) e).lineNumber(), 0);
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 1, scriptType, sourceCode);
            }
            
            throw e;
        } finally {
            Context.exit();
        }
    }

    public boolean compileAndAddScript(String scriptId, String script, String defaultScript, boolean includeChannelMap, boolean includeGlobalChannelMap) throws Exception {
        // Note: If the defaultScript is NULL, this means that the script should
        // always be inserted without being compared.

        Context context = enterContext();
        boolean scriptInserted = false;

        try {
            logger.debug("compiling script " + scriptId);
            String generatedScript = generateScript(script, includeChannelMap, includeGlobalChannelMap);
            Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
            String decompiledScript = context.decompileScript(compiledScript, 0);

            String decompiledDefaultScript = null;

            if (defaultScript != null) {
                Script compiledDefaultScript = context.compileString(generateScript(defaultScript, includeChannelMap, includeGlobalChannelMap), scriptId, 1, null);
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

    public String generateScript(String script, boolean includeChannelMap, boolean includeGlobalChannelMap) {
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
            builtScript.append("return Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().getAttachmentsByMessageId(messageObject.getId());");
            builtScript.append("}");

            // Helper function to set attachment
            builtScript.append("function addAttachment(data, type) {");
            builtScript.append("var attachment = Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().createAttachment(data, type, messageObject);");
            builtScript.append("Packages.com.mirth.connect.server.controllers.MessageObjectController.getInstance().insertAttachment(attachment); \n");
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
    
    // utility to get source code from script. Used to generate error report.
    public static String getSourceCode(String script, int errorLineNumber, int offset){
        String[] lines = script.split("\n");
        int startingLineNumber = errorLineNumber - offset;
        
        if (startingLineNumber < SOURCE_CODE_LINE_WRAPPER){
            startingLineNumber = SOURCE_CODE_LINE_WRAPPER;
        }
        
        int currentLineNumber = startingLineNumber - SOURCE_CODE_LINE_WRAPPER;
        StringBuilder source = new StringBuilder();
        
        while ((currentLineNumber < (startingLineNumber + SOURCE_CODE_LINE_WRAPPER)) && (currentLineNumber < lines.length)){
        	source.append(System.getProperty("line.separator") + currentLineNumber + ": " + lines[currentLineNumber - 1]);
            currentLineNumber++;
        }
        
        return source.toString();
    }

}
