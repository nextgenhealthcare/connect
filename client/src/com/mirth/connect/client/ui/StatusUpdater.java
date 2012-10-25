/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.prefs.Preferences;

/**
 * The status updater class has a thread that updates the status panel every
 * specified interval if the status panel is being viewed.
 */
public class StatusUpdater implements Runnable {

    private final int DEFAULT_INTERVAL_TIME = 20;
    private static Preferences userPreferences;
    Frame parent;
    int refreshRate;
    boolean interrupted;

    public StatusUpdater() {
        this.parent = PlatformUI.MIRTH_FRAME;
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        interrupted = false;
    }

    public void run() {
        try {
            while (!interrupted) {
                while (parent.isRefreshingStatuses()) {
                    Thread.sleep(100);
                }

                refreshRate = userPreferences.getInt("intervalTime", DEFAULT_INTERVAL_TIME) * 1000;
                Thread.sleep(refreshRate);

                if (interrupted) {
                    return;
                }

                if (parent.currentContentPage != null && parent.currentContentPage == parent.dashboardPanel) {
                    parent.doRefreshStatuses(false);
                }
            }
        } catch (InterruptedException e) {
            // should happen when closed.
        }
    }

    public void interruptThread() {
        interrupted = true;
    }
}
