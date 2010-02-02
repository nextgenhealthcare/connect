package com.webreach.mirth.client.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.syntax.jedit.JEditTextArea;

import com.webreach.mirth.client.ui.Frame;

public class ShowLineEndingsAction extends AbstractAction {

    JEditTextArea textArea;
    Frame frame;

    public ShowLineEndingsAction(JEditTextArea textArea) {
        super("Show Line Endings");
        this.textArea = textArea;

    }

    public void actionPerformed(ActionEvent e) {
        if (this.textArea.isShowLineEndings()) {
            this.textArea.setShowLineEndings(false);

        } else {
            this.textArea.setShowLineEndings(true);
        }
    }

    public boolean isEnabled() {
        return this.textArea.isEnabled();
    }
}
