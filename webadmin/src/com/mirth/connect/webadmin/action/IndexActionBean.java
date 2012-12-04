/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import com.mirth.connect.webadmin.utils.Constants;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class IndexActionBean extends BaseActionBean {
    private boolean secureHttps;

    @DefaultHandler
    public Resolution init() {
        HttpServletRequest request = getContext().getRequest();

        // Set default ports in case mirth.properties fails to load
        String httpsPort = "8443";
        String httpPort = "8080";

        InputStream mirthPropertiesStream = null;
        mirthPropertiesStream = this.getClass().getResourceAsStream("/mirth.properties");

        if (mirthPropertiesStream != null) {
            Properties mirthProps = new Properties();
            try {
                mirthProps.load(mirthPropertiesStream);
                httpsPort = mirthProps.getProperty("https.port", "8443");
                httpPort = mirthProps.getProperty("http.port", "8080");
            } catch (IOException e) {
                // Ignore
            }
        }

        // Save the port values to the context
        getContext().setHttpsPort(httpsPort);
        getContext().setHttpPort(httpPort);

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
}
