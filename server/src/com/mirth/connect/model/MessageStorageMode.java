/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

public enum MessageStorageMode {
    DEVELOPMENT(5), PRODUCTION(4), RAW(3), METADATA(2), DISABLED(1);
    
    private int value;

    private MessageStorageMode(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static MessageStorageMode fromInt(int value) {
        switch (value) {
            case 1: return DISABLED;
            case 2: return METADATA;
            case 3: return RAW;
            case 4: return PRODUCTION;
            case 5: return DEVELOPMENT;
        }

        return null;
    }
}
