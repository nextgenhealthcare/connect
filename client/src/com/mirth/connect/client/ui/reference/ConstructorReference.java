/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

public class ConstructorReference extends FunctionReference {

    private String className;

    public ConstructorReference(int scope, String category, String name, String className, String description, String replacementCode, String functionName) {
        this(scope, category, name, className, description, replacementCode, functionName, null);
    }

    public ConstructorReference(int scope, String category, String name, String className, String description, String replacementCode, String functionName, Parameters parameters) {
        this(scope, category, name, className, description, replacementCode, functionName, parameters, null, null);
    }

    public ConstructorReference(int scope, String category, String name, String className, String description, String replacementCode, String functionName, Parameters parameters, String returnType, String returnDescription) {
        super(scope, category, name, name, description, replacementCode, functionName, parameters, returnType, returnDescription);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}