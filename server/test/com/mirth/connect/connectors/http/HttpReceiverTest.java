package com.mirth.connect.connectors.http;

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
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.util.MessageMaps;

public class HttpReceiverTest {

    HttpReceiver receiver;
    HttpReceiverProperties props;
    Channel channel;
    Message message;
    Response response;
    DispatchResult dispatchResult;
    CustomMessageMap messageMap;
    Map<Object, Object> headersFromMessageMap;

    @Before
    public void setup() {
        receiver = new HttpReceiver();
        props = new HttpReceiverProperties();
        channel = Mockito.mock(Channel.class);
        doReturn("mockChannelId").when(channel).getChannelId();
        receiver.setChannel(channel);
        receiver.setConnectorProperties(props);
        message = new Message();
        message.setMessageId(1L);
        response = Mockito.mock(Response.class);
        messageMap = new CustomMessageMap();
        dispatchResult = new TestDispatchResult(1L, message, response, true, true);
        doReturn(messageMap).when(channel).getMessageMaps();
    }

    @Test
    public void testGetHeadersFromMap() {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setResponseHeadersMap(responseHeaders);

        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariable() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableWithListOfValues() {
        Map<Object, Object> headerMap = new HashMap<>();
        ArrayList<String> list = new ArrayList<>();
        list.add("custom1");
        list.add("custom2");
        headerMap.put("customHeader", list);
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> expectedList = new ArrayList<String>();
        expectedList.add("custom1");
        expectedList.add("custom2");
        expected.put("customHeader", expectedList);
        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableWithNonStringValues() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        headerMap.put("numValue", 1);
        headerMap.put(4, 4);
        List<Integer> numList = new ArrayList<>();
        numList.add(11);
        numList.add(12);
        headerMap.put("numValue2", numList);
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<Object>> expected = new HashMap<>();
        expected.put("customHeader", Collections.singletonList("customValue"));
        expected.put("numValue", Collections.singletonList(String.valueOf(1)));
        expected.put(String.valueOf(4), Collections.singletonList(String.valueOf(4)));

        List<Object> list = new ArrayList<Object>();
        list.add("11");
        list.add("12");
        expected.put("numValue2", list);

        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromVariableWithListThatHasBothBothStringAndNonStringEntries() {
        Map<Object, Object> headerMap = new HashMap<>();
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(11);
        mixedList.add("goodValue");
        headerMap.put("customHeader", mixedList);
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();

        List<String> list = new ArrayList<String>();
        list.add("11");
        list.add("goodValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(expected, result);
    }

    @Test
    public void testGetHeadersFromMapWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setResponseHeadersMap(responseHeaders);
        props.setUseHeadersVariable(false);

        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(responseHeaders, result);
    }

    @Test
    public void testGetHeadersFromVariableWhenBothMapAndVariableAreSet() {
        Map<Object, Object> headerMap = new HashMap<>();
        headerMap.put("customHeader", "customValue");
        messageMap.map.put("myVar", headerMap);
        props.setResponseHeadersVariable("myVar");
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        responseHeaders.put("testKey", value);
        props.setResponseHeadersMap(responseHeaders);
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customHeader", list);
        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertEquals(expected, result);
    }

    @Test
    public void testGetEmptyMapWhenHeadersVariableDoesNotExist() {
        props.setResponseHeadersVariable("doesn't exist");
        props.setUseHeadersVariable(true);
        Map<String, List<String>> result = receiver.getHeaders(dispatchResult);
        assertTrue(result.isEmpty());
    }

    class TestDispatchResult extends DispatchResult {
        protected TestDispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean lockAcquired) {
            super(messageId, processedMessage, selectedResponse, markAsProcessed, lockAcquired);
        }
    }

    class CustomMessageMap extends MessageMaps {
        protected Map<Object, Object> map = new HashMap<>();

        @Override
        public Object get(String key, ConnectorMessage connectorMessage, boolean includeResponseMap) {
            return map.get(key);
        }
    }
}
