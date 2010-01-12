package com.webreach.mirth.model.util;

import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.server.controllers.ConfigurationController;

public class PasswordRequirementsChecker implements Serializable {
	private static final String CHARACTERS = " characters";
	private static final String PASSWORD_IS_TOO_SHORT_MINIMUM_LENGTH_IS = "Password is too short. Minimum length is ";
	private static final String PASSWORD_MUST_CONTAIN_A_SPECIAL_CHARACTER = "Password must contain a special character";
	private static final String PASSWORD_MUST_CONTAIN_A_NUMERIC_VALUE = "Password must contain a numeric value";
	private static final String PASSWORD_MUST_CONTAIN_A_LOWERCASE_LETTER = "Password must contain a lowercase letter";
	private static final String PASSWORD_MUST_CONTAIN_AN_UPPERCASE_LETTER = "Password must contain an uppercase letter";
	private static final String PASSWORD_REQUIRE_NUMERIC = "password.require.numeric";
	private static final String PASSWORD_REQUIRE_LOWER = "password.require.lower";
	private static final String PASSWORD_REQUIRE_UPPER = "password.require.upper";
	private static final String PASSWORD_REQUIRE_SPECIAL = "password.require.special";
	private static final String PASSWORD_MINLENGTH = "password.minlength";
	private static final String PROPERTIES_SECURITY = "security";

	private static PasswordRequirementsChecker instance = null;

	private PasswordRequirementsChecker() {
		
	}
	
	public static PasswordRequirementsChecker getInstance() {
		synchronized (PasswordRequirementsChecker.class) {
			if (instance == null) {
				instance = new PasswordRequirementsChecker();
			}
			return instance;
		}
	}    
	
	public PasswordRequirements loadPasswordRequirements() {
		PasswordRequirements passwordRequirements = new PasswordRequirements();
		
		// load the mirth properties
		Properties securityProperties = ConfigurationController.getInstance().getPropertiesForGroup(PROPERTIES_SECURITY);
		
		if (securityProperties.getProperty(PASSWORD_REQUIRE_UPPER, "false").equalsIgnoreCase("true"))
			passwordRequirements.setRequireUpper(true);
		if (securityProperties.getProperty(PASSWORD_REQUIRE_LOWER, "false").equalsIgnoreCase("true"))
			passwordRequirements.setRequireLower(true);
		if (securityProperties.getProperty(PASSWORD_REQUIRE_NUMERIC, "false").equalsIgnoreCase("true"))
			passwordRequirements.setRequireNumeric(true);
		if (securityProperties.getProperty(PASSWORD_REQUIRE_SPECIAL, "false").equalsIgnoreCase("true"))
			passwordRequirements.setRequireSpecial(true);
		String minlength = securityProperties.getProperty(PASSWORD_MINLENGTH, "0");
		try {
			int minLength = Integer.parseInt(minlength);
			passwordRequirements.setMinLength(minLength);
		} catch (Exception e) {
			passwordRequirements.setMinLength(0);
		}
		
		return passwordRequirements;
	}

	// Determines if password matches criteria
	// Returns a vector with a description of any conditions not met
	// or null if the password meets all requirements
	public Vector<String> doesPasswordMeetRequirements(String plainTextPassword, PasswordRequirements passwordRequirements) {
		Vector<String> responses = new Vector<String>();
		if (passwordRequirements.isRequireLower()) {
			String message = checkRequireLower(plainTextPassword);
			if (message != null)
				responses.add(message);
		}
		if (passwordRequirements.isRequireUpper()) {
			String message = checkRequireUpper(plainTextPassword);
			if (message != null)
				responses.add(message);
		}
		if (passwordRequirements.isRequireNumeric()) {
			String message = checkRequireNumeric(plainTextPassword);
			if (message != null)
				responses.add(message);
		}
		if (passwordRequirements.isRequireSpecial()) {
			String message = checkRequireSpecial(plainTextPassword);
			if (message != null)
				responses.add(message);
		}
		if (passwordRequirements.getMinLength() > 0) {
			if (plainTextPassword.length() < passwordRequirements.getMinLength())
				responses.add(PASSWORD_IS_TOO_SHORT_MINIMUM_LENGTH_IS + passwordRequirements.getMinLength() + CHARACTERS);
		}
		if (responses.size() == 0)
			return null;
		else
			return responses;
	}

	private String checkRequireNumeric(String plainTextPassword) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(plainTextPassword);
		if (!matcher.find())
			return PASSWORD_MUST_CONTAIN_A_NUMERIC_VALUE;
		else
			return null;
	}

	private String checkRequireUpper(String plainTextPassword) {
		Pattern pattern = Pattern.compile("[A-Z]");
		Matcher matcher = pattern.matcher(plainTextPassword);
		if (!matcher.find())
			return PASSWORD_MUST_CONTAIN_AN_UPPERCASE_LETTER;
		else
			return null;
	}

	private String checkRequireLower(String plainTextPassword) {
		Pattern pattern = Pattern.compile("[a-z]");
		Matcher matcher = pattern.matcher(plainTextPassword);
		if (!matcher.find())
			return PASSWORD_MUST_CONTAIN_A_LOWERCASE_LETTER;
		else
			return null;
	}

	private String checkRequireSpecial(String plainTextPassword) {
		Pattern pattern = Pattern.compile(".[!,@,#,$,%,^,&,*,?,_,~,\\+,-,=,\\(,\\),`,\\[,\\],:,;,\",\',/,<,>]");
		Matcher matcher = pattern.matcher(plainTextPassword);
		if (!matcher.find())
			return PASSWORD_MUST_CONTAIN_A_SPECIAL_CHARACTER;
		else
			return null;
	}
	
}
