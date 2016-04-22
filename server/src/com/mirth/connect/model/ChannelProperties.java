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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelProperties")
public class ChannelProperties implements Serializable, Migratable, Purgable {
    private boolean clearGlobalChannelMap;
    private MessageStorageMode messageStorageMode;
    private boolean encryptData;
    private boolean removeContentOnCompletion;
    private boolean removeOnlyFilteredOnCompletion;
    private boolean removeAttachmentsOnCompletion;
    private DeployedState initialState;
    private boolean storeAttachments;
    private Set<String> tags;
    private List<MetaDataColumn> metaDataColumns;
    private AttachmentHandlerProperties attachmentProperties;
    private Integer pruneMetaDataDays;
    private Integer pruneContentDays;
    private boolean archiveEnabled;
    private Map<String, String> resourceIds;

    public ChannelProperties() {
        clearGlobalChannelMap = true;
        messageStorageMode = MessageStorageMode.DEVELOPMENT;
        encryptData = false;
        initialState = DeployedState.STARTED;
        tags = new LinkedHashSet<String>();
        metaDataColumns = new ArrayList<MetaDataColumn>();
        attachmentProperties = AttachmentHandlerType.NONE.getDefaultProperties();
        archiveEnabled = true;
        resourceIds = new LinkedHashMap<String, String>();
        resourceIds.put(ResourceProperties.DEFAULT_RESOURCE_ID, ResourceProperties.DEFAULT_RESOURCE_NAME);
    }

    public boolean isClearGlobalChannelMap() {
        return clearGlobalChannelMap;
    }

    public void setClearGlobalChannelMap(boolean clearGlobalChannelMap) {
        this.clearGlobalChannelMap = clearGlobalChannelMap;
    }

    public MessageStorageMode getMessageStorageMode() {
        return messageStorageMode;
    }

    public void setMessageStorageMode(MessageStorageMode messageStorageMode) {
        this.messageStorageMode = messageStorageMode;
    }

    public boolean isEncryptData() {
        return encryptData;
    }

    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    public boolean isRemoveContentOnCompletion() {
        return removeContentOnCompletion;
    }

    public void setRemoveContentOnCompletion(boolean removeContentOnCompletion) {
        this.removeContentOnCompletion = removeContentOnCompletion;
    }

    public boolean isRemoveOnlyFilteredOnCompletion() {
        return removeOnlyFilteredOnCompletion;
    }

    public void setRemoveOnlyFilteredOnCompletion(boolean removeOnlyFilteredOnCompletion) {
        this.removeOnlyFilteredOnCompletion = removeOnlyFilteredOnCompletion;
    }

    public boolean isRemoveAttachmentsOnCompletion() {
        return removeAttachmentsOnCompletion;
    }

    public void setRemoveAttachmentsOnCompletion(boolean removeAttachmentsOnCompletion) {
        this.removeAttachmentsOnCompletion = removeAttachmentsOnCompletion;
    }

    public DeployedState getInitialState() {
        return initialState;
    }

    public void setInitialState(DeployedState initialState) {
        this.initialState = initialState;
    }

    public boolean isStoreAttachments() {
        return storeAttachments;
    }

    public void setStoreAttachments(boolean storeAttachments) {
        this.storeAttachments = storeAttachments;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }

    public AttachmentHandlerProperties getAttachmentProperties() {
        return attachmentProperties;
    }

    public void setAttachmentProperties(AttachmentHandlerProperties attachmentProperties) {
        this.attachmentProperties = attachmentProperties;
    }

    public Integer getPruneMetaDataDays() {
        return pruneMetaDataDays;
    }

    public void setPruneMetaDataDays(Integer pruneMetaDataDays) {
        this.pruneMetaDataDays = pruneMetaDataDays;
    }

    public Integer getPruneContentDays() {
        return pruneContentDays;
    }

    public void setPruneContentDays(Integer pruneContentDays) {
        this.pruneContentDays = pruneContentDays;
    }

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        this.archiveEnabled = archiveEnabled;
    }

    public Map<String, String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Map<String, String> resourceIds) {
        this.resourceIds = resourceIds;
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
    public void migrate3_2_0(DonkeyElement element) {
        DonkeyElement resourceIdsElement = element.addChildElement("resourceIds");
        resourceIdsElement.setAttribute("class", "linked-hash-set");
        resourceIdsElement.addChildElement("string", "Default Resource");
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        element.addChildElementIfNotExists("removeOnlyFilteredOnCompletion", "false");
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {
        DonkeyElement resourceIdsElement = element.getChildElement("resourceIds");
        List<DonkeyElement> resourceIdsList = resourceIdsElement.getChildElements();
        resourceIdsElement.removeChildren();
        resourceIdsElement.setAttribute("class", "linked-hash-map");

        for (DonkeyElement resourceId : resourceIdsList) {
            DonkeyElement entry = resourceIdsElement.addChildElement("entry");
            String resourceIdText = resourceId.getTextContent();
            entry.addChildElement("string", resourceIdText);
            if (resourceIdText.equals("Default Resource")) {
                entry.addChildElement("string", "[Default Resource]");
            } else {
                entry.addChildElement("string");
            }
        }
    }

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("clearGlobalChannelMap", clearGlobalChannelMap);
        purgedProperties.put("messageStorageMode", messageStorageMode);
        purgedProperties.put("encryptData", encryptData);
        purgedProperties.put("removeContentOnCompletion", removeContentOnCompletion);
        purgedProperties.put("removeAttachmentsOnCompletion", removeAttachmentsOnCompletion);
        purgedProperties.put("initialState", initialState);
        purgedProperties.put("storeAttachments", storeAttachments);
        purgedProperties.put("tagCount", tags.size());
        purgedProperties.put("metaDataColumns", PurgeUtil.purgeList(metaDataColumns));
        purgedProperties.put("attachmentProperties", attachmentProperties.getPurgedProperties());
        purgedProperties.put("pruneMetaDataDays", pruneMetaDataDays);
        purgedProperties.put("pruneContentDays", pruneContentDays);
        purgedProperties.put("archiveEnabled", archiveEnabled);
        purgedProperties.put("resourceIdsCount", resourceIds.size());
        return purgedProperties;
    }
}
