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
    private Client client;

    @DefaultHandler
    public Resolution login() {
        HttpServletRequest request = getContext().getRequest();
        LoginStatus loginStatus = null;

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String version = request.getParameter("version");

        try {
            StringBuffer fullPath = getContext().getRequest().getRequestURL();
            String requestUri = getContext().getRequest().getRequestURI();
            String serverAddress = fullPath.toString().replaceAll(requestUri, "");

            client = new Client(serverAddress);
            loginStatus = client.login(username, password, version);
        } catch (Exception e) {
            return new RedirectResolution(Constants.INDEX_PAGE, false).addParameter("showAlert", true);
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
                return new RedirectResolution(Constants.INDEX_PAGE, false).addParameter("showAlert", true);
            }
        } else {
            return new RedirectResolution(Constants.INDEX_PAGE, false).addParameter("showAlert", true);
        }
    }
}
