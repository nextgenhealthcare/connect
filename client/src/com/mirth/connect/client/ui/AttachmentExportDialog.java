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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.util.attachmentwriter.AttachmentWriter;

public class AttachmentExportDialog extends MirthDialog {
    private Preferences userPreferences;

    public AttachmentExportDialog() {
        super(PlatformUI.MIRTH_FRAME);
        userPreferences = Preferences.userNodeForPackage(Mirth.class);

        setTitle("Export Attachment");
        setPreferredSize(new Dimension(500, 145));
        getContentPane().setBackground(Color.white);
        setLocationRelativeTo(null);
        setModal(true);

        initComponents();
        initLayout();
        pack();
    }

    private void initComponents() {
        attachmentExportPanel = new JPanel();
        attachmentExportPanel.setBackground(Color.white);

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
        attachmentExportPanel.setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0", "[right][left]"));

        attachmentExportPanel.add(new JLabel("File Type:"));
        attachmentExportPanel.add(binaryButton, "split 2");
        attachmentExportPanel.add(textButton, "wrap");

        attachmentExportPanel.add(new JLabel("Export To:"));
        attachmentExportPanel.add(serverButton, "split 3");
        attachmentExportPanel.add(localButton);
        attachmentExportPanel.add(browseButton, "wrap");

        attachmentExportPanel.add(new JLabel("File:"));
        attachmentExportPanel.add(fileField, "push, grow, span");

        setLayout(new MigLayout("insets 12, wrap", "", "[fill][]"));

        add(attachmentExportPanel, "grow, push");
        add(new JSeparator(), "grow, span");
        add(exportButton, "split 2, alignx right, width 60, gaptop 4");
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
                        AttachmentWriter.write(fileField.getText(), getSelectedAttachment(), binary);
                    } else {
                        PlatformUI.MIRTH_FRAME.mirthClient.exportAttachmentServer(PlatformUI.MIRTH_FRAME.messageBrowser.getChannelId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedAttachmentId(), fileField.getText(), binary);
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
        return PlatformUI.MIRTH_FRAME.mirthClient.getAttachment(PlatformUI.MIRTH_FRAME.messageBrowser.getChannelId(), PlatformUI.MIRTH_FRAME.messageBrowser.getSelectedAttachmentId());
    }

    private JPanel attachmentExportPanel;
    private JRadioButton textButton;
    private JRadioButton binaryButton;

    private JRadioButton serverButton;
    private JRadioButton localButton;
    private JButton browseButton;

    private JTextField fileField;

    private JButton exportButton;
    private JButton cancelButton;
}