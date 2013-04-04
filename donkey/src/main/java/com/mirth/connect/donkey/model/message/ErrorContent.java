/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public class ErrorContent {
    private String error = null;
    private transient boolean persisted = false;

    public ErrorContent() {

    }
    
    public ErrorContent(String error, boolean persisted) {
        this.error = error;
        this.persisted = persisted;
    }

    public String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}
