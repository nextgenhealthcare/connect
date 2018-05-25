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

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

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
import com.mirth.connect.server.api.providers.ResponseCodeFilter;
import com.mirth.connect.server.controllers.AuthorizationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.UserController;

public class MessageServletTest {
    static ControllerFactory controllerFactory;
    static EngineController engineController;
    static HttpSession session;
    static HttpServletRequest request;
    static ContainerRequestContext context;
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
        when(engineController.dispatchRawMessage(eq("channelException"), any(), anyBoolean(), anyBoolean())).thenThrow(new ChannelException(false));
        when(engineController.dispatchRawMessage(eq("batchMessageException"), any(), anyBoolean(), anyBoolean())).thenThrow(new BatchMessageException());
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

        context = new MessageServletTest().new TestContainerRequestContext();

        sc = mock(SecurityContext.class);
    }

    @Test
    public void testProcessMessageReturnsMessageId() {
        MessageServlet servlet = new MessageServlet(request, context, sc);

        Long messageId = servlet.processMessage("channel1", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertEquals(1L, messageId.longValue());
        assertEquals(201, context.getProperty(ResponseCodeFilter.RESPONSE_CODE_PROPERTY));

        messageId = servlet.processMessage("channel2", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertEquals(100L, messageId.longValue());
        assertEquals(201, context.getProperty(ResponseCodeFilter.RESPONSE_CODE_PROPERTY));
    }

    @Test
    public void testProcessMessageWithException() {
        MessageServlet servlet = new MessageServlet(request, context, sc);

        Long messageId = servlet.processMessage("channelException", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertNull(messageId);
        assertEquals(500, context.getProperty(ResponseCodeFilter.RESPONSE_CODE_PROPERTY));

        messageId = servlet.processMessage("batchMessageException", "test data", new HashSet<Integer>(), new HashSet<String>(), false, false, null);
        assertNull(messageId);
        assertEquals(500, context.getProperty(ResponseCodeFilter.RESPONSE_CODE_PROPERTY));
    }

    private class TestDispatchResult extends DispatchResult {

        public TestDispatchResult(long messageId) {
            super(messageId, null, null, true, true);
        }
    }

    private class TestContainerRequestContext implements ContainerRequestContext {

        private Map<String, Object> properties = new HashMap<>();

        @Override
        public Object getProperty(String arg0) {
            return properties.get(arg0);
        }

        @Override
        public void setProperty(String arg0, Object arg1) {
            properties.put(arg0, arg1);
        }

        // Unimplemented methods
        @Override
        public void abortWith(Response arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public List<Locale> getAcceptableLanguages() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Map<String, Cookie> getCookies() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Date getDate() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getEntityStream() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getHeaderString(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Locale getLanguage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public MediaType getMediaType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMethod() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<String> getPropertyNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Request getRequest() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SecurityContext getSecurityContext() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public UriInfo getUriInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasEntity() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void removeProperty(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setEntityStream(InputStream arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setMethod(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setRequestUri(URI arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setRequestUri(URI arg0, URI arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setSecurityContext(SecurityContext arg0) {
            // TODO Auto-generated method stub

        }

    }

}
