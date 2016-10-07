/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.javascript;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;

import com.mirth.connect.client.ui.FunctionList;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.util.JavaScriptContextUtil;

public class JavaScriptHttpAuthDialog extends MirthDialog {

    private boolean saved;

    public JavaScriptHttpAuthDialog(Frame owner, String script) {
        super(owner, true);
        initComponents(script);
        initLayout();

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public boolean wasSaved() {
        return saved;
    }

    public String getScript() {
        return scriptPane.getText();
    }

    private void initComponents(String script) {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());
        setTitle("HTTP Authentication Script");
        setPreferredSize(new Dimension(750, 500));

        scriptPane = new MirthRTextScrollPane(ContextType.SOURCE_RECEIVER, true);
        scriptPane.setSaveEnabled(false);
        scriptPane.setCaretPosition(0);
        scriptPane.setText(script);

        functionList = new FunctionList(ContextType.SOURCE_RECEIVER);

        separator = new JSeparator();

        validateScriptButton = new JButton("Validate Script");
        validateScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                validateScript(true);
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (validateScript(false)) {
                    saved = true;
                    dispose();
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 6, fill"));
        add(scriptPane, "grow, push");
        add(functionList, "growy, w 200!");
        add(separator, "newline, growx, sx");
        add(validateScriptButton, "newline, sx, right, split 3");
        add(okButton);
        add(cancelButton);
    }

    private boolean validateScript(boolean alertOnSuccess) {
        boolean success = false;
        StringBuilder sb = new StringBuilder();
        Context context = JavaScriptContextUtil.getGlobalContextForValidation();
        try {
            context.compileString("function rhinoWrapper() {" + scriptPane.getText() + "\n}", UUID.randomUUID().toString(), 1, null);
            sb.append("JavaScript was successfully validated.");
            success = true;
        } catch (EvaluatorException e) {
            sb.append("Error on line " + e.lineNumber() + ": " + e.getMessage() + " of the current script.");
        } catch (Exception e) {
            sb.append("Unknown error occurred during validation.");
        } finally {
            Context.exit();
        }

        if (!success || alertOnSuccess) {
            PlatformUI.MIRTH_FRAME.alertInformation(JavaScriptHttpAuthDialog.this, sb.toString());
        }

        return success;
    }

    private MirthRTextScrollPane scriptPane;
    private FunctionList functionList;
    private JSeparator separator;
    private JButton validateScriptButton;
    private JButton okButton;
    private JButton cancelButton;
}