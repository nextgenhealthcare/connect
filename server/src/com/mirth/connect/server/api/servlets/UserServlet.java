/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.UserServletInterface;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.LoginStatus.Status;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.User;
import com.mirth.connect.server.api.CheckAuthorizedUserId;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.util.UserSessionCache;

public class UserServlet extends MirthServlet implements UserServletInterface {

    private static final UserController userController = ControllerFactory.getFactory().createUserController();
    private static final EventController eventController = ControllerFactory.getFactory().createEventController();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    public UserServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, false);
    }

    @Override
    @DontCheckAuthorized
    public LoginStatus login(String username, String password) {
        LoginStatus loginStatus = null;

        try {
            int tryCount = 0;
            int status = configurationController.getStatus();
            while (status != ConfigurationController.STATUS_INITIAL_DEPLOY && status != ConfigurationController.STATUS_OK) {
                if (tryCount >= 5) {
                    loginStatus = new LoginStatus(Status.FAIL, "Server is still starting or otherwise unavailable. Please try again shortly.");
                    break;
                }

                Thread.sleep(1000);
                status = configurationController.getStatus();
                tryCount++;
            }

            if (loginStatus == null) {
                // Used for the second leg of multi-factor authentication
                String loginData = request.getHeader(LOGIN_DATA_HEADER);

                if (StringUtils.isNotBlank(loginData) && ControllerFactory.getFactory().createExtensionController().getMultiFactorAuthenticationPlugin() != null) {
                    // We're on the second leg of multi-factor authentication, so delegate to the plugin
                    loginStatus = ControllerFactory.getFactory().createExtensionController().getMultiFactorAuthenticationPlugin().authenticate(loginData);
                } else {
                    // Primary authentication
                    loginStatus = userController.authorizeUser(username, password);
                }

                ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

                HttpSession session = request.getSession();

                /*
                 * Default is 72 hours (3 days). The default SSL connection timeout is 24 hours, but
                 * session data can remain active after that period and persist across multiple
                 * connections.
                 */
                session.setMaxInactiveInterval(configurationController.getMaxInactiveSessionInterval());

                username = StringUtils.defaultString(loginStatus.getUpdatedUsername(), username);

                User validUser = null;

                if ((loginStatus.getStatus() == LoginStatus.Status.SUCCESS) || (loginStatus.getStatus() == LoginStatus.Status.SUCCESS_GRACE_PERIOD)) {
                    validUser = userController.getUser(null, username);

                    /*
                     * There must be a user to login with and store in the session, even if an
                     * Authorization Plugin returned a LoginStatus of SUCCESS
                     */
                    if (validUser == null) {
                        loginStatus = new LoginStatus(LoginStatus.Status.FAIL, "Could not find a valid user with username: " + username);
                    } else {
                        // set the sessions attributes
                        session.setAttribute(SESSION_USER, validUser.getId());
                        session.setAttribute(SESSION_AUTHORIZED, true);

                        // set the user status to logged in in the database
                        userController.loginUser(validUser);

                        // add the user's session to to session map
                        UserSessionCache.getInstance().registerSessionForUser(session, validUser);
                    }
                }

                // Manually audit the Login event with the username since the user ID has not been stored to the session yet
                ServerEvent event = new ServerEvent(configurationController.getServerId(), operation.getDisplayName());
                if (validUser != null) {
                    event.setUserId(validUser.getId());
                }
                event.setIpAddress(getRequestIpAddress());
                event.setLevel(Level.INFORMATION);

                // Set the outcome to the result of the login attempt
                event.setOutcome(((loginStatus.getStatus() == LoginStatus.Status.SUCCESS) || (loginStatus.getStatus() == LoginStatus.Status.SUCCESS_GRACE_PERIOD)) ? Outcome.SUCCESS : Outcome.FAILURE);

                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("username", username);
                event.setAttributes(attributes);

                eventController.dispatchEvent(event);
            }
        } catch (Exception e) {
            throw new MirthApiException(e);
        }

        if (loginStatus.getStatus() != Status.SUCCESS && loginStatus.getStatus() != Status.SUCCESS_GRACE_PERIOD) {
            throw new MirthApiException(Response.status(Response.Status.UNAUTHORIZED).entity(loginStatus).build());
        }

        return loginStatus;
    }

    @Override
    @DontCheckAuthorized
    public void logout() {
        // Audit the logout request but don't block it
        isUserAuthorized();

        HttpSession session = request.getSession();

        // save the session id before removing them from the session
        Integer userId = (Integer) session.getAttribute(SESSION_USER);

        // remove the sessions attributes
        session.removeAttribute(SESSION_USER);
        session.removeAttribute(SESSION_AUTHORIZED);

        // invalidate the current sessions
        session.invalidate();

        // set the user status to logged out in the database
        User user = new User();
        user.setId(userId);

        try {
            userController.logoutUser(user);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void createUser(User user) {
        try {
            userController.updateUser(user);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public List<User> getAllUsers() {
        try {
            if (!isUserAuthorized()) {
                List<User> users = new ArrayList<User>();
                users.add(userController.getUser(getCurrentUserId(), null));
                return users;
            }
            return userController.getAllUsers();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public User getUser(String userIdOrName) {
        parameterMap.put("userIdOrName", userIdOrName);

        try {
            String str = userIdOrName;
            Integer userId = null;
            User user = null;

            try {
                userId = Integer.parseInt(str);
                user = userController.getUser(userId, null);
            } catch (NumberFormatException e) {
            }

            if (user != null) {
                if (!isUserAuthorized() && !isCurrentUser(userId)) {
                    throw new MirthApiException(Response.Status.FORBIDDEN);
                }
                return user;
            }

            // Try to query by name instead
            user = userController.getUser(null, str);

            if (user != null) {
                userId = user.getId();
                if (!isUserAuthorized() && !isCurrentUser(userId)) {
                    throw new MirthApiException(Response.Status.FORBIDDEN);
                }
                return user;
            }

            if (!isUserAuthorized()) {
                throw new MirthApiException(Response.Status.FORBIDDEN);
            }
            throw new MirthApiException(Response.Status.NOT_FOUND);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            return userController.getUser(getCurrentUserId(), null);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId
    public void updateUser(Integer userId, User user) {
        try {
            userController.updateUser(user);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<String> checkUserPassword(String plainPassword) {
        try {
            return userController.checkOrUpdateUserPassword(null, plainPassword);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId
    public List<String> updateUserPassword(Integer userId, String plainPassword) {
        try {
            return userController.checkOrUpdateUserPassword(userId, plainPassword);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void removeUser(Integer userId) {
        try {
            userController.removeUser(userId, getCurrentUserId());
            UserSessionCache.getInstance().invalidateAllSessionsForUser(userId);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public boolean isUserLoggedIn(Integer userId) {
        try {
            return userController.isUserLoggedIn(userId);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId(auditCurrentUser = false)
    public Properties getUserPreferences(Integer userId, Set<String> names) {
        try {
            return userController.getUserPreferences(userId, names);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId(auditCurrentUser = false)
    public String getUserPreference(Integer userId, String name) {
        try {
            return userController.getUserPreference(userId, name);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId(auditCurrentUser = false)
    public void setUserPreferences(Integer userId, Properties properties) {
        try {
            userController.setUserPreferences(userId, properties);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedUserId(auditCurrentUser = false)
    public void setUserPreference(Integer userId, String name, String value) {
        try {
            userController.setUserPreference(userId, name, value);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }
}