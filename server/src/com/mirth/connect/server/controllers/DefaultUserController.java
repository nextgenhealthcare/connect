/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;

import com.ibm.crypto.fips.provider.IBMJCEFIPS;
import com.mirth.connect.model.Credentials;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.LoginRequirementsChecker;
import com.mirth.connect.server.util.PasswordRequirementsChecker;
import com.mirth.connect.server.util.Pre22PasswordChecker;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultUserController extends UserController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurablePasswordEncryptor encrypter = new ConfigurablePasswordEncryptor();

    private static DefaultUserController instance = null;

    private DefaultUserController() {
        encrypter.setProvider(new IBMJCEFIPS());
        encrypter.setAlgorithm("SHA256");
    }

    public static UserController create() {
        synchronized (DefaultUserController.class) {
            if (instance == null) {
                instance = new DefaultUserController();
            }

            return instance;
        }
    }

    public void resetUserStatus() {
        try {
            SqlConfig.getSqlMapClient().update("User.resetUserStatus");
        } catch (SQLException e) {
            logger.error("Could not reset user status.");
        }
    }

    public List<User> getUser(User user) throws ControllerException {
        logger.debug("getting user: " + user);

        try {
            return SqlConfig.getSqlMapClient().queryForList("User.getUser", user);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void updateUser(User user) throws ControllerException {
        try {
            if (user.getId() == null) {

                User checkUserName = new User();
                checkUserName.setUsername(user.getUsername());

                if (getUser(checkUserName).size() != 0) {
                    throw new ControllerException("Error adding user: username must be unique");
                }

                logger.debug("adding user: " + user);
                SqlConfig.getSqlMapClient().insert("User.insertUser", getUserMap(user));
            } else {
                logger.debug("updating user: " + user);
                SqlConfig.getSqlMapClient().update("User.updateUser", getUserMap(user));
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public List<String> updateUserPassword(User user, String plainPassword) throws ControllerException {
        try {
            PasswordRequirements passwordRequirements = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
            List<String> responses = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(user, plainPassword, passwordRequirements);
            if (responses != null) {
                return responses;
            }

            logger.debug("updating password for user id: " + user.getId());

            Calendar pruneDate = PasswordRequirementsChecker.getInstance().getLastExpirationDate(passwordRequirements);
            
            // If a null prune date is returned, do not prune
            if (pruneDate != null) {
                Map<String, Object> userDateMap = new HashMap<String, Object>();
                userDateMap.put("id", user.getId());
                userDateMap.put("pruneDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", pruneDate));
            
                try {
                    SqlConfig.getSqlMapClient().delete("User.prunePasswords", userDateMap);
                } catch (Exception e) {
                    // Don't abort changing the password if pruning fails.
                    logger.error("There was an error pruning passwords for user: " + user.getUsername(), e);
                }
            }

            Map<String, Object> userPasswordMap = new HashMap<String, Object>();
            userPasswordMap.put("id", user.getId());
            userPasswordMap.put("password", encrypter.encryptPassword(plainPassword));
            SqlConfig.getSqlMapClient().insert("User.updateUserPassword", userPasswordMap);

            return null;
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeUser(User user, Integer currentUserId) throws ControllerException {
        logger.debug("removing user: " + user);

        if (user.getId() == null) {
            throw new ControllerException("Error removing user: User Id cannot be null");
        }

        if (user.getId().equals(currentUserId)) {
            throw new ControllerException("Error removing user: You cannot remove yourself");
        }

        try {
            SqlConfig.getSqlMapClient().delete("User.deleteUser", user);

            if (DatabaseUtil.statementExists("User.vacuumPersonTable")) {
                SqlConfig.getSqlMapClient().update("User.vacuumPersonTable");
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public LoginStatus authorizeUser(String username, String plainPassword) throws ControllerException {
        try {
            LoginRequirementsChecker loginRequirementsChecker = new LoginRequirementsChecker(username);
            if (loginRequirementsChecker.isUserLockedOut()) {
                return new LoginStatus(LoginStatus.Status.FAIL_LOCKED_OUT, "User account \"" + username + "\" has been locked. You may attempt to login again in " + loginRequirementsChecker.getPrintableStrikeTimeRemaining() + ".");
            }

            loginRequirementsChecker.resetExpiredStrikes();
            boolean authorized = false;

            // Validate the user
            User userFilter = new User();
            userFilter.setUsername(username);
            List<User> userResults = getUser(userFilter);

            Credentials credentials = null;
            User validUser = null;
            if (CollectionUtils.isNotEmpty(userResults)) {
                validUser = userResults.get(0);
                credentials = (Credentials) SqlConfig.getSqlMapClient().queryForObject("User.getLatestUserCredentials", validUser.getId());

                if (credentials != null) {
                    if (Pre22PasswordChecker.isPre22Hash(credentials.getPassword())) {
                        if (Pre22PasswordChecker.checkPassword(plainPassword, credentials.getPassword())) {
                            updateUserPassword(validUser, plainPassword);
                            authorized = true;
                        }
                    } else {
                        authorized = encrypter.checkPassword(plainPassword, credentials.getPassword());
                    }
                }
            }

            PasswordRequirements passwordRequirements = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
            LoginStatus loginStatus = null;

            if (authorized) {
                loginRequirementsChecker.resetStrikes();

                // If password expiration is enabled, do checks now
                if (passwordRequirements.getExpiration() > 0) {
                    long passwordTime = credentials.getPasswordDate().getTimeInMillis();
                    long currentTime = System.currentTimeMillis();

                    // If the password is expired, do grace period checks
                    if (loginRequirementsChecker.isPasswordExpired(passwordTime, currentTime)) {
                        // Let 0 be infinite grace period, -1 be no grace period
                        if (passwordRequirements.getGracePeriod() == 0) {
                            loginStatus = new LoginStatus(LoginStatus.Status.SUCCESS_GRACE_PERIOD, "Your password has expired. Please change your password now.");
                        } else if (passwordRequirements.getGracePeriod() > 0) {
                            // If there has never been a grace time, start it now
                            long gracePeriodStartTime;
                            if (validUser.getGracePeriodStart() == null) {
                                gracePeriodStartTime = currentTime;
                                SqlConfig.getSqlMapClient().update("User.startGracePeriod", validUser.getId());
                            } else {
                                gracePeriodStartTime = validUser.getGracePeriodStart().getTimeInMillis();
                            }

                            long graceTimeRemaining = loginRequirementsChecker.getGraceTimeRemaining(gracePeriodStartTime, currentTime);
                            if (graceTimeRemaining > 0) {
                                loginStatus = new LoginStatus(LoginStatus.Status.SUCCESS_GRACE_PERIOD, "Your password has expired. You are required to change your password in the next " + loginRequirementsChecker.getPrintableGraceTimeRemaining(graceTimeRemaining) + ".");
                            }
                        }

                        // If there is no grace period or it has passed, FAIL_EXPIRED
                        if (loginStatus == null) {
                            loginStatus = new LoginStatus(LoginStatus.Status.FAIL_EXPIRED, "Your password has expired. Please contact an administrator to have your password reset.");
                        }
                        
                        /*
                         * Reset the user's grace period if it isn't being used
                         * but one was previously set. This should only happen
                         * if a user is in a grace period before grace periods
                         * are disabled.
                         */
                        if ((passwordRequirements.getGracePeriod() <= 0) && (validUser.getGracePeriodStart() != null)) {
                            SqlConfig.getSqlMapClient().update("User.clearGracePeriod", validUser.getId());
                        }
                    }
                }
                // End of password expiration and grace period checks

                // If nothing failed (loginStatus != null), set SUCCESS now
                if (loginStatus == null) {
                    loginStatus = new LoginStatus(LoginStatus.Status.SUCCESS, "");

                    // Clear the user's grace period if one exists
                    if (validUser.getGracePeriodStart() != null) {
                        SqlConfig.getSqlMapClient().update("User.clearGracePeriod", validUser.getId());
                    }
                }
            } else {
                loginRequirementsChecker.incrementStrikes();
                String failMessage = "Incorrect username or password.";
                if (loginRequirementsChecker.isLockoutEnabled()) {
                    failMessage += " " + loginRequirementsChecker.getStrikesRemaining() + " login attempt(s) remaining for \"" + username + "\" until the account is locked for " + loginRequirementsChecker.getPrintableLockoutPeriod() + ".";
                }
                loginStatus = new LoginStatus(LoginStatus.Status.FAIL, failMessage);
            }

            return loginStatus;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
    
    public boolean checkPassword(String plainPassword, String encryptedPassword) {
        return encrypter.checkPassword(plainPassword, encryptedPassword);
    }

    public void loginUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlMapClient().update("User.loginUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void logoutUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlMapClient().update("User.logoutUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }

    }

    public boolean isUserLoggedIn(User user) throws ControllerException {
        try {
            return (Boolean) SqlConfig.getSqlMapClient().queryForObject("User.isUserLoggedIn", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private Map<String, Object> getUserMap(User user) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        if (user.getId() != null) {
            parameterMap.put("id", user.getId());
        }

        parameterMap.put("username", user.getUsername());
        parameterMap.put("firstName", user.getFirstName());
        parameterMap.put("lastName", user.getLastName());
        parameterMap.put("organization", user.getOrganization());
        parameterMap.put("email", user.getEmail());
        parameterMap.put("phoneNumber", user.getPhoneNumber());
        parameterMap.put("description", user.getDescription());
        return parameterMap;
    }

    public Properties getUserPreferences(User user) throws ControllerException {
        try {
            return ConfigurationController.getInstance().getPropertiesForGroup("user." + user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void setUserPreference(User user, String name, String value) throws ControllerException {
        try {
            ConfigurationController.getInstance().saveProperty("user." + user.getId(), name, value);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public List<Credentials> getUserCredentials(User user) throws ControllerException {
        try {
            return SqlConfig.getSqlMapClient().queryForList("User.getLatestUserCredentials", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
}
