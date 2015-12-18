/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.ExtensionOperation;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.model.Auditable;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;

public abstract class AuthorizationController {
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private String serverId = ControllerFactory.getFactory().createConfigurationController().getServerId();

    public abstract boolean isUserAuthorized(Integer userId, Operation operation, Map<String, Object> parameterMap, String address, boolean audit) throws ControllerException;

    public abstract void addExtensionPermission(ExtensionPermission extensionPermission);

    public abstract boolean doesUserHaveChannelRestrictions(Integer userId) throws ControllerException;

    public abstract List<String> getAuthorizedChannelIds(Integer userId) throws ControllerException;

    public void auditAuthorizationRequest(Integer userId, Operation operation, Map<String, Object> parameterMap, ServerEvent.Outcome outcome, String address) {
        if (operation != null && operation.isAuditable()) {
            String displayName = operation.getDisplayName();
            if (operation instanceof ExtensionOperation) {
                displayName += " invoked through " + ((ExtensionOperation) operation).getExtensionName();
            }
            ServerEvent serverEvent = new ServerEvent(serverId, displayName);
            serverEvent.setLevel(Level.INFORMATION);
            serverEvent.setUserId(userId);
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
