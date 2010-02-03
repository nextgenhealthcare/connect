/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.EDITokenMarker;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.X12TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.webreach.mirth.client.ui.beans.DelimitedProperties;
import com.webreach.mirth.client.ui.beans.EDIProperties;
import com.webreach.mirth.client.ui.beans.HL7Properties;
import com.webreach.mirth.client.ui.beans.HL7V3Properties;
import com.webreach.mirth.client.ui.beans.NCPDPProperties;
import com.webreach.mirth.client.ui.beans.X12Properties;
import com.webreach.mirth.client.ui.beans.XMLProperties;
import com.webreach.mirth.client.ui.editors.BoundPropertiesSheetDialog;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.DICOMSerializer;

public class TemplatePanel extends javax.swing.JPanel implements DropTargetListener {

    public final String DEFAULT_TEXT = "Paste a sample message here.";
    protected MirthEditorPane parent;
    private SyntaxDocument HL7Doc;
    private TreePanel treePanel;
    private String currentMessage = "";
    private String data;
    private Properties dataProperties;
    private Timer timer;

    public TemplatePanel() {
        initComponents();
    }

    public TemplatePanel(MirthEditorPane m) {
        this.parent = m;

        initComponents();
        openFileButton.setIcon(UIConstants.FILE_PICKER_ICON);

        if (PlatformUI.MIRTH_FRAME != null) {
            dataType.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.protocols.values().toArray()));
        }

        HL7Doc = new SyntaxDocument();
        HL7Doc.setTokenMarker(new HL7TokenMarker());
        pasteBox.setDocument(HL7Doc);
        //  pasteBox.setPreferredSize(new Dimension(100,100));
        //  pasteBox.setFont(EditorConstants.DEFAULT_FONT);

        // handles updating the tree
        pasteBox.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                updateText();
            }

            public void insertUpdate(DocumentEvent e) {
                updateText();
            }

            public void removeUpdate(DocumentEvent e) {
                updateText();
            }
        });
        pasteBox.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (pasteBox.getText().equals(DEFAULT_TEXT)) {
                        pasteBox.setText("");
                    }
                }
            }

            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (pasteBox.getText().length() == 0) {
                        pasteBox.setText(DEFAULT_TEXT);
                    }
                }
            }
        });

        new DropTarget(pasteBox, this);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
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

                java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);

                File file = (File) fileList.get(0);

                if (getProtocol().equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DICOM))) {
                    //pasteBox.setText(file.getPath());
                    pasteBox.setText(new DICOMSerializer().toXML(file));
                } else {
                    pasteBox.setText(FileUtil.read(file));
                }

                parent.modified = true;
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    public void setDataTypeEnabled(boolean enabled) {
        dataType.setEnabled(enabled);
    }

    public void setTreePanel(TreePanel tree) {
        this.treePanel = tree;
    }

    private void updateText() {
        class UpdateTimer extends TimerTask {

            @Override
            public void run() {
                if (!currentMessage.equals(pasteBox.getText())) {
                    PlatformUI.MIRTH_FRAME.setWorking("Parsing...", true);

                    String message = pasteBox.getText();
                    currentMessage = message;
                    treePanel.setMessage(dataProperties, (String) dataType.getSelectedItem(), message, DEFAULT_TEXT, dataProperties);
                    PlatformUI.MIRTH_FRAME.setWorking("", false);

                } else {
                    treePanel.clearMessage();
                }
            }
        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        } else {
            timer.cancel();
            PlatformUI.MIRTH_FRAME.setWorking("", false);
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
    }

    public String getMessage() {
        if (pasteBox.getText().equals(DEFAULT_TEXT)) {
            return "";
        }
//        else
//            return pasteBox.getText().replace('\n', '\r');  // Not required with current text area
        return pasteBox.getText();
    }

    public void setMessage(String msg) {
//        if (msg != null)
//            msg = msg.replace('\r', '\n');  // Not required with current text area
        pasteBox.setText(msg);
        pasteBoxFocusLost(null);
        updateText();
    }

    public void clearMessage() {
        treePanel.clearMessage();
        pasteBoxFocusLost(null);
        updateText();
    }

    public void setProtocol(String protocol) {
        dataType.setSelectedItem(protocol);

        setDocType(protocol);
    }

    private void setDocType(String protocol) {
        if (protocol.equals("HL7 v2.x")) {
            HL7Doc.setTokenMarker(new HL7TokenMarker());
        } else if (protocol.equals("EDI")) {
            HL7Doc.setTokenMarker(new EDITokenMarker());
        } else if (protocol.equals("X12")) {
            HL7Doc.setTokenMarker(new X12TokenMarker());
        } else if (protocol.equals("HL7 v3.0") || protocol.equals("XML")) {
            HL7Doc.setTokenMarker(new XMLTokenMarker());
        }
        pasteBox.setDocument(HL7Doc);
    }

    public String getProtocol() {
        return (String) dataType.getSelectedItem();
    }

    public Properties getDataProperties() {
        return dataProperties;
    }

    public void setDataProperties(Properties p) {
        if (p != null) {
            dataProperties = p;
        } else {
            dataProperties = new Properties();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        dataType = new javax.swing.JComboBox();
        properties = new javax.swing.JButton();
        pasteBox = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        openFileButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1), "Message Template", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 51))); // NOI18N

        jLabel5.setText("Data Type:");

        dataType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeActionPerformed(evt);
            }
        });

        properties.setText("Properties");
        properties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesActionPerformed(evt);
            }
        });

        pasteBox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pasteBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pasteBoxFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pasteBoxFocusLost(evt);
            }
        });

        openFileButton.setToolTipText("Open File...");
        openFileButton.setMargin(new java.awt.Insets(0, 1, 0, 1));
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pasteBox, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(properties)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openFileButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(properties, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openFileButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pasteBox, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel5)
                .addGap(207, 207, 207))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dataType, openFileButton, properties});

    }// </editor-fold>//GEN-END:initComponents

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileButtonActionPerformed
    {//GEN-HEADEREND:event_openFileButtonActionPerformed
        File file = PlatformUI.MIRTH_FRAME.importFile(null);

        if (file != null) {
            try {
                if (getProtocol().equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DICOM))) {
                    //pasteBox.setText(file.getPath());
                    pasteBox.setText(new DICOMSerializer().toXML(file));
                } else {
                    pasteBox.setText(FileUtil.read(file));
                }

                parent.modified = true;
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertException(this, e.getStackTrace(), "Invalid template file. " + e.getMessage());
                return;
            }
        }
    }//GEN-LAST:event_openFileButtonActionPerformed

    private void pasteBoxFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_pasteBoxFocusLost
    {//GEN-HEADEREND:event_pasteBoxFocusLost
    }//GEN-LAST:event_pasteBoxFocusLost

    private void pasteBoxFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_pasteBoxFocusGained
    {//GEN-HEADEREND:event_pasteBoxFocusGained
        if (pasteBox.getText().equals(DEFAULT_TEXT)) {
            pasteBox.setText("");
        }
    }//GEN-LAST:event_pasteBoxFocusGained

    private void propertiesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_propertiesActionPerformed
    {//GEN-HEADEREND:event_propertiesActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI))) {
            new BoundPropertiesSheetDialog(dataProperties, new EDIProperties());
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12))) {
            new BoundPropertiesSheetDialog(dataProperties, new X12Properties());
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2))) {
            new BoundPropertiesSheetDialog(dataProperties, new HL7Properties());
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3))) {
            new BoundPropertiesSheetDialog(dataProperties, new HL7V3Properties());
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP))) {
            new BoundPropertiesSheetDialog(dataProperties, new NCPDPProperties());
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DELIMITED))) {
            new BoundPropertiesSheetDialog(dataProperties, new DelimitedProperties(), 550, 370);
        } else if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML))) {
            new BoundPropertiesSheetDialog(dataProperties, new XMLProperties());
        }
        updateText();
    }//GEN-LAST:event_propertiesActionPerformed

    private void dataTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeActionPerformed
    {//GEN-HEADEREND:event_dataTypeActionPerformed
        PlatformUI.MIRTH_FRAME.enableSave();
        currentMessage = "";
        if (((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DELIMITED))
                || ((String) dataType.getSelectedItem()).equals(PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML))) {
            properties.setEnabled(true);
        } else {
            properties.setEnabled(false);
        }
        dataProperties = new Properties();
        setDocType((String) dataType.getSelectedItem());
        updateText();
    }//GEN-LAST:event_dataTypeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dataType;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton openFileButton;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea pasteBox;
    private javax.swing.JButton properties;
    // End of variables declaration//GEN-END:variables
}
