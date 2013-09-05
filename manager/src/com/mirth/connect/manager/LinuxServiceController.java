/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public class LinuxServiceController implements ServiceController {

    private final String LINUX_SERVICE_NAME = "mcservice";
    private final String LINUX_SERVICE_CMD = "/etc/init.d/";
    private final String LINUX_SERVICE_START = "start";
    private final String LINUX_SERVICE_STOP = "stop";
    private final String LINUX_SERVICE_STATUS = "status";

    @Override
    public int checkService() {
        try {
            String[] input = new String[]{LINUX_SERVICE_CMD + LINUX_SERVICE_NAME + " " + LINUX_SERVICE_STATUS};
            String output = CmdUtil.execCmdWithOutput(input);
            System.out.println(output);
            if (output.indexOf("running") != -1) {
                return 1;
            } else if (output.indexOf("stopped") != -1) {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public boolean startService() {
        try {
            if (CmdUtil.execCmd(new String[]{LINUX_SERVICE_CMD + LINUX_SERVICE_NAME + " " + LINUX_SERVICE_START}, true) == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean stopService() {
        try {
            if (CmdUtil.execCmd(new String[]{LINUX_SERVICE_CMD + LINUX_SERVICE_NAME + " " + LINUX_SERVICE_STOP}, true) == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    @Override
    public boolean isShowTrayIcon() {
        return true;
    }

    @Override
    public boolean isShowServiceTab() {
        return true;
    }

    @Override
    public void migrate() {
    }

}
