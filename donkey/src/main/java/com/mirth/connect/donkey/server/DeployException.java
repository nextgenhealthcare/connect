/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

public class DeployException extends Exception {
    public DeployException(Throwable cause) {
        super(cause);
    }
    
    public DeployException(String message) {
        super(message);
    }

    public DeployException(String message, Throwable cause) {
        super(message, cause);
    }
}
