/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.mirth.connect.donkey.model.message.Response;

@WebService
public class DefaultAcceptMessage extends AcceptMessage {

    public DefaultAcceptMessage(WebServiceMessageReceiver webServiceMessageReceiver) {
        super(webServiceMessageReceiver);
    }

    @WebMethod
    public String acceptMessage(String message) {
        Response response = webServiceMessageReceiver.processData(message);

        if (response != null) {
            return response.getMessage();
        }
        
        return null;
    }

}
