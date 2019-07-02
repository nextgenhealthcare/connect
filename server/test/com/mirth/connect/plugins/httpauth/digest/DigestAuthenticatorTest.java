package com.mirth.connect.plugins.httpauth.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.util.MessageMaps;

public class DigestAuthenticatorTest {

    DigestAuthenticator authenticator;
    Connector connector;
    DigestAuthenticatorProvider provider;
    CustomMessageMap messageMap;
    DigestHttpAuthProperties props;

    @Before
    public void setup() {
        connector = Mockito.mock(Connector.class);
        provider = Mockito.mock(DigestAuthenticatorProvider.class);
        doReturn(connector).when(provider).getConnector();
        messageMap = new CustomMessageMap();
        messageMap.map = new HashMap<>();
        authenticator = new DigestAuthenticator(provider, messageMap);
        props = new DigestHttpAuthProperties();
    }

    @Test
    public void testGetCredentailsFromMap() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("user", "name");
        credentials.put("pass", "123");
        props.setCredentialsMap(credentials);

        Map<String, String> result = authenticator.getCredentials(props);
        assertEquals(credentials, result);
    }

    @Test
    public void testGetCredentailsFromVariable() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("user", "name");
        credentials.put("pass", "123");
        messageMap.map.put("myVar", credentials);
        props.setCredentialsVariable("myVar");
        props.setUseCredentialsVariable(true);

        Map<String, String> result = authenticator.getCredentials(props);
        assertEquals(credentials, result);
    }

    @Test
    public void testGetCredentialsFromVariableWithNonStringValues() {
        Map<Object, Object> credentials = new HashMap<>();
        credentials.put("user", "name");
        credentials.put("pass", "123");
        credentials.put("numValue", 1);
        credentials.put(4, 4);
        messageMap.map.put("myVar", credentials);
        props.setCredentialsVariable("myVar");
        props.setUseCredentialsVariable(true);

        HashMap<String, String> expected = new HashMap<>();
        expected.put("user", "name");
        expected.put("pass", "123");
        expected.put("numValue", "1");
        expected.put("4", "4");
        Map<String, String> result = authenticator.getCredentials(props);
        assertEquals(expected, result);
    }

    @Test
    public void testGetCredentialsFromMapWhenBothMapAndVariableAreSet() {
        Map<Object, Object> credentialsFromVar = new HashMap<>();
        credentialsFromVar.put("user", "name");
        credentialsFromVar.put("pass", "123");
        messageMap.map.put("myVar", credentialsFromVar);
        props.setCredentialsVariable("myVar");
        Map<String, String> credentialsMap = new HashMap<>();
        credentialsMap.put("user", "name");
        credentialsMap.put("pass", "123");
        props.setCredentialsMap(credentialsMap);
        props.setUseCredentialsVariable(false);

        Map<String, String> result = authenticator.getCredentials(props);
        assertEquals(credentialsMap, result);
    }

    @Test
    public void testGetCredentialsFromVariableWhenBothMapAndVariableAreSet() {
        Map<Object, Object> credentialsFromVar = new HashMap<>();
        credentialsFromVar.put("user", "name");
        credentialsFromVar.put("pass", "123");
        messageMap.map.put("myVar", credentialsFromVar);
        props.setCredentialsVariable("myVar");
        Map<String, String> credentialsMap = new HashMap<>();
        credentialsMap.put("user", "name");
        credentialsMap.put("pass", "123");
        props.setCredentialsMap(credentialsMap);
        props.setUseCredentialsVariable(false);

        Map<String, String> result = authenticator.getCredentials(props);
        assertEquals(credentialsFromVar, result);
    }

    @Test
    public void testGetEmptyMapWhenCredentialsVariableDoesNotExist() {
        props.setCredentialsVariable("doesn't exist");
        props.setUseCredentialsVariable(true);
        Map<String, String> result = authenticator.getCredentials(props);
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