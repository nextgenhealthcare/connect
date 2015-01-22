/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import org.fife.ui.autocomplete.CompletionProvider;

import com.mirth.connect.client.ui.reference.CodeReference;

public class MirthJavaScriptCodeCompletion extends MirthJavaScriptBasicCompletion {

    public MirthJavaScriptCodeCompletion(CompletionProvider provider, CodeReference reference) {
        super(provider, reference);
    }

    @Override
    public String getSummary() {
        if (summary != null) {
            return "<html><body><b><h4>" + getInputText() + "</h4></b><br/>" + summary + "<br/><hr/><br/><code>" + getReplacementText() + "</code></body></html>";
        }
        return super.getSummary();
    }

    @Override
    public int getRelevance() {
        return 50;
    }
}