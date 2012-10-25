/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

public class ExtensionPermission implements Serializable {
    private String extensionName;
    private String displayName;
    private String description;
    private String[] operationNames;
    private String[] taskNames;

    public ExtensionPermission(String extensionName, String displayName, String description, String[] operationNames, String[] taskNames) {
        this.extensionName = extensionName;
        this.displayName = displayName;
        this.description = description;
        this.operationNames = operationNames;
        this.taskNames = taskNames;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getOperationNames() {
        return operationNames;
    }

    public void setOperationNames(String[] operationNames) {
        this.operationNames = operationNames;
    }

    public String[] getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(String[] taskNames) {
        this.taskNames = taskNames;
    }
}
