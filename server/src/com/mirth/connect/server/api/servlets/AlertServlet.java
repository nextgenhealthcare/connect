/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.AlertServletInterface;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.server.alert.action.ChannelProtocol;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class AlertServlet extends MirthServlet implements AlertServletInterface {

    private static final AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public AlertServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public void createAlert(AlertModel alertModel) {
        try {
            alertController.updateAlert(alertModel);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public AlertModel getAlert(String alertId) {
        try {
            AlertModel alert = alertController.getAlert(alertId);
            if (alert == null) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            return alert;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<AlertModel> getAlerts(Set<String> alertIds) {
        try {
            if (CollectionUtils.isEmpty(alertIds)) {
                return alertController.getAlerts();
            }

            List<AlertModel> alerts = new ArrayList<AlertModel>();
            for (AlertModel alert : alertController.getAlerts()) {
                if (alertIds.contains(alert.getId())) {
                    alerts.add(alert);
                }
            }
            return alerts;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<AlertModel> getAlertsPost(Set<String> alertIds) {
        return getAlerts(alertIds);
    }

    @Override
    public List<AlertStatus> getAlertStatusList() {
        try {
            return alertController.getAlertStatusList();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public AlertInfo getAlertInfo(String alertId, Map<String, ChannelHeader> cachedChannels) {
        try {
            List<ChannelSummary> channelSummaries = redactChannelSummaries(channelController.getChannelSummary(cachedChannels, false));
            Map<String, Map<String, String>> protocolOptions = redactProtocolOptions(alertController.getAlertActionProtocolOptions());

            AlertInfo alertInfo = new AlertInfo();
            alertInfo.setModel(alertController.getAlert(alertId));
            alertInfo.setChangedChannels(channelSummaries);
            alertInfo.setProtocolOptions(protocolOptions);
            return alertInfo;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public AlertInfo getAlertInfo(Map<String, ChannelHeader> cachedChannels) {
        try {
            List<ChannelSummary> channelSummaries = redactChannelSummaries(channelController.getChannelSummary(cachedChannels, false));
            Map<String, Map<String, String>> protocolOptions = redactProtocolOptions(alertController.getAlertActionProtocolOptions());

            AlertInfo alertInfo = new AlertInfo();
            alertInfo.setChangedChannels(channelSummaries);
            alertInfo.setProtocolOptions(protocolOptions);
            return alertInfo;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Map<String, Map<String, String>> getAlertProtocolOptions() {
        return redactProtocolOptions(alertController.getAlertActionProtocolOptions());
    }

    @Override
    public void updateAlert(String alertId, AlertModel alertModel) {
        try {
            alertController.updateAlert(alertModel);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void enableAlert(String alertId) {
        try {
            AlertModel alertModel = alertController.getAlert(alertId);
            if (alertModel == null) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            alertModel.setEnabled(true);
            alertController.updateAlert(alertModel);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void disableAlert(String alertId) {
        try {
            AlertModel alertModel = alertController.getAlert(alertId);
            if (alertModel == null) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            alertModel.setEnabled(false);
            alertController.updateAlert(alertModel);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void removeAlert(String alertId) {
        try {
            alertController.removeAlert(alertId);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    private Map<String, Map<String, String>> redactProtocolOptions(Map<String, Map<String, String>> protocolOptions) {
        if (userHasChannelRestrictions) {
            Map<String, String> channelOptions = protocolOptions.get(ChannelProtocol.NAME);

            if (channelOptions != null) {
                Set<String> authorizedChannelIds = new HashSet<>(getAuthorizedChannelIds());
                Map<String, String> authorizedChannelOptions = new HashMap<>();

                for (String channelId : channelOptions.keySet()) {
                    if (authorizedChannelIds.contains(channelId)) {
                        authorizedChannelOptions.put(channelId, channelOptions.get(channelId));
                    }
                }

                protocolOptions.put(ChannelProtocol.NAME, authorizedChannelOptions);
            }
        }

        return protocolOptions;
    }
}