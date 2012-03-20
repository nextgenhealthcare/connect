/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Map;

import com.mirth.connect.model.Event;
import com.mirth.connect.model.ExtensionPermission;

public class DefaultAuthorizationController extends AuthorizationController {

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

    @Override
    public boolean isUserAuthorized(Integer userId, String operation, Map<String, Object> parameterMap, String address) throws ControllerException {
        auditAuthorizationRequest(userId, operation, parameterMap, Event.Outcome.SUCCESS, address);
        return true;
    }

    @Override
    public boolean isUserAuthorizedForExtension(Integer userId, String extensionName, String operationName, Map<String, Object> parameterMap, String address) throws ControllerException {
        auditAuthorizationRequest(userId, extensionName + "#" + operationName, parameterMap, Event.Outcome.SUCCESS, address);
        return true;
    }

    @Override
    public void addExtensionPermission(ExtensionPermission extensionPermission) {

    }

    @Override
    public boolean doesUserHaveChannelRestrictions(Integer userId) throws ControllerException {
        return false;
    }

    @Override
    public List<String> getAuthorizedChannelIds(Integer userId) throws ControllerException {
        return null;
    }
}
