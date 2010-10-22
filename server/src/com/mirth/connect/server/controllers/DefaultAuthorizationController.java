/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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

    public boolean isUserAuthorizedForExtension(String userId, String extensionName, String method) {
        return true;
    }
}
