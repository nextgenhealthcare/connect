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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.PaginatedMessageList;
import com.mirth.connect.client.ui.panels.export.MessageExportPanel;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.MessageExporter;
import com.mirth.connect.util.messagewriter.AttachmentSource;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterFactory;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

/**
 * Dialog containing MessageExportPanel that is used in the message browser to export messages
 */
public class MessageExportDialog extends MirthDialog {
    private Frame parent;
    private String channelId;
    private MessageFilter messageFilter;
    private int pageSize;
    private Encryptor encryptor;

    public MessageExportDialog() {
        super(PlatformUI.MIRTH_FRAME);
        parent = PlatformUI.MIRTH_FRAME;

        setTitle("Export Results");
        setPreferredSize(new Dimension(800, 300));
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
        initLayout();
        pack();
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

    private void initComponents() {
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        messageExportPanel = new MessageExportPanel(Frame.userPreferences, false, true);
        messageExportPanel.setExportLocal(true);
        messageExportPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                export();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, wrap", "[]", "[fill][]"));

        add(messageExportPanel, "grow,push");
        add(new JSeparator(), "grow, gaptop 4, span");
        add(exportButton, "split 2, gaptop 4, alignx right, width 60");
        add(cancelButton, "width 60");
    }

    private void export() {
        String errorMessage = messageExportPanel.validate(true);
        if (StringUtils.isNotEmpty(errorMessage)) {
            parent.alertError(this, errorMessage);
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

                writerOptions.setBaseFolder(SystemUtils.getUserHome().getAbsolutePath());

                MessageWriter messageWriter = MessageWriterFactory.getInstance().getMessageWriter(writerOptions, encryptor);

                AttachmentSource attachmentSource = null;
                if (writerOptions.includeAttachments()) {
                    attachmentSource = new AttachmentSource() {
                        @Override
                        public List<Attachment> getMessageAttachments(Message message) throws ClientException {
                            return PlatformUI.MIRTH_FRAME.mirthClient.getAttachmentsByMessageId(message.getChannelId(), message.getMessageId());
                        }
                    };
                }

                try {
                    exportCount = new MessageExporter().exportMessages(messageList, messageWriter, attachmentSource);
                    messageWriter.finishWrite();
                } finally {
                    messageWriter.close();
                }
            } else {
                writerOptions.setIncludeAttachments(messageExportPanel.isIncludeAttachments());
                exportCount = parent.mirthClient.exportMessagesServer(channelId, messageFilter, pageSize, writerOptions);
            }

            setVisible(false);
            setCursor(Cursor.getDefaultCursor());
            parent.alertInformation(parent, exportCount + " message" + ((exportCount == 1) ? " has" : "s have") + " been successfully exported to: " + writerOptions.getRootFolder());
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            Throwable cause = (e.getCause() == null) ? e : e.getCause();
            parent.alertThrowable(parent, cause);
        }
    }

    private MessageExportPanel messageExportPanel;
    private JButton exportButton;
    private JButton cancelButton;
}
