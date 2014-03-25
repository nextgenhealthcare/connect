/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;

public abstract class MirthServlet extends HttpServlet {
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_XML = "application/xml";

    private AuthorizationController authorizationController = ControllerFactory.getFactory().createAuthorizationController();

    public boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (session.getAttribute(UserServlet.SESSION_AUTHORIZED) != null) && (session.getAttribute(UserServlet.SESSION_AUTHORIZED).equals(true));
    }

    public boolean isUserAuthorized(HttpServletRequest request, Map<String, Object> parameterMap) throws ServletException {
        try {
            return authorizationController.isUserAuthorized(getCurrentUserId(request), request.getParameter("op"), parameterMap, getRequestIpAddress(request));
        } catch (ControllerException e) {
            throw new ServletException(e);
        }
    }

    public boolean isUserAuthorizedForExtension(HttpServletRequest request, String extensionName, String operation, Map<String, Object> parameterMap) throws ServletException {
        try {
            return authorizationController.isUserAuthorizedForExtension(getCurrentUserId(request), extensionName, operation, parameterMap, getRequestIpAddress(request));
        } catch (ControllerException e) {
            throw new ServletException(e);
        }
    }

    public boolean doesUserHaveChannelRestrictions(HttpServletRequest request) throws ServletException {
        try {
            return authorizationController.doesUserHaveChannelRestrictions(getCurrentUserId(request));
        } catch (ControllerException e) {
            throw new ServletException(e);
        }
    }

    public List<String> getAuthorizedChannelIds(HttpServletRequest request) throws ServletException {
        try {
            return authorizationController.getAuthorizedChannelIds(getCurrentUserId(request));
        } catch (ControllerException e) {
            throw new ServletException(e);
        }
    }

    protected int getCurrentUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getSession().getAttribute(UserServlet.SESSION_USER).toString());
    }

    protected String getRequestIpAddress(HttpServletRequest request) {
        String address = request.getHeader("x-forwarded-for");

        if (address == null) {
            address = request.getRemoteAddr();
        }

        return address;
    }

    protected Integer getIntegerParameter(HttpServletRequest request, String key, Map<String, Object> parameterMap) {
        Integer param = null;

        if (request.getParameterMap().containsKey(key)) {
            param = Integer.parseInt(request.getParameter(key));
            parameterMap.put(key, param);
        }

        return param;
    }

    protected boolean getBooleanParameter(HttpServletRequest request, String key, Map<String, Object> parameterMap) {
        boolean param = false;
        String paramString = request.getParameter(key);

        if (StringUtils.isNotBlank(paramString)) {
            param = Boolean.valueOf(paramString).booleanValue();
            parameterMap.put(key, param);
        }

        return param;
    }

    protected <T> T getSerializedParameter(HttpServletRequest request, String key, Map<String, Object> parameterMap, Serializer serializer, Class<T> clazz) {
        T param = null;
        String requestValue = request.getParameter(key);

        if (StringUtils.isNotBlank(requestValue)) {
            param = serializer.deserialize(requestValue, clazz);
            parameterMap.put(key, param);
        }

        return param;
    }

    protected Set<String> redactChannelIds(HttpServletRequest request, Set<String> channelIds) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        Set<String> finishedChannelIds = new HashSet<String>();

        for (String channelId : channelIds) {
            if (authorizedChannelIds.contains(channelId)) {
                finishedChannelIds.add(channelId);
            }
        }

        return finishedChannelIds;
    }
}
