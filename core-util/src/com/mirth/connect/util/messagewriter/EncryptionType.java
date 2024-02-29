/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import net.lingala.zip4j.util.Zip4jConstants;

public enum EncryptionType {
    STANDARD("Standard", -1), AES128("AES-128", Zip4jConstants.AES_STRENGTH_128), AES256("AES-256",
            Zip4jConstants.AES_STRENGTH_256);

    private String name;
    private int keyStrength;

    private EncryptionType(String name, int keyStrength) {
        this.name = name;
        this.keyStrength = keyStrength;
    }

    public String getDisplayName() {
        return name;
    }

    public int getKeyStrength() {
        return keyStrength;
    }

    public static EncryptionType fromDisplayName(String displayName) {
        for (EncryptionType type : EncryptionType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }

        return null;
    }
}