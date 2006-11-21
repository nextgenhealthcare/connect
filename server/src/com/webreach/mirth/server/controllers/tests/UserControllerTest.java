package com.webreach.mirth.server.controllers.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.User;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.UserController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class UserControllerTest extends TestCase {
	private UserController userController = new UserController();
	private ConfigurationController configurationController = new ConfigurationController();
	private List<User> sampleUserList;
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();

		sampleUserList = new ArrayList<User>();
		
		for (int i = 0; i < 10; i++) {
			User sampleUser = new User();
			int id = configurationController.getNextId();
			sampleUser.setId(String.valueOf(id));
			sampleUser.setUsername("user" + id);
			sampleUser.setPassword("password");
			sampleUserList.add(sampleUser);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testUpdateUser() throws ControllerException {
		User sampleUser = sampleUserList.get(0);
		userController.updateUser(sampleUser);
		List<User> testUserList = userController.getUser(sampleUser);
		User testUser = testUserList.get(0);
		
		Assert.assertEquals(1, testUserList.size());
		Assert.assertEquals(sampleUser, testUser);
	}
	
	public void testGetUser() throws ControllerException {
		insertSampleUsers();
		
		List<User> testUserList = userController.getUser(null);
		
		for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
			User sampleUser = (User) iter.next();
			Assert.assertTrue(testUserList.contains(sampleUser));
		}
	}
	
	public void testRemoveUser() throws ControllerException {
		insertSampleUsers();
		
		User sampleUser = sampleUserList.get(0);
		userController.removeUser(sampleUser);
		List<User> testUserList = userController.getUser(null);

		Assert.assertFalse(testUserList.contains(sampleUser));
	}
	
	public void insertSampleUsers() throws ControllerException {
		for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
			User sampleUser = (User) iter.next();
			userController.updateUser(sampleUser);
		}
	}

}
