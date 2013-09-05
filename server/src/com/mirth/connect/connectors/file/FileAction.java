/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

public enum FileAction {
    NONE("None"), MOVE("Move"), DELETE("Delete"), AFTER_PROCESSING(
            "After Processing Action");

    private String value;

    private FileAction(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}