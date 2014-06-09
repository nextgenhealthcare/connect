/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.List;

public class GetOperationsResult {

    private List<String> loadedMethods;
    private String serviceName;
    private String portName;

    public GetOperationsResult(List<String> loadedMethods, String serviceName, String portName) {
        this.loadedMethods = loadedMethods;
        this.serviceName = serviceName;
        this.portName = portName;
    }

    public List<String> getLoadedMethods() {
        return loadedMethods;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }
}