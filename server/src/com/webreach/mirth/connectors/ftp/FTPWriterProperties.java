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

package com.webreach.mirth.connectors.ftp;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class FTPWriterProperties implements ComponentProperties
{
	public static final String name = "FTP Writer";
	
    public static final String DATATYPE = "DataType";
    public static final String FTP_URL = "host";
    public static final String FTP_ANONYMOUS = "FTPAnonymous";
    public static final String FTP_USERNAME = "username";
    public static final String FTP_PASSWORD = "password";
    public static final String FTP_OUTPUT_PATTERN = "outputPattern";
    public static final String FTP_PASSIVE_MODE = "passive";
    public static final String FTP_FILE_TYPE = "binary";
    public static final String FTP_VALIDATE_CONNECTION = "validateConnections";
    public static final String FTP_CONTENTS = "template";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FTP_URL, "/");
        properties.put(FTP_ANONYMOUS, "1");
        properties.put(FTP_USERNAME, "anonymous");
        properties.put(FTP_PASSWORD, "anonymous");
        properties.put(FTP_OUTPUT_PATTERN, "");
        properties.put(FTP_PASSIVE_MODE, "1");
        properties.put(FTP_FILE_TYPE, "0");
        properties.put(FTP_VALIDATE_CONNECTION, "1");
        properties.put(FTP_CONTENTS, "");
        return properties;
    }
}
