/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import org.apache.commons.lang.StringUtils;

public class ConnectorTypeData {

    private String transportName;
    private ConnectorTypeDecoration decoration;

    public ConnectorTypeData(String transportName) {
        this(transportName, null);
    }

    public ConnectorTypeData(String transportName, ConnectorTypeDecoration decoration) {
        this.transportName = transportName;
        this.decoration = decoration;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public ConnectorTypeDecoration getDecoration() {
        return decoration;
    }

    public void setDecoration(ConnectorTypeDecoration decoration) {
        this.decoration = decoration;
    }

    @Override
    public String toString() {
        if (decoration != null) {
            return (transportName + " " + StringUtils.defaultString(decoration.getSuffix())).trim();
        } else {
            return transportName;
        }
    }
}