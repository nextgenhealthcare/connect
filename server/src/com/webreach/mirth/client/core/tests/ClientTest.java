package com.webreach.mirth.client.core.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;

public class ClientTest extends TestCase {
	private Client client = null;

	public void setUp() throws Exception {
		client = new Client("https://localhost:8443");
		client.login("admin", "admin", "1.4.0");
	}

	public void testGetStatus() throws ClientException {
		Assert.assertEquals(client.getStatus(), 0);
	}
}