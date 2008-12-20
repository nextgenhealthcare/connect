package com.webreach.mirth.connectors.file;

import java.net.URL;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnectionFactory;
import com.webreach.mirth.util.ConnectionTestResponse;

public class FileReaderService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String scheme = params.get(FileReaderProperties.FILE_SCHEME);
            URL address = new URL(params.get(FileReaderProperties.FILE_HOST));
            String dir = params.get(FileReaderProperties.FILE_DIRECTORY);
            String username = params.get(FileReaderProperties.FILE_USERNAME);
            String password = params.get(FileReaderProperties.FILE_PASSWORD);
            boolean passive = false;

            if (scheme.equals(FileReaderProperties.SCHEME_FTP) || scheme.equals(FileReaderProperties.SCHEME_SFTP)) {
                passive = Boolean.parseBoolean(params.get(FileWriterProperties.FILE_PASSIVE_MODE));
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, address.getHost(), address.getPort(), passive);
            FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

            if (connection.canRead(dir)) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to: " + dir);
            } else {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + dir);
            }
        }

        return null;
    }
}
