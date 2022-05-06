/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.providers;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

@Provider
public class RequestedWithFilter implements Filter {

    private boolean isRequestedWithHeaderRequired = true; 


    public RequestedWithFilter(PropertiesConfiguration mirthProperties) {
        
        isRequestedWithHeaderRequired = mirthProperties.getBoolean("server.api.require-requested-with", true);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        String requestedWithHeader = (String) servletRequest.getHeader("X-Requested-With");
        
        //if header is required and not present, send an error
        if(isRequestedWithHeaderRequired && StringUtils.isBlank(requestedWithHeader)) {
            res.sendError(400, "All requests must have 'X-Requested-With' header");
        }
        else {
            chain.doFilter(request, response);
        }
        
    }
    
    public boolean isRequestedWithHeaderRequired() {
        return isRequestedWithHeaderRequired;
    }

    @Override
    public void destroy() {}
}