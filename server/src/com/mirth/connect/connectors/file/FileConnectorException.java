/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

public class FileConnectorException extends Exception {

    public FileConnectorException(String message) {
        super(message);
    }

    public FileConnectorException(Throwable cause) {
        super(cause);
    }

    public FileConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
