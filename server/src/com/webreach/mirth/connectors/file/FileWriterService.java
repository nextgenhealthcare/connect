package com.webreach.mirth.connectors.file;

import java.net.URL;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnectionFactory;
import com.webreach.mirth.util.ConnectionTestResponse;

public class FileWriterService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            Map<String, String> params = (Map<String, String>) object;
            String scheme = params.get(FileWriterProperties.FILE_SCHEME);
            URL address = new URL(params.get(FileWriterProperties.FILE_HOST));
            String dir = params.get(FileWriterProperties.FILE_DIRECTORY);
            String username = params.get(FileWriterProperties.FILE_USERNAME);
            String password = params.get(FileWriterProperties.FILE_PASSWORD);
            boolean passive = false;

            if (scheme.equals(FileWriterProperties.SCHEME_FTP) || scheme.equals(FileWriterProperties.SCHEME_SFTP)) {
                passive = Boolean.parseBoolean(params.get(FileWriterProperties.FILE_PASSIVE_MODE));
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, address.getHost(), address.getPort(), passive);
            FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

            if (connection.canWrite(dir)) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to: " + dir);
            } else {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + dir);
            }
        }

        return null;
    }
}
