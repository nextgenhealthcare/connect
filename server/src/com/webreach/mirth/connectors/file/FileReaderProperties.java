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

import com.webreach.mirth.model.ComponentProperties;

import java.util.Properties;

public class FileReaderProperties implements ComponentProperties
{
    public static final String name = "File Reader";
	
    public static final String DATATYPE = "DataType";
    public static final String FILE_HOST = "host";
    public static final String FILE_DIRECTORY = "directory";
    public static final String FILE_ANONYMOUS = "FTPAnonymous";
    public static final String FILE_USERNAME = "username";
    public static final String FILE_PASSWORD = "password";
	public static final String FILE_SECURE_MODE = "secure";
	public static final String FILE_REGEX = "regex";
    public static final String FILE_PASSIVE_MODE = "passive";
    public static final String FILE_VALIDATE_CONNECTION = "validateConnections";
    public static final String FILE_POLLING_TYPE = "pollingType";
    public static final String FILE_POLLING_TIME = "pollingTime";
    public static final String FILE_POLLING_FREQUENCY = "pollingFrequency";
    public static final String FILE_MOVE_TO_PATTERN = "moveToPattern";
    public static final String FILE_MOVE_TO_DIRECTORY = "moveToDirectory";
    public static final String FILE_MOVE_TO_ERROR_DIRECTORY = "moveToErrorDirectory";
    public static final String FILE_DELETE_AFTER_READ = "autoDelete";
    public static final String FILE_CHECK_FILE_AGE = "checkFileAge";
    public static final String FILE_FILE_AGE = "fileAge";
    public static final String FILE_SORT_BY = "sortAttribute";
    public static final String FILE_PROCESS_BATCH_FILES = "processBatchFiles";
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_SIZE = "size";
    public static final String SORT_BY_DATE = "date";
    public static final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    public static final String FILE_FILTER = "fileFilter";
    public static final String FILE_TYPE = "binary";
	public static final String FILE_SCHEME = "scheme";
	
	public static final String SCHEME_FILE = "file";
	public static final String SCHEME_FTP = "ftp";
	public static final String SCHEME_SFTP = "sftp";
	public static final String SCHEME_SMB = "smb";
	public static final String SCHEME_WEBDAV = "webdav";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(FILE_SCHEME, SCHEME_FILE);
        properties.put(FILE_HOST, "");
        properties.put(FILE_DIRECTORY, "");
        properties.put(FILE_ANONYMOUS, "1");
        properties.put(FILE_USERNAME, "anonymous");
        properties.put(FILE_PASSWORD, "anonymous");
	    properties.put(FILE_SECURE_MODE, "1");
	    properties.put(FILE_REGEX, "0");
        properties.put(FILE_PASSIVE_MODE, "1");
        properties.put(FILE_VALIDATE_CONNECTION, "1");
        properties.put(FILE_POLLING_TYPE, "interval");
        properties.put(FILE_POLLING_TIME, "12:00 AM");
        properties.put(FILE_POLLING_FREQUENCY, "1000");
        properties.put(FILE_MOVE_TO_PATTERN, "");
        properties.put(FILE_MOVE_TO_DIRECTORY, "");
        properties.put(FILE_MOVE_TO_ERROR_DIRECTORY, "");
        properties.put(FILE_DELETE_AFTER_READ, "0");
        properties.put(FILE_CHECK_FILE_AGE, "0");
        properties.put(FILE_FILE_AGE, "0");
        properties.put(FILE_SORT_BY, SORT_BY_DATE);
        properties.put(FILE_TYPE, "0");
        properties.put(CONNECTOR_CHARSET_ENCODING, "DEFAULT_ENCODING");
        properties.put(FILE_FILTER, "*");
        properties.put(FILE_PROCESS_BATCH_FILES, "0");
        return properties;
    }
}
