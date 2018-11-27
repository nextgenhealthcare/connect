/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;

public class NoneHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    public NoneHttpAuthProperties() {
        super(AuthType.NONE);
    }

    @Override
    public ConnectorPluginProperties clone() {
        return new NoneHttpAuthProperties();
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        return purgedProperties;
    }
}