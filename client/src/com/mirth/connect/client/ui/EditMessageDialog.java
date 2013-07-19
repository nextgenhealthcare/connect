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
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;

public class EditMessageDialog extends javax.swing.JDialog implements DropTargetListener {

    private Frame parent;
    private String channelId;

    /**
     * 
     * @param message
     * @param dataType
     * @param channelId
     * @param selectedMetaDataIds
     *            The connectors that will be pre-selected for processing the
     *            message. If null, all connectors will be pre-selected.
     */
    public EditMessageDialog(String message, String dataType, String channelId, Map<Integer, String> destinationConnectors, List<Integer> selectedMetaDataIds) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.channelId = channelId;
        initComponents();
        setCorrectDocument(messageContent, message, dataType);
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

        initDestinationConnectorTable(destinationConnectors, selectedMetaDataIds);
        setVisible(true);
    }

    private void initDestinationConnectorTable(Map<Integer, String> destinationConnectors, List<Integer> selectedMetaDataIds) {
        mirthTable1 = new ItemSelectionTable();
        mirthTable1.setModel(new ItemSelectionTableModel<Integer, String>(destinationConnectors, selectedMetaDataIds, "Destination", "Included"));
        jScrollPane1.setViewportView(mirthTable1);
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

    public void dragOver(DropTargetDragEvent dtde) {}

    public void dropActionChanged(DropTargetDragEvent dtde) {}

    public void dragExit(DropTargetEvent dte) {}

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

    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, String dataType) {
        SyntaxDocument newDoc = new SyntaxDocument();

        if (message != null) {
            if (dataType != null) {
                TokenMarker tokenMarker = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getTokenMarker();

                if (tokenMarker != null) {
                    newDoc.setTokenMarker(tokenMarker);
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
        openTextFileButton = new javax.swing.JButton();
        openBinaryFileButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthTable1 = new com.mirth.connect.client.ui.components.MirthTable();
        jLabel1 = new javax.swing.JLabel();

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

        openTextFileButton.setText("Open Text File...");
        openTextFileButton.setToolTipText("Open a text file into the editor above.");
        openTextFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTextFileButtonActionPerformed(evt);
            }
        });

        openBinaryFileButton.setText("Open Binary File...");
        openBinaryFileButton.setToolTipText("<html>Open a binary file into the editor above.<br>The file will be encoded and displayed as Base64.</html>");
        openBinaryFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBinaryFileButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(mirthTable1);

        jLabel1.setText("Send to the following destination(s):");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageContent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 354, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(processMessageButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(openTextFileButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openBinaryFileButton)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageContent, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openBinaryFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openTextFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeButton)
                    .addComponent(processMessageButton))
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

    private void openBinaryFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openBinaryFileButtonActionPerformed
    {//GEN-HEADEREND:event_openBinaryFileButtonActionPerformed
        byte[] content = parent.browseForFileBytes(null);

        if (content != null) {
            messageContent.setText(new String(Base64.encodeBase64Chunked(content)));
        }
    }//GEN-LAST:event_openBinaryFileButtonActionPerformed

    private void openTextFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTextFileButtonActionPerformed
        String content = parent.browseForFileString(null);

        if (content != null) {
            messageContent.setText(content);
        }
    }//GEN-LAST:event_openTextFileButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void processMessageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_processMessageButtonActionPerformed
    {//GEN-HEADEREND:event_processMessageButtonActionPerformed
        ItemSelectionTableModel<Integer, String> model = (ItemSelectionTableModel<Integer, String>) mirthTable1.getModel();
        List<Integer> metaDataIds = model.getKeys(true);

        if (metaDataIds.size() == model.getRowCount()) {
            metaDataIds = null;
        }

        parent.processMessage(channelId, messageContent.getText(), metaDataIds);
        this.dispose();
    }//GEN-LAST:event_processMessageButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea messageContent;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable1;
    private javax.swing.JButton openBinaryFileButton;
    private javax.swing.JButton openTextFileButton;
    private javax.swing.JButton processMessageButton;
    // End of variables declaration//GEN-END:variables
}
