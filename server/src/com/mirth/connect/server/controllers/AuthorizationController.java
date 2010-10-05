package com.mirth.connect.server.controllers;

public interface AuthorizationController {
    public boolean isUserAuthorized(String userId, String operation) throws ControllerException;
    
    public boolean isUserAuthorizedForExtension(String userId, String extensionName, String method) throws ControllerException;
}
