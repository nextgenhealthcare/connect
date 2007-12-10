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

package com.webreach.mirth.connectors.sftp;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class SFTPReaderProperties implements ComponentProperties
{
	public static final String name = "SFTP Reader";

    public static final String DATATYPE = "DataType";
    public static final String FTP_HOST = "host";
    public static final String FTP_USERNAME = "username";
    public static final String FTP_PASSWORD = "password";
    public static final String FTP_POLLING_TYPE = "pollingType";
    public static final String FTP_POLLING_TIME = "pollingTime";
    public static final String FTP_POLLING_FREQUENCY = "pollingFrequency";
    public static final String FTP_MOVE_TO_PATTERN = "moveToPattern";
    public static final String FTP_MOVE_TO_DIRECTORY = "moveToDirectory";
    public static final String FTP_MOVE_TO_ERROR_DIRECTORY = "moveToErrorDirectory";
    public static final String FTP_DELETE_AFTER_READ = "autoDelete";
    public static final String FTP_CHECK_FTP_AGE = "checkFileAge";
    public static final String FTP_FTP_AGE = "fileAge";
    public static final String FTP_SORT_BY = "sortAttribute";
    public static final String FTP_PROCESS_BATCH_FILES = "processBatchFiles";
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_SIZE = "size";
    public static final String SORT_BY_DATE = "date";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String FTP_FILTER = "fileFilter";
    public static final String FTP_TYPE = "binary";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FTP_HOST, "/");
        properties.put(FTP_USERNAME, "");
        properties.put(FTP_PASSWORD, "");
        properties.put(FTP_POLLING_TYPE, "interval");
        properties.put(FTP_POLLING_TIME, "12:00 AM");
        properties.put(FTP_POLLING_FREQUENCY, "1000");
        properties.put(FTP_MOVE_TO_PATTERN, "");
        properties.put(FTP_MOVE_TO_DIRECTORY, "");
        properties.put(FTP_MOVE_TO_ERROR_DIRECTORY, "");
        properties.put(FTP_DELETE_AFTER_READ, "0");
        properties.put(FTP_CHECK_FTP_AGE, "0");
        properties.put(FTP_FTP_AGE, "0");
        properties.put(FTP_SORT_BY, SORT_BY_DATE);
        properties.put(FTP_TYPE, "0");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        properties.put(FTP_FILTER, "*");
        properties.put(FTP_PROCESS_BATCH_FILES, "0");
        return properties;
    }
}
