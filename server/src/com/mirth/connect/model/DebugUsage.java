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
    private Integer duppCount;
    private Integer attachBatchCount;
    private Integer sourceConnectorCount;
    private Integer sourceFilterTransCount;
    private Integer destinationFilterTransCount;
    private Integer destinationConnectorCount;
    private Integer responseCount;
    private Integer invocationCount;

	public DebugUsage() {
		this.id = 0;
		this.serverId = null;
		this.duppCount = 0;
		this.attachBatchCount = 0;
		this.sourceConnectorCount = 0;
		this.sourceFilterTransCount = 0;
		this.destinationFilterTransCount = 0;
		this.destinationConnectorCount = 0;
		this.responseCount = 0;
		this.invocationCount = 0;
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

    public Integer getDuppCount() {
        return duppCount;
    }

    public void setDuppCount(Integer deployCount) {
        this.duppCount = deployCount;
    }
    
    public Integer getAttachBatchCount() {
		return attachBatchCount;
	}

	public void setAttachBatchCount(Integer attachBatchCount) {
		this.attachBatchCount = attachBatchCount;
	}

	public Integer getSourceConnectorCount() {
		return sourceConnectorCount;
	}

	public void setSourceConnectorCount(Integer sourceConnectorCount) {
		this.sourceConnectorCount = sourceConnectorCount;
	}


    public Integer getSourceFilterTransCount() {
		return sourceFilterTransCount;
	}

	public void setSourceFilterTransCount(Integer sourceFilterTransCount) {
		this.sourceFilterTransCount = sourceFilterTransCount;
	}

	public Integer getDestinationFilterTransCount() {
		return destinationFilterTransCount;
	}

	public void setDestinationFilterTransCount(Integer destinationFilterTransCount) {
		this.destinationFilterTransCount = destinationFilterTransCount;
	}

	public Integer getDestinationConnectorCount() {
		return destinationConnectorCount;
	}

	public void setDestinationConnectorCount(Integer destinationConnectorCount) {
		this.destinationConnectorCount = destinationConnectorCount;
	}

	public Integer getResponseCount() {
		return responseCount;
	}

	public void setResponseCount(Integer responseCount) {
		this.responseCount = responseCount;
	}
	
    public Integer getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(Integer invocationCount) {
        this.invocationCount = invocationCount;
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
