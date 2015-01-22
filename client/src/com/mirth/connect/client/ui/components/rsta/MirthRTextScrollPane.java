/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.Color;

import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.model.CodeTemplate.ContextType;

public class MirthRTextScrollPane extends RTextScrollPane implements SyntaxConstants {

    private MirthRSyntaxTextArea textArea;

    public MirthRTextScrollPane() {
        this(true);
    }

    public MirthRTextScrollPane(boolean lineNumbers) {
        this(lineNumbers, ContextType.GLOBAL_CONTEXT.getContext());
    }

    public MirthRTextScrollPane(boolean lineNumbers, int context) {
        this(lineNumbers, context, SYNTAX_STYLE_JAVASCRIPT);
    }

    public MirthRTextScrollPane(boolean lineNumbers, int context, String styleKey) {
        this(lineNumbers, context, styleKey, true);
    }

    public MirthRTextScrollPane(boolean lineNumbers, int context, String styleKey, boolean autoCompleteEnabled) {
        textArea = new MirthRSyntaxTextArea(styleKey, context, autoCompleteEnabled);
        setViewportView(textArea);
        setFoldIndicatorEnabled(true);
        setLineNumbersEnabled(lineNumbers);
        getGutter().setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    public MirthRSyntaxTextArea getTextArea() {
        return textArea;
    }

    public String getText() {
        return textArea.getEOLFixedText();
    }

    public String getSelectedText() {
        return textArea.getEOLFixedSelectedText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public void setSelectedText(String text) {
        textArea.setSelectedText(text);
    }

    public Document getDocument() {
        return textArea.getDocument();
    }

    public void setSaveEnabled(boolean saveEnabled) {
        textArea.setSaveEnabled(saveEnabled);
    }

    public void setCaretPosition(int position) {
        textArea.setCaretPosition(position);
    }

    public void setSyntaxEditingStyle(String styleKey) {
        textArea.setSyntaxEditingStyle(styleKey);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (textArea != null) {
            textArea.setEnabled(enabled);
        }
    }

    @Override
    public void setBackground(Color bg) {
        if (bg == null) {
            bg = UIConstants.BACKGROUND_COLOR;
        }

        super.setBackground(bg);
        if (textArea != null) {
            textArea.setBackground(bg);
        }
    }
}