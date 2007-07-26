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
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptUtil
{
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
    
    public static Context getContext()
    {
        Context context = Context.enter();

        if (sealedSharedScope == null)
        {
            String importScript = getJavascriptImportScript();
            sealedSharedScope = new ImporterTopLevel(context);
            JavaScriptScopeUtil.buildScope(sealedSharedScope);
            Script script = context.compileString(importScript, UUIDGenerator.getUUID(), 1, null);
            script.exec(context, sealedSharedScope);
            sealedSharedScope.sealObject();
        }

        return context;
    }

    public static String getJavascriptImportScript()
    {
        StringBuilder script = new StringBuilder();
        script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
        script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
        return script.toString();

    }

    public Scriptable getScope()
    {
        Scriptable scope = getContext().newObject(sealedSharedScope);
        scope.setPrototype(sealedSharedScope);
        scope.setParentScope(null);
        return scope;
    }

    public void executeScript(String scriptId, String scriptType, String channelId)
    {
        Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);
        
        if(compiledScript == null)
            return;

        try
        {
            
            Context context = getContext();

            Scriptable scope = getScope();
            
            if(channelId != null)
                JavaScriptScopeUtil.buildScope(scope, channelId, logger);
            else
                JavaScriptScopeUtil.buildScope(scope, logger);
            
            logger.debug("executing " + scriptType + " script. id=" + scriptId);    
            compiledScript.exec(context, scope);
        }
        catch (Exception e)
        {
            logger.error("failure to execute: " +  scriptType + " script. id=" + scriptId, e);
        }
        finally
        {
            Context.exit();
        }
    }
    
    public void compileScript(String scriptId, String script)
    {
        Context context = getContext();
        logger.debug("compiling script. id=" + scriptId);
        String generatedScript = generateDeployScript(script);
        Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
        compiledScriptCache.putCompiledScript(scriptId, compiledScript);
        Context.exit();
    }

    public String generateDeployScript(String script)
    {
        StringBuilder builtScript = new StringBuilder();
        builtScript.append("function doScript() {" + script + " }\n");
        builtScript.append("doScript()\n");
        return builtScript.toString();
    }
    
    public void removeScriptFromCache(String scriptId)
    {
        if(compiledScriptCache.getCompiledScript(scriptId) != null)
            compiledScriptCache.removeCompiledScript(scriptId);
    }
}
