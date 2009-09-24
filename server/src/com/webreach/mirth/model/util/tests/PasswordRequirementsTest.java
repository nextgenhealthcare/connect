package com.webreach.mirth.model.util.tests;

import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.model.util.PasswordRequirementsChecker;
import com.webreach.mirth.server.controllers.ControllerException;

public class PasswordRequirementsTest extends TestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testMinLength() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(false,false,false,false,10);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("testtesttest", req);
		Assert.assertNull(result2);
	}
	public void testRequireUpper() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(true,false,false,false,0);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("aTest", req);
		Assert.assertNull(result2);
	}
	public void testRequireLower() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(false,true,false,false,0);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt", req);
		Assert.assertNull(result2);
	}
	public void testRequireNumeric() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(false,false,true,false,0);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST9", req);
		Assert.assertNull(result2);
	}
	public void testRequireSpecial() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(false,false,false,true,0);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST$TEST", req);
		Assert.assertNull(result2);
	}
	public void testAllConditions() throws ControllerException {
		PasswordRequirements req = new PasswordRequirements(true,true,true,true,15);
		Vector<String> result = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req);
		Assert.assertNotNull(result);
		Vector<String> result2 = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("Th1$isAtestTEST*#", req);
		Assert.assertNull(result2);
	}
}