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

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public abstract class DebugUsageController extends Controller {
    
    public static DebugUsageController getInstance() {
        return ControllerFactory.getFactory().createDebugUsageController();
    }
    
    public abstract HashMap<String, Object> getDebugUsageMap(DebugUsage debugUsage);
        
    public abstract void upsertDebugUsage(DebugUsage debugUsage) throws ControllerException;
    
    public abstract void resetDebugUsage(String serverId) throws ControllerException;
    
    public abstract DebugUsage getDebugUsage(String serverId) throws ControllerException;
    
}
