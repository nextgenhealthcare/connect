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
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.CalendarToStringStyle;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A MessageObjectFilter is used to search the message store.
 * 
 */
@XStreamAlias("messageFilter")
public class MessageFilter implements Serializable {
    /*
     * Note that any filter criteria that is an int must be represented using
     * Integer otherwise it will default to 0 and not pass the isNotNull check
     * in the SQL mapping.
     */
    private Long maxMessageId;
    private Long messageIdUpper;
    private Long messageIdLower;
    private Long importIdUpper;
    private Long importIdLower;
    private Calendar startDate;
    private Calendar endDate;
    private String quickSearch;
    private Set<Status> statuses;
    private List<Integer> includedMetaDataIds;
    private List<Integer> excludedMetaDataIds;
    private String serverId;
    private List<ContentSearchElement> contentSearch;
    private List<MetaDataSearchElement> metaDataSearch;
    private List<String> quickSearchMetaDataColumns;
    private Integer sendAttemptsLower;
    private Integer sendAttemptsUpper;
    private String type;
    private String source;
    private Boolean attachment;

    public Long getMessageIdUpper() {
        return messageIdUpper;
    }

    public void setMessageIdUpper(Long messageIdUpper) {
        this.messageIdUpper = messageIdUpper;
    }

    public Long getMessageIdLower() {
        return messageIdLower;
    }

    public void setMessageIdLower(Long messageIdLower) {
        this.messageIdLower = messageIdLower;
    }

    public Long getImportIdUpper() {
        return importIdUpper;
    }

    public void setImportIdUpper(Long importIdUpper) {
        this.importIdUpper = importIdUpper;
    }

    public Long getImportIdLower() {
        return importIdLower;
    }

    public void setImportIdLower(Long importIdLower) {
        this.importIdLower = importIdLower;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public String getQuickSearch() {
        return quickSearch;
    }

    public void setQuickSearch(String quickSearch) {
        this.quickSearch = quickSearch;
    }

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<Status> statuses) {
        this.statuses = statuses;
    }

    public List<Integer> getIncludedMetaDataIds() {
        return includedMetaDataIds;
    }

    public void setIncludedMetaDataIds(List<Integer> includedMetaDataIds) {
        this.includedMetaDataIds = includedMetaDataIds;
    }
    
    public List<Integer> getExcludedMetaDataIds() {
        return excludedMetaDataIds;
    }

    public void setExcludedMetaDataIds(List<Integer> excludedMetaDataIds) {
        this.excludedMetaDataIds = excludedMetaDataIds;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

	public List<ContentSearchElement> getContentSearch() {
        return contentSearch;
    }

    public void setContentSearch(List<ContentSearchElement> contentSearch) {
        this.contentSearch = contentSearch;
    }

    public List<MetaDataSearchElement> getMetaDataSearch() {
        return metaDataSearch;
    }

    public void setMetaDataSearch(List<MetaDataSearchElement> metaDataSearch) {
        this.metaDataSearch = metaDataSearch;
    }

    public List<String> getQuickSearchMetaDataColumns() {
        return quickSearchMetaDataColumns;
    }

    public void setQuickSearchMetaDataColumns(List<String> quickSearchMetaDataColumns) {
        this.quickSearchMetaDataColumns = quickSearchMetaDataColumns;
    }

    public Integer getSendAttemptsLower() {
        return sendAttemptsLower;
    }

    public void setSendAttemptsLower(Integer sendAttemptsLower) {
        this.sendAttemptsLower = sendAttemptsLower;
    }

    public Integer getSendAttemptsUpper() {
        return sendAttemptsUpper;
    }

    public void setSendAttemptsUpper(Integer sendAttemptsUpper) {
        this.sendAttemptsUpper = sendAttemptsUpper;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getMaxMessageId() {
        return maxMessageId;
    }

    public void setMaxMessageId(Long maxMessageId) {
        this.maxMessageId = maxMessageId;
    }

    public Boolean getAttachment() {
        return attachment;
    }

    public void setAttachment(Boolean attachment) {
        this.attachment = attachment;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
