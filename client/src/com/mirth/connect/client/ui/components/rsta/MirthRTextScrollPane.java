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
import com.mirth.connect.model.codetemplates.ContextType;

public class MirthRTextScrollPane extends RTextScrollPane implements SyntaxConstants {

    private MirthRSyntaxTextArea textArea;

    public MirthRTextScrollPane(ContextType contextType) {
        this(contextType, true);
    }

    public MirthRTextScrollPane(ContextType contextType, boolean lineNumbers) {
        this(contextType, lineNumbers, SYNTAX_STYLE_JAVASCRIPT);
    }

    public MirthRTextScrollPane(ContextType contextType, boolean lineNumbers, String styleKey) {
        this(contextType, lineNumbers, styleKey, true);
    }

    public MirthRTextScrollPane(ContextType contextType, boolean lineNumbers, String styleKey, boolean autoCompleteEnabled) {
        textArea = new MirthRSyntaxTextArea(contextType, styleKey, autoCompleteEnabled);
        setViewportView(textArea);
        setFoldIndicatorEnabled(true);
        setLineNumbersEnabled(lineNumbers);
        getGutter().setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    public MirthRSyntaxTextArea getTextArea() {
        return textArea;
    }

    public void setContextType(ContextType contextType) {
        textArea.setContextType(contextType);
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

    public void setText(String text, boolean discardEdits) {
        textArea.setText(text, discardEdits);
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

    public void updateDisplayOptions() {
        textArea.updateDisplayOptions();
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