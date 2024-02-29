/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

public class MessageWriterException extends Exception {
    public MessageWriterException(String message) {
        super(message);
    }

    public MessageWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageWriterException(Throwable cause) {
        super(cause);
    }
}
