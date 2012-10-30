/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

public class StorageSettings {
    private boolean enabled = true;
    private boolean durable = true;
    private boolean rawDurable = true;
    
    private boolean messageRecoveryEnabled = true;

    private boolean storeAttachments = true;
    private boolean storeCustomMetaData = true;

    private boolean storeRaw = true;
    private boolean storeProcessedRaw = true;
    private boolean storeTransformed = true;
    private boolean storeEncoded = true;
    private boolean storeResponse = true;
    private boolean storeSent = true;
    private boolean storeProcessedResponse = true;
    private boolean storeSentResponse = true;

    private boolean storeMaps = true;
    private boolean storeResponseMap = true;
    private boolean storeMergedResponseMap = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Tell whether or not storage operations are durable (see setDurable())
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * If enabled, storage operations will guarantee (as much as possible) that the data is written
     * to the storage medium upon return (fsync and synchronous commits should be enabled for
     * database storage engines).
     */
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * Tell whether or not the initial raw message storage operation is durable
     */
    public boolean isRawDurable() {
        return rawDurable;
    }

    /**
     * See setDurable(). This setting defines durability specifically for the transaction that
     * stores the initial raw message
     */
    public void setRawDurable(boolean rawDurable) {
        this.rawDurable = rawDurable;
    }

    public boolean isMessageRecoveryEnabled() {
        return messageRecoveryEnabled;
    }

    public void setMessageRecoveryEnabled(boolean messageRecoveryEnabled) {
        this.messageRecoveryEnabled = messageRecoveryEnabled;
    }

    public boolean isStoreAttachments() {
        return storeAttachments;
    }

    public void setStoreAttachments(boolean storeAttachments) {
        this.storeAttachments = storeAttachments;
    }

    public boolean isStoreCustomMetaData() {
        return storeCustomMetaData;
    }

    public void setStoreCustomMetaData(boolean storeCustomMetaData) {
        this.storeCustomMetaData = storeCustomMetaData;
    }

    public boolean isStoreRaw() {
        return storeRaw;
    }

    public void setStoreRaw(boolean storeRaw) {
        this.storeRaw = storeRaw;
    }

    public boolean isStoreProcessedRaw() {
        return storeProcessedRaw;
    }

    public void setStoreProcessedRaw(boolean storeProcessedRaw) {
        this.storeProcessedRaw = storeProcessedRaw;
    }

    public boolean isStoreTransformed() {
        return storeTransformed;
    }

    public void setStoreTransformed(boolean storeTransformed) {
        this.storeTransformed = storeTransformed;
    }

    public boolean isStoreEncoded() {
        return storeEncoded;
    }

    public void setStoreEncoded(boolean storeEncoded) {
        this.storeEncoded = storeEncoded;
    }

    public boolean isStoreResponse() {
        return storeResponse;
    }

    public void setStoreResponse(boolean storeResponse) {
        this.storeResponse = storeResponse;
    }

    public boolean isStoreSent() {
        return storeSent;
    }

    public void setStoreSent(boolean storeSent) {
        this.storeSent = storeSent;
    }

    public boolean isStoreProcessedResponse() {
        return storeProcessedResponse;
    }

    public void setStoreProcessedResponse(boolean storeProcessedResponse) {
        this.storeProcessedResponse = storeProcessedResponse;
    }

    public boolean isStoreSentResponse() {
        return storeSentResponse;
    }

    public void setStoreSentResponse(boolean storeSentResponse) {
        this.storeSentResponse = storeSentResponse;
    }

    public boolean isStoreMaps() {
        return storeMaps;
    }

    public void setStoreMaps(boolean storeMaps) {
        this.storeMaps = storeMaps;
    }

    public boolean isStoreResponseMap() {
        return storeResponseMap;
    }

    public void setStoreResponseMap(boolean storeResponseMap) {
        this.storeResponseMap = storeResponseMap;
    }

    public boolean isStoreMergedResponseMap() {
        return storeMergedResponseMap;
    }

    public void setStoreMergedResponseMap(boolean storeMergedResponseMap) {
        this.storeMergedResponseMap = storeMergedResponseMap;
    }
}
