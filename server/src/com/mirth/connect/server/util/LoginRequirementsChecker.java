/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.LoginStrike;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;

public class LoginRequirementsChecker {

    private static Logger logger = LogManager.getLogger(LoginRequirementsChecker.class);

    private User user;
    private PasswordRequirements passwordRequirements;
    private UserController userController;

    public LoginRequirementsChecker(User user) {
        this(user, ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements(), ControllerFactory.getFactory().createUserController());
    }

    public LoginRequirementsChecker(User user, PasswordRequirements passwordRequirements, UserController userController) {
        this.user = user;
        this.passwordRequirements = passwordRequirements;
        this.userController = userController;
    }

    // --- Login Strikes --- //

    public int getStrikeCount() {
        return user.getStrikeCount() != null ? user.getStrikeCount() : 0;
    }

    public void incrementStrikes() {
        try {
            updateStrikeInfo(userController.incrementStrikes(user.getId()));
        } catch (ControllerException e) {
            logger.error("Unable to increment login strikes for user: " + user.toAuditString(), e);
        }
    }

    public void resetStrikes() {
        try {
            updateStrikeInfo(userController.resetStrikes(user.getId()));
        } catch (ControllerException e) {
            logger.error("Unable to increment login strikes for user: " + user.toAuditString(), e);
        }
    }

    private void updateStrikeInfo(LoginStrike strikeInfo) {
        if (strikeInfo != null) {
            user.setStrikeCount(strikeInfo.getLastStrikeCount());
            user.setLastStrikeTime(strikeInfo.getLastStrikeTime());
        }
    }

    public void resetExpiredStrikes() {
        if ((getStrikeCount() > 0) && (getStrikeTimeRemaining() <= 0)) {
            resetStrikes();
        }
    }

    public boolean isUserLockedOut() {
        if (!isLockoutEnabled()) {
            return false;
        }

        return ((getAttemptsRemaining() <= 0) && (getStrikeTimeRemaining() > 0));
    }

    public boolean isLockoutEnabled() {
        return (passwordRequirements.getRetryLimit() > 0);
    }

    public int getAttemptsRemaining() {
        int retryLimit = passwordRequirements.getRetryLimit();

        return (retryLimit + 1 - getStrikeCount());
    }

    public long getStrikeTimeRemaining() {
        Duration lockoutPeriod = Duration.standardHours(passwordRequirements.getLockoutPeriod());

        long lastStrikeTime = user.getLastStrikeTime() != null ? user.getLastStrikeTime().getTimeInMillis() : 0;
        Duration strikeDuration = new Duration(lastStrikeTime, System.currentTimeMillis());
        return lockoutPeriod.minus(strikeDuration).getMillis();
    }

    public String getPrintableStrikeTimeRemaining() {
        Period period = new Period(getStrikeTimeRemaining());

        PeriodFormatter periodFormatter;
        if (period.toStandardMinutes().getMinutes() > 0) {
            periodFormatter = new PeriodFormatterBuilder().printZeroNever().appendHours().appendSuffix(" hour", " hours").appendSeparator(" and ").printZeroAlways().appendMinutes().appendSuffix(" minute", " minutes").toFormatter();
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
        Period period = new Period(graceTimeRemaining);

        PeriodFormatter periodFormatter;
        if (period.toStandardHours().getHours() > 0) {
            periodFormatter = new PeriodFormatterBuilder().printZeroRarelyFirst().appendDays().appendSuffix(" day", " days").appendSeparator(" and ").printZeroAlways().appendHours().appendSuffix(" hour", " hours").toFormatter();
        } else {
            periodFormatter = new PeriodFormatterBuilder().printZeroNever().appendMinutes().appendSuffix(" minute", " minutes").appendSeparator(" and ").printZeroAlways().appendSeconds().appendSuffix(" second", " seconds").toFormatter();
        }

        return periodFormatter.print(period);
    }

    private Duration getDurationRemainingFromDays(long passwordTime, long currentTime, int durationDays) {
        Duration expirationDuration = Duration.standardDays(durationDays);
        Duration passwordDuration = new Duration(passwordTime, currentTime);

        return expirationDuration.minus(passwordDuration);
    }

}
