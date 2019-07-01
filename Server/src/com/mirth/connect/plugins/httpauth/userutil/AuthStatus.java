/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.userutil;

/**
 * Denotes the result of an HTTP authentication attempt. Available statuses:
 * 
 * CHALLENGED, SUCCESS, FAILURE
 */
public enum AuthStatus {
    /**
     * Indicates that the request should be rejected and an authentication challenge has been sent.
     */
    CHALLENGED,

    /**
     * Indicates that the request should be accepted.
     */
    SUCCESS,

    /**
     * Indicates that the request should be rejected without an authentication challenge.
     */
    FAILURE;
}