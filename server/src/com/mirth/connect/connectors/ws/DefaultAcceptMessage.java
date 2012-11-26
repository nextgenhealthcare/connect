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

@WebService
public class DefaultAcceptMessage extends AcceptMessage {

    public DefaultAcceptMessage(WebServiceReceiver webServiceReceiver) {
        super(webServiceReceiver);
    }

    @WebMethod
    public String acceptMessage(String message) {
        String response = webServiceReceiver.processData(message);

        if (response != null) {
            return response;
        }
        
        return null;
    }

}
