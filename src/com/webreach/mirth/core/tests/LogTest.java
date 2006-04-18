package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Log;

import junit.framework.TestCase;

public class LogTest extends TestCase {
	
	private Log log;
	
	protected void setUp() throws Exception {
		super.setUp();
		log = new Log();
		log.setId(0);
		log.setDate("2006-04-06");
		log.setEvent("Message sucessfully transformed.");
		log.setLevel(0);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
