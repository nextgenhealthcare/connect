/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import com.mirth.connect.model.transmission.StreamHandlerException;

public class MLLPv2StreamHandlerException extends StreamHandlerException {

    public MLLPv2StreamHandlerException() {
        super();
    }

    public MLLPv2StreamHandlerException(String message) {
        super(message);
    }

    public MLLPv2StreamHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MLLPv2StreamHandlerException(Throwable cause) {
        super(cause);
    }
}
