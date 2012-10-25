/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class ServerConnectionFactory {
    public static ServerConnection createServerConnection(String address) {
        return new ServerConnection(address);
    }

    public static ServerConnection createServerConnection(String address, int timeout) {
        return new ServerConnection(address, timeout);
    }
}
