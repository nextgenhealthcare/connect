/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import com.mirth.connect.donkey.model.DonkeyException;

public class SerializerException extends DonkeyException {
    private static final long serialVersionUID = 1L;

    public SerializerException(Throwable cause) {
        super(cause);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(Throwable cause, String formattedError) {
        super(cause, formattedError);
    }

    public SerializerException(String message, Throwable cause, String formattedError) {
        super(message, cause, formattedError);
    }

    public SerializerException(String message, String formattedError) {
        super(message, formattedError);
    }
}
