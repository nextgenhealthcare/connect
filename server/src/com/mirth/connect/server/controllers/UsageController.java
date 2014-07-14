package com.mirth.connect.server.controllers;

public abstract class UsageController extends Controller {
    public static UsageController getInstance() {
        return ControllerFactory.getFactory().createUsageController();
    }
    
    public abstract String createUsageStats(boolean checkLastStatsTime);
}
