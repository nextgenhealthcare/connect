/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.ExtensionOperation;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;

public abstract class MirthServlet {

    public static final String BYPASS_USERNAME = "bypass";

    protected static final String SESSION_USER = "user";
    protected static final String SESSION_AUTHORIZED = "authorized";

    protected HttpServletRequest request;
    protected SecurityContext sc;
    protected ServerEventContext context;
    protected Operation operation;
    protected Map<String, Object> parameterMap;
    protected boolean userHasChannelRestrictions;

    private static final UserController userController = ControllerFactory.getFactory().createUserController();
    private static final AuthorizationController authorizationController = ControllerFactory.getFactory().createAuthorizationController();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    private List<String> authorizedChannelIds;
    private String extensionName;
    private boolean bypassUser;
    private int currentUserId;

    public MirthServlet(HttpServletRequest request, SecurityContext sc) {
        this(request, sc, true);
    }

    public MirthServlet(HttpServletRequest request, SecurityContext sc, boolean initLogin) {
        this(request, sc, null, initLogin);
    }

    public MirthServlet(HttpServletRequest request, SecurityContext sc, String extensionName) {
        this(request, sc, extensionName, true);
    }

    public MirthServlet(HttpServletRequest request, SecurityContext sc, String extensionName, boolean initLogin) {
        this.request = request;
        this.sc = sc;
        this.extensionName = extensionName;
        parameterMap = new HashMap<String, Object>();
        if (initLogin) {
            initLogin();
        }
    }

    protected void initLogin() {
        boolean validLogin = false;

        if (isUserLoggedIn()) {
            currentUserId = Integer.parseInt(request.getSession().getAttribute(SESSION_USER).toString());
            setContext();
            validLogin = true;
        } else {
            // Allow Basic auth
            String authHeader = request.getHeader("Authorization");

            if (StringUtils.startsWith(authHeader, "Basic ")) {
                String username = null;
                String password = null;
                try {
                    authHeader = new String(Base64.decodeBase64(StringUtils.removeStartIgnoreCase(authHeader, "Basic ").trim()), "US-ASCII");
                    int colonIndex = StringUtils.indexOf(authHeader, ':');
                    if (colonIndex > 0) {
                        username = StringUtils.substring(authHeader, 0, colonIndex);
                        password = StringUtils.substring(authHeader, colonIndex + 1);
                    }
                } catch (Exception e) {
                }

                if (username != null && password != null) {
                    if (StringUtils.equals(username, BYPASS_USERNAME)) {
                        // Only allow bypass credentials if the request originated locally
                        if (configurationController.isBypasswordEnabled() && isRequestLocal() && configurationController.checkBypassword(password)) {
                            context = ServerEventContext.SYSTEM_USER_EVENT_CONTEXT;
                            currentUserId = context.getUserId();
                            bypassUser = true;
                            validLogin = true;
                        }
                    } else {
                        try {
                            int status = configurationController.getStatus(false);
                            if (status == ConfigurationController.STATUS_INITIAL_DEPLOY || status == ConfigurationController.STATUS_OK) {
                                LoginStatus loginStatus = userController.authorizeUser(username, password);

                                if ((loginStatus.getStatus() == LoginStatus.Status.SUCCESS) || (loginStatus.getStatus() == LoginStatus.Status.SUCCESS_GRACE_PERIOD)) {
                                    User user = userController.getUser(null, username);

                                    if (user != null) {
                                        currentUserId = user.getId();
                                        setContext();
                                        validLogin = true;
                                    } else {
                                        loginStatus = new LoginStatus(LoginStatus.Status.FAIL, "Could not find a valid user with username: " + username);
                                        throw new MirthApiException(Response.status(Status.UNAUTHORIZED).entity(loginStatus).build());
                                    }
                                } else {
                                    throw new MirthApiException(Response.status(Status.UNAUTHORIZED).entity(loginStatus).build());
                                }
                            } else {
                                LoginStatus loginStatus = new LoginStatus(LoginStatus.Status.FAIL, "Server is still starting or otherwise unavailable. Please try again shortly.");
                                throw new MirthApiException(Response.status(Status.SERVICE_UNAVAILABLE).entity(loginStatus).build());
                            }
                        } catch (ControllerException e) {
                            throw new MirthApiException(e);
                        }
                    }
                }
            }
        }

        if (!validLogin) {
            throw new MirthApiException(Status.UNAUTHORIZED);
        }
    }

