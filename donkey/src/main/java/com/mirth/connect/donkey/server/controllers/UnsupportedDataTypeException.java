/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.controllers;

import java.io.IOException;

public class UnsupportedDataTypeException extends IOException {

    public UnsupportedDataTypeException() {}

    public UnsupportedDataTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedDataTypeException(String message) {
        super(message);
    }

    public UnsupportedDataTypeException(Throwable cause) {
        super(cause);
    }
}
