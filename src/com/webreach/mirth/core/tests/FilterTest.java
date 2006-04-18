package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Filter;

import junit.framework.TestCase;

public class FilterTest extends TestCase {

	private Filter filter;
	
	protected void setUp() throws Exception {
		super.setUp();
		filter = new Filter();
		filter.setScript("return true;");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
