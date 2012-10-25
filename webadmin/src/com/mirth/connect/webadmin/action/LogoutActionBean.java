package com.mirth.connect.webadmin.action;

import java.io.IOException;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.webadmin.utils.Constants;

public class LogoutActionBean extends BaseActionBean {
    @DefaultHandler
    public Resolution logout() throws IOException, ClientException {
        try {
            Client client = getContext().getClient();

            // invalidate the current session and logout
            getContext().logout();
            client.logout();

            return new RedirectResolution(Constants.INDEX_PAGE, false);
        } catch (Exception e) {
            return new RedirectResolution(Constants.INDEX_PAGE, false).addParameter("showAlert", true);
        }
    }
}
