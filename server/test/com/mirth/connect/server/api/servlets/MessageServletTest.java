package com.mirth.connect.server.api.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.client.core.Operation;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.LoginStatus.Status;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.UserController;

public class MessageServletTest {
	static ControllerFactory controllerFactory;
    static EngineController engineController;
    static HttpSession session;
    static HttpServletRequest request;
    static SecurityContext sc;

    @SuppressWarnings("unchecked")
	@BeforeClass
    public static void setup() throws Exception {
        controllerFactory = mock(ControllerFactory.class);
        
        engineController = mock(EngineController.class);
        DispatchResult result1 = new MessageServletTest().new TestDispatchResult(1L);
        DispatchResult result2 = new MessageServletTest().new TestDispatchResult(100L);
        when(engineController.dispatchRawMessage(eq("channel1"), any(), anyBoolean(), anyBoolean())).thenReturn(result1);
        when(engineController.dispatchRawMessage(eq("channel2"), any(), anyBoolean(), anyBoolean())).thenReturn(result2);
        when(engineController.dispatchRawMessage(eq("channelException1"), any(), anyBoolean(), anyBoolean())).thenThrow(new ChannelException(false));
        when(engineController.dispatchRawMessage(eq("channelException2"), any(), anyBoolean(), anyBoolean())).thenThrow(new BatchMessageException());
        when(controllerFactory.createEngineController()).thenReturn(engineController);

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

	@Test
	public void testProcessMessageReturnsMessageId() { 
        MessageServlet servlet = new MessageServlet(request, sc);
        
        Response response = servlet.processMessage("channel1", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertEquals(1L, response.getEntity());
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        assertEquals(201, response.getStatus());
        
        response = servlet.processMessage("channel2", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertEquals(100L, response.getEntity());
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        assertEquals(201, response.getStatus());
	}
	
	@Test 
	public void testProcessMessageWithException() {
		MessageServlet servlet = new MessageServlet(request, sc);
		
        Response response = servlet.processMessage("channelException1", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertNull(response);
        
        response = servlet.processMessage("channelException2", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertNull(response);
	}
	
	public class TestDispatchResult extends DispatchResult {
		
		public TestDispatchResult(long messageId) {
			super(messageId, null, null, true, true);
		}
	}

}
