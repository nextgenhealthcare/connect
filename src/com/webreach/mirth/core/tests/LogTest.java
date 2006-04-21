package com.webreach.mirth.core.tests;

import java.sql.Timestamp;
import java.util.Calendar;

import com.webreach.mirth.core.Log;

import junit.framework.TestCase;

public class LogTest extends TestCase {
	
	private Log log;
	
	protected void setUp() throws Exception {
		super.setUp();
		Calendar calendar = Calendar.getInstance();
		
		log = new Log();
		log.setId(0);
		log.setDate(new Timestamp(calendar.getTimeInMillis()));
		log.setEvent("Message sucessfully transformed.");
		log.setLevel(0);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
