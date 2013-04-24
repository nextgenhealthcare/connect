package com.mirth.connect.model.alert;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertAction")
public class AlertAction {

    private AlertActionProtocol protocol;
    private String recipient;
    
    public AlertAction(AlertActionProtocol protocol, String recipient) {
        this.protocol = protocol;
        this.recipient = recipient;
    }

    public AlertActionProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(AlertActionProtocol protocol) {
        this.protocol = protocol;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