    private void setContext() {
        context = new ServerEventContext(currentUserId);

        try {
            userHasChannelRestrictions = authorizationController.doesUserHaveChannelRestrictions(currentUserId);

            if (userHasChannelRestrictions) {
                authorizedChannelIds = authorizationController.getAuthorizedChannelIds(currentUserId);
            }
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    public void setOperation(Operation operation) {
        if (extensionName != null) {
            operation = new ExtensionOperation(extensionName, operation);
        }
        this.operation = operation;
    }

    public void addToParameterMap(String name, Object value) {
        parameterMap.put(name, value);
    }

    protected String getSessionId() {
        return request.getSession().getId();
    }

    protected boolean isUserLoggedIn() {
        HttpSession session = request.getSession();
        return (session.getAttribute(SESSION_AUTHORIZED) != null) && (session.getAttribute(SESSION_AUTHORIZED).equals(true));
    }

    public void checkUserAuthorized() {
        if (!isUserAuthorized()) {
            throw new MirthApiException(Status.FORBIDDEN);
        }
    }

    public void checkUserAuthorized(Integer userId) {
        checkUserAuthorized(userId, true);
    }

    public void checkUserAuthorized(Integer userId, boolean auditCurrentUser) {
        if (auditCurrentUser) {
            if (!isUserAuthorized() && !isCurrentUser(userId)) {
                throw new MirthApiException(Status.FORBIDDEN);
            }
        } else {
            if (!isCurrentUser(userId) && !isUserAuthorized()) {
                throw new MirthApiException(Status.FORBIDDEN);
            }
        }
    }

    public void checkUserAuthorized(String channelId) {
        if (!isUserAuthorized() || isChannelRedacted(channelId)) {
            throw new MirthApiException(Status.FORBIDDEN);
        }
    }

    protected boolean isUserAuthorized() {
        return isUserAuthorized(true);
    }

    protected boolean isUserAuthorized(boolean audit) {
        if (context == null) {
            initLogin();
        }

        if (operation == null) {
            throw new MirthApiException("Method operation not set.");
        }

        try {
            if (bypassUser) {
                if (audit) {
                    auditAuthorizationRequest(Outcome.SUCCESS);
                }
                return true;
            } else {
                return authorizationController.isUserAuthorized(getCurrentUserId(), operation, parameterMap, getRequestIpAddress(), audit);
            }
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    protected void checkUserAuthorizedForExtension(String extensionName) {
        if (!isUserAuthorizedForExtension(extensionName)) {
            throw new MirthApiException(Status.FORBIDDEN);
        }
    }

    protected boolean isUserAuthorizedForExtension(String extensionName) {
        return isUserAuthorizedForExtension(extensionName, true);
    }

    protected boolean isUserAuthorizedForExtension(String extensionName, boolean audit) {
        if (operation == null) {
            throw new MirthApiException("Method operation not set.");
        }

        try {
            ExtensionOperation extensionOperation = new ExtensionOperation(extensionName, operation);

            if (bypassUser) {
                if (audit) {
                    auditAuthorizationRequest(Outcome.SUCCESS, extensionOperation);
                }
                return true;
            } else {
                return authorizationController.isUserAuthorized(getCurrentUserId(), extensionOperation, parameterMap, getRequestIpAddress(), audit);
            }
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    protected void auditAuthorizationRequest(Outcome outcome) {
        auditAuthorizationRequest(outcome, operation);
    }

    protected void auditAuthorizationRequest(Outcome outcome, Operation operation) {
        authorizationController.auditAuthorizationRequest(getCurrentUserId(), operation, parameterMap, outcome, getRequestIpAddress());
    }

    protected int getCurrentUserId() {
        return currentUserId;
    }

    protected String getRequestIpAddress() {
        String address = request.getHeader("x-forwarded-for");

        if (address == null) {
            address = request.getRemoteAddr();
        }

        return address;
    }

    protected List<Channel> redactChannels(List<Channel> channels) {
        if (userHasChannelRestrictions) {
            List<String> authorizedChannelIds = getAuthorizedChannelIds();
            List<Channel> authorizedChannels = new ArrayList<Channel>();

            for (Channel channel : channels) {
                if (authorizedChannelIds.contains(channel.getId())) {
                    authorizedChannels.add(channel);
                }
            }

            return authorizedChannels;
        } else {
            return channels;
        }
    }

    protected Set<String> redactChannelIds(Set<String> channelIds) {
        if (userHasChannelRestrictions) {
            List<String> authorizedChannelIds = getAuthorizedChannelIds();
            Set<String> finishedChannelIds = new HashSet<String>();

            for (String channelId : channelIds) {
                if (authorizedChannelIds.contains(channelId)) {
                    finishedChannelIds.add(channelId);
                }
            }

            return finishedChannelIds;
        } else {
            return channelIds;
        }
    }

    protected <T> Map<String, T> redactChannelIds(Map<String, T> map) {
        if (userHasChannelRestrictions) {
            Map<String, T> authorizedMap = new HashMap<String, T>();

            for (Entry<String, T> entry : map.entrySet()) {
                if (authorizedChannelIds.contains(entry.getKey())) {
                    authorizedMap.put(entry.getKey(), entry.getValue());
                }
            }

            return authorizedMap;
        } else {
            return map;
        }
    }

    protected List<ChannelSummary> redactChannelSummaries(List<ChannelSummary> channelSummaries) {
        if (userHasChannelRestrictions) {
            List<String> authorizedChannelIds = getAuthorizedChannelIds();
            List<ChannelSummary> authorizedChannelSummaries = new ArrayList<ChannelSummary>();

            for (ChannelSummary channelSummary : channelSummaries) {
                if (authorizedChannelIds.contains(channelSummary.getChannelId())) {
                    authorizedChannelSummaries.add(channelSummary);
                }
            }

            return authorizedChannelSummaries;
        } else {
            return channelSummaries;
        }
    }

    protected boolean isChannelRedacted(String channelId) {
        return userHasChannelRestrictions && !authorizedChannelIds.contains(channelId);
    }

    protected boolean isCurrentUser(Integer userId) {
        return userId == getCurrentUserId();
    }

    protected List<String> getAuthorizedChannelIds() {
        return authorizedChannelIds;
    }

    private boolean isRequestLocal() {
        String remoteAddr = request.getRemoteAddr();

        try {
            if (StringUtils.equals(InetAddress.getLocalHost().getHostAddress(), remoteAddr)) {
                return true;
            }
        } catch (UnknownHostException e) {
        }

        try {
            for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
                if (StringUtils.equals(inetAddress.getHostAddress(), remoteAddr)) {
                    return true;
                }
            }
        } catch (UnknownHostException e) {
        }

        return false;
    }
}