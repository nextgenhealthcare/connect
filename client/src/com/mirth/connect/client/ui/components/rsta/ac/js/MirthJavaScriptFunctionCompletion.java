/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.JavaScriptHelper;
import org.fife.ui.autocomplete.CompletionProvider;

import com.mirth.connect.client.ui.components.rsta.ac.MirthFunctionCompletion;
import com.mirth.connect.client.ui.reference.ConstructorReference;
import com.mirth.connect.client.ui.reference.FunctionReference;

public class MirthJavaScriptFunctionCompletion extends MirthFunctionCompletion {

    private boolean constructor;

    public MirthJavaScriptFunctionCompletion(CompletionProvider provider, FunctionReference reference) {
        super(provider, reference);
        constructor = reference instanceof ConstructorReference;
        if (StringUtils.isNotBlank(reference.getSummary())) {
            setSummary(reference.getSummary());
        }
    }

    @Override
    public String getAlreadyEntered(JTextComponent comp) {
        String temp = getProvider().getAlreadyEnteredText(comp);
        int lastDot = JavaScriptHelper.findLastIndexOfJavaScriptIdentifier(temp);
        if (lastDot > -1) {
            temp = temp.substring(lastDot + 1);
        }

        if (constructor && temp.trim().equals("new")) {
            return "";
        }
        return temp;
    }
}