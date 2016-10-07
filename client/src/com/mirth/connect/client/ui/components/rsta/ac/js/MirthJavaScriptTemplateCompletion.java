/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import org.fife.rsta.ac.js.completion.JavaScriptTemplateCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletion;
import com.mirth.connect.client.ui.reference.ParameterizedCodeReference;
import com.mirth.connect.client.ui.reference.Reference;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;

public class MirthJavaScriptTemplateCompletion extends JavaScriptTemplateCompletion implements MirthCompletion {

    protected String id;
    protected CodeTemplateContextSet contextSet;

    public MirthJavaScriptTemplateCompletion(CompletionProvider provider, ParameterizedCodeReference reference) {
        super(provider, reference.getName(), reference.getDefinitionString(), reference.getTemplate(), reference.getDescription(), reference.getSummary());
        id = reference.getId();
        contextSet = reference.getContextSet();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CodeTemplateContextSet getContextSet() {
        return contextSet;
    }

    @Override
    public int getRelevance() {
        return 50;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MirthCompletion) {
            return ((MirthCompletion) obj).getId().equals(id);
        } else if (obj instanceof Reference) {
            return ((Reference) obj).getId().equals(id);
        }
        return false;
    }
}