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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public class PollConnectorProperties implements Serializable, Migratable, Purgable {
    private PollingType pollingType;
    private boolean pollOnStart;

    private int pollingFrequency;

    private int pollingHour;
    private int pollingMinute;

    private List<CronProperty> cronJobs;

    private PollConnectorPropertiesAdvanced pollConnectorPropertiesAdvanced;

    public PollConnectorProperties() {
        pollingType = PollingType.INTERVAL;
        pollOnStart = false;
        pollingFrequency = 5000;
        pollingHour = 0;
        pollingMinute = 0;
        cronJobs = new ArrayList<CronProperty>();

        pollConnectorPropertiesAdvanced = new PollConnectorPropertiesAdvanced();
    }

    public PollConnectorProperties(PollConnectorProperties properties) {
        pollingType = properties.getPollingType();
        pollOnStart = properties.isPollOnStart();
        pollingFrequency = properties.getPollingFrequency();
        pollingHour = properties.getPollingHour();
        pollingMinute = properties.getPollingMinute();

        List<CronProperty> cron = new ArrayList<CronProperty>();
        for (CronProperty property : properties.getCronJobs()) {
            cron.add(new CronProperty(property.getDescription(), property.getExpression()));
        }

        cronJobs = cron;

        pollConnectorPropertiesAdvanced = properties.getPollConnectorPropertiesAdvanced().clone();
    }

    public PollingType getPollingType() {
        return pollingType;
    }

    public void setPollingType(PollingType pollingType) {
        this.pollingType = pollingType;
    }

    public void setPollOnStart(boolean pollOnStart) {
        this.pollOnStart = pollOnStart;
    }

    public boolean isPollOnStart() {
        return pollOnStart;
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

    public List<CronProperty> getCronJobs() {
        return cronJobs;
    }

    public void setCronJobs(List<CronProperty> cronJobs) {
        this.cronJobs = cronJobs;
    }

    public PollConnectorPropertiesAdvanced getPollConnectorPropertiesAdvanced() {
        return pollConnectorPropertiesAdvanced;
    }

    public void setPollConnectorPropertiesAdvanced(PollConnectorPropertiesAdvanced pollConnectorPropertiesAdvanced) {
        this.pollConnectorPropertiesAdvanced = pollConnectorPropertiesAdvanced;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public PollConnectorProperties clone() {
        return new PollConnectorProperties(this);
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
    public void migrate3_3_0(DonkeyElement element) {
        DonkeyElement pollingFrequencyElement = element.getChildElement("pollingFrequency");
        int pollingFrequency = NumberUtils.toInt(pollingFrequencyElement.getTextContent());
        if (pollingFrequency <= 0) {
            pollingFrequency = 5000;
        } else if (pollingFrequency >= 86400000) {
            pollingFrequency = 86399999;
        }
        pollingFrequencyElement.setTextContent(String.valueOf(pollingFrequency));

        DonkeyElement pollingType = element.getChildElement("pollingType");
        boolean isInterval = pollingType.getTextContent().equals("interval");
        pollingType.setTextContent(isInterval ? "INTERVAL" : "TIME");
        element.addChildElementIfNotExists("pollOnStart", isInterval ? "true" : "false");

        element.addChildElementIfNotExists("cronJobs");

        DonkeyElement advancedProperties = element.addChildElementIfNotExists("pollConnectorPropertiesAdvanced");
        if (advancedProperties != null) {
            advancedProperties.addChildElementIfNotExists("weekly", "true");

            DonkeyElement activeDays = advancedProperties.addChildElementIfNotExists("inactiveDays");
            if (activeDays != null) {
                for (int counter = 0; counter < 8; ++counter) {
                    activeDays.addChildElement("boolean", "false");
                }
            }

            advancedProperties.addChildElementIfNotExists("dayOfMonth", "1");
            advancedProperties.addChildElementIfNotExists("allDay", "true");
            advancedProperties.addChildElementIfNotExists("startingHour", "8");
            advancedProperties.addChildElementIfNotExists("startingMinute", "0");
            advancedProperties.addChildElementIfNotExists("endingHour", "17");
            advancedProperties.addChildElementIfNotExists("endingMinute", "0");
        }
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("pollingType", pollingType);
        purgedProperties.put("pollOnStart", pollOnStart);
        purgedProperties.put("pollingFrequency", pollingFrequency);
        purgedProperties.put("pollingHour", pollingHour);
        purgedProperties.put("pollingMinute", pollingMinute);
        purgedProperties.put("cronJobsCount", cronJobs.size());
        purgedProperties.put("advancedPurgedProperties", pollConnectorPropertiesAdvanced.getPurgedProperties());

        return purgedProperties;
    }
}