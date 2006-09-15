package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MessageObject implements Serializable {
	public enum Protocol {
		HL7, X12, XML
	}

	public enum Status {
		UNKNOWN, RECEIVED, ACCEPTED, REJECTED, TRANSFORMED, ERROR, SENT
	}

	private String id;
	private String channelId;
	private Status status;
	private Calendar dateCreated;
	private String rawData;
	private Protocol rawDataProtocol;
	private String transformedData;
	private Protocol transformedDataProtocol;
	private String encodedData;
	private Protocol encodedDataProtocol;
	private Map variableMap;
	private String connectorName;
	private boolean encrypted;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MessageObject() {
		this.variableMap = new HashMap();
		this.status = Status.UNKNOWN;
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

	public Map getVariableMap() {
		return this.variableMap;
	}

	public void setVariableMap(Map variableMap) {
		this.variableMap = variableMap;
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

	public boolean equals(Object source) {
		if (source instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) source;
			
			return (messageObject.getChannelId().equals(getChannelId()) &&
			messageObject.getConnectorName().equals(getConnectorName()) &&
			messageObject.getDateCreated().equals(getDateCreated()) &&
			messageObject.getEncodedData().equals(getEncodedData()) &&
			messageObject.getEncodedDataProtocol().equals(getEncodedDataProtocol()) &&
			messageObject.getId().equals(getId()) &&
			messageObject.getRawData().equals(getRawData()) &&
			messageObject.getRawDataProtocol().equals(getRawDataProtocol()) &&
			messageObject.getStatus().equals(getStatus()) &&
			messageObject.getTransformedData().equals(getTransformedData()) &&
			messageObject.getTransformedDataProtocol().equals(getTransformedDataProtocol()) &&
			messageObject.getVariableMap().equals(getVariableMap()));
		} else {
			return false;
		}
	}
}