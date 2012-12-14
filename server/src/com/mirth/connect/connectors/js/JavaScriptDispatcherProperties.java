/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class JavaScriptDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {
    public static final String NAME = "JavaScript Writer";

    private QueueConnectorProperties queueConnectorProperties;
    private String script;

    public JavaScriptDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        script = "// The return value of this script is stored as the response\nreturn 'Script execution successful';";
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public String toFormattedString() {
        return "Script Executed";
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }
}
