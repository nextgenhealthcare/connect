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
import java.util.Map;

public class RawMessage implements Serializable {
    private boolean overwrite;
    private boolean imported;
    private Long originalMessageId;
    private String rawData;
    private byte[] rawBytes;
    private Collection<Integer> destinationMetaDataIds;
    private Map<String, Object> sourceMap = new HashMap<String, Object>();
    private Boolean binary;

    public RawMessage(String rawData) {
        this(rawData, null);
    }

    public RawMessage(String rawData, Collection<Integer> destinationMetaDataIds) {
        this(rawData, destinationMetaDataIds, null);
    }

    public RawMessage(String rawData, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this.rawData = rawData;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        this.destinationMetaDataIds = destinationMetaDataIds;
        this.binary = false;
    }

    public RawMessage(byte[] rawBytes) {
        this(rawBytes, null);
    }

    public RawMessage(byte[] rawBytes, Collection<Integer> destinationMetaDataIds) {
        this(rawBytes, destinationMetaDataIds, null);
    }

    public RawMessage(byte[] rawBytes, Collection<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this.rawBytes = rawBytes;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        this.destinationMetaDataIds = destinationMetaDataIds;
        this.binary = true;
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

    public void clearMessage() {
        this.rawBytes = null;
        this.rawData = null;
    }
}
