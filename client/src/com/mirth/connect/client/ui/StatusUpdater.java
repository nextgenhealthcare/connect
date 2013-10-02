/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
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

    public StatusUpdater() {
        this.parent = PlatformUI.MIRTH_FRAME;
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            refreshRate = userPreferences.getInt("intervalTime", DEFAULT_INTERVAL_TIME) * 1000;
            
            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {
                return;
            }

            // Stop this thread if the current content page is neither dashboard or alert panel
            if (parent.currentContentPage != null) {
                if (parent.currentContentPage == parent.dashboardPanel) {
                    parent.doRefreshStatuses(false);
                } else if (parent.currentContentPage == parent.alertPanel){
                    parent.doRefreshAlerts(false);
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }
}
