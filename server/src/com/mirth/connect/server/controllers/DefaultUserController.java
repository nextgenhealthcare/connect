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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.model.Credentials;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.model.util.PasswordRequirementsChecker;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.EncryptionException;
import com.mirth.connect.util.FIPSEncrypter;

public class DefaultUserController extends UserController {
    private Logger logger = Logger.getLogger(this.getClass());
    private FIPSEncrypter encrypter = FIPSEncrypter.getInstance();

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

    public void updateUser(User user, String plainTextPassword) throws ControllerException {
        try {

            PasswordRequirements passwordRequirments = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
            List<String> responses = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(plainTextPassword, passwordRequirments);
            if (responses != null) {
                String resString = "";
                for (String response : responses) {
                    resString += response + "\n";
                }
                throw new ControllerException(resString);
            }
            if (user.getId() == null) {

                User checkUserName = new User();
                checkUserName.setUsername(user.getUsername());

                if (getUser(checkUserName).size() != 0) {
                    throw new ControllerException("Error adding user: username must be unique");
                }

                logger.debug("adding user: " + user);
                SqlConfig.getSqlMapClient().insert("User.insertUser", getUserMap(user, plainTextPassword));
            } else {
                logger.debug("updating user: " + user);
                SqlConfig.getSqlMapClient().update("User.updateUser", getUserMap(user, plainTextPassword));
            }
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

    public boolean authorizeUser(User user, String plainTextPassword) throws ControllerException {
        try {
            Credentials credentials = (Credentials) SqlConfig.getSqlMapClient().queryForObject("User.getUserCredentials", user);

            if (credentials != null) {
                String checkPasswordHash = encrypter.getHash(plainTextPassword, credentials.getSalt());
                return checkPasswordHash.equals(credentials.getPassword());
            }

            return false;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
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

    private Map<String, Object> getUserMap(User user, String plainTextPassword) {
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

        // hash the user's password before storing it in the database
        try {
            String salt = encrypter.getSalt();
            parameterMap.put("password", encrypter.getHash(plainTextPassword, salt));
            parameterMap.put("salt", salt);
        } catch (EncryptionException ee) {
            // ignore this
        }

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

}
