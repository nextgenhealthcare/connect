/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultScriptController extends ScriptController {
    private static final String CHANNEL_POSTPROCESSOR_DEFAULT_SCRIPT = "// This script executes once after a message has been processed\nreturn;";
    private static final String CHANNEL_PREPROCESSOR_DEFAULT_SCRIPT = "// Modify the message variable below to pre process data\nreturn message;";
    private static final String GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT = "// Modify the message variable below to pre process data\n// This script applies across all channels\nreturn message;";
    private static final String GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT = "// This script executes once after a message has been processed\n// This script applies across all channels\nreturn;";
    private static final String GLOBAL_DEPLOY_DEFAULT_SCRIPT = "// This script executes once when all channels start up from a redeploy\n// You only have access to the globalMap here to persist data\nreturn;";
    private static final String GLOBAL_SHUTDOWN_DEFAULT_SCRIPT = "// This script executes once when all channels shut down from a redeploy\n// You only have access to the globalMap here to persist data\nreturn;";

    private Logger logger = Logger.getLogger(this.getClass());
    private static DefaultScriptController instance = null;
    private JavaScriptUtil javaScriptUtil = JavaScriptUtil.getInstance();

    private DefaultScriptController() {

    }

    public static ScriptController create() {
        synchronized (DefaultScriptController.class) {
            if (instance == null) {
                instance = new DefaultScriptController();
            }

            return instance;
        }
    }

    /**
     * Adds a script with the specified id to the database. If a script with the
     * id already exists it will be overwritten.
     * 
     * @param id
     * @param script
     * @throws ControllerException
     */
    public void putScript(String groupId, String id, String script) throws ControllerException {
        logger.debug("adding script: groupId=" + groupId + ", id=" + id);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);
            parameterMap.put("script", script);

            if (getScript(groupId, id) == null) {
                SqlConfig.getSqlMapClient().insert("Script.insertScript", parameterMap);
            } else {
                SqlConfig.getSqlMapClient().update("Script.updateScript", parameterMap);
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Returns the script with the specified id, null otherwise.
     * 
     * @param id
     * @return
     * @throws ControllerException
     */
    public String getScript(String groupId, String id) throws ControllerException {
        logger.debug("retrieving script: groupId=" + groupId + ", id=" + id);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);
            return (String) SqlConfig.getSqlMapClient().queryForObject("Script.getScript", parameterMap);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeScripts(String groupId) throws ControllerException {
        logger.debug("deleting scripts: groupId=" + groupId);

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("groupId", groupId);

        try {
            SqlConfig.getSqlMapClient().delete("Script.deleteScript", parameterMap);
        } catch (SQLException e) {
            throw new ControllerException("Error deleting scripts", e);
        }
    }

    public void removeAllExceptGlobalScripts() throws ControllerException {
        logger.debug("clearing scripts table");

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("notGroupId", GLOBAL_SCRIPT_KEY);

        try {
            SqlConfig.getSqlMapClient().delete("Script.deleteScript", parameterMap);

            if (DatabaseUtil.statementExists("Script.vacuumScriptTable")) {
                SqlConfig.getSqlMapClient().update("Script.vacuumScriptTable");
            }
        } catch (SQLException e) {
            throw new ControllerException("Error clearing scripts", e);
        }
    }

    // Non-database actions

    public void compileChannelScript(Channel channel) throws Exception {
        if (channel.isEnabled()) {
            javaScriptUtil.compileAndAddScript(channel.getId() + "_Deploy", channel.getDeployScript(), null, false, true, false);
            javaScriptUtil.compileAndAddScript(channel.getId() + "_Shutdown", channel.getShutdownScript(), null, false, true, false);

            // only compile and run pre processor if it's not the default
            if (!javaScriptUtil.compileAndAddScript(channel.getId() + "_Preprocessor", channel.getPreprocessingScript(), CHANNEL_PREPROCESSOR_DEFAULT_SCRIPT, false, true, true)) {
                logger.debug("removing " + channel.getId() + "_Preprocessor");
                javaScriptUtil.removeScriptFromCache(channel.getId() + "_Preprocessor");
            }

            // only compile and run post processor if it's not the default
            if (!javaScriptUtil.compileAndAddScript(channel.getId() + "_Postprocessor", channel.getPostprocessingScript(), CHANNEL_POSTPROCESSOR_DEFAULT_SCRIPT, true, true, false)) {
                logger.debug("removing " + channel.getId() + "_Postprocessor");
                javaScriptUtil.removeScriptFromCache(channel.getId() + "_Postprocessor");
            }
        } else {
            javaScriptUtil.removeScriptFromCache(channel.getId() + "_Deploy");
            javaScriptUtil.removeScriptFromCache(channel.getId() + "_Shutdown");
            javaScriptUtil.removeScriptFromCache(channel.getId() + "_Postprocessor");
        }
    }

    public void compileGlobalScripts() {
        Map<String, String> globalScripts = null;

        try {
            globalScripts = getGlobalScripts();
        } catch (ControllerException e) {
            logger.error("Error getting global scripts.", e);
            return;
        }

        for (Entry<String, String> entry : globalScripts.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                if (key.equals(PREPROCESSOR_SCRIPT_KEY)) {
                    if (!javaScriptUtil.compileAndAddScript(key, value, GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT, false, true, true)) {
                        logger.debug("removing global preprocessor");
                        javaScriptUtil.removeScriptFromCache(PREPROCESSOR_SCRIPT_KEY);
                    }
                } else if (key.equals(POSTPROCESSOR_SCRIPT_KEY)) {
                    if (!javaScriptUtil.compileAndAddScript(key, value, GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT, true, true, false)) {
                        logger.debug("removing global postprocessor");
                        javaScriptUtil.removeScriptFromCache(POSTPROCESSOR_SCRIPT_KEY);
                    }
                } else {
                    // add the DEPLOY and SHUTDOWN scripts,
                    // which do not have defaults
                    if (!javaScriptUtil.compileAndAddScript(key, value, "", false, false, false)) {
                        logger.debug("remvoing " + key);
                        javaScriptUtil.removeScriptFromCache(key);
                    }
                }
            } catch (Exception e) {
                logger.error("Error compiling global script: " + key, e);
            }
        }
    }

    public void executeGlobalDeployScript() {
        javaScriptUtil.executeGlobalDeployOrShutdownScript(DEPLOY_SCRIPT_KEY);
    }

    public void executeChannelDeployScript(String channelId) {
        javaScriptUtil.executeChannelDeployOrShutdownScript(channelId + "_" + DEPLOY_SCRIPT_KEY, DEPLOY_SCRIPT_KEY, channelId);
    }

    public void executeGlobalShutdownScript() {
        javaScriptUtil.executeGlobalDeployOrShutdownScript(SHUTDOWN_SCRIPT_KEY);
    }

    public void executeChannelShutdownScript(String channelId) {
        javaScriptUtil.executeChannelDeployOrShutdownScript(channelId + "_" + SHUTDOWN_SCRIPT_KEY, SHUTDOWN_SCRIPT_KEY, channelId);
    }

    public Map<String, String> getGlobalScripts() throws ControllerException {
        Map<String, String> scripts = new HashMap<String, String>();

        String deployScript = getScript(GLOBAL_SCRIPT_KEY, DEPLOY_SCRIPT_KEY);
        String shutdownScript = getScript(GLOBAL_SCRIPT_KEY, SHUTDOWN_SCRIPT_KEY);
        String preprocessorScript = getScript(GLOBAL_SCRIPT_KEY, PREPROCESSOR_SCRIPT_KEY);
        String postprocessorScript = getScript(GLOBAL_SCRIPT_KEY, POSTPROCESSOR_SCRIPT_KEY);

        if ((deployScript == null) || deployScript.equals("")) {
            deployScript = GLOBAL_DEPLOY_DEFAULT_SCRIPT;
        }

        if ((shutdownScript == null) || shutdownScript.equals("")) {
            shutdownScript = GLOBAL_SHUTDOWN_DEFAULT_SCRIPT;
        }

        if ((preprocessorScript == null) || preprocessorScript.equals("")) {
            preprocessorScript = GLOBAL_PREPROCESSOR_DEFAULT_SCRIPT;
        }

        if ((postprocessorScript == null) || postprocessorScript.equals("")) {
            postprocessorScript = GLOBAL_POSTPROCESSOR_DEFAULT_SCRIPT;
        }

        scripts.put(DEPLOY_SCRIPT_KEY, deployScript);
        scripts.put(SHUTDOWN_SCRIPT_KEY, shutdownScript);
        scripts.put(PREPROCESSOR_SCRIPT_KEY, preprocessorScript);
        scripts.put(POSTPROCESSOR_SCRIPT_KEY, postprocessorScript);

        return scripts;
    }

    public void setGlobalScripts(Map<String, String> scripts) throws ControllerException {
        for (Entry<String, String> entry : scripts.entrySet()) {
            putScript(GLOBAL_SCRIPT_KEY, entry.getKey().toString(), scripts.get(entry.getKey()));
        }

        compileGlobalScripts();
    }
}
