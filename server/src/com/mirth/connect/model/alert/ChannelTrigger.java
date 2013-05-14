package com.mirth.connect.model.alert;


public abstract class ChannelTrigger {
    
    private AlertChannels channels = new AlertChannels();

    public AlertChannels getChannels() {
        return channels;
    }

    public void setChannels(AlertChannels channels) {
        this.channels = channels;
    }
    
}
