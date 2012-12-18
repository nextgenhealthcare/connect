/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.webadmin.utils.Constants;

public class DashboardStatisticsActionBean extends BaseActionBean {
    private List<DashboardStatus> dashboardStatusList;
    private int nextNodeCount = 1;
    private boolean showLifetimeStats = false;
    private boolean showAlert;

    @DefaultHandler
    public Resolution list() {
        Client client = getContext().getClient();

        if (client != null) {
            // Put message Status enums into scope for statistics map key retrieval 
            HttpServletRequest request = getContext().getRequest();
            for (Status status : Status.values()) {
                request.setAttribute(status.toString(), status);
            }

            try {
                dashboardStatusList = client.getChannelStatusList();
                setShowAlert(false);
            } catch (ClientException e) {
                setShowAlert(true);
            }
        } else {
            setShowAlert(true);
        }
        return new ForwardResolution(Constants.DASHBOARD_STATS_JSP);
    }

    private JSONObject populateStats(DashboardStatus dashboardStatus, String nodeId, boolean lifetimeStats) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", nodeId);

        Map<Status, Long> statistics = (lifetimeStats ? dashboardStatus.getLifetimeStatistics() : dashboardStatus.getStatistics());

        jsonObj.put("name", dashboardStatus.getName());
        jsonObj.put("status", checkState(dashboardStatus.getState()));
        jsonObj.put("received", checkNullValue(statistics.get(Status.RECEIVED)));
        jsonObj.put("filtered", checkNullValue(statistics.get(Status.FILTERED)));
        jsonObj.put("queued", checkNullValue(dashboardStatus.getQueued()));
        jsonObj.put("sent", checkNullValue(statistics.get(Status.SENT)));
        jsonObj.put("errored", checkNullValue(statistics.get(Status.ERROR)));

        return jsonObj;
    }

    public Resolution getStats() {
        JSONObject hashMap = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Client client = getContext().getClient();

        if (client != null) {
            try {
                dashboardStatusList = client.getChannelStatusList();

                // Check if channel was deployed/undeployed
                if (getContext().getRequest().getSession().getAttribute("size") != null) {
                    if ((Integer) getContext().getRequest().getSession().getAttribute("size") != dashboardStatusList.size()) {
                        getContext().getRequest().getSession().setAttribute("size", dashboardStatusList.size());
                        return new StreamingResolution("application/json", "0");
                    }
                }
                getContext().getRequest().getSession().setAttribute("size", dashboardStatusList.size());

                setShowAlert(false);
            } catch (ClientException e) {
                setShowAlert(true);
            }

            if (dashboardStatusList != null) {
                for (int i = 0; i < dashboardStatusList.size(); i++) {
                    DashboardStatus dashboardStatus = dashboardStatusList.get(i);
                    jsonArray.add(populateStats(dashboardStatus, "node-" + i, showLifetimeStats));

                    List<DashboardStatus> childStatuses = dashboardStatus.getChildStatuses();
                    for (int j = 0; j < childStatuses.size(); j++) {
                        String childId = ((childStatuses.get(j).getName()).replaceAll("\\s", "-")) + "-" + i;
                        jsonArray.add(populateStats(childStatuses.get(j), childId, showLifetimeStats));
                    }
                }
            }
        } else {
            setShowAlert(true);
        }

        return new StreamingResolution("application/json", jsonArray.toString());
    }

    // Getters & Setters
    public List<DashboardStatus> getDashboardStatusList() {
        return dashboardStatusList;
    }

    public void setDashboardStatusList(List<DashboardStatus> dashboardStatusList) {
        this.dashboardStatusList = dashboardStatusList;
    }

    public int getNextNodeCount() {
        return nextNodeCount++;
    }

    public void setNextNodeCount(int nextNodeCount) {
        this.nextNodeCount = nextNodeCount;
    }

    public boolean isShowLifetimeStats() {
        return showLifetimeStats;
    }

    public void setShowLifetimeStats(boolean showLifetimeStats) {
        this.showLifetimeStats = showLifetimeStats;
    }

    public boolean isShowAlert() {
        return showAlert;
    }

    public void setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
    }

    // Private Helper Functions
    private long checkNullValue(Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    private String checkState(ChannelState state) {
        if (state == ChannelState.STARTING)
            return "STARTING";
        if (state == ChannelState.STARTED)
            return "STARTED";
        if (state == ChannelState.PAUSING)
            return "PAUSING";
        if (state == ChannelState.PAUSED)
            return "PAUSED";
        if (state == ChannelState.STOPPING)
            return "STOPPING";
        if (state == ChannelState.STOPPED)
            return "STOPPED";
        return null;
    }
}