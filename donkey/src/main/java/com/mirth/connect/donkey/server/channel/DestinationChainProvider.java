/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;

public class DestinationChainProvider {
    private Integer chainId;
    private String channelId;
    private List<Integer> metaDataIds = new ArrayList<Integer>();
    private Map<Integer, DestinationConnector> destinationConnectors = new LinkedHashMap<Integer, DestinationConnector>();
    private DonkeyDaoFactory daoFactory;
    private StorageSettings storageSettings;

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void addDestination(int metaDataId, DestinationConnector connector) {
        if (!metaDataIds.contains(metaDataId)) {
            metaDataIds.add(metaDataId);
        }

        destinationConnectors.put(metaDataId, connector);
        connector.setOrderId(destinationConnectors.size());
    }

    public Map<Integer, DestinationConnector> getDestinationConnectors() {
        return destinationConnectors;
    }

    public List<Integer> getMetaDataIds() {
        return metaDataIds;
    }

    protected DonkeyDaoFactory getDaoFactory() {
        return daoFactory;
    }

    protected void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    protected StorageSettings getStorageSettings() {
        return storageSettings;
    }

    protected void setStorageSettings(StorageSettings storageSettings) {
        this.storageSettings = storageSettings;
    }

    public DestinationChain getChain() {
        return new DestinationChain(this);
    }
}