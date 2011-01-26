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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.mirth.connect.model.PasswordRequirements;

public class PasswordRequirementsChecker implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String PASSWORD_IS_TOO_SHORT_MINIMUM_LENGTH = "Password is too short. Minimum length is %d characters";

    private static final String PASSWORD_MUST_CONTAIN_A_SPECIAL_CHARACTER = "Password must contain %d special character(s)";
    private static final String PASSWORD_MUST_NOT_CONTAIN_A_SPECIAL_CHARACTER = "Password must not contain a special character";

    private static final String PASSWORD_MUST_CONTAIN_A_NUMERIC_VALUE = "Password must contain %d numeric value(s)";
    private static final String PASSWORD_MUST_NOT_CONTAIN_A_NUMERIC_VALUE = "Password must not contain a numeric value";

    private static final String PASSWORD_MUST_CONTAIN_A_LOWERCASE_LETTER = "Password must contain %d lowercase letter(s)";
    private static final String PASSWORD_MUST_NOT_CONTAIN_A_LOWERCASE_LETTER = "Password must not contain a lowercase letter";

    private static final String PASSWORD_MUST_CONTAIN_AN_UPPERCASE_LETTER = "Password must contain %d uppercase letter(s)";
    private static final String PASSWORD_MUST_NOT_CONTAIN_AN_UPPERCASE_LETTER = "Password not must contain an uppercase letter";

    private static final String PASSWORD_MIN_NUMERIC = "password.minnumeric";
    private static final String PASSWORD_MIN_LOWER = "password.minlower";
    private static final String PASSWORD_MIN_UPPER = "password.minupper";
    private static final String PASSWORD_MIN_SPECIAL = "password.minspecial";
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
        passwordRequirements.setMinUpper(securityProperties.getInt(PASSWORD_MIN_UPPER, 0));
        passwordRequirements.setMinLower(securityProperties.getInt(PASSWORD_MIN_LOWER, 0));
        passwordRequirements.setMinNumeric(securityProperties.getInt(PASSWORD_MIN_NUMERIC, 0));
        passwordRequirements.setMinSpecial(securityProperties.getInt(PASSWORD_MIN_SPECIAL, 0));
        passwordRequirements.setMinLength(securityProperties.getInt(PASSWORD_MINLENGTH, 0));
        return passwordRequirements;
    }

    // Determines if password matches criteria
    // Returns a vector with a description of any conditions not met
    // or null if the password meets all requirements
    public List<String> doesPasswordMeetRequirements(String plainTextPassword, PasswordRequirements passwordRequirements) {
        List<String> responses = new ArrayList<String>();
        String message = null;

        message = checkMinLower(plainTextPassword, passwordRequirements.getMinLower());
        if (message != null) {
            responses.add(message);
        }

        message = checkMinUpper(plainTextPassword, passwordRequirements.getMinUpper());
        if (message != null) {
            responses.add(message);
        }

        message = checkMinNumeric(plainTextPassword, passwordRequirements.getMinNumeric());
        if (message != null) {
            responses.add(message);
        }

        message = checkMinSpecial(plainTextPassword, passwordRequirements.getMinSpecial());
        if (message != null) {
            responses.add(message);
        }

        if (passwordRequirements.getMinLength() > 0) {
            if (plainTextPassword.length() < passwordRequirements.getMinLength())
                responses.add(String.format(PASSWORD_IS_TOO_SHORT_MINIMUM_LENGTH, passwordRequirements.getMinLength()));
        }
        if (responses.size() == 0)
            return null;
        else
            return responses;
    }

    private String checkMinNumeric(String plainTextPassword, int minNumeric) {

        if (minNumeric == -1) {
            Pattern pattern = Pattern.compile("[0-9]");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (matcher.find())
                return PASSWORD_MUST_NOT_CONTAIN_A_NUMERIC_VALUE;
        } else {
            Pattern pattern = Pattern.compile("[0-9]{" + minNumeric + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (!matcher.find())
                return String.format(PASSWORD_MUST_CONTAIN_A_NUMERIC_VALUE, minNumeric);
        }
        return null;
    }

    private String checkMinUpper(String plainTextPassword, int minUpper) {

        if (minUpper == -1) {
            Pattern pattern = Pattern.compile("[A-Z]");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (matcher.find())
                return PASSWORD_MUST_NOT_CONTAIN_AN_UPPERCASE_LETTER;
        } else {
            Pattern pattern = Pattern.compile("[A-Z]{" + minUpper + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (!matcher.find())
                return String.format(PASSWORD_MUST_CONTAIN_AN_UPPERCASE_LETTER, minUpper);
        }
        return null;
    }

    private String checkMinLower(String plainTextPassword, int minLower) {

        if (minLower == -1) {
            Pattern pattern = Pattern.compile("[a-z]");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (matcher.find())
                return PASSWORD_MUST_NOT_CONTAIN_A_LOWERCASE_LETTER;
        } else {
            Pattern pattern = Pattern.compile("[a-z]{" + minLower + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (!matcher.find())
                return String.format(PASSWORD_MUST_CONTAIN_A_LOWERCASE_LETTER, minLower);
        }
        return null;
    }

    private String checkMinSpecial(String plainTextPassword, int minSpecial) {
        if (minSpecial == -1) {
            Pattern pattern = Pattern.compile("[!,@,#,$,%,^,&,*,?,_,~,\\+,-,=,\\(,\\),`,\\[,\\],:,;,\",\',/,<,>]");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (matcher.find())
                return PASSWORD_MUST_NOT_CONTAIN_A_SPECIAL_CHARACTER;
        } else {
            Pattern pattern = Pattern.compile("[!,@,#,$,%,^,&,*,?,_,~,\\+,-,=,\\(,\\),`,\\[,\\],:,;,\",\',/,<,>]{" + minSpecial + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);
            if (!matcher.find())
                return String.format(PASSWORD_MUST_CONTAIN_A_SPECIAL_CHARACTER, minSpecial);
        }
        return null;
    }

}
