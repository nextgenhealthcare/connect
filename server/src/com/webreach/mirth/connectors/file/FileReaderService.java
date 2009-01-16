package com.webreach.mirth.connectors.file;

import java.net.URI;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnectionFactory;
import com.webreach.mirth.util.ConnectionTestResponse;

public class FileReaderService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testRead")) {
            Map<String, String> params = (Map<String, String>) object;
            String fileHost = null;
            String scheme = params.get(FileReaderProperties.FILE_SCHEME);
            String host = null;
            int port = 0;
            String dir = null;

            if (scheme.equals(FileReaderProperties.SCHEME_FILE)) {
                fileHost = params.get(FileReaderProperties.FILE_HOST);
                dir = params.get(FileReaderProperties.FILE_HOST);
            } else {
                URI address = new URI(scheme + "://" + params.get(FileReaderProperties.FILE_HOST));
                fileHost = address.toString();
                host = address.getHost();
                port = address.getPort();
                dir = address.getPath();
            }

            String username = params.get(FileReaderProperties.FILE_USERNAME);
            String password = params.get(FileReaderProperties.FILE_PASSWORD);
            boolean passive = false;

            if (scheme.equals(FileReaderProperties.SCHEME_FTP) || scheme.equals(FileReaderProperties.SCHEME_SFTP)) {
                passive = Boolean.parseBoolean(params.get(FileWriterProperties.FILE_PASSIVE_MODE));
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, host, port, passive);

            try {
                FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

                if (connection.canRead(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to: " + fileHost);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
            }
        }

        return null;
    }
}
