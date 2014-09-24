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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("defaultTrigger")
public class DefaultTrigger extends ChannelTrigger implements AlertTrigger, Migratable, Purgable {

    public static final String TRIGGER_NAME = "Channel Error";
    private Set<ErrorEventType> errorEventTypes;
    private String regex;

    public DefaultTrigger() {
        this(new HashSet<ErrorEventType>(), "");
    }

    public DefaultTrigger(Set<ErrorEventType> errorAlertTypes, String regex) {
        this.errorEventTypes = errorAlertTypes;
        this.regex = regex;
    }

    public Set<ErrorEventType> getErrorEventTypes() {
        return errorEventTypes;
    }

    public void setErrorEventTypes(Set<ErrorEventType> errorEventTypes) {
        this.errorEventTypes = errorEventTypes;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String getName() {
        return TRIGGER_NAME;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement eventTypes = element.getChildElement("errorEventTypes");

        if (eventTypes != null) {
            for (DonkeyElement eventType : eventTypes.getChildElements()) {
                if (eventType.getNodeName().equals("errorEventType") && eventType.getTextContent().equals("SHUTDOWN_SCRIPT")) {
                    eventType.setTextContent("UNDEPLOY_SCRIPT");
                }
            }
        }
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("errorEventTypes", errorEventTypes);
        return purgedProperties;
    }
}
