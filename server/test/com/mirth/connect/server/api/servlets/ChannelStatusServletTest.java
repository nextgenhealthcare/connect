/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.SecurityContext;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.LoginStatus.Status;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.UserController;

public class ChannelStatusServletTest {

    static ControllerFactory controllerFactory;
    static EngineController engineController;
    static HttpSession session;
    static HttpServletRequest request;
    static SecurityContext sc;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws Exception {
        controllerFactory = mock(ControllerFactory.class);

        engineController = mock(EngineController.class);
        when(engineController.getChannelStatus(anyString())).thenAnswer((InvocationOnMock invocation) -> {
            DashboardStatus status = new DashboardStatus();
            status.setChannelId(invocation.getArgument(0));
            return status;
        });
        when(engineController.getChannelStatusList(any(), anyBoolean())).thenAnswer((InvocationOnMock invocation) -> {
            return getStatusList();
        });
        when(engineController.getChannelStatusList(any())).thenAnswer((InvocationOnMock invocation) -> {
            return getStatusList();
        });
        when(engineController.getDeployedIds()).thenAnswer((InvocationOnMock invocation) -> {
            Set<String> deployed = new HashSet<>();
            deployed.add("3");
            deployed.add("4");
            deployed.add("5");
            return deployed;
        });
        when(controllerFactory.createEngineController()).thenReturn(engineController);

        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(configurationController.getChannelTags()).thenAnswer((InvocationOnMock invocation) -> {
            Set<ChannelTag> tags = new HashSet<>();
            tags.add(ChannelStatusServletTest.createTag("Tag1", Sets.newHashSet()));
            tags.add(ChannelStatusServletTest.createTag("Tag2", Sets.newHashSet("A", "B")));
            tags.add(ChannelStatusServletTest.createTag("Tag3", Sets.newHashSet("3", "4")));
            tags.add(ChannelStatusServletTest.createTag("Tag4", Sets.newHashSet("4")));
            return tags;
        });
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        UserController userController = mock(UserController.class);
        when(userController.authorizeUser(anyString(), anyString())).thenReturn(new LoginStatus(Status.SUCCESS, ""));
        when(userController.getUser(anyInt(), anyString())).thenAnswer((InvocationOnMock invocation) -> {
            User user = new User();
            user.setId(1);
            user.setUsername(invocation.getArgument(1));
            return user;
        });
        when(controllerFactory.createUserController()).thenReturn(userController);

        AuthorizationController authorizationController = mock(AuthorizationController.class);
        when(authorizationController.doesUserHaveChannelRestrictions(anyInt())).thenReturn(false);
        when(authorizationController.isUserAuthorized(anyInt(), any(Operation.class), any(Map.class), anyString(), anyBoolean())).thenReturn(true);
        when(controllerFactory.createAuthorizationController()).thenReturn(authorizationController);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);

        session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("1");
        when(session.getAttribute("authorized")).thenReturn(Boolean.TRUE);

