/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class JavaScriptPropertyEditor extends PropertyEditorSupport {

    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns the custom property editor.
     */
    public Component getCustomEditor() {
        JPanel fieldPanel = new JPanel(new BorderLayout());

        JLabel scriptLabel = new JLabel();

        if (getValue() == null) {
            scriptLabel.setText("");
        } else {
            scriptLabel.setText(getAsText());
        }

        JButton scriptButton = new JButton("...");

        scriptLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() >= 2) {
                    displayEditor((Component) evt.getSource());
                }
            }
        });

        scriptButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                displayEditor((Component) ae.getSource());
            }
        });

        fieldPanel.add(scriptLabel, BorderLayout.CENTER);

        fieldPanel.add(scriptButton, BorderLayout.EAST);

        return fieldPanel;
    }

    private void displayEditor(Component source) {
        Window win = SwingUtilities.windowForComponent(source);

        JavaScriptEditorDialog dialog = new JavaScriptEditorDialog((Dialog) win, (String) getValue());

        setValue(dialog.getSavedScript());

        firePropertyChange();
    }
}
