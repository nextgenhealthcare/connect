/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class DocumentConnectorServlet extends MirthServlet implements DocumentConnectorServletInterface {

    private static final TemplateValueReplacer replacer = new TemplateValueReplacer();

    public DocumentConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public ConnectionTestResponse testWrite(String channelId, String channelName, String directory) throws ClientException {
        directory = replacer.replaceValues(directory, channelId, channelName);
        try {
            File writeDirectory = new File(directory);

            if (writeDirectory.isDirectory() && writeDirectory.canWrite()) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + directory);
            } else {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + directory);
            }
        } catch (Exception e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + directory + ", Reason: " + e.getMessage());
        }
    }
}