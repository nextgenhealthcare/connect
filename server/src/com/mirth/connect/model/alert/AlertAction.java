/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertAction")
public class AlertAction implements Migratable, Purgable {

    private String protocol;
    private String recipient;

    public AlertAction(String protocol, String recipient) {
        this.protocol = protocol;
        this.recipient = recipient;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("protocol=").append(protocol).append(", ");
        builder.append("recipient=").append(recipient).append(']');
        return builder.toString();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {
        DonkeyElement protocol = element.getChildElement("protocol");

        if (protocol != null) {
            protocol.setTextContent(StringUtils.capitalize(protocol.getTextContent().toLowerCase()));
        }
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}
    
    @Override
    public void migrate3_6_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("protocol", protocol);
        return purgedProperties;
    }
}
