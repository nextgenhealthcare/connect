/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.server.queue.SourceQueue;
import com.mirth.connect.donkey.util.SerializerProvider;

public class ChannelTest {

    @BeforeClass
    public static void setupControllers() throws Exception {
        Donkey donkey = mock(Donkey.class);
        when(donkey.getEventDispatcher()).thenReturn(mock(EventDispatcher.class));

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(Donkey.class);
                bind(Donkey.class).toInstance(donkey);
            }
        });
        injector.getInstance(Donkey.class);

        // Make sure injection succeeded
        assertTrue(donkey == Donkey.getInstance());
    }

    @Test
    public void testRawMessageWithAttachments() throws Exception {
        Channel channel = createChannel();
        channel.start(null);
        long expectedMessageId = 0;

        // Test with no attachments
        RawMessage rawMessage = new RawMessage("");
        channel.dispatchRawMessage(rawMessage, false);
        expectedMessageId++;
        verify(channel.getDaoFactory().getDao(), times(0)).insertMessageAttachment(anyString(), anyLong(), any(Attachment.class));

        // Test with attachments
        List<Attachment> attachments = new ArrayList<Attachment>();
        Attachment attachment1 = new Attachment("1", new byte[0], "");
        attachments.add(attachment1);
        Attachment attachment2 = new Attachment("2", new byte[0], "");
        attachments.add(attachment2);
        rawMessage = new RawMessage("", null, null, attachments);

        channel.dispatchRawMessage(rawMessage, false);
        expectedMessageId++;
        verify(channel.getDaoFactory().getDao(), times(1)).insertMessageAttachment(channel.getChannelId(), expectedMessageId, attachment1);
        verify(channel.getDaoFactory().getDao(), times(1)).insertMessageAttachment(channel.getChannelId(), expectedMessageId, attachment2);
        assertNull(rawMessage.getAttachments());

        // Test with attachments, storage settings off
        channel.getStorageSettings().setStoreAttachments(false);

        attachments = new ArrayList<Attachment>();
        attachment1 = new Attachment("1", new byte[0], "");
        attachments.add(attachment1);
        rawMessage = new RawMessage("", null, null, attachments);

        channel.dispatchRawMessage(rawMessage, false);
        expectedMessageId++;
        verify(channel.getDaoFactory().getDao(), times(0)).insertMessageAttachment(channel.getChannelId(), expectedMessageId, attachment1);
        assertNull(rawMessage.getAttachments());
    }

    private Channel createChannel() {
        Channel channel = new Channel();
        channel.setChannelId(UUID.randomUUID().toString());
        channel.setProcessLock(mock(ChannelProcessLock.class));
        channel.setSourceQueue(mock(SourceQueue.class));

        SourceConnector sourceConnector = mock(SourceConnector.class);

        ConnectorProperties sourceConnectorProperties = mock(ConnectorProperties.class, withSettings().extraInterfaces(SourceConnectorPropertiesInterface.class));
        when(((SourceConnectorPropertiesInterface) sourceConnectorProperties).getSourceConnectorProperties()).thenReturn(mock(SourceConnectorProperties.class));
        when(sourceConnector.getConnectorProperties()).thenReturn(sourceConnectorProperties);

        when(sourceConnector.getInboundDataType()).thenReturn(mock(DataType.class));

        channel.setSourceConnector(sourceConnector);

        channel.addDestinationChainProvider(mock(DestinationChainProvider.class));

        DonkeyDaoFactory daoFactory = mock(DonkeyDaoFactory.class);

        DonkeyDao dao = mock(DonkeyDao.class);
        when(dao.getNextMessageId(anyString())).thenAnswer(new Answer<Long>() {
            private AtomicLong messageIdCounter = new AtomicLong(0);

            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return messageIdCounter.incrementAndGet();
            }
        });

        when(daoFactory.getDao()).thenReturn(dao);
        when(daoFactory.getDao(any(SerializerProvider.class))).thenReturn(dao);
        channel.setDaoFactory(daoFactory);

        return channel;
    }
}
