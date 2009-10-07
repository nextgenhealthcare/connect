/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui;

import java.util.prefs.Preferences;

/**
 * The status updater class has a thread that updates the status panel every
 * specified interval if the status panel is being viewed.
 */
public class StatusUpdater implements Runnable
{
    private final int DEFAULT_INTERVAL_TIME = 20;
    private static Preferences userPreferences;
    Frame parent;
    int refreshRate;
    boolean interrupted;

    public StatusUpdater()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        interrupted = false;
    }

    public void run()
    {
        try
        {
            while (!interrupted)
            {
                while (parent.isRefreshingStatuses()) {
                	Thread.sleep(100);
                }
            	
                refreshRate = userPreferences.getInt("intervalTime", DEFAULT_INTERVAL_TIME) * 1000;
                Thread.sleep(refreshRate);

                if (interrupted)
                    return;

                if (parent.currentContentPage != null && parent.currentContentPage == parent.dashboardPanel)
                {
                    parent.doRefreshStatuses();
                }
            }
        }
        catch (InterruptedException e)
        {
            // should happen when closed.
        }
    }

    public void interruptThread()
    {
        interrupted = true;
    }
}
