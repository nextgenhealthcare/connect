/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.util.DonkeyElement;

public abstract class HttpAuthConnectorPluginProperties extends ConnectorPluginProperties {

    public static final String PLUGIN_POINT = "HTTP Auth Connector Plugin Properties";

    public enum AuthType {
        NONE("None"), BASIC("Basic Authentication"), DIGEST("Digest Authentication"), JAVASCRIPT(
                "JavaScript"), CUSTOM(
                        "Custom Java Class"), OAUTH2_VERIFICATION("OAuth 2.0 Token Verification");

        private String value;

        private AuthType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private AuthType authType;

    public HttpAuthConnectorPluginProperties(AuthType authType) {
        this.authType = authType;
    }

    public AuthType getAuthType() {
        return authType;
    }

    @Override
    public String getName() {
        return PLUGIN_POINT;
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
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}
}