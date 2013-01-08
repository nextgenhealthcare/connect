/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

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
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.TokenMarker;
import com.mirth.connect.client.ui.editors.BoundPropertiesSheetDialog;
import com.mirth.connect.client.ui.editors.MirthEditorPane;
import com.mirth.connect.plugins.DataTypeClientPlugin;

public class TemplatePanel extends javax.swing.JPanel implements DropTargetListener {
    public final String DEFAULT_TEXT = "Paste a sample message here.";
    protected MirthEditorPane parent;
    private SyntaxDocument hl7Document;
    private TreePanel treePanel;
    private String currentMessage = "";
    private String currentDataType;
    private Properties dataProperties;
    private Timer timer;
    private String lastWorkingId = null;

    public TemplatePanel() {
        initComponents();
    }

    public TemplatePanel(MirthEditorPane m) {
        this.parent = m;

        initComponents();
        openFileButton.setIcon(UIConstants.ICON_FILE_PICKER);

        if (PlatformUI.MIRTH_FRAME != null) {
            dataTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.values().toArray()));
        }

        hl7Document = new SyntaxDocument();
        hl7Document.setTokenMarker(new HL7TokenMarker());
        pasteBox.setDocument(hl7Document);

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
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (pasteBox.getText().equals(DEFAULT_TEXT)) {
                        pasteBox.setText("");
                    }
                }
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
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
                File file = ((List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor)).get(0);

                String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(getDataType());
                DataTypeClientPlugin dataTypePlugin = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType);
                if (dataTypePlugin.isBinary()) {
                    byte[] content = FileUtils.readFileToByteArray(file);
                    
                    // The plugin should decide how to convert the byte array to string
                    pasteBox.setText(dataTypePlugin.getTemplateString(content));
                } else {
                    pasteBox.setText(FileUtils.readFileToString(file, UIConstants.CHARSET));
                }

                parent.modified = true;
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    public void setDataTypeEnabled(boolean dataTypeEnabled, boolean propertiesEnabled) {
        dataTypeComboBox.setEnabled(dataTypeEnabled);
        properties.setEnabled(propertiesEnabled);
    }

    public void setTreePanel(TreePanel tree) {
        this.treePanel = tree;
    }

    private void updateText() {
        class UpdateTimer extends TimerTask {
            @Override
            public void run() {
                // If the current message and pasteBox are blank then a new
                // template tree is loading, so the treePanel should be cleared.
                if (currentMessage.equals("") && currentMessage.equals(pasteBox.getText())) {
                    treePanel.clearMessage();
                } else if (!currentMessage.equals(pasteBox.getText())) {
                    final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Parsing...");
                    lastWorkingId = workingId;
                    String message = pasteBox.getText();
                    currentMessage = message;
                    
                    // Some invalid message templates cause this method to throw a NullPointer.
                    // Catch it so that we can still stop the "Parsing..." working status.
                    // TODO: Fix the possible null pointers inside of the setMessage method.
                    try {
                        treePanel.setMessage(dataProperties, (String) dataTypeComboBox.getSelectedItem(), message, DEFAULT_TEXT, dataProperties);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                }
            }
        }
        
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        } else {
            timer.cancel();
            PlatformUI.MIRTH_FRAME.stopWorking(lastWorkingId);
            timer = new Timer();
            timer.schedule(new UpdateTimer(), 1000);
        }
    }

    public String getMessage() {
        if (pasteBox.getText().equals(DEFAULT_TEXT)) {
            return "";
        }

        return pasteBox.getText();
    }

    public void setMessage(String msg) {
        pasteBox.setText(msg);
        pasteBoxFocusLost(null);
        updateText();
    }

    public void clearMessage() {
        treePanel.clearMessage();
        pasteBoxFocusLost(null);
        updateText();
    }

    public void setDataType(String dataType) {
        currentDataType = dataType;
        dataTypeComboBox.setSelectedItem(dataType);
        setDocType(dataType);
    }

    private void setDocType(String dataType) {
        TokenMarker tokenMarker = LoadedExtensions.getInstance().getDataTypePlugins().get(PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataType)).getTokenMarker();
        
        if (tokenMarker != null) {
            hl7Document.setTokenMarker(tokenMarker);
        }
        
        pasteBox.setDocument(hl7Document);
    }

    public String getDataType() {
        return (String) dataTypeComboBox.getSelectedItem();
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
        dataTypeComboBox = new javax.swing.JComboBox();
        properties = new javax.swing.JButton();
        pasteBox = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        openFileButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1), "Message Template", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 51, 51))); // NOI18N

        jLabel5.setText("Data Type:");

        dataTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeComboBoxActionPerformed(evt);
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
                        .addComponent(dataTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(dataTypeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openFileButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pasteBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel5)
                .addGap(207, 207, 207))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dataTypeComboBox, openFileButton, properties});

    }// </editor-fold>//GEN-END:initComponents

    private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileButtonActionPerformed
    {//GEN-HEADEREND:event_openFileButtonActionPerformed
        try {
            String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(getDataType());
            DataTypeClientPlugin dataTypePropertiesPlugin = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType);
            if (dataTypePropertiesPlugin.isBinary()) {
                byte[] content = PlatformUI.MIRTH_FRAME.browseForFileBytes(null);
                
                if (content != null) {
                    // The plugin should decide how to convert the byte array to string
                    pasteBox.setText(dataTypePropertiesPlugin.getTemplateString(content));
                }
            } else {
                String content = PlatformUI.MIRTH_FRAME.browseForFileString(null);
                
                if (content != null) {
                    pasteBox.setText(content);
                }
            }

            parent.modified = true;
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertException(this, e.getStackTrace(), "Invalid template file. " + e.getMessage());
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
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        currentMessage = "";
        
        String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get((String) dataTypeComboBox.getSelectedItem());
        
        DataTypeClientPlugin dataTypeClientPlugin = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType);
        Object beanProperties = dataTypeClientPlugin.getBeanProperties();
        
        if (beanProperties != null) {
            new BoundPropertiesSheetDialog(dataProperties, beanProperties, dataTypeClientPlugin.getBeanDimensions());
        }
        
        updateText();
    }//GEN-LAST:event_propertiesActionPerformed

    private void dataTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeComboBoxActionPerformed
    {//GEN-HEADEREND:event_dataTypeComboBoxActionPerformed
        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        currentMessage = "";
        
        // Only conditionally enable the properties if the data type is enabled.
        if (dataTypeComboBox.isEnabled()) {
            String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get((String) dataTypeComboBox.getSelectedItem());
            properties.setEnabled(LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getBeanProperties() != null);
        }
        
        // Only set the default properties if the data type is changing
        if (!currentDataType.equals(dataTypeComboBox.getSelectedItem())) {
            // Set the default properties for the data type selected
            for (String dataType : LoadedExtensions.getInstance().getDataTypePlugins().keySet()) {
                if (PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(dataType).equals(dataTypeComboBox.getSelectedItem())) {
                    dataProperties = MapUtils.toProperties(LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties());
                }
            }
        }
        
        setDocType((String) dataTypeComboBox.getSelectedItem());
        updateText();
    }//GEN-LAST:event_dataTypeComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dataTypeComboBox;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton openFileButton;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea pasteBox;
    private javax.swing.JButton properties;
    // End of variables declaration//GEN-END:variables
}
