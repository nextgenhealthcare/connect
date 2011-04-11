/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.filters;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.mirth.connect.model.CalendarToStringStyle;
import com.mirth.connect.model.MessageObject.Protocol;
import com.mirth.connect.model.MessageObject.Status;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A MessageObjectFilter is used to search the message store.
 * 
 */
@XStreamAlias("messageObjectFilter")
public class MessageObjectFilter implements Serializable {
    /*
     * Note that any filter criteria that is an int must be represented using
     * Integer otherwise it will default to 0 and not pass the isNotNull check
     * in the SQL mapping.
     */
    private String id;
    private String correlationId;
    private String channelId;
    private Calendar startDate;
    private Calendar endDate;
    private Status status;
    private String source;
    private String connectorName;
    private boolean searchRawData;
    private boolean searchTransformedData;
    private boolean searchEncodedData;
    private boolean searchErrors;
    private String quickSearch;
    private String searchCriteria;
    private String type;
    private Protocol protocol;
    private boolean ignoreQueued;
    private List<String> channelIdList;

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Calendar getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getConnectorName() {
        return this.connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public boolean isSearchEncodedData() {
        return searchEncodedData;
    }

    public void setSearchEncodedData(boolean searchEncodedData) {
        this.searchEncodedData = searchEncodedData;
    }

    public boolean isSearchRawData() {
        return searchRawData;
    }

    public void setSearchRawData(boolean searchRawData) {
        this.searchRawData = searchRawData;
    }

    public boolean isSearchTransformedData() {
        return searchTransformedData;
    }

    public void setSearchTransformedData(boolean searchTransformedData) {
        this.searchTransformedData = searchTransformedData;
    }

    public boolean isSearchErrors() {
        return searchErrors;
    }

    public void setSearchErrors(boolean searchErrors) {
        this.searchErrors = searchErrors;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getQuickSearch() {
        return quickSearch;
    }

    public void setQuickSearch(String quickSearch) {
        this.quickSearch = quickSearch;
    }

    public boolean isIgnoreQueued() {
        return ignoreQueued;
    }

    public void setIgnoreQueued(boolean ignoreQueued) {
        this.ignoreQueued = ignoreQueued;
    }

    public List<String> getChannelIdList() {
        return channelIdList;
    }

    public void setChannelIdList(List<String> channelIdList) {
        this.channelIdList = channelIdList;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
