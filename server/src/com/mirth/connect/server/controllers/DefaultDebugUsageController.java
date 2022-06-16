/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.server.util.StatementLock;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public class DefaultDebugUsageController extends DebugUsageController {

    private static final String VACUUM_LOCK_ID = "DebugUsage.vacuumDebuggerUsageTable";
	private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    // singleton pattern
    private static DebugUsageController instance = null;

    public DefaultDebugUsageController() {

    }

    public static DebugUsageController create() {
        synchronized (DefaultDebugUsageController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(DebugUsageController.class);
                if (instance == null) {
                    instance = new DefaultDebugUsageController();
                    ((DefaultDebugUsageController) instance).initialize();
                } else {
                    try {
                        instance.getClass().getMethod("initialize").invoke(instance);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultDebugUsageController.class).error("Error calling initialize method in DefaultConfigurationController", e);
                    }
                }
            }
            return instance;
        }
    }

    public void initialize() {

    }

    public HashMap<String, Object> getDebugUsageMap(DebugUsage debugUsage) {
        HashMap<String, Object> map = new HashMap<>();
        if (debugUsage!=null) {
            map.put("serverId", debugUsage.getServerId());
            map.put("duppCount", debugUsage.getDuppCount());
            map.put("attachBatchCount", debugUsage.getAttachBatchCount());
            map.put("sourceConnectorCount", debugUsage.getSourceConnectorCount());
            map.put("sourceFilterTransCount", debugUsage.getSourceFilterTransCount());
            map.put("destinationFilterTransCount", debugUsage.getDestinationFilterTransCount());
            map.put("destinationConnectorCount", debugUsage.getDestinationConnectorCount());
            map.put("responseCount", debugUsage.getResponseCount());
            map.put("invocationCount", debugUsage.getInvocationCount());
        }
        return map;
    }

 
    public synchronized boolean upsertDebugUsage(DebugUsage debugUsage) throws ControllerException {

        StatementLock.getInstance(VACUUM_LOCK_ID).readLock();
        try {

            DebugUsage persistedDebugUsage = getDebugUsage(configurationController.getServerId());

            // if server id record exists, update current record
            if (persistedDebugUsage != null) {
                logger.debug("updating debug usage statistics for serverId" + debugUsage.getServerId());
                SqlConfig.getInstance().getSqlSessionManager().update("DebugUsage.updateDebugUsageStatistics", getDebugUsageMap(debugUsage));
                return true;
            // otherwise, insert a new record
            } else {
                
                logger.debug("inserting debug usage statistics for serverId" + debugUsage.getServerId());
                SqlConfig.getInstance().getSqlSessionManager().insert("DebugUsage.insertDebugUsageStatistics", getDebugUsageMap(debugUsage));
                return true;
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
        StatementLock.getInstance(VACUUM_LOCK_ID).readUnlock();
        }
    }

    // returns debug usage for each server id
    public DebugUsage getDebugUsage(String serverId) throws ControllerException {

        logger.debug("getting debug usage for serverId: " + serverId);

        if (serverId == null) {
            throw new ControllerException("Error getting usage for serverId: serverId cannot be null.");
        }

        StatementLock.getInstance(VACUUM_LOCK_ID).readLock();
        try {
            DebugUsage debugUsage = new DebugUsage();
            debugUsage.setServerId(serverId);

            return getReadOnlySqlSessionManager().selectOne("DebugUsage.getDebugUsageStatistics", debugUsage);

        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
        StatementLock.getInstance(VACUUM_LOCK_ID).readUnlock();
        }
    }
    
    // returns object for all server id's in the debugger_usage table
    public List<DebugUsage> getDebugUsages() throws ControllerException {

        logger.debug("getting debug usage for all serverIds");
        
        StatementLock.getInstance(VACUUM_LOCK_ID).readLock();
        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<Map<String, Object>> rows = SqlConfig.getInstance().getReadOnlySqlSessionManager().selectList("DebugUsage.getDebugUsageStatistics", null);
            List<DebugUsage> debugUsages = new ArrayList<DebugUsage>();

            for (Map<String, Object> row : rows) {
                try {
                    debugUsages.add(serializer.deserialize((String) row.get("debugUsage"), DebugUsage.class));
                } catch (Exception e) {
                    logger.warn("Failed to load debugUsage " + row.get("id"), e);
                }
            }

            return debugUsages;

        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
        StatementLock.getInstance(VACUUM_LOCK_ID).readUnlock();
        }
    }
    

    public int deleteDebugUsage(String serverId) throws ControllerException {

        logger.debug("deleting debug usage for serverId: " + serverId);

        if (serverId == null) {
            throw new ControllerException("Error getting usage for serverId: serverId cannot be null.");
        }

        StatementLock.getInstance(VACUUM_LOCK_ID).readLock();
        try {
            DebugUsage debugUsage = new DebugUsage();
            debugUsage.setServerId(serverId);

            return SqlConfig.getInstance().getSqlSessionManager().delete("DebugUsage.deleteDebugUsageStatistics", debugUsage);

        } catch (PersistenceException e) {
            throw new ControllerException(e);
        } finally {
        StatementLock.getInstance(VACUUM_LOCK_ID).readUnlock();
        }
    }
    
    
    
    protected SqlSessionManager getReadOnlySqlSessionManager() {
    	return SqlConfig.getInstance().getReadOnlySqlSessionManager();
    }

}
