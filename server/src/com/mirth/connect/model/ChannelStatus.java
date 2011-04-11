/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A ChannelStatus represents the status of a deployed Channel.
 * 
 */
@XStreamAlias("channelStatus")
public class ChannelStatus implements Serializable {
    public enum State {
        STARTED, STOPPED, PAUSED
    };

    private String channelId;
    private String name;
    private State state;
    private int deployedRevisionDelta;
    private Calendar deployedDate;

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setDeployedDate(Calendar deployedDate) {
        this.deployedDate = deployedDate;
    }

    public Calendar getDeployedDate() {
        return this.deployedDate;
    }

    public void setDeployedRevisionDelta(int deployedRevisionDelta) {
        this.deployedRevisionDelta = deployedRevisionDelta;
    }

    public int getDeployedRevisionDelta() {
        return this.deployedRevisionDelta;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
