/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("passwordRequirements")
public class PasswordRequirements implements Serializable {

    private static final long serialVersionUID = 1L;

    private int minLength;
    private int minUpper;
    private int minLower;
    private int minNumeric;
    private int minSpecial;
    private int retryLimit;
    private int lockoutPeriod;
    private int expiration;
    private int gracePeriod;
    private int reusePeriod;
    private int reuseLimit;

    public PasswordRequirements() {
        this.minLength = 0;
        this.minUpper = 0;
        this.minLower = 0;
        this.minNumeric = 0;
        this.minSpecial = 0;
        this.retryLimit = 0;
        this.lockoutPeriod = 0;
        this.expiration = 0;
        this.gracePeriod = 0;
        this.reusePeriod = 0;
        this.reuseLimit = 0;
    }

    public PasswordRequirements(int minLength, int minUpper, int minLower, int minNumeric, int minSpecial, int retryLimit, int lockoutPeriod, int expiration, int gracePeriod, int reusePeriod, int reuseLimit) {
        this.minLength = minLength;
        this.minUpper = minUpper;
        this.minLower = minLower;
        this.minNumeric = minNumeric;
        this.minSpecial = minSpecial;
        this.retryLimit = retryLimit;
        this.lockoutPeriod = lockoutPeriod;
        this.expiration = expiration;
        this.gracePeriod = gracePeriod;
        this.reusePeriod = reusePeriod;
        this.reuseLimit = reuseLimit;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMinUpper() {
        return minUpper;
    }

    public void setMinUpper(int minUpper) {
        this.minUpper = minUpper;
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

    public int getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(int retryLimit) {
        this.retryLimit = retryLimit;
    }

    public int getLockoutPeriod() {
        return lockoutPeriod;
    }

    public void setLockoutPeriod(int lockoutPeriod) {
        this.lockoutPeriod = lockoutPeriod;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public int getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(int gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public int getReusePeriod() {
        return reusePeriod;
    }

    public void setReusePeriod(int reusePeriod) {
        this.reusePeriod = reusePeriod;
    }

    public int getReuseLimit() {
        return reuseLimit;
    }

    public void setReuseLimit(int reuseLimit) {
        this.reuseLimit = reuseLimit;
    }
}
