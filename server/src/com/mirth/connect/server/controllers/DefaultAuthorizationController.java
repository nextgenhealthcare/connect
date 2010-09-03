package com.mirth.connect.server.controllers;


public class DefaultAuthorizationController implements AuthorizationController {
    
    private static DefaultAuthorizationController instance = null;

    private DefaultAuthorizationController() {

    }

    public static AuthorizationController create() {
        synchronized (DefaultAuthorizationController.class) {
            if (instance == null) {
                instance = new DefaultAuthorizationController();
            }

            return instance;
        }
    }

    public boolean isUserAuthorized(String userId, String operation) {
        return true;
    }
}
