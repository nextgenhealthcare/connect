package com.mirth.connect.model.alert;


public abstract class ChannelTrigger {
    
    private AlertChannels alertChannels = new AlertChannels();

    public AlertChannels getAlertChannels() {
        return alertChannels;
    }

    public void setAlertChannels(AlertChannels channels) {
        this.alertChannels = channels;
    }
    
}
