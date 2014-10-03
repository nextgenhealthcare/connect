/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

public abstract class UsageController extends Controller {
    public static UsageController getInstance() {
        return ControllerFactory.getFactory().createUsageController();
    }
    
    public abstract String createUsageStats(boolean checkLastStatsTime);
}
