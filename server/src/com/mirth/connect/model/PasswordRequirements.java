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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("passwordRequirements")
public class PasswordRequirements implements Serializable {

    private static final long serialVersionUID = 1L;

    private int minUpper;
    private int minLower;
    private int minNumeric;
    private int minSpecial;
    private int minLength;

    public PasswordRequirements() {
        this.minUpper = 0;
        this.minLower = 0;
        this.minNumeric = 0;
        this.minSpecial = 0;

        this.minLength = 0;
    }

    public PasswordRequirements(int minUpper, int minLower, int minNumeric, int minSpecial, int minLength) {
        this.minUpper = minUpper;
        this.minLower = minLower;
        this.minNumeric = minNumeric;
        this.minSpecial = minSpecial;
        this.minLength = minLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMinLower() {
        return minLower;
    }

    public void setMinLower(int minLower) {
        this.minLower = minLower;
    }

    public int getMinNumeric() {
        return minNumeric;
    }

    public void setMinNumeric(int minNumeric) {
        this.minNumeric = minNumeric;
    }

    public int getMinSpecial() {
        return minSpecial;
    }

    public void setMinSpecial(int minSpecial) {
        this.minSpecial = minSpecial;
    }

    public int getMinUpper() {
        return minUpper;
    }

    public void setMinUpper(int minUpper) {
        this.minUpper = minUpper;
    }
}
