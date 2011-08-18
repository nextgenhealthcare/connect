/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.MessageObject.Protocol;

/** Creates the Edit Message dialog. */
public class EditMessageDialog extends javax.swing.JDialog implements DropTargetListener {

    private Frame parent;
    private MessageObject message;

    public EditMessageDialog(MessageObject message) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.message = message;
        initComponents();
        setCorrectDocument(messageContent, message.getRawData(), message.getRawDataProtocol());
        messageContent.setCaretPosition(0);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        pack();
        new DropTarget(messageContent, this);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        setVisible(true);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                }
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    File file = iterator.next();
                    messageContent.setText(messageContent.getText() + FileUtils.readFileToString(file, UIConstants.CHARSET));
                }
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, MessageObject.Protocol protocol) {
        SyntaxDocument newDoc = new SyntaxDocument();

        if (message != null) {
            if (protocol != null) {
                if (protocol.equals(MessageObject.Protocol.HL7V2) || protocol.equals(MessageObject.Protocol.NCPDP) || protocol.equals(MessageObject.Protocol.DICOM)) {
                    newDoc.setTokenMarker(new HL7TokenMarker());
//                    message = message.replace('\r', '\n');  // Not required with current text area
                    // HL7 (ER7) encoded messages have \r as end of line
                    // segments
                    // The syntax editor box only recognizes \n
                    // Add \n to make things look normal
                } else if (protocol.equals(MessageObject.Protocol.XML) || protocol.equals(Protocol.HL7V3)) {
                    newDoc.setTokenMarker(new XMLTokenMarker());
                } else if (protocol.equals(MessageObject.Protocol.X12)) {
                    newDoc.setTokenMarker(new X12TokenMarker());
                } else if (protocol.equals(MessageObject.Protocol.EDI)) {
                    newDoc.setTokenMarker(new EDITokenMarker());
                }
            }

            textPane.setDocument(newDoc);
            textPane.setText(message);
        } else {
            textPane.setDocument(newDoc);
            textPane.setText("");
        }

        textPane.setCaretPosition(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        processMessageButton = new javax.swing.JButton();
        messageContent = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        openFileButton = new javax.swing.JButton();
        processBinaryFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Message");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        closeButton.setText("Close");
        closeButton.setToolTipText("Close this message sender dialog.");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        processMessageButton.setText("Process Message");
        processMessageButton.setToolTipText("Process the message displayed in the editor above.");
        processMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processMessageButtonActionPerformed(evt);
            }
        });

        messageContent.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        openFileButton.setText("Open File...");
        openFileButton.setToolTipText("Open a file into the editor above.");
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        processBinaryFileButton.setText("Process Binary File...");
        processBinaryFileButton.setToolTipText("Process a file without first loading the contents into the editor.");
        processBinaryFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBinaryFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(openFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processBinaryFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processMessageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addComponent(messageContent, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageContent, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(processMessageButton)
                    .addComponent(openFileButton)
                    .addComponent(processBinaryFileButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void processBinaryFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_processBinaryFileButtonActionPerformed
    {//GEN-HEADEREND:event_processBinaryFileButtonActionPerformed
        String content = parent.browseForFileString(null);

        if (content != null) {
            message.setRawData(Base64.encodeBase64String(content.getBytes()));
            parent.processMessage(message);
            this.dispose();
        }
    }//GEN-LAST:event_processBinaryFileButtonActionPerformed

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileButtonActionPerformed
        String content = parent.browseForFileString(null);

        if (content != null) {
            messageContent.setText(content);
        }
    }//GEN-LAST:event_openFileButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void processMessageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_processMessageButtonActionPerformed
    {//GEN-HEADEREND:event_processMessageButtonActionPerformed
        message.setRawData(messageContent.getText());
        parent.processMessage(message);
        this.dispose();
    }//GEN-LAST:event_processMessageButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel jPanel1;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea messageContent;
    private javax.swing.JButton openFileButton;
    private javax.swing.JButton processBinaryFileButton;
    private javax.swing.JButton processMessageButton;
    // End of variables declaration//GEN-END:variables
}
