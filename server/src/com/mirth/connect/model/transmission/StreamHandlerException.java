/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission;

import java.io.IOException;

public class StreamHandlerException extends IOException {

    public StreamHandlerException() {
        super();
    }

    public StreamHandlerException(String message) {
        super(message);
    }

    public StreamHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamHandlerException(Throwable cause) {
        super(cause);
    }
}
