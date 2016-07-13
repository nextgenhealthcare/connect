/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.attachments;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;

public class IdentityAttachmentDialog extends MirthDialog {

    private Frame parent;
    private AttachmentHandlerProperties attachmentHandlerProperties;

    public IdentityAttachmentDialog(AttachmentHandlerProperties attachmentHandlerProperties) {
        super(PlatformUI.MIRTH_FRAME, true);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();
        setProperties(attachmentHandlerProperties);

        setTitle("Set Attachment Properties");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(309, 130));
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        propertiesPanel = new JPanel();
        propertiesPanel.setBackground(getBackground());
        propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));

        mimeTypeLabel = new JLabel("MIME Type:");
        mimeTypeField = new JTextField();

        separator = new JSeparator(SwingConstants.HORIZONTAL);

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                close();
            }
        });
    }

    private void initToolTips() {
        mimeTypeField.setToolTipText("The MIME type of the message. Source map variables may be used here.");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 10, novisualpadding, hidemode 3, fill"));

        propertiesPanel.setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3", "[]13[]"));
        propertiesPanel.add(mimeTypeLabel, "right");
        propertiesPanel.add(mimeTypeField, "growx, pushx");
        add(propertiesPanel, "grow, push");

        add(separator, "newline, growx, pushx");
        add(closeButton, "newline, right");
    }

    private void setProperties(AttachmentHandlerProperties attachmentHandlerProperties) {
        this.attachmentHandlerProperties = attachmentHandlerProperties;
        mimeTypeField.setText(StringUtils.defaultString(attachmentHandlerProperties.getProperties().get("identity.mimetype")));
    }

    private void close() {
        attachmentHandlerProperties.getProperties().clear();
        attachmentHandlerProperties.getProperties().put("identity.mimetype", mimeTypeField.getText());
        parent.setSaveEnabled(true);
        dispose();
    }

    private JPanel propertiesPanel;
    private JLabel mimeTypeLabel;
    private JTextField mimeTypeField;
    private JSeparator separator;
    private JButton closeButton;
}