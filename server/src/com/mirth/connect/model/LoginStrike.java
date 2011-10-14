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

public class LoginStrike implements Serializable {

    private int lastStrikeCount;
    private Calendar lastStrikeTime;

    public LoginStrike(int lastStrikeCount, Calendar lastStrikeTime) {
        this.lastStrikeCount = lastStrikeCount;
        this.lastStrikeTime = lastStrikeTime;
    }

    public int getLastStrikeCount() {
        return lastStrikeCount;
    }

    public Calendar getLastStrikeTime() {
        return lastStrikeTime;
    }

}
