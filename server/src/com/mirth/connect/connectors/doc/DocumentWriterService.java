/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.connectors.file.FileScheme;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnection;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ConnectionTestResponse;

public class DocumentWriterService implements ConnectorService {
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            DocumentDispatcherProperties props = (DocumentDispatcherProperties) object;
            String directory = replacer.replaceValues(props.getHost(), channelId);

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileScheme.FILE, null, null, directory, 0, false, false, 0);
            FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

            try {
                if (connection.canWrite(directory)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: " + directory);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + directory);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + directory + ", Reason: " + e.getMessage());
            }
        }

        return null;
    }
}
