package com.webreach.mirth.connectors.ws;


public abstract class AcceptMessage {
    protected WebServiceMessageReceiver webServiceMessageReceiver;
    
    public AcceptMessage(WebServiceMessageReceiver webServiceMessageReceiver) {
        this.webServiceMessageReceiver = webServiceMessageReceiver;
    }
    
    public abstract String acceptMessage(String message);
    
}
