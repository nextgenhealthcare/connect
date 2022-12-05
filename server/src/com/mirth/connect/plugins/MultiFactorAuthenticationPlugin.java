/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 *
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.ExtendedLoginStatus;

/**
 * A plugin which implements multi-factor authentication.
 */
public abstract class MultiFactorAuthenticationPlugin implements ServicePlugin {

    /**
     * Called after primary authentication (e.g. username/password) has
     * completed successfully. The plug-in must decide whether the user
     * should be allowed to successfully login (by returning the
     * <code>primaryStatus</code>) or to challenge them with some
     * additional authentication step on the client side. To signal an
     * additional challenge, return {@link ExtendedLoginStatus} with
     * an appropriate <code>clientPluginClass</code> and
     * <code>message</code>.
     *
     * @param username The username of the user attempting to authenticate.
     * @param primaryStatus The result of the primary authentication process.
     *
     * @return Either the <code>primaryStatus</code> to proceed with login
     *         or an <code>ExtendedloginStatus</code> which specifies the
     *         client-side plug-in class to perform a second-factor
     *         authentication step.
     */
    public abstract LoginStatus authenticate(String username, LoginStatus primaryStatus);

    /**
     * Called when secondary authentication is being attempted.
     *
     * Note that this will be called without any context of the previous
     * (primary) authentication step carried out prior to
     * {@link #authenticate(String,LoginStatus)}. Note also that the user's
     * username and password are not available (directly).
     *
     * You may wish to arrange for the "message" response from the
     * {@link #authenticate(String,LoginStatus)} method include whatever
     * information this plug-in needs to maintain state between the two calls,
     * paying special attention to protecting against authentication forgery,
     * message interception, and message replays.
     *
     * @param loginData Data sent from the client in a special "login data"
     *                  header.
     *
     * @return The appropriate LoginStatus indicating whether the second-factor
     *         challenge has been successful or has failed.
     */
    public abstract LoginStatus authenticate(String loginData);
}
