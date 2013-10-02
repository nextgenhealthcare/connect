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

            return new RedirectResolution(Constants.INDEX_PAGE);
        } catch (Exception e) {
            return new RedirectResolution(Constants.INDEX_PAGE).addParameter("showAlert", true);
        }
    }
}
