/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tests;

import java.util.Map;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.message.AutoResponder;

public class TestAutoResponder implements AutoResponder {

    @Override
    public Response getResponse(Status status, String message, ConnectorMessage connectorMessage) {
        return null;
    }

    @Override
    public String generateResponseMessage(String message, Map<String, Object> properties) throws Exception {
        return null;
    }
}
