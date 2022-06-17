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
import java.util.List;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;

/**
 * The DebugUsageController provides access to the Mirth debug usage.
 * 
 */
public abstract class DebugUsageController extends Controller {
    
    public static DebugUsageController getInstance() {
        return ControllerFactory.getFactory().createDebugUsageController();
    }
    
    public abstract Map<String, Object> getDebugUsageMap(DebugUsage debugUsage);
        
    public abstract boolean upsertDebugUsage(DebugUsage debugUsage) throws ControllerException;
    
    public abstract int deleteDebugUsage(String serverId) throws ControllerException;
    
    public abstract DebugUsage getDebugUsage(String serverId) throws ControllerException;

	public abstract List<DebugUsage> getDebugUsages() throws ControllerException;
    
}
