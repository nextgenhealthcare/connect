/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.User;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.util.UserSessionCache;

public class UserServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public static final String SESSION_USER = "user";
    public static final String SESSION_AUTHORIZED = "authorized";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserController userController = ControllerFactory.getFactory().createUserController();
        EventController eventController = ControllerFactory.getFactory().createEventController();
        PrintWriter out = response.getWriter();
        Operation operation = Operations.getOperation(request.getParameter("op"));
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (operation.equals(Operations.USER_LOGIN)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String version = request.getParameter("version");
            response.setContentType("text/plain");
            out.print(login(request, response, userController, eventController, username, password, version));
        } else if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                
                if (operation.equals(Operations.USER_AUTHORIZE)) {
                    response.setContentType("text/plain");
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    String password = request.getParameter("password");
                    out.print(userController.authorizeUser(user, password));
                } else if (operation.equals(Operations.USER_LOGOUT)) {
                    logout(request, userController, eventController);
                } else if (operation.equals(Operations.USER_GET)) {
                    /*
                     * If the requesting user does not have permission, only
                     * return themselves.
                     */
                    response.setContentType("application/xml");
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (!isUserAuthorized(request, parameterMap)) {
                        user = new User();
                        user.setId(getCurrentUserId(request));
                    }

                    out.println(serializer.toXML(userController.getUser(user)));
                } else if (operation.equals(Operations.USER_UPDATE)) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (isUserAuthorized(request, parameterMap) || isCurrentUser(request, user)) {
                        String password = request.getParameter("password");
                        userController.updateUser(user, password);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.USER_REMOVE)) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (isUserAuthorized(request, parameterMap)) {
                        UserSessionCache.getInstance().invalidateAllSessionsForUser(user);
                        userController.removeUser(user, (Integer) request.getSession().getAttribute(SESSION_USER));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.USER_IS_USER_LOGGED_IN)) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (isUserAuthorized(request, parameterMap)) {
                        response.setContentType("text/plain");
                        out.print(userController.isUserLoggedIn(user));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.USER_PREFERENCES_GET)) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (isUserAuthorized(request, parameterMap) || isCurrentUser(request, user)) {
                        response.setContentType("text/plain");
                        out.println(serializer.toXML(userController.getUserPreferences(user)));
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else if (operation.equals(Operations.USER_PREFERENCES_SET)) {
                    User user = (User) serializer.fromXML(request.getParameter("user"));
                    parameterMap.put("user", user);

                    if (isUserAuthorized(request, parameterMap) || isCurrentUser(request, user)) {
                        String name = request.getParameter("name");
                        String value = request.getParameter("value");
                        userController.setUserPreference(user, name, value);
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }

    private boolean login(HttpServletRequest request, HttpServletResponse response, UserController userController, EventController eventController, String username, String password, String version) throws ServletException {
        try {
            ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

            // if the version of the client in is not the same as the server and
            // the version is not 0.0.0 (bypass)
            if (!version.equals(configurationController.getServerVersion()) && !version.equals("0.0.0")) {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return false;
            }

            HttpSession session = request.getSession();

            User user = new User();
            user.setUsername(username);

            if (userController.authorizeUser(user, password)) {
                User validUser = userController.getUser(user).get(0);

                // set the sessions attributes
                session.setAttribute(SESSION_USER, validUser.getId());
                session.setAttribute(SESSION_AUTHORIZED, true);

                // this prevents the session from timing out
                session.setMaxInactiveInterval(-1);

                // set the user status to logged in in the database
                userController.loginUser(validUser);

                // add the user's session to to session map
                UserSessionCache.getInstance().registerSessionForUser(session, validUser);

                return true;
            }

            // failed to login
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void logout(HttpServletRequest request, UserController userController, EventController eventController) throws ServletException {
        HttpSession session = request.getSession();

        // save the session id before removing them from the session
        Integer userId = (Integer) session.getAttribute(SESSION_USER);
        String sessionId = session.getId();

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
        } catch (ControllerException ce) {
            throw new ServletException(ce);
        }

        // delete any temp tables created for this session
        ControllerFactory.getFactory().createMessageObjectController().removeFilterTable(sessionId);
        eventController.removeEventFilterTable(sessionId);
    }

    private boolean isCurrentUser(HttpServletRequest request, User user) {
        return user.getId() == getCurrentUserId(request);
    }
}
