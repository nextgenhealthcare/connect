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

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.server.ExtensionLoader;

public class DefaultAuthorizationController extends AuthorizationController {

    private static AuthorizationController instance = null;

    private DefaultAuthorizationController() {}

    public static AuthorizationController create() {
        synchronized (DefaultAuthorizationController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(AuthorizationController.class);

                if (instance == null) {
                    instance = new DefaultAuthorizationController();
                }
            }

            return instance;
        }
    }

    @Override
    public boolean isUserAuthorized(Integer userId, Operation operation, Map<String, Object> parameterMap, String address, boolean audit) throws ControllerException {
        if (audit) {
            auditAuthorizationRequest(userId, operation, parameterMap, ServerEvent.Outcome.SUCCESS, address);
        }
        return true;
    }

    @Override
    public void addExtensionPermission(ExtensionPermission extensionPermission) {}

    @Override
    public boolean doesUserHaveChannelRestrictions(Integer userId, Operation operation) throws ControllerException {
        return false;
    }

    @Override
    public ChannelAuthorizer getChannelAuthorizer(Integer userId, Operation operation) throws ControllerException {
        return null;
    }

    @Override
    public void usernameChanged(String oldName, String newName) throws ControllerException {}
}
