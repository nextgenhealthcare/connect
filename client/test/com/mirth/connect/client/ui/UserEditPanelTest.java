package com.mirth.connect.client.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class UserEditPanelTest {
	
	private static int NUM_OF_COUNTRY_CODES = 245;
	
	private UserEditPanel userEditPanel;
	
	@Before
	public void setup() {
		userEditPanel = new UserEditPanel();
	}

	@Test
	public void testValidatePhoneNumber() {
		// Valid US phone numbers
		assertTrue(userEditPanel.validatePhoneNumber("(714) 555-5555", "US"));
		assertTrue(userEditPanel.validatePhoneNumber("1 (714) 555-5555", "US"));
		assertTrue(userEditPanel.validatePhoneNumber("7145555555", "US"));
		assertTrue(userEditPanel.validatePhoneNumber("714-555-5555", "US"));
		
		// Invalid US phone numbers
		assertFalse(userEditPanel.validatePhoneNumber("(714) 555-555", "US"));	// Too few digits
		assertFalse(userEditPanel.validatePhoneNumber("71455555555", "US"));	// Too many digits
		assertFalse(userEditPanel.validatePhoneNumber("555-5555", "US"));		// No area code
		assertFalse(userEditPanel.validatePhoneNumber("555-555-5555", "US"));	// Non-existent area code
		
		// Valid AU phone numbers
		assertTrue(userEditPanel.validatePhoneNumber("0455 555 555", "AU"));
		assertTrue(userEditPanel.validatePhoneNumber("0455555555", "AU"));
		assertTrue(userEditPanel.validatePhoneNumber("455 555 555", "AU"));
		assertTrue(userEditPanel.validatePhoneNumber("455555555", "AU"));
		
		// Invalid AU phone numbers
		assertFalse(userEditPanel.validatePhoneNumber("0455 555 55", "AU"));		// Too few digits
		assertFalse(userEditPanel.validatePhoneNumber("04555555555", "AU"));		// Too many digits
		assertFalse(userEditPanel.validatePhoneNumber("00455 555 555", "AU"));	// Too many leading zeroes
	}
		
}
