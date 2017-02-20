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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private String serverId = ControllerFactory.getFactory().createConfigurationController().getServerId();

    public abstract boolean isUserAuthorized(Integer userId, Operation operation, Map<String, Object> parameterMap, String address, boolean audit) throws ControllerException;

    public abstract void addExtensionPermission(ExtensionPermission extensionPermission);

    public abstract boolean doesUserHaveChannelRestrictions(Integer userId) throws ControllerException;

    public abstract List<String> getAuthorizedChannelIds(Integer userId) throws ControllerException;

    public abstract void usernameChanged(String oldName, String newName) throws ControllerException;

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
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // If channelId was one of the params, print out each channel as separate attributes and add the channel name
                    if (key.contains("channelId")) {
                        Set<String> collection = null;

                        if (value instanceof Set) {
                            collection = (Set<String>) value;
                        } else if (value instanceof String) {
                            collection = Collections.singleton((String) value);
                        }

                        if (collection != null) {
                            String[] channelIds = collection.toArray(new String[collection.size()]);
                            for (int i = 0; i < channelIds.length; i++) {
                                String name = "channel";
                                if (channelIds.length > 1) {
                                    name += "[" + i + "]";
                                }
                                value = channelController.getChannelById(channelIds[i]);
                                addAttribute(serverEvent.getAttributes(), name, value);
                            }
                        }
                    } else {
                        addAttribute(serverEvent.getAttributes(), key, value);
                    }
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

    private void addAttribute(Map<String, String> attributes, String name, Object value) {
        StringBuilder builder = new StringBuilder();
        getAuditDescription(value, builder);
        attributes.put(name, builder.toString());
    }
}
