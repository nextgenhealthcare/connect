/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;

public class JavaScriptReceiverProperties extends ConnectorProperties implements PollConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    public static final String NAME = "JavaScript Reader";

    private PollConnectorProperties pollConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;
    private String script;

    public JavaScriptReceiverProperties() {
        pollConnectorProperties = new PollConnectorProperties();
        responseConnectorProperties = new ResponseConnectorProperties();

        script = "";
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
        return null;
    }

    @Override
    public PollConnectorProperties getPollConnectorProperties() {
        return pollConnectorProperties;
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
}
