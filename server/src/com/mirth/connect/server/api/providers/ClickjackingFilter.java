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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration.PropertiesConfiguration;

@Provider
public class ClickjackingFilter implements Filter {

    private String contentSecurityPolicy;
    private String xFrameOptions;

    public ClickjackingFilter(PropertiesConfiguration mirthProperties) {
        contentSecurityPolicy = mirthProperties.getString("server.api.contentsecuritypolicy", "frame-ancestors 'none'");
        xFrameOptions = mirthProperties.getString("server.api.xframeoptions", "DENY");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Content-Security-Policy", contentSecurityPolicy);
        res.addHeader("X-Frame-Options", xFrameOptions);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}