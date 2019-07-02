package com.mirth.connect.connectors.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.MessageMaps;
import com.mirth.connect.userutil.AttachmentEntry;

public class SmtpDispatcherTest {

    SmtpDispatcher dispatcher;
    SmtpDispatcherProperties props;
    Channel channel;
    Message message;
    Response response;
    CustomMessageMap messageMap;
    Map<Object, Object> headersFromMessageMap;

    @Before
    public void setup() {
        dispatcher = new SmtpDispatcher();
        props = new SmtpDispatcherProperties();
        channel = Mockito.mock(Channel.class);
        doReturn("mockChannelId").when(channel).getChannelId();
        dispatcher.setChannel(channel);
        dispatcher.setConnectorProperties(props);
        message = new Message();
        message.setMessageId(1L);
        response = Mockito.mock(Response.class);
        messageMap = new CustomMessageMap(new HashMap<>());
        doReturn(messageMap).when(channel).getMessageMaps();
    }

    @Test
    public void testGetHeadersFromMap() {
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("testKey", "testItem");
        props.setHeadersMap(responseHeaders);

        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariable() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, String> expected = new HashMap<>();
        expected.put("customHeader", "customValue");
        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableWithNonStringValues() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        headerMap.put("numValue", 1);
        headerMap.put(4, 4);
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(11);
        mixedList.add("goodValue");
        headerMap.put("mixedList", mixedList);
        messageMap.map.put("myVar", headerMap);
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        Map<Object, Object> expected = new HashMap<>();
        expected.put("customHeader", "customValue");
        expected.put("numValue", "1");
        expected.put("4", "4");
        List<Object> expectedMixedList = new ArrayList<>();
        expectedMixedList.add("11");
        expectedMixedList.add("goodValue");
        expected.put("mixedList", String.valueOf(expectedMixedList));

        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromMapWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setHeadersVariable("myVar");
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("testKey", "testItem");
        props.setHeadersMap(responseHeaders);
        props.setUseHeadersVariable(false);

        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariableWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setHeadersVariable("myVar");
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("testKey", "testItem");
        props.setHeadersMap(responseHeaders);
        props.setUseHeadersVariable(true);

        HashMap<String, String> expected = new HashMap<>();
        expected.put("customHeader", "customValue");
        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromList() {
        List<Attachment> attachments = new ArrayList<>();
        Attachment att = new Attachment();
        att.setName("name");
        att.setContent("content");
        att.setMimeType("mimeType");
        attachments.add(att);
        props.setAttachmentsList(attachments);

        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(attachments, result);
    }

    @Test
    public void testGetAttachmentsFromVariable() {
        List<AttachmentEntry> attachmentList = new ArrayList<>();
        AttachmentEntry entry = new AttachmentEntry("name", "content", "mimeType");
        attachmentList.add(entry);
        messageMap.map.put("myVar", attachmentList);
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(true);

        List<Attachment> expected = new ArrayList<>();
        Attachment att = new Attachment();
        att.setName("name");
        att.setContent("content");
        att.setMimeType("mimeType");
        expected.add(att);
        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromVariableSkippingInvalidValues() {
        List<Object> attachments = new ArrayList<>();
        attachments.add(3);
        attachments.add("badValue");
        AttachmentEntry entry = new AttachmentEntry("name", "content", "mimeType");
        attachments.add(entry);
        messageMap.map.put("myVar", attachments);
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(true);

        List<Attachment> expected = new ArrayList<>();
        Attachment att = new Attachment();
        att.setName("name");
        att.setContent("content");
        att.setMimeType("mimeType");
        expected.add(att);
        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromListWhenBothMapAndVariableAreSet() {
        List<AttachmentEntry> attachmentsFromVariable = new ArrayList<>();
        AttachmentEntry entry = new AttachmentEntry("name", "content", "mimeType");
        attachmentsFromVariable.add(entry);
        messageMap.map.put("myVar", attachmentsFromVariable);
        props.setAttachmentsVariable("myVar");

        List<Attachment> attachmentsFromList = new ArrayList<>();
        Attachment att = new Attachment();
        att.setName("name");
        att.setContent("content");
        att.setMimeType("mimeType");
        attachmentsFromList.add(att);
        props.setAttachmentsList(attachmentsFromList);

        props.setUseAttachmentsVariable(false);

        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(attachmentsFromList, result);
    }

    @Test
    public void testGetParametersFromVariableWhenBothMapAndVariableAreSet() {
        List<AttachmentEntry> attachmentsFromVariable = new ArrayList<>();
        AttachmentEntry entry = new AttachmentEntry("name", "content", "mimeType");
        attachmentsFromVariable.add(entry);
        messageMap.map.put("myVar", attachmentsFromVariable);
        props.setAttachmentsVariable("myVar");

        List<Attachment> attachmentsFromList = new ArrayList<>();
        Attachment att = new Attachment();
        att.setName("name");
        att.setContent("content");
        att.setMimeType("mimeType");
        attachmentsFromList.add(att);
        props.setAttachmentsList(attachmentsFromList);

        props.setUseAttachmentsVariable(true);

        List<Attachment> expected = new ArrayList<>();
        Attachment att2 = new Attachment();
        att2.setName("name");
        att2.setContent("content");
        att2.setMimeType("mimeType");
        expected.add(att2);
        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetEmptyMapWhenHeadersVariableDoesNotExist() {
        props.setHeadersVariable("doesn't exist");
        props.setUseHeadersVariable(true);
        Map<String, String> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmptyListWhenAttachmentsVariableDoesNotExist() {
        props.setAttachmentsVariable("doesn't exist");
        props.setUseAttachmentsVariable(true);
        List<Attachment> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertTrue(result.isEmpty());
    }

    class CustomMessageMap extends MessageMaps {
        protected Map<Object, Object> map;

        public CustomMessageMap(Map<Object, Object> map) {
            this.map = map;
        }

        public CustomMessageMap() {}

        @Override
        public Object get(String key, ConnectorMessage connectorMessage, boolean includeResponseMap) {
            return map.get(key);
        }
    }
}
