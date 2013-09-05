/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.mirth.connect.webadmin.utils.Constants;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

public class SecureAccessActionBean extends BaseActionBean {
    @DefaultHandler
    public Resolution secureAccess() {
        HttpServletRequest request = getContext().getRequest();
        StringBuffer requestURL = request.getRequestURL();
        String hostName = request.getRemoteHost();

        // Get hostName
        try {
            URL url = new URL(requestURL.toString());
            hostName = url.getHost();
        } catch (MalformedURLException e) {
            // Ignore
        }

        return new RedirectResolution("https://" + hostName + ":" + getContext().getHttpsPort() + request.getContextPath() + Constants.INDEX_PAGE, false);
    }
}
