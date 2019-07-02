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
import com.mirth.connect.donkey.util.MessageMaps;

public class HttpDispatcherTest {

    HttpDispatcher dispatcher;
    HttpDispatcherProperties props;
    Channel channel;
    Message message;
    Response response;
    CustomMessageMap messageMap;
    Map<Object, Object> headersFromMessageMap;

    @Before
    public void setup() {
        dispatcher = new HttpDispatcher();
        props = new HttpDispatcherProperties();
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
        messageMap.map.put("myVar", headerMap);
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
        headerMap.put("numericValue", 1);
        headerMap.put(4, 4);
        messageMap.map.put("myVar", headerMap);
        props.setHeadersVariable("myVar");
        props.setUseHeadersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        expected.put("customHeader", Collections.singletonList("customValue"));
        expected.put("numericValue", Collections.singletonList(String.valueOf(1)));
        expected.put(String.valueOf(4), Collections.singletonList(String.valueOf(4)));
        Map<String, List<String>> result = dispatcher.getHeaders(props, Mockito.mock(ConnectorMessage.class));
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
        messageMap.map.put("myVar", headerMap);
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
        messageMap.map.put("myVar", headerMap);
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
    public void testGetParametersFromMap() {
        Map<String, List<String>> parameters = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        parameters.put("testKey", value);
        props.setParametersMap(parameters);

        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(parameters, result);
    }

    @Test
    public void testGetParametersFromVariable() {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("customParam", "customValue");
        messageMap.map.put("myVar", parameters);
        props.setParametersVariable("myVar");
        props.setUseParametersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customParam", list);
        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetParametersWithNonStringValues() {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("customParam", "customValue");
        parameters.put("numValue", 1);
        parameters.put(4, 4);
        List<Integer> numList = new ArrayList<>();
        numList.add(11);
        numList.add(12);
        parameters.put("numValue2", numList);
        messageMap.map.put("myVar", parameters);
        props.setParametersVariable("myVar");
        props.setUseParametersVariable(true);

        HashMap<String, List<Object>> expected = new HashMap<>();
        expected.put("customParam", Collections.singletonList("customValue"));
        expected.put("numValue", Collections.singletonList(String.valueOf(1)));
        expected.put(String.valueOf(4), Collections.singletonList(String.valueOf(4)));

        List<Object> list = new ArrayList<Object>();
        list.add("11");
        list.add("12");
        expected.put("numValue2", list);

        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetParametersFromVariableWithListThatHasBothBothStringAndNonStringEntries() {
        Map<Object, Object> parameters = new HashMap<>();
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(11);
        mixedList.add("goodValue");
        parameters.put("customParam", mixedList);
        messageMap.map.put("myVar", parameters);
        props.setParametersVariable("myVar");
        props.setUseParametersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("11");
        list.add("goodValue");
        expected.put("customParam", list);
        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(expected, result);
    }

    @Test
    public void testGetParametersFromMapWhenBothMapAndVariableAreSet() {
        Map<Object, Object> varMap = new HashMap<>();
        varMap.put("customParam", "customValue");
        messageMap.map.put("myVar", varMap);
        props.setParametersVariable("myVar");
        Map<String, List<String>> parameters = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        parameters.put("testKey", value);
        props.setParametersMap(parameters);
        props.setUseParametersVariable(false);

        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
        assertEquals(parameters, result);
    }

    @Test
    public void testGetParametersFromVariableWhenBothMapAndVariableAreSet() {
        Map<Object, Object> varMap = new HashMap<>();
        varMap.put("customParam", "customValue");
        messageMap.map.put("myVar", varMap);
        props.setParametersVariable("myVar");
        Map<String, List<String>> parameters = new HashMap<>();
        List<String> value = new ArrayList<String>();
        value.add("testItem");
        parameters.put("testKey", value);
        props.setParametersMap(parameters);
        props.setUseParametersVariable(true);

        HashMap<String, List<String>> expected = new HashMap<>();
        List<String> list = new ArrayList<String>();
        list.add("customValue");
        expected.put("customParam", list);
        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
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
    public void testGetEmptyMapWhenParameterVariableDoesNotExist() {
        props.setParametersVariable("doesn't exist");
        props.setUseParametersVariable(true);
        Map<String, List<String>> result = dispatcher.getParameters(props, Mockito.mock(ConnectorMessage.class));
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
