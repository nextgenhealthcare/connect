package com.mirth.connect.server.controllers;

public interface AuthorizationController {
    public boolean isUserAuthorized(String userId, String operation) throws ControllerException;
}
