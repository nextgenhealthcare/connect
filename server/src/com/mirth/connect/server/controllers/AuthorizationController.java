/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import com.mirth.connect.model.ExtensionPermission;

public interface AuthorizationController {
    public boolean isUserAuthorized(Integer userId, String operation) throws ControllerException;

    public boolean isUserAuthorizedForExtension(Integer userId, String extensionName, String operation) throws ControllerException;

    public void addExtensionPermission(ExtensionPermission extensionPermission);
}
