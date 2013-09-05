/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.framemode;

import com.mirth.connect.model.transmission.TransmissionModeProperties;

public class FrameModeProperties extends TransmissionModeProperties {

    private String startOfMessageBytes;
    private String endOfMessageBytes;

    public FrameModeProperties(String pluginPointName) {
        super(pluginPointName);
        this.startOfMessageBytes = "";
        this.endOfMessageBytes = "";
    }

    public String getStartOfMessageBytes() {
        return startOfMessageBytes;
    }

    public void setStartOfMessageBytes(String startOfMessageBytes) {
        this.startOfMessageBytes = startOfMessageBytes;
    }

    public String getEndOfMessageBytes() {
        return endOfMessageBytes;
    }

    public void setEndOfMessageBytes(String endOfMessageBytes) {
        this.endOfMessageBytes = endOfMessageBytes;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;

        if (obj instanceof FrameModeProperties) {
            FrameModeProperties props = (FrameModeProperties) obj;
            equal = props.getStartOfMessageBytes().equals(startOfMessageBytes) && props.getEndOfMessageBytes().equals(endOfMessageBytes);
        }

        return equal && super.equals(obj);
    }
}
