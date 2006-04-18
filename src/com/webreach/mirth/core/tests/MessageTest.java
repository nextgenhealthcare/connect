package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Message;

import junit.framework.TestCase;

public class MessageTest extends TestCase {

	private Message message;
	
	protected void setUp() throws Exception {
		super.setUp();
		message = new Message();
		message.setId(0);
		message.setDate("2006-04-06");
		message.setSendingFacility("Hospital A");
		message.setEvent("ADT_A01");
		message.setControlId("123456");
		message.setSize(10);
		message.setMessage("MSH");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
