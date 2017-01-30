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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Provider
public class ApiOriginFilter implements Filter {

    private String allowOrigin;
    private String allowCredentials;
    private String allowMethods;
    private String allowHeaders;
    private String exposeHeaders;
    private String maxAge;

    public ApiOriginFilter(PropertiesConfiguration mirthProperties) {
        allowOrigin = mirthProperties.getString("server.api.accesscontrolalloworigin");
        allowCredentials = mirthProperties.getString("server.api.accesscontrolallowcredentials");
        allowMethods = mirthProperties.getString("server.api.accesscontrolallowmethods");
        allowHeaders = mirthProperties.getString("server.api.accesscontrolallowheaders");
        exposeHeaders = mirthProperties.getString("server.api.accesscontrolexposeheaders");
        maxAge = mirthProperties.getString("server.api.accesscontrolmaxage");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        if (StringUtils.isNotBlank(allowOrigin)) {
            res.addHeader("Access-Control-Allow-Origin", allowOrigin);
        }
        if (StringUtils.isNotBlank(allowCredentials)) {
            res.addHeader("Access-Control-Allow-Credentials", Boolean.toString(BooleanUtils.toBoolean(allowCredentials)));
        }
        if (StringUtils.isNotBlank(allowMethods)) {
            res.addHeader("Access-Control-Allow-Methods", allowMethods);
        }
        if (StringUtils.isNotBlank(allowHeaders)) {
            res.addHeader("Access-Control-Allow-Headers", allowHeaders);
        }
        if (StringUtils.isNotBlank(exposeHeaders)) {
            res.addHeader("Access-Control-Expose-Headers", exposeHeaders);
        }
        if (StringUtils.isNotBlank(maxAge)) {
            res.addHeader("Access-Control-Max-Age", Long.toString(NumberUtils.toLong(maxAge, 300L)));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}