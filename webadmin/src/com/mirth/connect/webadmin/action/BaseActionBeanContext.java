/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import net.sourceforge.stripes.action.ActionBeanContext;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.model.User;

/**
 * ActionBeanContext subclass that manages where values like the logged in user
 * are stored.
 */
public class BaseActionBeanContext extends ActionBeanContext {
    public User getUser() {
        return (User) getRequest().getSession().getAttribute("user");
    }

    public void setUser(User currentUser) {
        getRequest().getSession().setAttribute("user", currentUser);
    }

    public boolean getAuthorized() {
        return (Boolean) getRequest().getSession().getAttribute("authorized");
    }

    public void setAuthorized(boolean authorized) {
        getRequest().getSession().setAttribute("authorized", authorized);
    }

    public Client getClient() {
        return (Client) getRequest().getSession().getAttribute("client");
    }

    public void setClient(Client client) {
        getRequest().getSession().setAttribute("client", client);
    }

    public String getHttpPort() {
        return (String) getRequest().getSession().getAttribute("httpPort");
    }

    public void setHttpPort(String httpPort) {
        getRequest().getSession().setAttribute("httpPort", httpPort);
    }

    public String getHttpsPort() {
        return (String) getRequest().getSession().getAttribute("httpsPort");
    }

    public void setHttpsPort(String httpsPort) {
        getRequest().getSession().setAttribute("httpsPort", httpsPort);
    }

    public String getServerAddress() {
        return (String) getRequest().getSession().getAttribute("serverAddress");
    }

    public void setServerAddress(String serverAddress) {
        getRequest().getSession().setAttribute("serverAddress", serverAddress);
    }

    // Logs the user out by invalidating the session.
    public void logout() {
        getRequest().getSession().invalidate();
    }
}
