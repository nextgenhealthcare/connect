/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Map;

import com.mirth.connect.model.Channel;

public abstract class ScriptController extends Controller {
    // Static group IDs
    public static final String GLOBAL_GROUP_ID = "Global";

    // Static script keys
    public static final String ATTACHMENT_SCRIPT_KEY = "Attachment";
    public static final String POSTPROCESSOR_SCRIPT_KEY = "Postprocessor";
    public static final String PREPROCESSOR_SCRIPT_KEY = "Preprocessor";
    public static final String SHUTDOWN_SCRIPT_KEY = "Shutdown";
    public static final String DEPLOY_SCRIPT_KEY = "Deploy";

    public static final String DELIMITER = "_";

    public static String getScriptId(String scriptKey) {
        return getScriptId(scriptKey, GLOBAL_GROUP_ID);
    }

    public static String getScriptId(String scriptKey, String groupId) {
        if (groupId != null && !groupId.equals(GLOBAL_GROUP_ID)) {
            return groupId + DELIMITER + scriptKey;
        } else {
            return scriptKey;
        }
    }

    public static String getGroupId(String scriptId) {
        if (scriptId.contains(DELIMITER)) {
            return scriptId.substring(0, scriptId.indexOf(DELIMITER));
        } else {
            return GLOBAL_GROUP_ID;
        }
    }

    public static String getScriptKey(String scriptId) {
        if (scriptId.contains(DELIMITER)) {
            return scriptId.substring(scriptId.indexOf(DELIMITER) + 1);
        } else {
            return scriptId;
        }
    }

    public static boolean isScriptGlobal(String scriptId) {
        return getGroupId(scriptId).equals(GLOBAL_GROUP_ID);
    }

    public static ScriptController getInstance() {
        return ControllerFactory.getFactory().createScriptController();
    }

    /**
     * Adds a script with the specified groupId and id to the database. If a
     * script with the id already exists it will be overwritten.
     * 
     * @param groupId
     * @param id
     * @param script
     * @throws ControllerException
     */
    public abstract void putScript(String groupId, String id, String script) throws ControllerException;

    /**
     * Returns the script with the specified id, null otherwise.
     * 
     * @param groupId
     * @param id
     * @return
     * @throws ControllerException
     */
    public abstract String getScript(String groupId, String id) throws ControllerException;

    public abstract void removeScripts(String groupId) throws ControllerException;

    public abstract void removeAllExceptGlobalScripts() throws ControllerException;

    // Non-database actions

    public abstract Map<String, String> getGlobalScripts() throws ControllerException;

    public abstract void setGlobalScripts(Map<String, String> scripts) throws ControllerException;

    public abstract void compileGlobalScripts();

    public abstract void compileChannelScripts(Channel channel) throws ScriptCompileException;

    public abstract void removeChannelScriptsFromCache(String channelId);

    // Deploy Script Execution

    public abstract void executeGlobalDeployScript();

    public abstract void executeChannelDeployScript(String channelId) throws Exception;

    // Shutdown Script Execution

    public abstract void executeGlobalShutdownScript() throws InterruptedException;

    public abstract void executeChannelShutdownScript(String channelId) throws Exception;
}
