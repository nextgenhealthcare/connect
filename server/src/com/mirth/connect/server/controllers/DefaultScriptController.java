/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.builders.JavaScriptBuilder;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.StatementLock;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DefaultScriptController extends ScriptController {
    private static final String VACUUM_LOCK_SCRIPT_STATEMENT_ID = "Script.vacuumScriptTable";

    private Logger logger = Logger.getLogger(this.getClass());
    private static ScriptController instance = null;

    protected DefaultScriptController() {

    }

    public static ScriptController create() {
        synchronized (DefaultScriptController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ScriptController.class);

                if (instance == null) {
                    instance = new DefaultScriptController();
                }
            }

            return instance;
        }
    }

    /**
     * Adds a script with the specified id to the database. If a script with the id already exists
     * it will be overwritten.
     * 
     * @param id
     * @param script
     * @throws ControllerException
     */
    @Override
    public void putScript(String groupId, String id, String script) throws ControllerException {
        logger.debug("adding script: groupId=" + groupId + ", id=" + id);

        StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).readLock();
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);
            parameterMap.put("script", script);

            if (getScript(groupId, id) == null) {
                SqlConfig.getSqlSessionManager().insert("Script.insertScript", parameterMap);
            } else {
                SqlConfig.getSqlSessionManager().update("Script.updateScript", parameterMap);
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).readUnlock();
        }
    }

    /**
     * Returns the script with the specified id, null otherwise.
     * 
     * @param id
     * @return
     * @throws ControllerException
     */
    @Override
    public String getScript(String groupId, String id) throws ControllerException {
        logger.debug("retrieving script: groupId=" + groupId + ", id=" + id);

        StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).readLock();
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("groupId", groupId);
            parameterMap.put("id", id);
            return (String) SqlConfig.getSqlSessionManager().selectOne("Script.getScript", parameterMap);
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).readUnlock();
        }
    }

    @Override
    public void removeScripts(String groupId) throws ControllerException {
        logger.debug("deleting scripts: groupId=" + groupId);

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("groupId", groupId);

        StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).writeLock();
        try {
            SqlConfig.getSqlSessionManager().delete("Script.deleteScript", parameterMap);

            if (DatabaseUtil.statementExists("Script.vacuumScriptTable")) {
                vacuumScriptTable();
            }
        } catch (PersistenceException e) {
            throw new ControllerException("Error deleting scripts", e);
        } finally {
            StatementLock.getInstance(VACUUM_LOCK_SCRIPT_STATEMENT_ID).writeUnlock();
        }
    }

    /**
     * When calling this method, a StatementLock writeLock should surround it
     */
    public void vacuumScriptTable() {
        SqlSession session = null;
        try {
            session = SqlConfig.getSqlSessionManager().openSession(false);
            if (DatabaseUtil.statementExists("Script.lockScriptTable")) {
                session.update("Script.lockScriptTable");
            }
            session.update("Script.vacuumScriptTable");
            session.commit();
        } catch (Exception e) {
            logger.error("Could not compress Script table", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Non-database actions
    @Override
    public void compileGlobalScripts(MirthContextFactory contextFactory, boolean multiServer) {
        Map<String, String> globalScripts = null;

        try {
            globalScripts = getGlobalScripts();
        } catch (ControllerException e) {
            logger.error("Error getting global scripts.", e);
            return;
        }

        try {
            JavaScriptUtil.compileGlobalScripts(contextFactory, globalScripts);
        } catch (Exception e) {
            logger.error("Error compiling global scripts.", e);
        }
    }

    @Override
    public void compileChannelScripts(MirthContextFactory contextFactory, Channel channel) throws ScriptCompileException {
        JavaScriptUtil.compileChannelScripts(contextFactory, channel);
    }

    @Override
    public void removeChannelScriptsFromCache(String channelId) {
        JavaScriptUtil.removeChannelScriptsFromCache(channelId);
    }

    @Override
    public void executeGlobalDeployScript() throws Exception {
        JavaScriptUtil.executeGlobalDeployScript(DEPLOY_SCRIPT_KEY);
    }

    @Override
    public void executeChannelDeployScript(MirthContextFactory contextFactory, String channelId, String channelName) throws Exception {
        JavaScriptUtil.executeChannelDeployScript(contextFactory, getScriptId(DEPLOY_SCRIPT_KEY, channelId), DEPLOY_SCRIPT_KEY, channelId, channelName);
    }

    @Override
    public void executeGlobalUndeployScript() throws Exception {
        JavaScriptUtil.executeGlobalUndeployScript(UNDEPLOY_SCRIPT_KEY);
    }

    @Override
    public void executeChannelUndeployScript(MirthContextFactory contextFactory, String channelId, String channelName) throws Exception {
        JavaScriptUtil.executeChannelUndeployScript(contextFactory, getScriptId(UNDEPLOY_SCRIPT_KEY, channelId), UNDEPLOY_SCRIPT_KEY, channelId, channelName);
    }

    @Override
    public Map<String, String> getGlobalScripts() throws ControllerException {
        Map<String, String> scripts = new HashMap<String, String>();

        String deployScript = getScript(GLOBAL_GROUP_ID, DEPLOY_SCRIPT_KEY);
        String undeployScript = getScript(GLOBAL_GROUP_ID, UNDEPLOY_SCRIPT_KEY);
        String preprocessorScript = getScript(GLOBAL_GROUP_ID, PREPROCESSOR_SCRIPT_KEY);
        String postprocessorScript = getScript(GLOBAL_GROUP_ID, POSTPROCESSOR_SCRIPT_KEY);

        if (StringUtils.isBlank(deployScript)) {
            deployScript = JavaScriptBuilder.generateDefaultKeyScript(DEPLOY_SCRIPT_KEY);
        }

        if (StringUtils.isBlank(undeployScript)) {
            undeployScript = JavaScriptBuilder.generateDefaultKeyScript(UNDEPLOY_SCRIPT_KEY);
        }

        if (StringUtils.isBlank(preprocessorScript)) {
            preprocessorScript = JavaScriptBuilder.generateDefaultKeyScript(PREPROCESSOR_SCRIPT_KEY);
        }

        if (StringUtils.isBlank(postprocessorScript)) {
            postprocessorScript = JavaScriptBuilder.generateDefaultKeyScript(POSTPROCESSOR_SCRIPT_KEY);
        }

        scripts.put(DEPLOY_SCRIPT_KEY, deployScript);
        scripts.put(UNDEPLOY_SCRIPT_KEY, undeployScript);
        scripts.put(PREPROCESSOR_SCRIPT_KEY, preprocessorScript);
        scripts.put(POSTPROCESSOR_SCRIPT_KEY, postprocessorScript);

        return scripts;
    }

    @Override
    public void setGlobalScripts(Map<String, String> scripts) throws ControllerException {
        putGlobalScriptsToDB(scripts);

        try {
            compileGlobalScripts(ControllerFactory.getFactory().createContextFactoryController().getGlobalScriptContextFactory(), true);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    protected void putGlobalScriptsToDB(Map<String, String> scripts) throws ControllerException {
        for (Entry<String, String> entry : scripts.entrySet()) {
            putScript(GLOBAL_GROUP_ID, entry.getKey().toString(), scripts.get(entry.getKey()));
        }
    }
}
