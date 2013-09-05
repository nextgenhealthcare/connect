/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import org.apache.commons.lang3.SystemUtils;

public class ServiceControllerFactory {

    private static ServiceController serviceController;

    public static ServiceController getServiceController() throws Exception {
        synchronized (ServiceController.class) {
            if (serviceController == null) {
                if (SystemUtils.IS_OS_WINDOWS) {
                    serviceController = new WindowsServiceController();
                } else if (SystemUtils.IS_OS_MAC) {
                    serviceController = new MacServiceController();
                } else if (SystemUtils.IS_OS_UNIX) {
                    serviceController = new LinuxServiceController();
                } else {
                    throw new Exception("Operating system must be Windows, Mac, or Unix/Linux.");
                }
            }

            return serviceController;
        }
    }

}
