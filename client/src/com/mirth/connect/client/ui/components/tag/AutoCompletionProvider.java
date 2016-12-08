/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.DefaultCompletionProvider;

public class AutoCompletionProvider extends DefaultCompletionProvider {
    public AutoCompletionProvider() {
        super();
    }

    @Override
    public String getAlreadyEnteredText(JTextComponent component) {
        return component.getText();
    }

    public void updateUI() {
        updateUI();
    }
}