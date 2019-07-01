/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.SerializerProvider;
import com.mirth.connect.donkey.util.xstream.XStreamSerializer;

public class TestChannel extends Channel {
    private List<Long> messageIds = new ArrayList<Long>();
    private boolean isDeployed = false;
    private volatile boolean queueThreadRunning = false;
    private List<Message> unfinishedMessages = null;

    public TestChannel() {
        super();
        setDaoFactory(new BufferedDaoFactory(Donkey.getInstance().getDaoFactory(), new SerializerProvider() {
            @Override
            public Serializer getSerializer(Integer metaDataId) {
                return new XStreamSerializer();
            }
        }, Donkey.getInstance().getStatisticsUpdater()));
    }

    public int getNumMessages() {
        return messageIds.size();
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public boolean isDeployed() {
        return isDeployed;
    }

    public boolean isQueueThreadRunning() {
        return queueThreadRunning;
    }

    /**
     * Deprecating this method as part of MIRTH-3181 since the tests are the only thing that was
     * using the returned value from RecoveryTask. RecoveryTask no longer returns anything
     * 
     * @deprecated
     */
    @Deprecated
    public List<Message> getUnfinishedMessages() {
        return unfinishedMessages;
    }

    @Override
    public void deploy() throws DeployException {
        super.deploy();
        isDeployed = true;
    }

    @Override
    public void undeploy() throws UndeployException {
        super.undeploy();
        isDeployed = false;
    }

    @Override
    public void queue(ConnectorMessage sourceMessage) {
        super.queue(sourceMessage);
    }

    @Override
    public Message process(ConnectorMessage sourceMessage, boolean markAsProcessed) throws InterruptedException {
        Message message = super.process(sourceMessage, markAsProcessed);
        messageIds.add(message.getMessageId());
        return message;
    }

    @Override
    public void processUnfinishedMessages() throws Exception {
        // We only run it once and store it because the tests usually call channel.start() before calling this method directly. 
        // Channel.start() also calls this method so there is nothing left to process by the time we actual want the return value.

        super.processUnfinishedMessages();
    }

    @Override
    public void run() {
        queueThreadRunning = true;
        super.run();
        queueThreadRunning = false;
    }
}