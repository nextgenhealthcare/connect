package com.webreach.mirth.connectors.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class DefaultAcceptMessage extends AcceptMessage {

    public DefaultAcceptMessage(WebServiceMessageReceiver webServiceMessageReceiver) {
        super(webServiceMessageReceiver);
    }

    @WebMethod
    public String acceptMessage(String message) {
        return webServiceMessageReceiver.processData(message);
    }

}
