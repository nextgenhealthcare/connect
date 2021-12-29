/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Map;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public abstract class DebugController extends Controller {
    
    public static DebugController getInstance() {
        return ControllerFactory.getFactory().createDebugController();
    }
    
    public abstract Integer getStepOverCount();

    public abstract void setStepOverCount(Integer stepOverCount);

    public abstract Integer getStepInCount();

    public abstract void setStepInCount(Integer stepInCount);

    public abstract Integer getStepOutCount();

    public abstract void setStepOutCount(Integer stepOutCount);

    public abstract Integer getPauseCount();

    public abstract void setPauseCount(Integer pauseCount);

    public abstract Integer getHaltCount();

    public abstract void setHaltCount(Integer haltCount);

    public abstract Integer getNextCount();

    public abstract void setNextCount(Integer nextCount);

    public abstract void setDebugInvocationCount(Integer debugInvocationCount);

    public abstract Integer getDebugInvocationCount();
    
    public abstract Map<String, Object> getDebugStatsMap();

}
