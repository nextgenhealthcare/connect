/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import com.mirth.connect.webadmin.utils.Constants;

public class IndexActionBean extends BaseActionBean {
    private boolean secureHttps;

    @DefaultHandler
    public Resolution init() {
        BaseActionBeanContext context = getContext();
        HttpServletRequest request = context.getRequest();
        InputStream mirthPropertiesStream = getClass().getResourceAsStream("/mirth.properties");

        // Set the default properties in case mirth.properties fails to load
        String httpsPort = "8443";
        String httpPort = "8080";
        String contextPath = "/";
        String httpsHost = "0.0.0.0";

        if (mirthPropertiesStream != null) {
            Properties mirthProps = new Properties();

            try {
                mirthProps.load(mirthPropertiesStream);

                httpsPort = mirthProps.getProperty("https.port", httpsPort);
                httpPort = mirthProps.getProperty("http.port", httpPort);
                contextPath = mirthProps.getProperty("http.contextpath", contextPath);

                // Add a starting slash if one does not exist
                if (!contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                }

                // Remove a trailing slash if one exists
                if (contextPath.endsWith("/")) {
                    contextPath = contextPath.substring(0, contextPath.length() - 1);
                }

            } catch (IOException e) {
                // Ignore
            }
        }

        context.setHttpsPort(httpsPort);
        context.setHttpPort(httpPort);
        context.setContextPath(contextPath);
        context.setServerAddress(getWebServerUrl("https://", httpsHost, httpsPort, contextPath));

        // Check if http or https
        secureHttps = request.isSecure();

        return new ForwardResolution(Constants.INDEX_JSP);
    }

    public boolean isSecureHttps() {
        return secureHttps;
    }

    public void setSecureHttps(boolean secureHttps) {
        this.secureHttps = secureHttps;
    }

    private String getWebServerUrl(String prefix, String host, String port, String contextPath) {
        if (host.equals("0.0.0.0") || host.equals("::")) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                host = "localhost";
            }
        } else if (host.isEmpty()) {
            host = "localhost";
        }

        return prefix + host + ":" + port + contextPath;
    }
}
