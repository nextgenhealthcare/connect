/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.Properties;

import com.mirth.connect.model.ComponentProperties;

public class FileWriterProperties implements ComponentProperties {
    public static final String name = "File Writer";

    public static final String DATATYPE = "DataType";
    public static final String FILE_HOST = "host";
    public static final String FILE_DIRECTORY = "directory";
    public static final String FILE_ANONYMOUS = "FTPAnonymous";
    public static final String FILE_USERNAME = "username";
    public static final String FILE_PASSWORD = "password";
    public static final String FILE_SECURE_MODE = "secure";
    public static final String FILE_PASSIVE_MODE = "passive";
    public static final String FILE_VALIDATE_CONNECTION = "validateConnections";
    public static final String FILE_NAME = "outputPattern";
    public static final String FILE_APPEND = "outputAppend";
    public static final String FILE_ERROR_ON_EXISTS = "errorOnExists";
    public static final String FILE_TEMPORARY = "temporary";
    public static final String FILE_CONTENTS = "template";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String FILE_TYPE = "binary";
    public static final String FILE_TIMEOUT = "timeout";
    public static final String FILE_SCHEME = "scheme";

    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_FTP = "ftp";
    public static final String SCHEME_SFTP = "sftp";
    public static final String SCHEME_SMB = "smb";
    public static final String SCHEME_WEBDAV = "webdav";

    public Properties getDefaults() {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_SCHEME, SCHEME_FILE);
        properties.put(FILE_HOST, "");
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_NAME, "");
        properties.put(FILE_ANONYMOUS, "1");
        properties.put(FILE_USERNAME, "anonymous");
        properties.put(FILE_PASSWORD, "anonymous");
        properties.put(FILE_SECURE_MODE, "1");
        properties.put(FILE_PASSIVE_MODE, "1");
        properties.put(FILE_VALIDATE_CONNECTION, "1");
        properties.put(FILE_APPEND, "1");
        properties.put(FILE_ERROR_ON_EXISTS, "0");
        properties.put(FILE_TEMPORARY, "0");
        properties.put(FILE_CONTENTS, "");
        properties.put(FILE_TYPE, "0");
        properties.put(FILE_TIMEOUT, "10000");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        return properties;
    }

    /**
     * Return a suitable information string for the dashboard connector status
     * monitor plugin.
     * 
     * Background: The current dashboard connector status monitor plugin embeds
     * lots of knowledge about the properties for various types of connector.
     * This is, in my opinion, a bad design. The knowledge about
     * connector-specific properties should reside in the connector or the
     * connector-specific properties themselves. Rather than fix it everywhere,
     * since I'm short of time, I've fixed it only for the new merged File
     * connector.
     * 
     * @param properties
     *            The properties to be decided to an information string.
     * @return An information suitable for display by the dashboard.
     */
    public static String getInformation(Properties properties) {
        String info = "";
        String scheme = properties.getProperty(FILE_SCHEME);
        if (scheme.equals(SCHEME_FILE)) {
            info = "Result written to: " + properties.getProperty(FileWriterProperties.FILE_HOST) + "/" + properties.getProperty(FileWriterProperties.FILE_NAME);
        } else if (scheme.equals(SCHEME_FTP) || scheme.equals(SCHEME_SFTP)) {
            info = "Result written to: " + properties.getProperty(FileWriterProperties.FILE_HOST) + "/" + properties.getProperty(FileWriterProperties.FILE_NAME);
            if (properties.getProperty(FileWriterProperties.FILE_TYPE).equals("0")) {
                info += "   File Type: ASCII";
            } else {
                info += "   File Type: Binary";
            }
        }

        return info;
    }
}
