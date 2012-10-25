/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

public class LoginStatus implements Serializable {

    public enum Status {
        SUCCESS, SUCCESS_GRACE_PERIOD, FAIL, FAIL_EXPIRED, FAIL_LOCKED_OUT, FAIL_VERSION_MISMATCH
    }

    private Status status;
    private String message;

    public LoginStatus(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
