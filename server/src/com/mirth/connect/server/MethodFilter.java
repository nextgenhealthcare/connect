/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class MethodFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        String method = ((HttpServletRequest) req).getMethod();
        // Don't allow TRACE/TRACK requests
        if (StringUtils.equalsIgnoreCase(method, "TRACE") || StringUtils.equalsIgnoreCase(method, "TRACK")) {
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {}
}