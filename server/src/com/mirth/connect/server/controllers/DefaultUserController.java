/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.commons.encryption.Digester;
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
    private ExtensionController extensionController = null;
    
    private static DefaultUserController instance = null;

    private DefaultUserController() {

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
            SqlConfig.getSqlSessionManager().update("User.resetUserStatus");
        } catch (PersistenceException e) {
            logger.error("Could not reset user status.");
        }
    }

    public List<User> getUser(User user) throws ControllerException {
        logger.debug("getting user: " + user);

        try {
            return SqlConfig.getSqlSessionManager().selectList("User.getUser", user);
        } catch (PersistenceException e) {
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
                SqlConfig.getSqlSessionManager().insert("User.insertUser", getUserMap(user));
            } else {
                logger.debug("updating user: " + user);
                SqlConfig.getSqlSessionManager().update("User.updateUser", getUserMap(user));
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    public List<String> checkOrUpdateUserPassword(Integer userId, String plainPassword) throws ControllerException {
        try {
            Digester digester = ControllerFactory.getFactory().createConfigurationController().getDigester();
            PasswordRequirements passwordRequirements = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
            List<String> responses = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(userId, plainPassword, passwordRequirements);
            if (responses != null) {
                return responses;
            }

            /*
             * If no userId was passed in, stop here and don't try to add the
             * password.
             */
            if (userId == null) {
                return null;
            }

            logger.debug("updating password for user id: " + userId);

            Calendar pruneDate = PasswordRequirementsChecker.getInstance().getLastExpirationDate(passwordRequirements);

            // If a null prune date is returned, do not prune
            if (pruneDate != null) {
                Map<String, Object> userDateMap = new HashMap<String, Object>();
                userDateMap.put("id", userId);
                userDateMap.put("pruneDate", pruneDate);

                try {
                    SqlConfig.getSqlSessionManager().delete("User.prunePasswords", userDateMap);
                } catch (Exception e) {
                    // Don't abort changing the password if pruning fails.
                    logger.error("There was an error pruning passwords for user id: " + userId, e);
                }
            }

            Map<String, Object> userPasswordMap = new HashMap<String, Object>();
            userPasswordMap.put("id", userId);
            userPasswordMap.put("password", digester.digest(plainPassword));
            SqlConfig.getSqlSessionManager().insert("User.updateUserPassword", userPasswordMap);

            return null;
        } catch (PersistenceException e) {
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
            SqlConfig.getSqlSessionManager().delete("User.deleteUser", user);

            if (DatabaseUtil.statementExists("User.vacuumPersonTable")) {
                SqlConfig.getSqlSessionManager().update("User.vacuumPersonTable");
            }
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }

    public LoginStatus authorizeUser(String username, String plainPassword) throws ControllerException {
        try {
            // Invoke and return from the Authorization Plugin if one exists
            if (extensionController == null) {
                extensionController = ControllerFactory.getFactory().createExtensionController();
            }
            
            if (extensionController.getAuthorizationPlugin() != null) {
                return extensionController.getAuthorizationPlugin().authorizeUser(username, plainPassword);
            }
            
            Digester digester = ControllerFactory.getFactory().createConfigurationController().getDigester();
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
                credentials = (Credentials) SqlConfig.getSqlSessionManager().selectOne("User.getLatestUserCredentials", validUser.getId());

                if (credentials != null) {
                    if (Pre22PasswordChecker.isPre22Hash(credentials.getPassword())) {
                        if (Pre22PasswordChecker.checkPassword(plainPassword, credentials.getPassword())) {
                            checkOrUpdateUserPassword(validUser.getId(), plainPassword);
                            authorized = true;
                        }
                    } else {
                        authorized = digester.matches(plainPassword, credentials.getPassword());
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
                                SqlConfig.getSqlSessionManager().update("User.startGracePeriod", validUser.getId());
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
                            SqlConfig.getSqlSessionManager().update("User.clearGracePeriod", validUser.getId());
                        }
                    }
                }
                // End of password expiration and grace period checks

                // If nothing failed (loginStatus != null), set SUCCESS now
                if (loginStatus == null) {
                    loginStatus = new LoginStatus(LoginStatus.Status.SUCCESS, "");

                    // Clear the user's grace period if one exists
                    if (validUser.getGracePeriodStart() != null) {
                        SqlConfig.getSqlSessionManager().update("User.clearGracePeriod", validUser.getId());
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
        Digester digester = ControllerFactory.getFactory().createConfigurationController().getDigester();
        return digester.matches(plainPassword, encryptedPassword);
    }

    public void loginUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlSessionManager().update("User.loginUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void logoutUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlSessionManager().update("User.logoutUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }

    }

    public boolean isUserLoggedIn(User user) throws ControllerException {
        try {
            return (Boolean) SqlConfig.getSqlSessionManager().selectOne("User.isUserLoggedIn", user.getId());
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
    public List<Credentials> getUserCredentials(Integer userId) throws ControllerException {
        try {
            return SqlConfig.getSqlSessionManager().selectList("User.getUserCredentials", userId);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
}
