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

import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateFunctionDefinition;
import com.mirth.connect.util.CodeTemplateUtil;

public class FunctionReference extends Reference {

    private String className;
    private CodeTemplateFunctionDefinition functionDefinition;
    private List<String> beforeDotTextList;

    public FunctionReference(String category, CodeTemplate codeTemplate) {
        this(codeTemplate.getContextSet(), category, null, codeTemplate.getName(), codeTemplate.getDescription(), CodeTemplateUtil.stripDocumentation(codeTemplate.getCode()), codeTemplate.getFunctionDefinition());
    }

    public FunctionReference(CodeTemplateContextSet contextSet, String category, String className, String name, String description, String replacementCode, CodeTemplateFunctionDefinition functionDefinition) {
        this(contextSet, category, className, name, description, replacementCode, functionDefinition, null);
    }

    public FunctionReference(CodeTemplateContextSet contextSet, String category, String className, String name, String description, String replacementCode, CodeTemplateFunctionDefinition functionDefinition, List<String> beforeDotTextList) {
        super(Type.FUNCTION, contextSet, category, name, description, replacementCode);
        this.className = className;
        this.functionDefinition = functionDefinition;
        this.beforeDotTextList = beforeDotTextList;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public CodeTemplateFunctionDefinition getFunctionDefinition() {
        return functionDefinition;
    }

    public void setFunctionDefinition(CodeTemplateFunctionDefinition functionDefinition) {
        this.functionDefinition = functionDefinition;
    }

    public List<String> getBeforeDotTextList() {
        return beforeDotTextList;
    }

    public void setBeforeDotTextList(List<String> beforeDotTextList) {
        this.beforeDotTextList = beforeDotTextList;
    }
}