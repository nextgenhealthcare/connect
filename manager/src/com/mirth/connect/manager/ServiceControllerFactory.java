/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public class ServiceControllerFactory {

    private static ServiceController serviceController;

    public static ServiceController getServiceController() throws Exception {
        synchronized (ServiceController.class) {
            if (serviceController == null) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.indexOf("win") >= 0) {
                    serviceController = new WindowsServiceController();
                } else if (os.indexOf("mac") >= 0) {
                    serviceController = new MacServiceController();
                } else if ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0)) {
                    serviceController = new LinuxServiceController();
                } else {
                    throw new Exception("Operating system must be Windows, Mac, or Unix/Linux.");
                }
            }

            return serviceController;
        }
    }

}
