/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public interface ServiceController {
    
    public int checkService();
    public boolean startService();
    public boolean stopService();
    public boolean isStartupPossible();
    public void setStartup(boolean enabled);
    public boolean isStartup();
    public String getCommand();
    public boolean isShowTrayIcon();
    public boolean isShowServiceTab();
    public void migrate();
}
