/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.User;
import com.mirth.connect.webadmin.utils.Constants;

public class LoginActionBean extends BaseActionBean {
    @DefaultHandler
    public Resolution login() {
        Client client;
        HttpServletRequest request = getContext().getRequest();
        LoginStatus loginStatus = null;

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String version = request.getParameter("version");

        try {
            StringBuffer fullPath = getContext().getRequest().getRequestURL();
            // Strip off the context path of the /webadmin app, but leave the server context path
            String serverAddress = fullPath.toString().substring(0, fullPath.toString().lastIndexOf("/webadmin"));

            client = new Client(serverAddress);
            loginStatus = client.login(username, password, version);
        } catch (Exception e) {
            return new RedirectResolution(Constants.INDEX_PAGE).addParameter("showAlert", true);
        }

        if ((loginStatus != null) && ((loginStatus.getStatus() == LoginStatus.Status.SUCCESS) || (loginStatus.getStatus() == LoginStatus.Status.SUCCESS_GRACE_PERIOD))) {
            try {
                User user = new User();
                user.setUsername(username);
                User validUser = client.getUser(user).get(0);

                // set the sessions attributes
                getContext().setUser(validUser);
                getContext().setAuthorized(true);
                getContext().setClient(client);

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
