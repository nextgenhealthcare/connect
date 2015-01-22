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

import com.mirth.connect.client.ui.reference.ClassReference;

public class MirthJavaScriptClassCompletion extends MirthJavaScriptBasicCompletion {

    public MirthJavaScriptClassCompletion(CompletionProvider provider, ClassReference reference) {
        super(provider, reference);
    }

    public MirthJavaScriptClassCompletion(CompletionProvider provider, ClassReference reference, String alias) {
        super(provider, reference);
    }

    @Override
    public int getRelevance() {
        return 150;
    }
}