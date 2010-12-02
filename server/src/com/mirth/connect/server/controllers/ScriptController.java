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
    public static final String GLOBAL_SCRIPT_KEY = "GLOBAL";
    public static final String POSTPROCESSOR_SCRIPT_KEY = "Postprocessor";
    public static final String PREPROCESSOR_SCRIPT_KEY = "Preprocessor";
    public static final String SHUTDOWN_SCRIPT_KEY = "Shutdown";
    public static final String DEPLOY_SCRIPT_KEY = "Deploy";

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

    public abstract void compileChannelScript(Channel channel) throws Exception;
    
    public abstract void compileGlobalScripts();

    // deploy
    
    public abstract void executeGlobalDeployScript();
    
    public abstract void executeChannelDeployScript(String channelId);

    // shutdown
    
    public abstract void executeGlobalShutdownScript();
    
    public abstract void executeChannelShutdownScript(String channelId);
}
