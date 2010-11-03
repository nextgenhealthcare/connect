/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Properties;

public class AuthorizationControllerFactory {
    private static AuthorizationController authorizationController = null;

    public static AuthorizationController getAuthorizationController() {
        if (authorizationController == null) {
            try {
                Properties serverProperties = PlatformUI.MIRTH_FRAME.mirthClient.getServerProperties();
                authorizationController = (AuthorizationController) Class.forName(serverProperties.getProperty("clientAuthorizationController")).newInstance();
            } catch (Exception e) {
                authorizationController = new DefaultAuthorizationController();
            }
        }

        return authorizationController;
    }
}
