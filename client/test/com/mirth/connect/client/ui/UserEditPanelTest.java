package com.mirth.connect.client.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UserEditPanelTest {

	@Test
	public void testValidatePhoneNumber() {
		// Valid US phone numbers
		assertTrue(UserEditPanel.validatePhoneNumber("(714) 555-5555", "US"));
		assertTrue(UserEditPanel.validatePhoneNumber("1 (714) 555-5555", "US"));
		assertTrue(UserEditPanel.validatePhoneNumber("7145555555", "US"));
		assertTrue(UserEditPanel.validatePhoneNumber("714-555-5555", "US"));
		
		// Invalid US phone numbers
		assertFalse(UserEditPanel.validatePhoneNumber("(714) 555-555", "US"));	// Too few digits
		assertFalse(UserEditPanel.validatePhoneNumber("71455555555", "US"));	// Too many digits
		assertFalse(UserEditPanel.validatePhoneNumber("555-5555", "US"));		// No area code
		assertFalse(UserEditPanel.validatePhoneNumber("555-555-5555", "US"));	// Non-existent area code
		
		// Valid AU phone numbers
		assertTrue(UserEditPanel.validatePhoneNumber("0455 555 555", "AU"));
		assertTrue(UserEditPanel.validatePhoneNumber("0455555555", "AU"));
		assertTrue(UserEditPanel.validatePhoneNumber("455 555 555", "AU"));
		assertTrue(UserEditPanel.validatePhoneNumber("455555555", "AU"));
		
		// Invalid AU phone numbers
		assertFalse(UserEditPanel.validatePhoneNumber("0455 555 55", "AU"));		// Too few digits
		assertFalse(UserEditPanel.validatePhoneNumber("04555555555", "AU"));		// Too many digits
		assertFalse(UserEditPanel.validatePhoneNumber("00455 555 555", "AU"));	// Too many leading zeroes
	}
		
}
