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

package com.webreach.mirth.model;

import java.util.Properties;

public class QueuedSenderProperties implements ComponentProperties
{
    public static final String USE_PERSISTENT_QUEUES = "usePersistentQueues";
    public static final String RECONNECT_INTERVAL = "reconnectMillisecs";
    public static final String ROTATE_QUEUE = "rotateQueue";

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(USE_PERSISTENT_QUEUES, "0");
        properties.put(RECONNECT_INTERVAL, "10000");
        properties.put(ROTATE_QUEUE, "0");
        return properties;
    }
}
