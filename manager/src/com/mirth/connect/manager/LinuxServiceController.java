/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public class LinuxServiceController implements ServiceController {

    @Override
    public int checkService() {
        return 0;
    }

    @Override
    public boolean startService() {
        return false;
    }

    @Override
    public boolean stopService() {
        return false;
    }

    @Override
    public boolean isStartup() {
        return false;
    }

    @Override
    public boolean isStartupPossible() {
        return false;
    }

    @Override
    public void setStartup(boolean enabled) {
        // Not available
    }
    
    @Override
    public String getCommand() {
    	return "sh -c";
    }

}
