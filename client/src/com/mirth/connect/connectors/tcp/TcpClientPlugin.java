/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.converters.PluginPropertiesConverter;
import com.mirth.connect.plugins.ClientPlugin;
import com.thoughtworks.xstream.XStream;

public class TcpClientPlugin extends ClientPlugin {

    public TcpClientPlugin(String pluginName) {
        super(pluginName);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        XStream xstream = serializer.getXStream();
        xstream.registerLocalConverter(TcpReceiverProperties.class, "responseConnectorPluginProperties", new PluginPropertiesConverter(serializer.getNormalizedVersion(), xstream.getMapper()));
    }

    @Override
    public String getPluginPointName() {
        return "TCP Client Plugin";
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}