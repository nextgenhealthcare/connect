/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.List;
import java.util.Map;

/**
 * This class represents a raw message as it is received by a channel, and is
 * used to retrieve details such as the raw data or channel map.
 */
public class RawMessage {
    private com.mirth.connect.donkey.model.message.RawMessage rawMessage;

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawData
     *            The textual data to dispatch to the channel.
     */
    public RawMessage(String rawData) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData);
    }

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawData
     *            The textual data to dispatch to the channel.
     * @param destinationMetaDataIds
     *            A list of integers (metadata IDs) representing which
     *            destinations to dispatch the message to.
     */
    public RawMessage(String rawData, List<Integer> destinationMetaDataIds) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData, destinationMetaDataIds);
    }

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawData
     *            The textual data to dispatch to the channel.
     * @param destinationMetaDataIds
     *            A list of integers (metadata IDs) representing which
     *            destinations to dispatch the message to.
     * @param channelMap
     *            Any values placed in this map will be populated in the channel
     *            map at the beginning of the message's lifecycle.
     */
    public RawMessage(String rawData, List<Integer> destinationMetaDataIds, Map<String, Object> channelMap) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawData, destinationMetaDataIds, channelMap);
    }

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawBytes
     *            The binary data (byte array) to dispatch to the channel.
     */
    public RawMessage(byte[] rawBytes) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes);
    }

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawBytes
     *            The binary data (byte array) to dispatch to the channel.
     * @param destinationMetaDataIds
     *            A list of integers (metadata IDs) representing which
     *            destinations to dispatch the message to.
     */
    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes, destinationMetaDataIds);
    }

    /**
     * Instantiates a RawMessage object to dispatch to a channel.
     * 
     * @param rawBytes
     *            The binary data (byte array) to dispatch to the channel.
     * @param destinationMetaDataIds
     *            A list of integers (metadata IDs) representing which
     *            destinations to dispatch the message to.
     * @param channelMap
     *            Any values placed in this map will be populated in the channel
     *            map at the beginning of the message's lifecycle.
     */
    public RawMessage(byte[] rawBytes, List<Integer> destinationMetaDataIds, Map<String, Object> channelMap) {
        rawMessage = new com.mirth.connect.donkey.model.message.RawMessage(rawBytes, destinationMetaDataIds, channelMap);
    }

    /**
     * Returns the textual data to be dispatched to a channel.
     */
    public String getRawData() {
        return rawMessage.getRawData();
    }

    /**
     * Returns the binary data (byte array) to be dispatched to a channel.
     */
    public byte[] getRawBytes() {
        return rawMessage.getRawBytes();
    }

    /**
     * Returns the list of integers (metadata IDs) representing which
     * destinations to dispatch the message to.
     */
    public List<Integer> getDestinationMetaDataIds() {
        return rawMessage.getDestinationMetaDataIds();
    }

    /**
     * Sets which destinations to dispatch the message to.
     * 
     * @param destinationMetaDataIds
     *            A list of integers (metadata IDs) representing which
     *            destinations to dispatch the message to.
     */
    public void setDestinationMetaDataIds(List<Integer> destinationMetaDataIds) {
        rawMessage.setDestinationMetaDataIds(destinationMetaDataIds);
    }

    /**
     * Returns the channel map to be used at the beginning of the channel
     * dispatch.
     */
    public Map<String, Object> getChannelMap() {
        return rawMessage.getChannelMap();
    }

    /**
     * Sets the channel map to be used at the beginning of the channel dispatch.
     * 
     * @param channelMap
     *            Any values placed in this map will be populated in the channel
     *            map at the beginning of the message's lifecycle.
     */
    public void setChannelMap(Map<String, Object> channelMap) {
        rawMessage.setChannelMap(channelMap);
    }

    /**
     * Returns a Boolean representing whether this object contains textual or
     * binary data.
     */
    public Boolean isBinary() {
        return rawMessage.isBinary();
    }

    /**
     * Removes references to any data (textual or binary) currently stored by
     * the raw message.
     */
    public void clearMessage() {
        rawMessage.clearMessage();
    }
}