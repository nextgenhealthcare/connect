/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.util.AttachmentUtil;

public class AttachmentExportDialog extends MirthDialog {
    private Preferences userPreferences;

    public AttachmentExportDialog() {
        super(PlatformUI.MIRTH_FRAME);
        userPreferences = Preferences.userNodeForPackage(Mirth.class);

        setTitle("Export Attachment");
        setPreferredSize(new Dimension(500, 155));
        getContentPane().setBackground(Color.white);
        setLocationRelativeTo(null);
        DisplayUtil.setResizable(this, false);
        setModal(true);

        initComponents();
        initLayout();
        pack();
    }

    private void initComponents() {
        binaryButton = new JRadioButton("Binary");
        binaryButton.setSelected(true);
        binaryButton.setBackground(Color.white);
        binaryButton.setFocusable(false);

        textButton = new JRadioButton("Text");
        textButton.setBackground(Color.white);
        textButton.setFocusable(false);

        ButtonGroup typeButtonGroup = new ButtonGroup();
        typeButtonGroup.add(binaryButton);
        typeButtonGroup.add(textButton);

        serverButton = new JRadioButton("Server");
        serverButton.setBackground(Color.white);
        serverButton.setFocusable(false);

        localButton = new JRadioButton("My Computer");
        localButton.setBackground(Color.white);
        localButton.setSelected(true);
        localButton.setFocusable(false);

        browseButton = new MirthButton("Browse...");

        localButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButton.setEnabled(true);
                fileField.setText(null);
            }
        });

        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButton.setEnabled(false);
                fileField.setText(null);
            }
        });

        ButtonGroup locationButtonGroup = new ButtonGroup();
        locationButtonGroup.add(serverButton);
        locationButtonGroup.add(localButton);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseSelected();
            }
        });

        fileField = new JTextField();

        exportButton = new JButton("Export");
        cancelButton = new JButton("Cancel");

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, fill", "[right][left]"));

        add(new JLabel("File Type:"));
        add(binaryButton, "split 2");
        add(textButton, "wrap");

        add(new JLabel("Export To:"));
        add(serverButton, "split 3");
        add(localButton);
        add(browseButton, "wrap");

        add(new JLabel("File:"));
        add(fileField, "push, growx, span");

        add(new JSeparator(), "grow, span");
        add(exportButton, "width 60, right, split 2, spanx, gaptop 4");
        add(cancelButton, "width 60, gaptop 4");
    }

    private void browseSelected() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (userPreferences != null) {
            File currentDir = new File(userPreferences.get("currentDirectory", ""));

            if (currentDir.exists()) {
                chooser.setCurrentDirectory(currentDir);
            }
        }

        if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            if (userPreferences != null) {
                userPreferences.put("currentDirectory", chooser.getCurrentDirectory().getPath());
            }

            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void export() {
        if (StringUtils.isBlank(fileField.getText())) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Please fill in required fields.");
            fileField.setBackground(UIConstants.INVALID_COLOR);
            return;
        } else {
            fileField.setBackground(Color.white);
        }

        if (PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedMimeType().equalsIgnoreCase("dicom")) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Cannot export DICOM attachments.");
            return;
        }

        doExport();

        setVisible(false);
        setCursor(Cursor.getDefaultCursor());
    }

    private void doExport() {
        final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Exporting attachment...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String errorMessage = "";

            public Void doInBackground() {
                boolean binary = binaryButton.isSelected();
                try {
                    if (localButton.isSelected()) {
                        AttachmentUtil.writeToFile(fileField.getText(), getSelectedAttachment(), binary);
                    } else {
                        PlatformUI.MIRTH_FRAME.mirthClient.exportAttachmentServer(PlatformUI.MIRTH_FRAME.messageBrowser.getChannelId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedMessageId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedAttachmentId(), fileField.getText(), binary);
                    }
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                }

                return null;
            }

            public void done() {
                PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                PlatformUI.MIRTH_FRAME.alertInformation(PlatformUI.MIRTH_FRAME, StringUtils.isBlank(errorMessage) ? "Successfully exported attachment to: " + fileField.getText() : "Failed to export attachment: " + errorMessage);
            }
        };

        worker.execute();
    }

    public Attachment getSelectedAttachment() throws ClientException {
        return PlatformUI.MIRTH_FRAME.mirthClient.getAttachment(PlatformUI.MIRTH_FRAME.messageBrowser.getChannelId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedMessageId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedAttachmentId());
    }

    private JRadioButton textButton;
    private JRadioButton binaryButton;

    private JRadioButton serverButton;
    private JRadioButton localButton;
    private JButton browseButton;

    private JTextField fileField;

    private JButton exportButton;
    private JButton cancelButton;
}