        request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        sc = mock(SecurityContext.class);

    }

    private static DashboardStatus createStatus(String id, String name, DeployedState deployState) {
        DashboardStatus status = mock(DashboardStatus.class);
        when(status.getChannelId()).thenReturn(id);
        when(status.getName()).thenReturn(name);
        when(status.getState()).thenReturn(deployState);
        return status;
    }

    private static ChannelTag createTag(String name, Set<String> channelIds) {
        ChannelTag tag = mock(ChannelTag.class);
        when(tag.getName()).thenReturn(name);
        when(tag.getChannelIds()).thenReturn(channelIds);
        return tag;
    }

    private static List<DashboardStatus> getStatusList() {
        List<DashboardStatus> list = new LinkedList<>();
        list.add(ChannelStatusServletTest.createStatus("1", "One", DeployedState.STARTED));
        list.add(ChannelStatusServletTest.createStatus("2", "Two", DeployedState.STOPPED));
        list.add(ChannelStatusServletTest.createStatus("3", "Three", DeployedState.STARTED));
        list.add(ChannelStatusServletTest.createStatus("4", "Four", DeployedState.STARTED));
        list.add(ChannelStatusServletTest.createStatus("5", "Five", DeployedState.UNDEPLOYED));
        return list;
    }

    @Test
    public void testGetChannelStatusListFilterByName() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("1");
        when(session.getAttribute("authorized")).thenReturn(Boolean.TRUE);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        SecurityContext sc = mock(SecurityContext.class);
        ChannelStatusServlet servlet = new ChannelStatusServlet(request, sc);
        // don't need to pass in list, engineController.getChannelStatusList() has been mocked to return a list of DashboardStatus, which we will filter on
        List<DashboardStatus> statusList = servlet.getChannelStatusList(null, "Name:Three", true);
        assertEquals(1, statusList.size());
        assertEquals("3", statusList.get(0).getChannelId());
        assertEquals("Three", statusList.get(0).getName());

        statusList = servlet.getChannelStatusList(null, "Name:3", true);
        assertEquals(0, statusList.size());

        statusList = servlet.getChannelStatusList(null, "Name:FAKE", true);
        assertEquals(0, statusList.size());
    }

    @Test
    public void testGetChannelStatusListNoFilter() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("1");
        when(session.getAttribute("authorized")).thenReturn(Boolean.TRUE);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        SecurityContext sc = mock(SecurityContext.class);
        ChannelStatusServlet servlet = new ChannelStatusServlet(request, sc);
        // don't need to pass in list, engineController.getChannelStatusList() has been mocked to return a list of DashboardStatus, which we will filter on
        List<DashboardStatus> statusList = servlet.getChannelStatusList(null, null, true);

        // statusList == [{Dashboard Status with ID = "1"}, {Dashboard Status with ID = "2"}, {Dashboard Status with ID = "3"},
        //              {Dashboard Status with ID = "4"}, {Dashboard Status with ID = "5"}]
        assertEquals(5, statusList.size());
        assertEquals("1", statusList.get(0).getChannelId());
        assertEquals("2", statusList.get(1).getChannelId());
        assertEquals("3", statusList.get(2).getChannelId());
        assertEquals("4", statusList.get(3).getChannelId());
        assertEquals("5", statusList.get(4).getChannelId());
    }

    @Test
    public void testGetChannelStatusListFilterByTag() throws Exception {
        ChannelStatusServlet servlet = new ChannelStatusServlet(request, sc);
        // don't need to pass in list, engineController.getChannelStatusList() has been mocked to return a list of DashboardStatus, which we will filter on
        List<DashboardStatus> statusList = servlet.getChannelStatusList(null, "Tag:Tag3", true);
        // statusList == [{Dashboard Status with ID = "3"}, {Dashboard Status with ID = "4"}]
        assertEquals(2, statusList.size());
        assertEquals("3", statusList.get(0).getChannelId());
        assertEquals("4", statusList.get(1).getChannelId());

        // statusList == []
        statusList = servlet.getChannelStatusList(null, "Tag:Tag2", true);
        assertEquals(0, statusList.size());

        // statusList == []
        statusList = servlet.getChannelStatusList(null, "Tag:FAKE", true);
        assertEquals(0, statusList.size());
    }

    @Test
    public void testGetDashboardChannelInfo() throws Exception {
        ChannelStatusServlet servlet = new ChannelStatusServlet(request, sc);
        DashboardChannelInfo dashboardChannelInfo = servlet.getDashboardChannelInfo(1, "Tag:Tag3");
        // dashboardChannelInfo.getDeployedChannelCount() == 3
        assertEquals(3, dashboardChannelInfo.getDeployedChannelCount());
        // dashboardChannelInfo.getRemainingChannelIds() == ["4"]
        assertEquals(1, dashboardChannelInfo.getRemainingChannelIds().size());
        assertEquals("4", dashboardChannelInfo.getRemainingChannelIds().iterator().next());
        // dashboardChannelInfo.getRemainingChannelIds() == [{Dashboard Status with ID = "3"}, {Dashboard Status with ID = "4"}]
        assertEquals(2, dashboardChannelInfo.getDashboardStatuses().size());
        assertEquals("3", dashboardChannelInfo.getDashboardStatuses().get(0).getChannelId());
        assertEquals("4", dashboardChannelInfo.getDashboardStatuses().get(1).getChannelId());
    }
}