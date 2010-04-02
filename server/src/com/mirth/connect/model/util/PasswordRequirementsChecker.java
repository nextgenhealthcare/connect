/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.mirth.connect.model.PasswordRequirements;

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

    public PasswordRequirements loadPasswordRequirements(PropertiesConfiguration securityProperties) {
        PasswordRequirements passwordRequirements = new PasswordRequirements();
        passwordRequirements.setRequireUpper(securityProperties.getBoolean(PASSWORD_REQUIRE_UPPER, false));
        passwordRequirements.setRequireLower(securityProperties.getBoolean(PASSWORD_REQUIRE_LOWER, false));
        passwordRequirements.setRequireNumeric(securityProperties.getBoolean(PASSWORD_REQUIRE_NUMERIC, false));
        passwordRequirements.setRequireSpecial(securityProperties.getBoolean(PASSWORD_REQUIRE_SPECIAL, false));
        passwordRequirements.setMinLength(securityProperties.getInt(PASSWORD_MINLENGTH, 0));
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
