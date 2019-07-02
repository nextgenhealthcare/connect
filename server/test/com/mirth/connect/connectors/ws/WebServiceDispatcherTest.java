package com.mirth.connect.connectors.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
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

public class WebServiceDispatcherTest {

    WebServiceDispatcher dispatcher;
    WebServiceDispatcherProperties props;
    Channel channel;
    Message message;
    Response response;
    CustomMessageMap messageMap;
    Map<Object, Object> headersFromMessageMap;

    @Before
    public void setup() {
        dispatcher = new WebServiceDispatcher();
        props = new WebServiceDispatcherProperties();
        channel = Mockito.mock(Channel.class);
        doReturn("mockChannelId").when(channel).getChannelId();
        dispatcher.setChannel(channel);
        dispatcher.setConnectorProperties(props);
        message = new Message();
        message.setMessageId(1L);
        response = Mockito.mock(Response.class);
        messageMap = new CustomMessageMap();
        doReturn(messageMap).when(channel).getMessageMaps();
    }

    @Test
    public void testGetHeadersFromMap() {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setHeadersMap(responseHeaders);

        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariable() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", headerMap);
        messageMap.map = map;
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableHandlingInvalidValues() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        headerMap.put("badValue", 1);
        headerMap.put(4, 4);
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", headerMap);
        messageMap.map = map;
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        expected.put("customHeader", Collections.singletonList("customValue"));
        expected.put("badValue", Collections.singletonList("1"));
        expected.put("4", Collections.singletonList("4"));
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableWithListThatHasBothBothStringandNonStringEntries() {
        Map<Object, Object> headerMap = new HashMap<>();
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(11);
        mixedList.add("goodValue");
        headerMap.put("customHeader", mixedList);
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", headerMap);
        messageMap.map = map;
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("11");
        list.add("goodValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromMapWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", headerMap);
        messageMap.map = map;
        props.setHeadersVariable("myVar");
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setHeadersMap(responseHeaders);
        props.setUseHeadersVariable(false);

        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariableWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", headerMap);
        messageMap.map = map;
        props.setHeadersVariable("myVar");
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setHeadersMap(responseHeaders);
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromLists() {
        List<String> names = new ArrayList<String>();
        names.add("name");
        props.setAttachmentNames(names);
        List<String> contents = new ArrayList<String>();
        contents.add("content");
        props.setAttachmentContents(contents);
        List<String> types = new ArrayList<String>();
        types.add("type");
        props.setAttachmentTypes(types);

        List<AttachmentEntry> expected = new ArrayList<>();
        expected.add(new AttachmentEntry("name", "content", "type"));
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromVariable() {
        List<Object> attachmentList = new ArrayList<>();
        attachmentList.add(new AttachmentEntry("name", "content", "type"));
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", attachmentList);
        messageMap.map = map;
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(true);

        List<AttachmentEntry> expected = new ArrayList<>();
        expected.add(new AttachmentEntry("name", "content", "type"));
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromVariableSkippingInvalidValues() {
        List<Object> attachments = new ArrayList<>();
        attachments.add(3);
        attachments.add("badValue");
        AttachmentEntry entry = new AttachmentEntry("name", "content", "mimeType");
        attachments.add(entry);
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", attachments);
        messageMap.map = map;
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(true);

        List<AttachmentEntry> expected = new ArrayList<>();
        AttachmentEntry att = new AttachmentEntry("name", "content", "mimeType");
        expected.add(att);
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromListWhenBothMapAndVariableAreSet() {
        List<String> names = new ArrayList<String>();
        names.add("name1");
        props.setAttachmentNames(names);
        List<String> contents = new ArrayList<String>();
        contents.add("content1");
        props.setAttachmentContents(contents);
        List<String> types = new ArrayList<String>();
        types.add("type1");
        props.setAttachmentTypes(types);

        List<Object> attachmentList = new ArrayList<>();
        attachmentList.add(new AttachmentEntry("name2", "content2", "type2"));
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", attachmentList);
        messageMap.map = map;
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(false);

        List<AttachmentEntry> expected = new ArrayList<>();
        AttachmentEntry att = new AttachmentEntry("name1", "content1", "type1");
        expected.add(att);
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetAttachmentsFromVariableWhenBothMapAndVariableAreSet() {
        List<String> names = new ArrayList<String>();
        names.add("name1");
        props.setAttachmentNames(names);
        List<String> contents = new ArrayList<String>();
        contents.add("content1");
        props.setAttachmentContents(contents);
        List<String> types = new ArrayList<String>();
        types.add("type1");
        props.setAttachmentTypes(types);

        List<Object> attachmentList = new ArrayList<>();
        attachmentList.add(new AttachmentEntry("name2", "content2", "type2"));
        Map<Object, Object> map = new HashMap<>();
        map.put("myVar", attachmentList);
        messageMap.map = map;
        props.setAttachmentsVariable("myVar");
        props.setUseAttachmentsVariable(true);

        List<AttachmentEntry> expected = new ArrayList<>();
        AttachmentEntry att = new AttachmentEntry("name2", "content2", "type2");
        expected.add(att);
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetEmptyMapWhenHeadersVariableDoesNotExist() {
        props.setHeadersVariable("doesn't exist");
        props.setUseHeadersVariable(true);
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEmptyListWhenAttachmentsVariableDoesNotExist() {
        props.setAttachmentsVariable("doesn't exist");
        props.setUseAttachmentsVariable(true);
        List<AttachmentEntry> result = dispatcher.getAttachments(props, Mockito.mock(ConnectorMessage.class));
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
