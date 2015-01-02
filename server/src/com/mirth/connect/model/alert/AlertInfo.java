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
