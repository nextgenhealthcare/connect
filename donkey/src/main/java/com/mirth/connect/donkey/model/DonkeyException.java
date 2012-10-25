/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model;

public class DonkeyException extends Exception {
    private String formattedError;

    public DonkeyException(Throwable cause) {
        super(cause);
    }

    public DonkeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DonkeyException(String message) {
        super(message);
    }

    public DonkeyException(Throwable cause, String formattedError) {
        super(cause);
        this.formattedError = formattedError;
    }

    public DonkeyException(String message, Throwable cause, String formattedError) {
        super(message, cause);
        this.formattedError = formattedError;
    }

    public DonkeyException(String message, String formattedError) {
        super(message);
        this.formattedError = formattedError;
    }

    public String getFormattedError() {
        return formattedError;
    }
}
