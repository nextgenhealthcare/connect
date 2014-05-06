/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.server.Constants;

public class RawMessage {
    private boolean overwrite;
    private boolean imported;
    private Long originalMessageId;
    private String rawData;
    private byte[] rawBytes;
    private Map<String, Object> sourceMap = new HashMap<String, Object>();
    private Boolean binary;

    public RawMessage(String rawData) {
        this(rawData, null);
    }

    public RawMessage(String rawData, List<Integer> destinationMetaDataIds) {
        this(rawData, destinationMetaDataIds, null);
    }

    public RawMessage(String rawData, List<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this.rawData = rawData;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        setDestinationMetaDataIds(destinationMetaDataIds);
        this.binary = false;
    }

    public RawMessage(byte[] rawBytes) {
        this(rawBytes, null);
    }

    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds) {
        this(rawBytes, destinationMetaDataIds, null);
    }

    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds, Map<String, Object> sourceMap) {
        this.rawBytes = rawBytes;

        if (sourceMap != null) {
            setSourceMap(sourceMap);
        }

        setDestinationMetaDataIds(destinationMetaDataIds);
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

    public List<Integer> getDestinationMetaDataIds() {
        if (sourceMap != null) {
            return (List<Integer>) sourceMap.get(Constants.DESTINATION_META_DATA_IDS_KEY);
        }

        return null;
    }

    public void setDestinationMetaDataIds(List<Integer> destinationMetaDataIds) {
        if (destinationMetaDataIds != null) {
            sourceMap.put(Constants.DESTINATION_META_DATA_IDS_KEY, destinationMetaDataIds);
        } else {
            sourceMap.remove(Constants.DESTINATION_META_DATA_IDS_KEY);
        }
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
