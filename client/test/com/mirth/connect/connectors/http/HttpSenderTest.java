package com.mirth.connect.connectors.http;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;

public class HttpSenderTest {
	
	private HttpSender sender;

	@SuppressWarnings("serial")
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PlatformUI.MIRTH_FRAME = new Frame() {
			@SuppressWarnings("rawtypes")
			public void setupCharsetEncodingForConnector(JComboBox charsetEncodingCombobox) {
				// do nothing
			}
		};
	}
	
	@Before
	public void setup() throws Exception {
		sender = new HttpSender();
	}

	@Test
	public void testCheckProperties() {
		
		HttpDispatcherProperties props = new HttpDispatcherProperties();
		
		// host
		props.setHost("");
		assertFalse(sender.checkProperties(props, true));
		
		props.setHost("testHost");
		assert(sender.checkProperties(props, true));
		
		// proxy server
		props.setUseProxyServer(true);
		props.setProxyAddress(null);
		props.setProxyPort(null);
		assertFalse(sender.checkProperties(props, true));
		
		props.setProxyAddress("testAddress");
		props.setProxyPort(null);
		assertFalse(sender.checkProperties(props, true));
		
		props.setProxyAddress(null);
		props.setProxyPort("testPort");
		assertFalse(sender.checkProperties(props, true));
		
		props.setProxyAddress("testAddress");
		props.setProxyPort("testPort");
		assert(sender.checkProperties(props, true));
		
		props.setUseProxyServer(false);
		props.setProxyAddress(null);
		props.setProxyPort(null);
		assert(sender.checkProperties(props, true));
		
		props.setProxyAddress("testAddress");
		props.setProxyPort(null);
		assert(sender.checkProperties(props, true));
		
		props.setProxyAddress(null);
		props.setProxyPort("testPort");
		assert(sender.checkProperties(props, true));
		
		props.setProxyAddress("testAddress");
		props.setProxyPort("testPort");
		assert(sender.checkProperties(props, true));
		
		// PUT, POST, PATCH
		testContent("PUT");
		testContent("POST");
		testContent("PATCH");
	}

	private void testContent(String method) {
		Map<String, List<String>> params = new HashMap<>();
		params.put("testKey", new ArrayList<>());
		
		Map<String, List<String>> emptyParams = new HashMap<>();
		
		HttpDispatcherProperties props = new HttpDispatcherProperties();
		props.setHost("testHost");
		props.setUseProxyServer(false);
		
		props.setContentType("");
		assertFalse(sender.checkProperties(props, true));
		
		props.setContentType("contentType");
		assert(sender.checkProperties(props, true));
		
		props.setMethod(method);
		props.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		props.setParameters(null);
		assertFalse(sender.checkProperties(props, true));
		
		props.setParameters(emptyParams);
		assertFalse(sender.checkProperties(props, true));
		
		props.setParameters(params);
		assert(sender.checkProperties(props, true));
		
		props.setContentType(ContentType.APPLICATION_JSON.getMimeType());
		props.setParameters(null);
		assert(sender.checkProperties(props, true));
		
		props.setParameters(emptyParams);
		assert(sender.checkProperties(props, true));
		
		props.setParameters(params);
		assert(sender.checkProperties(props, true));
		
		props.setContent("");
		assert(sender.checkProperties(props, true));
		
		props.setContent("Test content");
		assert(sender.checkProperties(props, true));
	}
}
