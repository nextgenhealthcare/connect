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

public abstract class MultiFactorAuthenticationPlugin implements ServicePlugin {

    public abstract LoginStatus authenticate(String username, LoginStatus primaryStatus);

    public abstract LoginStatus authenticate(String loginData);
}
