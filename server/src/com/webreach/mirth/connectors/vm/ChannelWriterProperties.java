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

package com.webreach.mirth.connectors.vm;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

public class ChannelWriterProperties implements ComponentProperties
{
	public static final String name = "Channel Writer";
	
    public static final String DATATYPE = "DataType";
    public static final String CHANNEL_ID = "host";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_SYNCHRONOUS = "synchronised";
    public static final String CHANNEL_TEMPLATE = "template";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        properties.put(CHANNEL_SYNCHRONOUS, "0");
        properties.put(CHANNEL_TEMPLATE, "${message.encodedData}");
        return properties;
    }
}
