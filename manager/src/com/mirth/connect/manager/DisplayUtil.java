/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.Dialog;
import java.awt.Frame;

import org.apache.commons.lang3.math.NumberUtils;

public class DisplayUtil {

    /**
     * Sets a dialog's resizable property. When JDK 11 or greater is used, the property is forced to
     * be true due to https://bugs.openjdk.java.net/browse/JDK-8208743
     */
    public static void setResizable(Dialog dialog, boolean resizable) {
        if (isJDK11OrGreater()) {
            resizable = true;
        }
        dialog.setResizable(resizable);
    }

    /**
     * Sets a frame's resizable property. When JDK 11 or greater is used, the property is forced to
     * be true due to https://bugs.openjdk.java.net/browse/JDK-8208743
     */
    public static void setResizable(Frame frame, boolean resizable) {
        if (isJDK11OrGreater()) {
            resizable = true;
        }
        frame.setResizable(resizable);
    }

    public static boolean isJDK11OrGreater() {
        String version = System.getProperty("java.version");

        int index = version.indexOf('-');
        if (index > 0) {
            version = version.substring(0, index);
        }

        index = version.indexOf('.');
        if (index > 0) {
            version = version.substring(0, index);
        }

        return NumberUtils.toDouble(version) >= 11;
    }
}
