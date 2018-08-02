/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.LoginStatus.Status;
import com.mirth.connect.model.User;
import com.mirth.connect.server.alert.action.ChannelProtocol;
import com.mirth.connect.server.api.providers.MirthResourceInvocationHandlerProvider;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ChannelAuthorizer;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;

public class ServletTestBase {

    protected static String CHANNEL_ID1 = "channel1";
    protected static String CHANNEL_ID2 = "channel2";
    protected static String DISALLOWED_CHANNEL_ID = "disallowedchannelid";

    protected static DashboardStatus DASHBOARD_STATUS1;
    protected static DashboardStatus DASHBOARD_STATUS2;
    protected static DashboardStatus DISALLOWED_DASHBOARD_STATUS;

    protected static Channel CHANNEL1 = new Channel(CHANNEL_ID1);
    protected static Channel CHANNEL2 = new Channel(CHANNEL_ID2);
    protected static Channel DISALLOWED_CHANNEL = new Channel(DISALLOWED_CHANNEL_ID);

    protected static ControllerFactory controllerFactory;
    protected static ConfigurationController configurationController;
    protected static AuthorizationController authorizationController;
    protected static ChannelController channelController;
    protected static AlertController alertController;
    protected static UserController userController;
    protected static HttpSession session;
    protected static HttpServletRequest request;
    protected static SecurityContext sc;
    protected static InvocationHandler ih;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws Exception {
        DASHBOARD_STATUS1 = new DashboardStatus();
        DASHBOARD_STATUS1.setChannelId(CHANNEL_ID1);
        DASHBOARD_STATUS2 = new DashboardStatus();
        DASHBOARD_STATUS2.setChannelId(CHANNEL_ID2);
        DISALLOWED_DASHBOARD_STATUS = new DashboardStatus();
        DISALLOWED_DASHBOARD_STATUS.setChannelId(DISALLOWED_CHANNEL_ID);

        controllerFactory = mock(ControllerFactory.class);

        configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        userController = mock(UserController.class);
        when(userController.authorizeUser(anyString(), anyString())).thenReturn(new LoginStatus(Status.SUCCESS, ""));
        when(userController.getUser(any(), any())).thenAnswer((invocation) -> {
            User user = new User();
            user.setId(invocation.getArgument(0) != null ? invocation.getArgument(0) : 1);
            user.setUsername(invocation.getArgument(1) != null ? invocation.getArgument(1) : "test");
            return user;
        });
        when(controllerFactory.createUserController()).thenReturn(userController);

        authorizationController = mock(AuthorizationController.class);
        when(authorizationController.doesUserHaveChannelRestrictions(anyInt(), any())).thenReturn(true);
        when(authorizationController.isUserAuthorized(any(), any(), any(), any(), anyBoolean())).thenReturn(true);
        when(authorizationController.getChannelAuthorizer(any(), any())).thenAnswer((invocation) -> {
            return new ChannelAuthorizer() {
                @Override
                public boolean isChannelAuthorized(String channelId) {
                    return !StringUtils.equals(channelId, DISALLOWED_CHANNEL_ID);
                }
            };
        });
        when(controllerFactory.createAuthorizationController()).thenReturn(authorizationController);

        channelController = mock(ChannelController.class);
        when(channelController.getChannelSummary(any(Map.class), anyBoolean())).thenAnswer((invocation) -> {
            List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();
            for (String channelId : ((Map<String, ChannelHeader>) invocation.getArgument(0)).keySet()) {
                channelSummaries.add(new ChannelSummary(channelId));
            }
            return channelSummaries;
        });
        when(controllerFactory.createChannelController()).thenReturn(channelController);

        alertController = mock(AlertController.class);
        Map<String, Map<String, String>> options = new HashMap<String, Map<String, String>>();
        Map<String, String> channelOptions = new HashMap<String, String>();
        channelOptions.put(CHANNEL_ID1, CHANNEL_ID1);
        channelOptions.put(CHANNEL_ID2, CHANNEL_ID2);
        channelOptions.put(DISALLOWED_CHANNEL_ID, DISALLOWED_CHANNEL_ID);
        options.put(ChannelProtocol.NAME, channelOptions);
        when(alertController.getAlertActionProtocolOptions()).thenReturn(options);
        when(controllerFactory.createAlertController()).thenReturn(alertController);

        session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("1");
        when(session.getAttribute("authorized")).thenReturn(Boolean.TRUE);

        request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        sc = mock(SecurityContext.class);

        ih = new MirthResourceInvocationHandlerProvider().create(null);
    }

    protected void assertForbiddenInvocation(Object proxy, Method method, Object[] args) {
        try {
            ih.invoke(proxy, method, args);
            fail("Expected authorization exception");
        } catch (Throwable t) {
            assertForbiddenException(t);
        }
    }

    protected void assertForbiddenException(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
        }
        if (!(t instanceof MirthApiException)) {
            fail("Incorrect exception type, expected MirthApiException");
        }
        assertEquals(javax.ws.rs.core.Response.Status.FORBIDDEN.getStatusCode(), ((MirthApiException) t).getResponse().getStatus());
    }
}
