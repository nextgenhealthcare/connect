/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.client.core.PaginatedMessageList;
import com.mirth.connect.client.ui.panels.export.MessageExportPanel;
import com.mirth.connect.client.ui.util.DialogUtils;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.MessageUtils;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

/**
 * Dialog containing MessageExportPanel that is used in the message browser to
 * export messages
 */
public class MessageExportDialog extends JDialog {
    private Frame parent;
    private String channelId;
    private MessageFilter messageFilter;
    private int pageSize;
    private Encryptor encryptor;
    private MessageExportPanel messageExportPanel;
    private JButton exportButton = new JButton("Export");
    private JButton cancelButton = new JButton("Cancel");

    public MessageExportDialog() {
        parent = PlatformUI.MIRTH_FRAME;
        messageExportPanel = new MessageExportPanel(Frame.userPreferences, false, true);
        messageExportPanel.setExportLocal(true);

        setTitle("Export Results");
        setSize(800, 230);
        setBackground(new Color(255, 255, 255));
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
        initLayout();
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setMessageFilter(MessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        getContentPane().setBackground(color);
        messageExportPanel.setBackground(color);
    }

    private void initComponents() {
        ActionListener exportAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                export();
            }
        };

        ActionListener cancelAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };

        exportButton.addActionListener(exportAction);
        cancelButton.addActionListener(cancelAction);
        DialogUtils.registerEscapeKey(this, cancelAction);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, wrap", "[]", "[fill]12[]"));

        add(messageExportPanel, "grow,push");
        add(exportButton, "split 2, alignx right, width 70");
        add(cancelButton, "width 70");
    }

    private void export() {
        if (!messageExportPanel.validate(true)) {
            parent.alertError(this, "Please fill in required fields.");
            return;
        }

        int exportCount = 0;
        MessageWriterOptions writerOptions = messageExportPanel.getMessageWriterOptions();

        if (StringUtils.isBlank(writerOptions.getRootFolder())) {
            parent.alertError(parent, "Please enter a valid root path to store exported files.");
            setVisible(true);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            if (messageExportPanel.isExportLocal()) {
                PaginatedMessageList messageList = new PaginatedMessageList();
                messageList.setChannelId(channelId);
                messageList.setClient(parent.mirthClient);
                messageList.setMessageFilter(messageFilter);
                messageList.setPageSize(pageSize);
                messageList.setIncludeContent(true);

                MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(writerOptions, encryptor, channelId);
                exportCount = MessageUtils.exportMessages(messageList, messageWriter).size();
            } else {
                exportCount = parent.mirthClient.exportMessagesServer(channelId, messageFilter, pageSize, false, writerOptions);
            }

            setVisible(false);
            setCursor(Cursor.getDefaultCursor());
            parent.alertInformation(parent, exportCount + " message" + ((exportCount == 1) ? " has" : "s have") + " been successfully exported to " + writerOptions.getRootFolder());
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            Throwable cause = (e.getCause() == null) ? e : e.getCause();
            parent.alertException(parent, cause.getStackTrace(), cause.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Mirth.initUIManager();
        PlatformUI.MIRTH_FRAME = new Frame() {
            public void setSaveEnabled(boolean enabled) {}
        };

        JDialog dialog = new MessageExportDialog();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
}
