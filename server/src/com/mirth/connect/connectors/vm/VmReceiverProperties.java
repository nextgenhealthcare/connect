/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;

public class VmReceiverProperties extends ConnectorProperties implements ResponseConnectorPropertiesInterface {
    private ResponseConnectorProperties responseConnectorProperties;

    public VmReceiverProperties() {
        responseConnectorProperties = new ResponseConnectorProperties();
    }

    @Override
    public String getName() {
        return "Channel Reader";
    }

    @Override
    public String getProtocol() {
        return "VM";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("responseConnectorProperties", responseConnectorProperties.getPurgedProperties());
        return purgedProperties;
    }
}
