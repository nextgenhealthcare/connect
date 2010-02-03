/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.doc;

import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.connectors.file.FileConnector;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnectionFactory;
import com.webreach.mirth.util.ConnectionTestResponse;

public class DocumentWriterService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            Map<String, String> params = (Map<String, String>) object;
            String directory = params.get(DocumentWriterProperties.FILE_DIRECTORY);

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileConnector.SCHEME_FILE, null, null, directory, 0, false, false);
            FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

            try {
                if (connection.canWrite(directory)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to: " + directory);
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
