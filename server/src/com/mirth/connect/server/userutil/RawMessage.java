/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.List;
import java.util.Map;

public class RawMessage {
    private com.mirth.connect.donkey.model.message.RawMessage rawMessage;

    public RawMessage(String rawData) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData);
    }

    public RawMessage(String rawData, List<Integer> destinationMetaDataIds) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData, destinationMetaDataIds);
    }

    public RawMessage(String rawData, List<Integer> destinationMetaDataIds, Map<String, Object> channelMap) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData, destinationMetaDataIds, channelMap);
    }

    public RawMessage(byte[] rawBytes) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes);
    }

    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes, destinationMetaDataIds);
    }

    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds, Map<String, Object> channelMap) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes, destinationMetaDataIds, channelMap);
    }

    public String getRawData() {
        return rawMessage.getRawData();
    }

    public byte[] getRawBytes() {
        return rawMessage.getRawBytes();
    }

    public List<Integer> getDestinationMetaDataIds() {
        return rawMessage.getDestinationMetaDataIds();
    }

    public void setDestinationMetaDataIds(List<Integer> destinationMetaDataIds) {
        rawMessage.setDestinationMetaDataIds(destinationMetaDataIds);
    }

    public Map<String, Object> getChannelMap() {
        return rawMessage.getChannelMap();
    }

    public void setChannelMap(Map<String, Object> channelMap) {
        rawMessage.setChannelMap(channelMap);
    }

    public Boolean isBinary() {
        return rawMessage.isBinary();
    }

    public void clearMessage() {
        rawMessage.clearMessage();
    }
}