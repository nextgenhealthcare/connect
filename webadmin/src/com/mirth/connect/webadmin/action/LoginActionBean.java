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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.donkey.util.ResourceUtil;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.User;
import com.mirth.connect.webadmin.utils.Constants;

public class LoginActionBean extends BaseActionBean {
    @DefaultHandler
    public Resolution login() {
        Client client;
        HttpServletRequest request = getContext().getRequest();
        LoginStatus loginStatus = null;
        InputStream mirthPropertiesStream = getClass().getResourceAsStream("/mirth.properties");
        String httpsPort = "8443";
        String contextPath = "/";
        
        if (mirthPropertiesStream != null) {
            Properties mirthProps = new Properties();

            try {
                mirthProps.load(mirthPropertiesStream);

                httpsPort = mirthProps.getProperty("https.port", httpsPort);
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
            } finally {
                ResourceUtil.closeResourceQuietly(mirthPropertiesStream);
            }
        }
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            client = new Client(getContext().getServerAddress());
            loginStatus = client.login(username, password);
        } catch (Exception e) {
            return new RedirectResolution(Constants.INDEX_PAGE).addParameter("showAlert", true);
        }

        if ((loginStatus != null) && ((loginStatus.getStatus() == LoginStatus.Status.SUCCESS) || (loginStatus.getStatus() == LoginStatus.Status.SUCCESS_GRACE_PERIOD))) {
            try {
                User validUser = client.getUser(loginStatus.getUpdatedUsername() != null ? loginStatus.getUpdatedUsername() : username);

                // recreate the session to prevent session fixation attack
                request.getSession().invalidate();
                request.getSession(true);
                
                // set the sessions attributes
                getContext().setUser(validUser);
                getContext().setAuthorized(true);
                getContext().setClient(client);
                
                getContext().setCurrentPort(httpsPort);
                getContext().setContextPath(contextPath);
                getContext().setCurrentScheme(request.getScheme());

                // this prevents the session from timing out
                request.getSession().setMaxInactiveInterval(-1);

                // Redirect to Dashboard Statistics
                return new RedirectResolution(Constants.DASHBOARD_STATS);

            } catch (Exception e) {
                e.printStackTrace();
                return new RedirectResolution(Constants.INDEX_PAGE).addParameter("showAlert", true);
            }
        } else {
            return new RedirectResolution(Constants.INDEX_PAGE).addParameter("showAlert", true);
        }
    }
}
