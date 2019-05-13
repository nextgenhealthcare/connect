/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.browsers.message.MessageBrowser;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.util.MessageImporter;
import com.mirth.connect.util.MessageImporter.MessageImportInvalidPathException;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;

public class MessageImportDialog extends MirthDialog {
    private String channelId;
    private Frame parent;
    private Preferences userPreferences;

    public MessageImportDialog() {
        super(PlatformUI.MIRTH_FRAME);
        parent = PlatformUI.MIRTH_FRAME;
        userPreferences = Frame.userPreferences;

        setTitle("Import Messages");
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
        initLayout();
        pack();
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setMessageBrowser(MessageBrowser messageBrowser) {
        this.messageBrowser = messageBrowser;
    }

    private void initComponents() {
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        importFromLabel = new JLabel("Import From:");

        ActionListener importDestinationChanged = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (importServerRadio.isSelected()) {
                    fileTextField.setText(null);
                    browseButton.setEnabled(false);
                } else {
                    fileTextField.setText(null);
                    browseButton.setEnabled(true);
                }
            }
        };

        importServerRadio = new MirthRadioButton("Server");
        importServerRadio.setSelected(true);
        importServerRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        importServerRadio.addActionListener(importDestinationChanged);
        importServerRadio.setToolTipText("<html>Import messages from a file, folder or archive<br />on the Mirth Connect Server.</html>");

        importLocalRadio = new MirthRadioButton("My Computer");
        importLocalRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        importLocalRadio.addActionListener(importDestinationChanged);
        importLocalRadio.setToolTipText("<html>Import messages from a file, folder<br />or archive on this computer.</html>");

        importFromButtonGroup = new ButtonGroup();
        importFromButtonGroup.add(importServerRadio);
        importFromButtonGroup.add(importLocalRadio);

        browseButton = new MirthButton("Browse...");
        browseButton.setEnabled(false);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseSelected();
            }
        });

        fileLabel = new JLabel("File/Folder/Archive:");

        fileTextField = new MirthTextField();
        fileTextField.setToolTipText("<html>A file containing message(s) in XML format, or a folder/archive<br />containing files with message(s) in XML format.</html>");

        subfoldersCheckbox = new MirthCheckBox("Include Sub-folders");
        subfoldersCheckbox.setSelected(true);
        subfoldersCheckbox.setBackground(UIConstants.BACKGROUND_COLOR);
        subfoldersCheckbox.setToolTipText("<html>If checked, sub-folders of the folder/archive shown above<br />will be searched for messages to import.</html>");

        noteLabel = new JLabel("<html><i>Note: RECEIVED, QUEUED, or PENDING messages will be set to ERROR upon import.</i></html>");

        importButton = new MirthButton("Import");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importMessages();
            }
        });

        cancelButton = new MirthButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    private void browseSelected() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

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

            fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, wrap", "[right]4[left, grow]", ""));

        add(importFromLabel);
        add(importServerRadio, "split 3");
        add(importLocalRadio);
        add(browseButton);

        add(fileLabel);
        add(fileTextField, "grow");

        add(subfoldersCheckbox, "skip");

        add(noteLabel, "skip, grow, pushy, wrap push");
        add(new JSeparator(), "grow, gaptop 6, span");
        add(importButton, "skip, split 2, gaptop 4, alignx right, width 60");
        add(cancelButton, "width 60");
    }

    private void importMessages() {
        if (StringUtils.isBlank(fileTextField.getText())) {
            fileTextField.setBackground(UIConstants.INVALID_COLOR);
            parent.alertError(parent, "Please enter a file/folder to import.");
            setVisible(true);
            return;
        } else {
            fileTextField.setBackground(null);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MessageImportResult result;

        try {
            if (importLocalRadio.isSelected()) {
                MessageWriter messageWriter = new MessageWriter() {
                    @Override
                    public boolean write(Message message) throws MessageWriterException {
                        try {
                            parent.mirthClient.importMessage(channelId, message);
                        } catch (ClientException e) {
                            throw new MessageWriterException(e);
                        }

                        return true;
                    }

                    @Override
                    public void finishWrite() {}

                    @Override
                    public void close() throws MessageWriterException {}
                };

                try {
                    result = new MessageImporter().importMessages(fileTextField.getText(), subfoldersCheckbox.isSelected(), messageWriter, SystemUtils.getUserHome().getAbsolutePath());
                } catch (MessageImportInvalidPathException e) {
                    setCursor(Cursor.getDefaultCursor());
                    parent.alertError(parent, e.getMessage());
                    setVisible(true);
                    return;
                }
            } else {
                result = parent.mirthClient.importMessagesServer(channelId, fileTextField.getText(), subfoldersCheckbox.isSelected());
            }

            setVisible(false);
            setCursor(Cursor.getDefaultCursor());

            if (result.getSuccessCount() == 0 && result.getTotalCount() == 0) {
                parent.alertInformation(parent, "No messages were found to import");
            } else {
                if (result.getSuccessCount() > 0 && messageBrowser != null) {
                    messageBrowser.updateFilterButtonFont(Font.BOLD);
                }

                parent.alertInformation(parent, result.getSuccessCount() + " out of " + result.getTotalCount() + " message(s) have been successfully imported from " + fileTextField.getText() + ".");
            }
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            Throwable cause = (e.getCause() == null) ? e : e.getCause();
            parent.alertThrowable(parent, cause);
        }
    }

    private MessageBrowser messageBrowser;
    private JLabel importFromLabel;
    private ButtonGroup importFromButtonGroup;
    private MirthRadioButton importServerRadio;
    private MirthRadioButton importLocalRadio;
    private MirthButton browseButton;
    private JLabel fileLabel;
    private MirthTextField fileTextField;
    private MirthCheckBox subfoldersCheckbox;
    private JLabel noteLabel;
    private MirthButton importButton;
    private MirthButton cancelButton;
}
