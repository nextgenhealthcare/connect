package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.User;

import junit.framework.TestCase;

public class UserTest extends TestCase {

	private User user;
	
	protected void setUp() throws Exception {
		super.setUp();
		user = new User();
		user.setId(0);
		user.setUsername("gerald");
		user.setPassword("password");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
