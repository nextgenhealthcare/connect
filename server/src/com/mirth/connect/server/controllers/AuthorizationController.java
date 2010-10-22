/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

public interface AuthorizationController {
    public boolean isUserAuthorized(String userId, String operation) throws ControllerException;
    
    public boolean isUserAuthorizedForExtension(String userId, String extensionName, String method) throws ControllerException;
}
