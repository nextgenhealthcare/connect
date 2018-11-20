/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.LicenseInfo;

public class LicenseClient {

    private static Timer timer;

    public static void start() {
        stop();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                check();
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 24L * 60L * 60L * 1000L);
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private static void check() {
        try {
            LicenseInfo licenseInfo = PlatformUI.MIRTH_FRAME.mirthClient.getLicenseInfo();

            if (licenseInfo.getExpirationDate() != null && licenseInfo.getExpirationDate() > 0) {
                final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
                final ZonedDateTime expiration = ZonedDateTime.ofInstant(Instant.ofEpochMilli(licenseInfo.getExpirationDate()), ZoneId.systemDefault());

                Long warningPeriod = licenseInfo.getWarningPeriod();
                if (warningPeriod == null) {
                    warningPeriod = 7L * 24L * 60L * 60L * 1000L;
                }

                Long gracePeriod = licenseInfo.getGracePeriod();
                if (gracePeriod == null) {
                    gracePeriod = 7L * 24L * 60L * 60L * 1000L;
                }

                ZonedDateTime warningStart = expiration.minus(Duration.ofMillis(warningPeriod));
                ZonedDateTime graceEnd = expiration.plus(Duration.ofMillis(gracePeriod));

                if (now.isAfter(expiration) || now.isAfter(warningStart)) {
                    StringBuilder builder = new StringBuilder("<html>Your NextGen Connect license for the extensions<br/>[").append(StringUtils.join(licenseInfo.getExtensions(), ", ")).append("]<br/>");
                    Temporal endDate;

                    if (now.isAfter(expiration)) {
                        endDate = graceEnd;
                        builder.append(" has expired and you are now in a grace period.<br/>Extension functionality will cease in ");
                    } else {
                        endDate = expiration;
                        builder.append(" will expire in ");
                    }

                    int days = (int) Math.ceil((double) Duration.between(now, endDate).getSeconds() / 60 / 60 / 24);
                    builder.append(days).append(" day").append(days == 1 ? "" : "s").append(".<br/>Please contact your account manager or connectsales@nextgen.com to renew your commercial license.</html>");
                    final String message = builder.toString();

                    SwingUtilities.invokeLater(() -> {
                        if (now.isAfter(expiration)) {
                            PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, message);
                        } else {
                            PlatformUI.MIRTH_FRAME.alertWarning(PlatformUI.MIRTH_FRAME, message);
                        }
                    });
                }
            }
        } catch (ClientException e) {
            // Ignore
        }
    }
}
