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
import java.util.Calendar;

public class Credentials implements Serializable{
    private String password;
    private Calendar passwordDate;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Calendar getPasswordDate() {
        return passwordDate;
    }

    public void setPasswordDate(Calendar passwordDate) {
        this.passwordDate = passwordDate;
    }
}