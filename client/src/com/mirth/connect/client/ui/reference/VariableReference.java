/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import org.fife.rsta.ac.js.IconFactory;

import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;

public class VariableReference extends Reference {

    public VariableReference(CodeTemplateContextSet contextSet, String category, String name, String description, String replacementCode) {
        super(Type.VARIABLE, contextSet, category, name, description, replacementCode);
        setIconName(IconFactory.GLOBAL_VARIABLE_ICON);
    }
}