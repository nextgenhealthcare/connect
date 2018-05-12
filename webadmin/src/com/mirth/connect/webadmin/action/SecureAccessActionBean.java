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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

import com.mirth.connect.webadmin.utils.Constants;

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

        String httpsPort = getContext().getHttpsPort();

        // If there is no session yet, get port from the properties file
        if (httpsPort == null || httpsPort.isEmpty()) {
            httpsPort = "8443";

            InputStream mirthPropertiesStream = getClass().getResourceAsStream("/mirth.properties");
            if (mirthPropertiesStream != null) {
                Properties mirthProps = new Properties();

                try {
                    mirthProps.load(mirthPropertiesStream);
                    httpsPort = mirthProps.getProperty("https.port", httpsPort);
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        return new RedirectResolution("https://" + hostName + ":" + httpsPort + request.getContextPath() + Constants.INDEX_PAGE, false);
    }
}
