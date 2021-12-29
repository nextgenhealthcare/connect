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

import org.apache.log4j.Logger;

import com.mirth.connect.server.ExtensionLoader;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public class DefaultDebugController extends DebugController {

    private Logger logger = Logger.getLogger(this.getClass());

    private Integer debugInvocationCount;
    private Integer stepOverCount;
    private Integer stepInCount;
    private Integer stepOutCount;
    private Integer pauseCount;
    private Integer haltCount;
    private Integer nextCount;
    
    
    // singleton pattern
    private static DebugController instance = null;

    public DefaultDebugController() {

    }

    public static DebugController create() {
        synchronized (DefaultDebugController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(DebugController.class);
                if (instance == null) {
                    instance = new DefaultDebugController();
                    ((DefaultDebugController) instance).initialize();
                } else {
                    try {
                        instance.getClass().getMethod("initialize").invoke(instance);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultDebugController.class).error("Error calling initialize method in DefaultConfigurationController", e);
                    }
                }
            }
            return instance;
        }
    }

    public void initialize() {

    }

    public Integer getDebugInvocationCount() {
        return debugInvocationCount;
    }

    public void setDebugInvocationCount(Integer debugInvocationCount) {
        this.debugInvocationCount = debugInvocationCount;
    }

    public Integer getStepOverCount() {
        return stepOverCount;
    }

    public void setStepOverCount(Integer stepOverCount) {
        this.stepOverCount = stepOverCount;
    }

    public Integer getStepInCount() {
        return stepInCount;
    }

    public void setStepInCount(Integer stepInCount) {
        this.stepInCount = stepInCount;
    }

    public Integer getStepOutCount() {
        return stepOutCount;
    }

    public void setStepOutCount(Integer stepOutCount) {
        this.stepOutCount = stepOutCount;
    }

    public Integer getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(Integer pauseCount) {
        this.pauseCount = pauseCount;
    }

    public Integer getHaltCount() {
        return haltCount;
    }

    public void setHaltCount(Integer haltCount) {
        this.haltCount = haltCount;
    }

    public Integer getNextCount() {
        return nextCount;
    }

    public void setNextCount(Integer nextCount) {
        this.nextCount = nextCount;
    }
    
    public Map<String, Object> getDebugStatsMap() {
        
        HashMap<String, Object> debugStatsMap = new HashMap<>();
        debugStatsMap.put("debugInvocationCount", debugInvocationCount);
        debugStatsMap.put("stepOverCount", stepOverCount);
        debugStatsMap.put("stepInCount", stepInCount);
        debugStatsMap.put("stepOutCount", stepOutCount);
        debugStatsMap.put("pauseCount", pauseCount);
        debugStatsMap.put("debugStatsMap", debugStatsMap);
        debugStatsMap.put("haltCount", haltCount);
        debugStatsMap.put("nextCount", nextCount);
        
        return debugStatsMap;
    }
}
