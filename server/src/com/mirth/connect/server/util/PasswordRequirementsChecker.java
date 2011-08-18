/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.mirth.connect.model.Credentials;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;

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

    private static final String PASSWORD_MINLENGTH = "password.minlength";
    private static final String PASSWORD_MIN_NUMERIC = "password.minnumeric";
    private static final String PASSWORD_MIN_LOWER = "password.minlower";
    private static final String PASSWORD_MIN_UPPER = "password.minupper";
    private static final String PASSWORD_MIN_SPECIAL = "password.minspecial";
    private static final String PASSWORD_EXPIRATION = "password.expiration";
    private static final String PASSWORD_GRACE_PERIOD = "password.graceperiod";
    private static final String PASSWORD_RETRY_LIMIT = "password.retrylimit";
    private static final String PASSWORD_LOCKOUT_PERIOD = "password.lockoutperiod";
    private static final String PASSWORD_REUSE_PERIOD = "password.reuseperiod";
    private static final String PASSWORD_REUSE_LIMIT = "password.reuselimit";

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

        passwordRequirements.setMinLength(securityProperties.getInt(PASSWORD_MINLENGTH, 0));
        passwordRequirements.setMinUpper(securityProperties.getInt(PASSWORD_MIN_UPPER, 0));
        passwordRequirements.setMinLower(securityProperties.getInt(PASSWORD_MIN_LOWER, 0));
        passwordRequirements.setMinNumeric(securityProperties.getInt(PASSWORD_MIN_NUMERIC, 0));
        passwordRequirements.setMinSpecial(securityProperties.getInt(PASSWORD_MIN_SPECIAL, 0));
        passwordRequirements.setExpiration(securityProperties.getInt(PASSWORD_EXPIRATION, 0));
        passwordRequirements.setGracePeriod(securityProperties.getInt(PASSWORD_GRACE_PERIOD, 0));
        passwordRequirements.setRetryLimit(securityProperties.getInt(PASSWORD_RETRY_LIMIT, 0));
        passwordRequirements.setLockoutPeriod(securityProperties.getInt(PASSWORD_LOCKOUT_PERIOD, 0));
        passwordRequirements.setReusePeriod(securityProperties.getInt(PASSWORD_REUSE_PERIOD, 0));
        passwordRequirements.setReuseLimit(securityProperties.getInt(PASSWORD_REUSE_LIMIT, 0));

        return passwordRequirements;
    }

    /**
     * Determines if password matches criteria. Returns a vector with a
     * description of any conditions not met or null if the password meets all
     * requirements.
     * 
     * @param plainPassword
     * @param passwordRequirements
     * @return
     */
    public List<String> doesPasswordMeetRequirements(Integer userId, String plainPassword, PasswordRequirements passwordRequirements) {
        List<String> resultList = new ArrayList<String>();
        addResult(resultList, checkMinLower(plainPassword, passwordRequirements.getMinLower()));
        addResult(resultList, checkMinUpper(plainPassword, passwordRequirements.getMinUpper()));
        addResult(resultList, checkMinNumeric(plainPassword, passwordRequirements.getMinNumeric()));
        addResult(resultList, checkMinSpecial(plainPassword, passwordRequirements.getMinSpecial()));

        if (passwordRequirements.getMinLength() > 0) {
            if (plainPassword.length() < passwordRequirements.getMinLength()) {
                addResult(resultList, String.format(PASSWORD_IS_TOO_SHORT_MINIMUM_LENGTH, passwordRequirements.getMinLength()));
            }
        }

        /*
         * If no user/user id was passed in (new user), then don't do previous password
         * checks. Continue without checking if the reuse policies are off.
         */
        if ((userId != null) && ((passwordRequirements.getReusePeriod() != 0) || (passwordRequirements.getReuseLimit() != 0))) {
            try {
                List<Credentials> previousCredentials = ControllerFactory.getFactory().createUserController().getUserCredentials(userId);
                addResult(resultList, checkReusePeriod(previousCredentials, plainPassword, passwordRequirements.getReusePeriod()));
                addResult(resultList, checkReuseLimit(previousCredentials, plainPassword, passwordRequirements.getReuseLimit()));
            } catch (ControllerException e) {
                addResult(resultList, "There was an error checking against previous user passwords.");
            }
        }

        if (resultList.size() == 0) {
            return null;
        } else {
            return resultList;
        }
    }

    /**
     * Adds a check response to the list if it is not null.
     * 
     * @param responseList
     * @param result
     */
    private void addResult(List<String> responseList, String result) {
        if (StringUtils.isNotBlank(result)) {
            responseList.add(result);
        }
    }

    private String checkMinNumeric(String plainTextPassword, int minNumeric) {
        if (minNumeric == -1) {
            Pattern pattern = Pattern.compile("[0-9]");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (matcher.find()) {
                return PASSWORD_MUST_NOT_CONTAIN_A_NUMERIC_VALUE;
            }
        } else {
            Pattern pattern = Pattern.compile("[0-9]{" + minNumeric + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (!matcher.find()) {
                return String.format(PASSWORD_MUST_CONTAIN_A_NUMERIC_VALUE, minNumeric);
            }
        }

        return null;
    }

    private String checkMinUpper(String plainTextPassword, int minUpper) {
        if (minUpper == -1) {
            Pattern pattern = Pattern.compile("[A-Z]");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (matcher.find()) {
                return PASSWORD_MUST_NOT_CONTAIN_AN_UPPERCASE_LETTER;
            }
        } else {
            Pattern pattern = Pattern.compile("[A-Z]{" + minUpper + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (!matcher.find()) {
                return String.format(PASSWORD_MUST_CONTAIN_AN_UPPERCASE_LETTER, minUpper);
            }
        }

        return null;
    }

    private String checkMinLower(String plainTextPassword, int minLower) {
        if (minLower == -1) {
            Pattern pattern = Pattern.compile("[a-z]");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (matcher.find()) {
                return PASSWORD_MUST_NOT_CONTAIN_A_LOWERCASE_LETTER;
            }
        } else {
            Pattern pattern = Pattern.compile("[a-z]{" + minLower + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (!matcher.find()) {
                return String.format(PASSWORD_MUST_CONTAIN_A_LOWERCASE_LETTER, minLower);
            }
        }

        return null;
    }

    private String checkMinSpecial(String plainTextPassword, int minSpecial) {
        if (minSpecial == -1) {
            Pattern pattern = Pattern.compile("[!,@,#,$,%,^,&,*,?,_,~,\\+,-,=,\\(,\\),`,\\[,\\],:,;,\",\',/,<,>]");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (matcher.find()) {
                return PASSWORD_MUST_NOT_CONTAIN_A_SPECIAL_CHARACTER;
            }
        } else {
            Pattern pattern = Pattern.compile("[!,@,#,$,%,^,&,*,?,_,~,\\+,-,=,\\(,\\),`,\\[,\\],:,;,\",\',/,<,>]{" + minSpecial + ",}");
            Matcher matcher = pattern.matcher(plainTextPassword);

            if (!matcher.find()) {
                return String.format(PASSWORD_MUST_CONTAIN_A_SPECIAL_CHARACTER, minSpecial);
            }
        }

        return null;
    }

    private String checkReusePeriod(List<Credentials> previousCredentials, String plainPassword, int reusePeriod) {
        // Return without checking if the reuse policies are off
        if (reusePeriod == 0) {
            return null;
        }

        UserController userController = ControllerFactory.getFactory().createUserController();

        // Let -1 mean the duration is infinite
        Duration reusePeriodDuration = null;
        if (reusePeriod != -1) {
            reusePeriodDuration = Duration.standardDays(reusePeriod);
        }

        for (Credentials credentials : previousCredentials) {
            boolean checkPassword = false;
            if (reusePeriodDuration == null) {
                checkPassword = true;
            } else {
                checkPassword = reusePeriodDuration.isLongerThan(new Duration(credentials.getPasswordDate().getTimeInMillis(), System.currentTimeMillis()));
            }

            if (checkPassword && userController.checkPassword(plainPassword, credentials.getPassword())) {
                if (reusePeriod == -1) {
                    return "You cannot reuse the same password.";
                }
                return "You cannot reuse the same password within " + reusePeriod + " days.";
            }
        }

        return null;
    }

    private String checkReuseLimit(List<Credentials> previousCredentials, String plainPassword, int reuseLimit) {
        // Return without checking if the reuse limit is off
        if (reuseLimit == 0) {
            return null;
        }

        UserController userController = ControllerFactory.getFactory().createUserController();
        int reuseCount = 0;
        for (Credentials credentials : previousCredentials) {
            if (userController.checkPassword(plainPassword, credentials.getPassword())) {
                reuseCount++;
            }
        }

        if (reuseCount > reuseLimit) {
            if (reuseLimit == -1) {
                return "You cannot reuse the same password.";
            }
            return "You cannot reuse the same password more than " + reuseLimit + " times.";
        }

        return null;
    }
    
    public Calendar getLastExpirationDate(PasswordRequirements passwordRequirements) {
        DateTime dateTime = new DateTime();
        
        // Must keep all passwords if reuse period is -1 (infinite) or reuse limit is set
        if ((passwordRequirements.getReusePeriod() == -1) || passwordRequirements.getReuseLimit() != 0) {
            return null;
        }
        
        if (passwordRequirements.getReusePeriod() == 0) {
            return Calendar.getInstance();
        }
        
        return dateTime.minus(Duration.standardDays(passwordRequirements.getReusePeriod())).toCalendar(Locale.getDefault());
    }

}
