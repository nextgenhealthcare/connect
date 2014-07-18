/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public class PollConnectorProperties implements Serializable, Migratable, Purgable {
    public static final String POLLING_TYPE_INTERVAL = "interval";
    public static final String POLLING_TYPE_TIME = "time";

    private String pollingType;
    private int pollingHour;
    private int pollingMinute;
    private int pollingFrequency;

    public PollConnectorProperties() {
        pollingType = POLLING_TYPE_INTERVAL;
        pollingHour = 0;
        pollingMinute = 0;
        pollingFrequency = 5000;
    }

    public String getPollingType() {
        return pollingType;
    }

    public void setPollingType(String pollingType) {
        this.pollingType = pollingType;
    }

    public int getPollingHour() {
        return pollingHour;
    }

    public void setPollingHour(int pollingHour) {
        this.pollingHour = pollingHour;
    }

    public int getPollingMinute() {
        return pollingMinute;
    }

    public void setPollingMinute(int pollingMinute) {
        this.pollingMinute = pollingMinute;
    }

    public int getPollingFrequency() {
        return pollingFrequency;
    }

    public void setPollingFrequency(int pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
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
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("pollingType", pollingType);
        purgedProperties.put("pollingHour", pollingHour);
        purgedProperties.put("pollingMinute", pollingMinute);
        purgedProperties.put("pollingFrequency", pollingFrequency);
        return purgedProperties;
    }
}
