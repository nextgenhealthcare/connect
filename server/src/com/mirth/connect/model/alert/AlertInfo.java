/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.List;
import java.util.Map;

import com.mirth.connect.model.ChannelSummary;

public class AlertInfo {
    private AlertModel model;
    private Map<String, Map<String, String>> protocolOptions;
    private List<ChannelSummary> changedChannels;

    public AlertModel getModel() {
        return model;
    }

    public void setModel(AlertModel model) {
        this.model = model;
    }

    public Map<String, Map<String, String>> getProtocolOptions() {
        return protocolOptions;
    }

    public void setProtocolOptions(Map<String, Map<String, String>> protocolOptions) {
        this.protocolOptions = protocolOptions;
    }

    public List<ChannelSummary> getChangedChannels() {
        return changedChannels;
    }

    public void setChangedChannels(List<ChannelSummary> changedChannels) {
        this.changedChannels = changedChannels;
    }
}
