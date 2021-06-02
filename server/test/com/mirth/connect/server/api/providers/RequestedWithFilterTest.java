package com.mirth.connect.server.api.providers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.junit.Test;
import org.mockito.Mockito;

import com.mirth.connect.client.core.PropertiesConfigurationUtil;

import junit.framework.TestCase;

public class RequestedWithFilterTest extends TestCase {
    
    private PropertiesConfiguration mirthProperties = PropertiesConfigurationUtil.create();
    
    @Test
    //assert that if property is set to false, isRequestedWithHeaderRequired = false
    public void testConstructor() {
       
        mirthProperties.setProperty("server.api.require-requested-with", "false");
        RequestedWithFilter requestedWithFilter = new RequestedWithFilter(mirthProperties);
        assertEquals(requestedWithFilter.isRequestedWithHeaderRequired(), false);
    }
    
    @Test
    //assert that HttpServletResponse.sendError() is called when X-Requested-With is required but not present 
    public void testDoFilterRequestedWithTrue() {
        
        mirthProperties.setProperty("server.api.require-requested-with", "true");
        RequestedWithFilter testFilter = new RequestedWithFilter(mirthProperties);
        
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResp = Mockito.mock(HttpServletResponse.class);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        
        try {
            testFilter.doFilter(mockReq, mockResp, mockFilterChain);
            verify(mockResp).sendError(HttpServletResponse.SC_BAD_REQUEST, "All requests must have 'X-Requested-With' header");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
  //assert that HttpServletResponse.sendError() is NOT called when X-Requested-With is not required but and not present 
    public void testDoFilterRequestedWithFalse() {
        
        mirthProperties.setProperty("server.api.require-requested-with", "false");
        RequestedWithFilter testFilter = new RequestedWithFilter(mirthProperties);
        
        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResp = Mockito.mock(HttpServletResponse.class);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        
        try {
            testFilter.doFilter(mockReq, mockResp, mockFilterChain);
            verify(mockResp, never()).sendError(HttpServletResponse.SC_BAD_REQUEST, "All requests must have 'X-Requested-With' header");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
