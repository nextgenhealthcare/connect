/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.file;

import java.util.Properties;

import com.webreach.mirth.connectors.ftp.FTPWriterProperties;
import com.webreach.mirth.model.ComponentProperties;

public class FileWriterProperties implements ComponentProperties
{
	public static final String name = "File Writer";
	
    public static final String DATATYPE = "DataType";
    public static final String FILE_HOST = "host";
    public static final String FILE_DIRECTORY = "directory";
    public static final String FILE_ANONYMOUS = "FTPAnonymous";
    public static final String FILE_USERNAME = "username";
    public static final String FILE_PASSWORD = "password";
    public static final String FILE_PASSIVE_MODE = "passive";
    public static final String FILE_VALIDATE_CONNECTION = "validateConnections";
    public static final String FILE_NAME = "outputPattern";
    public static final String FILE_APPEND = "outputAppend";
    public static final String FILE_CONTENTS = "template";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String FILE_TYPE = "binary";
	public static final String FILE_SCHEME = "scheme";
	
	public static final String SCHEME_FILE = "file";
	public static final String SCHEME_FTP = "ftp";
	public static final String SCHEME_SFTP = "sftp";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_SCHEME, SCHEME_FILE);
        properties.put(FILE_HOST, "");
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_NAME, "");
        properties.put(FILE_ANONYMOUS, "1");
        properties.put(FILE_USERNAME, "anonymous");
        properties.put(FILE_PASSWORD, "anonymous");
        properties.put(FILE_PASSIVE_MODE, "1");
        properties.put(FILE_VALIDATE_CONNECTION, "1");
        properties.put(FILE_APPEND, "1");
        properties.put(FILE_CONTENTS, "");
        properties.put(FILE_TYPE, "0");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        return properties;
    }

    /** Return a suitable information string for the dashboard connector
     * status monitor plugin.
     * 
     * Background: The current dashboard connector status monitor plugin
     * embeds lots of knowledge about the properties for various types of
     * connector. This is, in my opinion, a bad design. The knowledge
     * about connector-specific properties should reside in the connector
     * or the connector-specific properties themselves. Rather than fix
     * it everywhere, since I'm short of time, I've fixed it only for the
     * new merged File connector. (erikh@webreachinc.com)
     * 
     * @param properties The properties to be decided to an information string.
     * @return An information suitable for display by the dashboard.
     */
    public static String getInformation(Properties properties) {
    	String info = "";
    	String scheme = properties.getProperty(FILE_SCHEME);
    	if (scheme.equals(SCHEME_FILE)) {
    		info = "Result written to: " 
            		+ properties.getProperty(FileWriterProperties.FILE_HOST)
            		+ "/"
            		+ properties.getProperty(FileWriterProperties.FILE_NAME);
    	}
    	else if (scheme.equals(SCHEME_FTP) || scheme.equals(SCHEME_SFTP)) {
    		info = "Result written to: " 
    			+ properties.getProperty(FileWriterProperties.FILE_HOST) 
    			+ "/" 
    			+ properties.getProperty(FileWriterProperties.FILE_NAME);
            if (properties.getProperty(FileWriterProperties.FILE_TYPE).equals("0")) {
            	info += "   File Type: ASCII";
            } else {
            	info += "   File Type: Binary";
            }
    	}

    	return info;
    }
}
