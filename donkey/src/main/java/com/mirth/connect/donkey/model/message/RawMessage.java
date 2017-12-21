/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.model.message.attachment.Attachment;

public class RawMessage implements Serializable {
    private boolean overwrite;
    private boolean imported;
    private Long originalMessageId;
    private String rawData;
    private byte[] rawBytes;
    private Collection<Integer> destinationMetaDataIds;
    private Map<String, Object> sourceMap = new HashMap<String, Object>();
    private Boolean binary;
    private List<Attachment> attachments;

    public RawMessage(String rawData) {
        this(rawData, null);
    }

    public RawMessage(String rawData, Collection<Integer> destinationMetaDataIds) {
        this(rawData, destinationMetaDataIds, null);
    }

    public RawMessage(String rawData, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this(rawData, destinationMetaDataIds, sourceMap, null);
    }

    public RawMessage(String rawData, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap, List<Attachment> attachments) {
        this.rawData = rawData;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        this.destinationMetaDataIds = destinationMetaDataIds;
        this.binary = false;
        this.attachments = attachments;
    }

    public RawMessage(byte[] rawBytes) {
        this(rawBytes, null);
    }

    public RawMessage(byte[] rawBytes, Collection<Integer> destinationMetaDataIds) {
        this(rawBytes, destinationMetaDataIds, null);
    }

    public RawMessage(byte[] rawBytes, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this(rawBytes, destinationMetaDataIds, sourceMap, null);
    }

    public RawMessage(byte[] rawBytes, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap, List<Attachment> attachments) {
        this.rawBytes = rawBytes;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        this.destinationMetaDataIds = destinationMetaDataIds;
        this.binary = true;
        this.attachments = attachments;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public Long getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(Long messageId) {
        this.originalMessageId = messageId;
    }

    public String getRawData() {
        return rawData == null ? "" : rawData;
    }

    public byte[] getRawBytes() {
        return rawBytes == null ? new byte[0] : rawBytes;
    }

    public Collection<Integer> getDestinationMetaDataIds() {
        return destinationMetaDataIds;
    }

    public void setDestinationMetaDataIds(Collection<Integer> destinationMetaDataIds) {
        this.destinationMetaDataIds = destinationMetaDataIds;
    }

    public Map<String, Object> getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Map<String, Object> sourceMap) {
        this.sourceMap = sourceMap;
    }

    public Boolean isBinary() {
        return binary;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void clearMessage() {
        this.rawBytes = null;
        this.rawData = null;
        this.attachments = null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("overwrite=").append(overwrite).append(", ");
        builder.append("imported=").append(imported).append(", ");
        builder.append("originalMessageId=").append(originalMessageId).append(", ");
        builder.append("destinationMetaDataIds=").append(destinationMetaDataIds).append(", ");
        builder.append("binary=").append(binary).append(", ");
        builder.append("attachmentCount=").append(attachments != null ? attachments.size() : 0).append(']');
        return builder.toString();
    }
}
