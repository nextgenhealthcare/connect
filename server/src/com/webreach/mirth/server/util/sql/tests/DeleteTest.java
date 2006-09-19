package com.webreach.mirth.server.util.sql.tests;

import junit.framework.TestCase;

import com.webreach.mirth.server.util.sql.Delete;

public class DeleteTest extends TestCase {
	private Delete delete;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		delete = new Delete("test");
		delete.addCriteria("first = 'Joe'");
		delete.addCriteria("last = 'Blow'");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testToString() {
		System.out.println(delete.toString());
	}
}
