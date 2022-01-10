/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("debugusage")
public class DebugUsage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String serverId;
    private Integer deployCount;
    private Integer invocationCount;
    private Integer postprocessorCount;
    private Integer preprocessorCount;
    private Integer undeployCount;
    private Calendar lastSent;
    
    

    public DebugUsage() {
        this.id = 0;
        this.deployCount = 0;
        this.invocationCount = 0;
        this.postprocessorCount = 0;
        this.preprocessorCount = 0;
        this.undeployCount = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Integer getDeployCount() {
        return deployCount;
    }

    public void setDeployCount(Integer deployCount) {
        this.deployCount = deployCount;
    }

    public Integer getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(Integer invocationCount) {
        this.invocationCount = invocationCount;
    }

    public Integer getPostprocessorCount() {
        return postprocessorCount;
    }

    public void setPostprocessorCount(Integer postprocessorCount) {
        this.postprocessorCount = postprocessorCount;
    }

    public Integer getPreprocessorCount() {
        return preprocessorCount;
    }

    public void setPreprocessorCount(Integer preprocessorCount) {
        this.preprocessorCount = preprocessorCount;
    }

    public Integer getUndeployCount() {
        return undeployCount;
    }

    public void setUndeployCount(Integer undeployCount) {
        this.undeployCount = undeployCount;
    }

    public Calendar getLastSent() {
        return lastSent;
    }

    public void setLastSent(Calendar lastSent) {
        this.lastSent = lastSent;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }

//    public String toAuditString() {
//        return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("id", id).append("username", username).toString();
//    }
}
