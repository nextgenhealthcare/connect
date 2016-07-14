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
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.plugins.AttachmentViewer;

public class AttachmentTypeDialog extends MirthDialog {

    private boolean okPressed;

    public AttachmentTypeDialog(String contentType) {
        super(PlatformUI.MIRTH_FRAME, true);
        initComponents(contentType);
        initLayout();

        setTitle("Select Attachment Viewer");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(300, checkBox.isVisible() ? 155 : 133));
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(true);
    }

    public AttachmentViewer getAttachmentViewer() {
        if (okPressed) {
            return LoadedExtensions.getInstance().getAttachmentViewerPlugins().get(comboBox.getSelectedItem());
        } else {
            return null;
        }
    }

    private void initComponents(String contentType) {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        Map<String, AttachmentViewer> pluginMap = LoadedExtensions.getInstance().getAttachmentViewerPlugins();

        String defaultPluginName = "Text Viewer";
        if (!pluginMap.containsKey(defaultPluginName)) {
            defaultPluginName = pluginMap.keySet().iterator().next();
        }
        boolean found = false;
        for (AttachmentViewer plugin : pluginMap.values()) {
            if (plugin.isContentTypeViewable(contentType)) {
                found = true;
                defaultPluginName = plugin.getPluginName();
                break;
            }
        }

        label = new JLabel();
        if (found) {
            label.setText("<html>Select an attachment viewer to use for content type \"" + contentType + "\":</html>");
        } else {
            label.setText("<html>Attachment viewer for content type \"" + contentType + "\" not found. Select one from the menu below:</html>");
        }

        comboBox = new JComboBox<String>(new DefaultComboBoxModel<String>(pluginMap.keySet().toArray(new String[pluginMap.size()])));
        comboBox.setSelectedItem(defaultPluginName);

        checkBox = new JCheckBox("Always choose viewer automatically from MIME type");
        checkBox.setBackground(getBackground());
        checkBox.setToolTipText("<html>Don't show this dialog again, and instead automatically<br/>choose the attachment viewer from the MIME type. This<br/>may be changed later in the Administrator settings.</html>");
        if (!found) {
            checkBox.setVisible(false);
        }

        separator = new JSeparator(SwingConstants.HORIZONTAL);

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okPressed = true;
                if (checkBox.isVisible() && checkBox.isSelected()) {
                    Preferences.userNodeForPackage(Mirth.class).putBoolean("messageBrowserShowAttachmentTypeDialog", false);
                }
                dispose();
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
        String rowConstraints = checkBox.isVisible() ? "[][][][grow,bottom][]" : "[][][grow,bottom][]";
        setLayout(new MigLayout("insets 10, novisualpadding, hidemode 3, fill", "", rowConstraints));

        add(label);
        add(comboBox, "newline, growx");
        if (checkBox.isVisible()) {
            add(checkBox, "newline");
        }
        add(separator, "newline, growx");
        add(okButton, "newline, right, w 45!, split 2");
        add(cancelButton, "w 45!");
    }

    private JLabel label;
    private JComboBox<String> comboBox;
    private JCheckBox checkBox;
    private JSeparator separator;
    private JButton okButton;
    private JButton cancelButton;
}