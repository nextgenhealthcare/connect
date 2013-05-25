/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Auditable;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;

public abstract class AuthorizationController {
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    public abstract boolean isUserAuthorized(Integer userId, String operation, Map<String, Object> parameterMap, String address) throws ControllerException;

    public abstract boolean isUserAuthorizedForExtension(Integer userId, String extensionName, String operation, Map<String, Object> parameterMap, String address) throws ControllerException;

    public abstract void addExtensionPermission(ExtensionPermission extensionPermission);
    
    public abstract boolean doesUserHaveChannelRestrictions(Integer userId) throws ControllerException;
    
    public abstract List<String> getAuthorizedChannelIds(Integer userId) throws ControllerException;

    public void auditAuthorizationRequest(Integer userId, String operationName, Map<String, Object> parameterMap, ServerEvent.Outcome outcome, String address) {
        Operation operation = null;
        String extensionName = null;

        /*
         * If this is an operation being invoked through an extension, get the
         * name of the extension
         */
        if (StringUtils.contains(operationName, "#")) {
            operation = Operations.getOperation(StringUtils.split(operationName)[0]);
            extensionName = StringUtils.split(operationName)[1];
        } else {
            operation = Operations.getOperation(operationName);
        }

        if ((operation != null) && operation.isAuditable()) {
            ServerEvent serverEvent = new ServerEvent();
            serverEvent.setLevel(Level.INFORMATION);
            serverEvent.setUserId(userId);

            if (extensionName != null) {
                serverEvent.setName(operation.getDisplayName() + " invoked through " + extensionName);
            } else {
                serverEvent.setName(operation.getDisplayName());
            }

            serverEvent.setOutcome(outcome);
            serverEvent.setIpAddress(address);

            if (MapUtils.isNotEmpty(parameterMap)) {
                for (Entry<String, Object> entry : parameterMap.entrySet()) {
                    StringBuilder builder = new StringBuilder();
                    getAuditDescription(entry.getValue(), builder);
                    serverEvent.getAttributes().put(entry.getKey(), builder.toString());
                }
            }
            eventController.dispatchEvent(serverEvent);
        }
    }

    private void getAuditDescription(Object value, StringBuilder builder) {
        if (value instanceof Collection) {
            for (Object obj : (Collection<?>) value) {
                getAuditDescription(obj, builder);
            }
        } else if (value instanceof Auditable) {
            builder.append(((Auditable) value).toAuditString() + "\n");
        } else if (value != null) {
            builder.append(value.toString() + "\n");
        }
    }
}
