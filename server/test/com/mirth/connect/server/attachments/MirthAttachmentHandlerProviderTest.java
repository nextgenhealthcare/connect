/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.server.attachments.passthru.PassthruAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.MessageController;

public class MirthAttachmentHandlerProviderTest {

    private static final String CHARSET = "UTF-8";

    private static String channelId1;
    private static long messageId1;
    private static Attachment attachment1;

    private static String channelId2;
    private static long messageId2;
    private static Attachment attachment2;

    private static MessageController messageController;
    private static MirthAttachmentHandlerProvider attachmentHandlerProvider;

    @BeforeClass
    public static void setup() throws Exception {
        channelId1 = UUID.randomUUID().toString();
        messageId1 = 1L;
        attachment1 = new Attachment(UUID.randomUUID().toString(), "attachment1".getBytes(CHARSET), "text/plain");

        channelId2 = UUID.randomUUID().toString();
        messageId2 = 1L;
        attachment2 = new Attachment(UUID.randomUUID().toString(), "attachment1".getBytes(CHARSET), "text/plain");

        messageController = mock(MessageController.class);
        when(messageController.getMessageAttachment(channelId1, messageId1)).thenReturn(Collections.singletonList(attachment1));
        when(messageController.getMessageAttachment(channelId2, messageId2)).thenReturn(Collections.singletonList(attachment2));
        when(messageController.getMessageAttachment(channelId1, attachment1.getId(), messageId1)).thenReturn(attachment1);
        when(messageController.getMessageAttachment(channelId2, attachment2.getId(), messageId2)).thenReturn(attachment2);

        attachmentHandlerProvider = new PassthruAttachmentHandlerProvider(messageController);
    }

    /**
     * Reattach content from attachment 1
     */
    @Test
    public void testReAttachMessage1() throws Exception {
        String encoded = new StringBuilder("test1").append(attachment1.getAttachmentId()).append("test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId1, messageId1);

        String expected = new StringBuilder("test1").append(new String(attachment1.getContent(), CHARSET)).append("test2").toString();

        assertEquals(expected, result);
    }

    /**
     * Do not reattach, just expand token for attachment 1
     */
    @Test
    public void testReAttachMessage2() throws Exception {
        String encoded = new StringBuilder("test1").append(attachment1.getAttachmentId()).append("test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, false), CHARSET);

        verifyZeroInteractions(messageController);

        String expected = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").toString();

        assertEquals(expected, result);
    }

    /**
     * Reattach content from attachment 1 using expanded token
     */
    @Test
    public void testReAttachMessage3() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId1, messageId1);

        String expected = new StringBuilder("test1").append(new String(attachment1.getContent(), CHARSET)).append("test2").toString();

        assertEquals(expected, result);
    }

    /**
     * Reattach content from attachment 1 using expanded token, and actual connector message having
     * different channel/message IDs
     */
    @Test
    public void testReAttachMessage4() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId2, messageId2, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId1, attachment1.getId(), messageId1);

        String expected = new StringBuilder("test1").append(new String(attachment1.getContent(), CHARSET)).append("test2").toString();

        assertEquals(expected, result);
    }

    /**
     * Do not reattach, and leave expanded token alone
     */
    @Test
    public void testReAttachMessage5() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, false), CHARSET);

        verifyZeroInteractions(messageController);

        String expected = encoded;

        assertEquals(expected, result);
    }

    /**
     * Reattach content from attachments 1 and 2
     */
    @Test
    public void testReAttachMessage6() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").append(attachment2.getAttachmentId()).toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId2, messageId2, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId1, attachment1.getId(), messageId1);
        verify(messageController).getMessageAttachment(channelId2, messageId2);

        String expected = new StringBuilder("test1").append(new String(attachment1.getContent(), CHARSET)).append("test2").append(new String(attachment2.getContent(), CHARSET)).toString();

        assertEquals(expected, result);
    }

    /**
     * Content from attachment 1 should be reattached, but not attachment 2
     */
    @Test
    public void testReAttachMessage7() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").append(attachment2.getAttachmentId()).toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId1, messageId1);

        String expected = new StringBuilder("test1").append(new String(attachment1.getContent(), CHARSET)).append("test2").toString();

        assertEquals(expected, result);
    }

    /**
     * Test bogus attachment tokens
     */
    @Test
    public void testReAttachMessage8() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verifyZeroInteractions(messageController);

        String expected = encoded;

        assertEquals(expected, result);
    }

    /**
     * Test bogus attachment tokens
     */
    @Test
    public void testReAttachMessage9() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(attachment1.getId()).append("test2").toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId1, messageId1, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true), CHARSET);

        verifyZeroInteractions(messageController);

        String expected = encoded;

        assertEquals(expected, result);
    }

    /**
     * Test localOnly flag
     */
    @Test
    public void testReAttachMessage10() throws Exception {
        String encoded = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").append(attachment2.getAttachmentId()).toString();
        ConnectorMessage connectorMessage = getMockConnectorMessage(channelId2, messageId2, encoded);

        clearInvocations(messageController);

        String result = new String(attachmentHandlerProvider.reAttachMessage(encoded, connectorMessage, CHARSET, false, true, true), CHARSET);

        verify(messageController).getMessageAttachment(channelId2, messageId2);
        verifyNoMoreInteractions(messageController);

        String expected = new StringBuilder("test1${ATTACH:").append(channelId1).append(':').append(messageId1).append(':').append(attachment1.getId()).append("}test2").append(new String(attachment2.getContent(), CHARSET)).toString();

        assertEquals(expected, result);
    }

    private ConnectorMessage getMockConnectorMessage(String channelId, Long messageId, String encoded) {
        ConnectorMessage connectorMessage = mock(ConnectorMessage.class);
        when(connectorMessage.getChannelId()).thenReturn(channelId);
        when(connectorMessage.getMessageId()).thenReturn(messageId);

        MessageContent encodedContent = mock(MessageContent.class);
        when(encodedContent.getContent()).thenReturn(encoded);
        when(connectorMessage.getEncoded()).thenReturn(encodedContent);

        return connectorMessage;
    }
}
