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

package com.webreach.mirth.connectors.jdbc;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class DatabaseReaderProperties implements ComponentProperties
{
	public static final String name = "Database Reader";
	
    public static final String DATATYPE = "DataType";
    public static final String DATABASE_HOST = "host";
    public static final String DATABASE_HOST_VALUE = "query";
    public static final String DATABASE_DRIVER = "driver";
    public static final String DATABASE_URL = "URL";
    public static final String DATABASE_USERNAME = "username";
    public static final String DATABASE_PASSWORD = "password";
    public static final String DATABASE_POLLING_TYPE = "pollingType";
    public static final String DATABASE_POLLING_TIME = "pollingTime";
    public static final String DATABASE_POLLING_FREQUENCY = "pollingFrequency";
    public static final String DATABASE_SQL_STATEMENT = "query";
    public static final String DATABASE_JS_SQL_STATEMENT = "script";
    public static final String DATABASE_USE_JS = "useScript";
    public static final String DATABASE_PROCESS_RESULTS_IN_ORDER = "processResultsInOrder";
    public static final String DATABASE_USE_ACK = "useAck";
    public static final String DATABASE_ACK = "ack";
    public static final String DATABASE_JS_ACK = "ackScript";
 
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DATABASE_HOST, DATABASE_HOST_VALUE);
        properties.put(DATABASE_DRIVER, "Please Select One");
        properties.put(DATABASE_URL, "");
        properties.put(DATABASE_USERNAME, "");
        properties.put(DATABASE_PASSWORD, "");
        properties.put(DATABASE_POLLING_FREQUENCY, "5000");
        properties.put(DATABASE_POLLING_TYPE, "interval");
        properties.put(DATABASE_POLLING_TIME, "12:00 AM");
        properties.put(DATABASE_SQL_STATEMENT, "");
        properties.put(DATABASE_USE_JS, "0");
        properties.put(DATABASE_JS_SQL_STATEMENT, "");
        properties.put(DATABASE_PROCESS_RESULTS_IN_ORDER, "1");
        properties.put(DATABASE_USE_ACK, "0");
        properties.put(DATABASE_ACK, "");
        properties.put(DATABASE_JS_ACK, "");
        return properties;
    }
}
