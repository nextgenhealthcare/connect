/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.util;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.webreach.mirth.model.CodeTemplate;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.CodeTemplate.CodeSnippetType;
import com.webreach.mirth.server.MirthJavascriptTransformerException;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.EventController;

public class JavaScriptUtil {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private static ScriptableObject sealedSharedScope;

    // singleton pattern
    private static JavaScriptUtil instance = null;

    public static JavaScriptUtil getInstance() {
        synchronized (JavaScriptUtil.class) {
            if (instance == null)
                instance = new JavaScriptUtil();

            return instance;
        }
    }

    public static Context enterContext() {
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

    public static String getJavascriptImportScript() {
        StringBuilder script = new StringBuilder();
        script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
        script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
        script.append("regex = new RegExp('');\n");
        script.append("xml = new XML('');\n");
        script.append("xmllist = new XMLList();\n");
        script.append("namespace = new Namespace();\n");
        script.append("qname = new QName();\n");
        script.append("XML.ignoreWhitespace=false;");
        script.append("XML.prettyPrinting=false;");
        return script.toString();
    }

    public Scriptable getScope() {
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
            logger.error("Error executing " + scriptType + " script.", e);
        }
    }

    private void executeScript(String scriptId, String scriptType, String channelId, MessageObject messageObject) throws Exception {
        Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);
        Logger scriptLogger = Logger.getLogger(scriptType.toLowerCase());

        if (compiledScript == null)
            return;

        try {
            Context context = enterContext();
            Scriptable scope = getScope();

            if (messageObject != null)
                JavaScriptScopeUtil.buildScope(scope, messageObject, scriptLogger);
            else if (channelId != null && channelId.length() > 0)
                JavaScriptScopeUtil.buildScope(scope, channelId, scriptLogger);
            else
                JavaScriptScopeUtil.buildScope(scope, scriptLogger);

            logger.debug("executing " + scriptType + " script. id=" + scriptId);
            compiledScript.exec(context, scope);
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                String connectorName = null;
                if (messageObject != null) {
                    connectorName = messageObject.getConnectorName();
                }
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 1, scriptType);
            }
            throw e;
        } finally {
            Context.exit();
        }
    }

    public boolean compileAndAddScript(String scriptId, String script, String defaultScript, boolean includeChannelMap) throws Exception {
        // Note: If the defaultScript is NULL, this means that the script should
        // always be inserted without being compared.

        Context context = enterContext();
        boolean scriptInserted = false;

        try {
            logger.debug("compiling script " + scriptId);
            String generatedScript = generateScript(script, includeChannelMap);
            Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
            String decompiledScript = context.decompileScript(compiledScript, 0);

            String decompiledDefaultScript = null;

            if (defaultScript != null) {
                Script compiledDefaultScript = context.compileString(generateScript(defaultScript, includeChannelMap), scriptId, 1, null);
                decompiledDefaultScript = context.decompileScript(compiledDefaultScript, 0);
            }

            if ((defaultScript == null) || !decompiledScript.equals(decompiledDefaultScript)) {
                logger.debug("adding script " + scriptId);
                compiledScriptCache.putCompiledScript(scriptId, compiledScript);
                scriptInserted = true;
            }
        } catch (EvaluatorException e) {
            if (e instanceof RhinoException) {
                MirthJavascriptTransformerException mjte = new MirthJavascriptTransformerException((RhinoException) e, null, null, 1, scriptId);
                throw new Exception(mjte);
            } else {
                throw new Exception(e);
            }
        } finally {
            Context.exit();
        }

        return scriptInserted;
    }

    public String generateScript(String script, boolean includeChannelMap) {
        StringBuilder builtScript = new StringBuilder();

        builtScript.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");

        builtScript.append("function $(string) { ");
        if (includeChannelMap) {
            builtScript.append("if (channelMap.containsKey(string)) { return channelMap.get(string);} else ");
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
            builtScript.append("return Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().getAttachmentsByMessageId(messageObject.getId());");
            builtScript.append("}");

            // Helper function to set attachment
            builtScript.append("function addAttachment(data, type) {");
            builtScript.append("var attachment = Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().createAttachment(data, type, messageObject);");
            builtScript.append("Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().insertAttachment(attachment); \n");
            builtScript.append("return attachment; }\n");
        }
        builtScript.append("function $g(key, value){");
        builtScript.append("if (arguments.length == 1){return globalMap.get(key); }");
        builtScript.append("else if (arguments.length == 2){globalMap.put(key, value); }}");

        try {
            for (CodeTemplate template : ControllerFactory.getFactory().createCodeTemplateController().getCodeTemplate(null)) {
                if (template.getType() == CodeSnippetType.FUNCTION) {
                    if (template.getScope() == CodeTemplate.ContextType.GLOBAL_CONTEXT.getContext()) {
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
        if (compiledScriptCache.getCompiledScript(scriptId) != null)
            compiledScriptCache.removeCompiledScript(scriptId);
    }
}
