/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateFunctionDefinition;

public class ConstructorReference extends FunctionReference {

    private String className;

    public ConstructorReference(CodeTemplateContextSet contextSet, String category, String className, String name, String description, String replacementCode, CodeTemplateFunctionDefinition functionDefinition) {
        super(contextSet, category, name, name, description, replacementCode, functionDefinition);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}