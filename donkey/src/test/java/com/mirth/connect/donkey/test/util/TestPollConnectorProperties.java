/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;

@SuppressWarnings("serial")
public class TestPollConnectorProperties extends ConnectorProperties implements PollConnectorPropertiesInterface {
    private PollConnectorProperties pollConnectorProperties = new PollConnectorProperties();

    @Override
    public PollConnectorProperties getPollConnectorProperties() {
        return pollConnectorProperties;
    }

    @Override
    public String getProtocol() {
        return "Test Protocol";
    }

    @Override
    public String getName() {
        return "Test Poll Connector";
    }

    @Override
    public String toFormattedString() {
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }
}
