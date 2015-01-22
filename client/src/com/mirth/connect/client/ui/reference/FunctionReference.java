/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import java.util.List;

public class FunctionReference extends Reference {

    private String className;
    private String functionName;
    private Parameters parameters;
    private String returnType;
    private String returnDescription;
    private List<String> beforeDotTextList;

    public FunctionReference(int scope, String category, String className, String name, String description, String replacementCode, String functionName) {
        this(scope, category, className, name, description, replacementCode, functionName, null);
    }

    public FunctionReference(int scope, String category, String className, String name, String description, String replacementCode, String functionName, Parameters parameters) {
        this(scope, category, className, name, description, replacementCode, functionName, parameters, null, null);
    }

    public FunctionReference(int scope, String category, String className, String name, String description, String replacementCode, String functionName, Parameters parameters, String returnType, String returnDescription) {
        this(scope, category, className, name, description, replacementCode, functionName, parameters, returnType, returnDescription, null);
    }

    public FunctionReference(int scope, String category, String className, String name, String description, String replacementCode, String functionName, Parameters parameters, String returnType, String returnDescription, List<String> beforeDotTextList) {
        super(Type.FUNCTION, scope, category, name, description, replacementCode);
        this.className = className;
        this.functionName = functionName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.returnDescription = returnDescription;
        this.beforeDotTextList = beforeDotTextList;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public void setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
    }

    public List<String> getBeforeDotTextList() {
        return beforeDotTextList;
    }

    public void setBeforeDotTextList(List<String> beforeDotTextList) {
        this.beforeDotTextList = beforeDotTextList;
    }
}