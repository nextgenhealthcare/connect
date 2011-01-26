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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;

import com.mirth.connect.model.Auditable;
import com.mirth.connect.model.Event;
import com.mirth.connect.model.Event.Level;
import com.mirth.connect.model.ExtensionPermission;

public abstract class AuthorizationController {
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    public abstract boolean isUserAuthorized(Integer userId, String operation, Map<String, Object> parameterMap, String address) throws ControllerException;

    public abstract boolean isUserAuthorizedForExtension(Integer userId, String extensionName, String operation, Map<String, Object> parameterMap, String address) throws ControllerException;

    public abstract void addExtensionPermission(ExtensionPermission extensionPermission);

    public void auditAuthorizationRequest(Integer userId, String operation, Map<String, Object> parameterMap, Event.Outcome outcome, String address) {
        Event event = new Event();
        event.setLevel(Level.INFORMATION);
        event.setUserId(userId);
        event.setEvent(operation);
        event.setOutcome(outcome);
        event.setIpAddress(address);

        if (MapUtils.isNotEmpty(parameterMap)) {
            for (Entry<String, Object> entry : parameterMap.entrySet()) {
                StringBuilder builder = new StringBuilder();
                getAuditDescription(entry.getValue(), builder);
                event.getAttributes().put(entry.getKey(), builder.toString());
            }
        }

        eventController.addEvent(event);
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
