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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("messageObject")
public class MessageObject implements Serializable, Cloneable, Auditable {
	/**
	 * Need to set this so that messages written to the queue will not fail with new versions of this class
	 */
	private static final long serialVersionUID = 2451629582991455311L;

	public enum Protocol {
        HL7V2, X12, XML, HL7V3, EDI, NCPDP, DICOM, DELIMITED
	}

	public enum Status {
		UNKNOWN, RECEIVED, ACCEPTED, FILTERED, TRANSFORMED, ERROR, SENT, QUEUED
	}

	private String id;
    private String serverId;
	private String channelId;
	private String source;
	private String type;
	private Status status;
	private Calendar dateCreated;
	private String rawData;
	private Protocol rawDataProtocol;
	private String transformedData;
	private Protocol transformedDataProtocol;
	private String encodedData;
	private Protocol encodedDataProtocol;
	private String connectorName;
	private boolean encrypted;
	private String errors;
	private String version;
	private String correlationId;
    private boolean attachment;

	private Map connectorMap;
	private Map responseMap;
	private Map channelMap;
	
	private Map<String, Object> context;
	
	public MessageObject() {
		this.connectorMap = new ConcurrentHashMap();
		this.responseMap = new ConcurrentHashMap();
		this.channelMap = new ConcurrentHashMap();
		
		this.context = new HashMap<String, Object>();
		
		this.status = Status.UNKNOWN;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Calendar getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Calendar dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getEncodedData() {
		return this.encodedData;
	}

	public void setEncodedData(String encodedData) {
		this.encodedData = encodedData;
	}

	public Protocol getEncodedDataProtocol() {
		return this.encodedDataProtocol;
	}

	public void setEncodedDataProtocol(Protocol encodedDataProtocol) {
		this.encodedDataProtocol = encodedDataProtocol;
	}

	public String getRawData() {
		return this.rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public Protocol getRawDataProtocol() {
		return this.rawDataProtocol;
	}

	public void setRawDataProtocol(Protocol rawDataProtocol) {
		this.rawDataProtocol = rawDataProtocol;
	}

	public String getTransformedData() {
		return this.transformedData;
	}

	public void setTransformedData(String transformedData) {
		this.transformedData = transformedData;
	}

	public Protocol getTransformedDataProtocol() {
		return this.transformedDataProtocol;
	}

	public void setTransformedDataProtocol(Protocol transformedDataProtocol) {
		this.transformedDataProtocol = transformedDataProtocol;
	}

	public Map getConnectorMap() {
		return this.connectorMap;
	}

	public void setConnectorMap(Map variableMap) {
		this.connectorMap = variableMap;
	}

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getErrors() {
		return this.errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}
	
	public Map getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map responseMap) {
		this.responseMap = responseMap;
	}

	public Map getChannelMap() {
		return channelMap;
	}

	public void setChannelMap(Map channelMap) {
		this.channelMap = channelMap;
	}

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isAttachment() {
        return attachment;
    }

    public void setAttachment(boolean attachment) {
        this.attachment = attachment;
    }

	public Object clone() {
		MessageObject messageObject = new MessageObject();
        messageObject.setServerId(this.getServerId());
		messageObject.setChannelId(this.getChannelId());
		messageObject.setSource(this.getSource());
		messageObject.setType(this.getType());
		messageObject.setConnectorName(this.getConnectorName());
		messageObject.setDateCreated(this.getDateCreated());
		messageObject.setEncodedData(this.getEncodedData());
		messageObject.setEncodedDataProtocol(this.getEncodedDataProtocol());
		messageObject.setEncrypted(this.isEncrypted());
		messageObject.setErrors(this.getErrors());
		messageObject.setId(this.getId());
		messageObject.setRawData(this.getRawData());
		messageObject.setRawDataProtocol(this.getRawDataProtocol());
		messageObject.setStatus(this.getStatus());
		messageObject.setTransformedData(this.getTransformedData());
		messageObject.setTransformedDataProtocol(this.getTransformedDataProtocol());
		messageObject.setVersion(this.getVersion());
		messageObject.setCorrelationId(this.getCorrelationId());
		messageObject.setConnectorMap(this.getConnectorMap());
		messageObject.setResponseMap(this.getResponseMap());
		messageObject.setChannelMap(this.getChannelMap());
        messageObject.setAttachment(this.isAttachment());
		return messageObject;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof MessageObject)) {
			return false;
		}

		MessageObject messageObject = (MessageObject) that;

		return
			ObjectUtils.equals(this.getId(), messageObject.getId()) &&
            ObjectUtils.equals(this.getServerId(), messageObject.getServerId()) &&
			ObjectUtils.equals(this.getChannelId(), messageObject.getChannelId()) &&
			ObjectUtils.equals(this.getSource(), messageObject.getSource()) &&
			ObjectUtils.equals(this.getType(), messageObject.getType()) &&
			ObjectUtils.equals(this.getStatus(), messageObject.getStatus()) &&
			ObjectUtils.equals(this.getDateCreated(), messageObject.getDateCreated()) &&
			ObjectUtils.equals(this.getRawData(), messageObject.getRawData()) &&
			ObjectUtils.equals(this.getRawDataProtocol(), messageObject.getRawDataProtocol()) &&
			ObjectUtils.equals(this.getTransformedData(), messageObject.getTransformedData()) &&
			ObjectUtils.equals(this.getTransformedDataProtocol(), messageObject.getTransformedDataProtocol()) &&
			ObjectUtils.equals(this.getEncodedData(), messageObject.getEncodedData()) &&
			ObjectUtils.equals(this.getEncodedDataProtocol(), messageObject.getEncodedDataProtocol()) &&
			ObjectUtils.equals(this.getConnectorName(), messageObject.getConnectorName()) &&
			ObjectUtils.equals(this.isEncrypted(), messageObject.isEncrypted()) &&
			ObjectUtils.equals(this.getErrors(), messageObject.getErrors()) &&
			ObjectUtils.equals(this.getVersion(), messageObject.getVersion()) &&
			ObjectUtils.equals(this.getCorrelationId(), messageObject.getCorrelationId()) &&
			ObjectUtils.equals(this.getConnectorMap(), messageObject.getConnectorMap()) &&
			ObjectUtils.equals(this.getResponseMap(), messageObject.getResponseMap()) &&
            ObjectUtils.equals(this.getChannelMap(), messageObject.getChannelMap()) &&
            ObjectUtils.equals(this.getContext(), messageObject.getContext()) &&
            ObjectUtils.equals(this.isAttachment(), messageObject.isAttachment());
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
	
	public String toString() {
	    return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("id", id).append("channelId", channelId).append("status", status).toString();
	}
	
	public String toAuditString() {
	    return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("id", id).toString();
	}
}
