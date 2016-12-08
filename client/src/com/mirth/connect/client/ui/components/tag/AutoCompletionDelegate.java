/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.tag;

import javax.swing.text.Document;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

public class AutoCompletionDelegate extends AutoCompletion {
    private AutoCompletionPopupWindow autoCompleteWindow;

    public AutoCompletionDelegate(CompletionProvider provider, AutoCompletionPopupWindow autoCompleteWindow) {
        super(provider);
        this.autoCompleteWindow = autoCompleteWindow;
        setHideOnNoText(false);
    }

    public void updateUI() {
        popupWindow.revalidate();
    }

    public void moveUp() {
        popupWindow.selectPreviousItem();
    }

    public void moveDown() {
        popupWindow.selectNextItem();
    }

    public void insertSelectedCompletion() {
        popupWindow.insertSelectedCompletion();
    }

    @Override
    public boolean hidePopupWindow() {
        return super.hidePopupWindow();
    }

    @Override
    public void setPopupVisible(boolean visible) {
        super.setPopupVisible(visible);
    }

    @Override
    protected String getReplacementText(Completion c, Document doc, int start, int len) {
        TagCompletion tagCompletion = (TagCompletion) c;
        autoCompleteWindow.setTag(tagCompletion.getReplacementText(), tagCompletion.getType());
        return c.getReplacementText();
    }

    public String getSelectedValue() {
        String completionText = "";
        Completion completion = popupWindow.getSelection();

        if (completion != null) {
            completionText = completion.getInputText();
            insertCompletion(completion);
        }

        return completionText;
    }
}
