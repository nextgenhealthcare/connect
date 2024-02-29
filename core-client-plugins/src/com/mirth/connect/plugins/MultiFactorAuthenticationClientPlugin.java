/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.Window;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.model.LoginStatus;

public interface MultiFactorAuthenticationClientPlugin {

    public LoginStatus authenticate(Window window, Client client, String username, LoginStatus primaryLoginStatus);
}
