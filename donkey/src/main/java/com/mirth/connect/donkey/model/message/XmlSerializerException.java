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

public class XmlSerializerException extends DonkeyException {
    private static final long serialVersionUID = 1L;

    public XmlSerializerException(Throwable cause) {
        super(cause);
    }

    public XmlSerializerException(String message) {
        super(message);
    }

    public XmlSerializerException(String message, Throwable cause, String formattedError) {
        super(message, cause, formattedError);
    }
}
