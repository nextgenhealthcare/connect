/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

public class ExtensionOperation extends Operation {

    private String extensionName;

    public ExtensionOperation(String extensionName, Operation operation) {
        this(extensionName, operation.getName(), operation.getDisplayName(), operation.getExecuteType(), operation.isAuditable());
    }

    public ExtensionOperation(String extensionName, String name, String displayName, ExecuteType executeType, boolean auditable) {
        super(name, displayName, executeType, auditable);
        this.extensionName = extensionName;
    }

    @Override
    public String getName() {
        return extensionName + '#' + super.getName();
    }

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }
}