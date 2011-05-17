/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

public class AuthorizationControllerFactory {
    private static AuthorizationController authorizationController = null;

    public static AuthorizationController getAuthorizationController() {
        if (authorizationController == null) {

            if (PlatformUI.MIRTH_FRAME.getPluginMetaData().containsKey("User Authorization")) {
                try {
                    authorizationController = (AuthorizationController) Class.forName("com.mirth.connect.plugins.auth.client.SecureAuthorizationController").newInstance();
                } catch (Exception e) {
                    authorizationController = new DefaultAuthorizationController();
                }
            } else {
                authorizationController = new DefaultAuthorizationController();
            }
        }

        return authorizationController;
    }
}
