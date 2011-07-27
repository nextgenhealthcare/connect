/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.mirth.connect.model.LoginStrike;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.server.controllers.ControllerFactory;

public class LoginRequirementsChecker {

    private String username;
    private PasswordRequirements passwordRequirements;
    private static Map<String, LoginStrike> userLoginStrikes = new ConcurrentHashMap<String, LoginStrike>();

    public LoginRequirementsChecker(String username) {
        this.username = username;
        this.passwordRequirements = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
    }

    // --- Login Strikes --- //
    
    public int getStrikeCount() {
        synchronized (userLoginStrikes) {
            return (userLoginStrikes.get(username) == null) ? 0 : userLoginStrikes.get(username).getLastStrikeCount();
        }
    }

    public void incrementStrikes() {
        synchronized (userLoginStrikes) {
            userLoginStrikes.put(username, new LoginStrike((getStrikeCount() + 1), Calendar.getInstance()));
        }
    }

    public void resetStrikes() {
        synchronized (userLoginStrikes) {
            userLoginStrikes.remove(username);
        }
    }

    public void resetExpiredStrikes() {
        synchronized (userLoginStrikes) {
            if ((getStrikeCount() > 0) && (getStrikeTimeRemaining() <= 0)) {
                resetStrikes();
            }
        }
    }

    public boolean isUserLockedOut() {
        if (!isLockoutEnabled()) {
            return false;
        }

        synchronized (userLoginStrikes) {
            return ((getStrikesRemaining() <= 0) && (getStrikeTimeRemaining() > 0));
        }
    }

    public boolean isLockoutEnabled() {
        return (passwordRequirements.getRetryLimit() > 0);
    }

    public int getStrikesRemaining() {
        int retryLimit = passwordRequirements.getRetryLimit();

        synchronized (userLoginStrikes) {
            return (retryLimit - getStrikeCount());
        }
    }

    public long getStrikeTimeRemaining() {
        Duration lockoutPeriod = Duration.standardHours(passwordRequirements.getLockoutPeriod());

        synchronized (userLoginStrikes) {
            Duration strikeDuration = new Duration(userLoginStrikes.get(username).getLastStrikeTime().getTimeInMillis(), System.currentTimeMillis());
            return lockoutPeriod.minus(strikeDuration).getMillis();
        }
    }

    public String getPrintableStrikeTimeRemaining() {
        Period period;
        synchronized (userLoginStrikes) {
            period = new Period(getStrikeTimeRemaining());
        }

        PeriodFormatter periodFormatter;
        if (period.toStandardMinutes().getMinutes() > 0) {
            periodFormatter = new PeriodFormatterBuilder().printZeroNever().appendHours().appendSuffix(" hour", " hours").appendSeparator(" and ").appendMinutes().appendSuffix(" minute", " minutes").toFormatter();
        } else {
            periodFormatter = new PeriodFormatterBuilder().printZeroAlways().appendSeconds().appendSuffix(" second", " seconds").toFormatter();
        }

        return periodFormatter.print(period);
    }
    
    public String getPrintableLockoutPeriod() {
        return PeriodFormat.getDefault().print(Period.hours(passwordRequirements.getLockoutPeriod()));
    }
    
    // --- Login Expiration --- //
    
    public boolean isPasswordExpired(long passwordTime, long currentTime) {
        return (getDurationRemainingFromDays(passwordTime, currentTime, passwordRequirements.getExpiration()).getMillis() < 0);
    }
    
    public long getGraceTimeRemaining(long gracePeriodStartTime, long currentTime) {
        return getDurationRemainingFromDays(gracePeriodStartTime, currentTime, passwordRequirements.getGracePeriod()).getMillis();
    }
    
    public String getPrintableGraceTimeRemaining(long graceTimeRemaining) {
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder().printZeroRarelyFirst().appendDays().appendSuffix(" day", " days").appendSeparator(" and ").appendHours().appendSuffix(" hour", " hours").toFormatter();
        return periodFormatter.print(new Period(graceTimeRemaining));
    }
    
    private Duration getDurationRemainingFromDays(long passwordTime, long currentTime, int durationDays) {
        Duration expirationDuration = Duration.standardDays(durationDays);
        Duration passwordDuration = new Duration(passwordTime, currentTime);
        
        return expirationDuration.minus(passwordDuration);
    }

}
