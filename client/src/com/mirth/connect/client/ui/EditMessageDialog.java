/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.TokenMarker;

import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.donkey.model.message.RawMessage;

public class EditMessageDialog extends MirthDialog implements DropTargetListener {

    private Frame parent;
    private String channelId;
    private String dataType;

    public EditMessageDialog() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();
        new DropTarget(messageContent, this);
    }

    /**
     * 
     * @param message
     * @param dataType
     * @param channelId
     * @param selectedMetaDataIds
     *            The connectors that will be pre-selected for processing the message. If null, all
     *            connectors will be pre-selected.
     */
    public void setPropertiesAndShow(String message, String dataType, String channelId, Map<Integer, String> destinationConnectors, List<Integer> selectedMetaDataIds, Map<String, Object> sourceMap) {
        this.channelId = channelId;
        setMessage(message, dataType);

        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        initDestinationConnectorTable(destinationConnectors, selectedMetaDataIds);
        initSourceMapTable(sourceMap);
        setVisible(true);
    }

    public String getDataType() {
        return dataType;
    }

    public void setMessage(String message, String dataType) {
        this.dataType = dataType;
        setCorrectDocument(messageContent, message, dataType);
        messageContent.setCaretPosition(0);
    }

    private void initDestinationConnectorTable(Map<Integer, String> destinationConnectors, List<Integer> selectedMetaDataIds) {
        mirthTable1 = new ItemSelectionTable();
        mirthTable1.setModel(new ItemSelectionTableModel<Integer, String>(destinationConnectors, selectedMetaDataIds, "Destination", "Included"));
        jScrollPane1.setViewportView(mirthTable1);
    }

    private void initSourceMapTable(Map<String, Object> sourceMap) {
        Object[][] data = new Object[sourceMap.size()][2];
        int i = 0;

        for (Entry<String, Object> entry : sourceMap.entrySet()) {
            data[i][0] = entry.getKey();
            data[i][1] = entry.getValue();
            i++;
        }

        sourceMapTable = new MirthTable();

        sourceMapTable.setModel(new RefreshTableModel(data, new Object[] { "Variable", "Value" }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        });

        sourceMapTable.setDragEnabled(false);
        sourceMapTable.setRowSelectionAllowed(true);
        sourceMapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceMapTable.setRowHeight(UIConstants.ROW_HEIGHT);
        sourceMapTable.setFocusable(false);
        sourceMapTable.setOpaque(true);
        sourceMapTable.getTableHeader().setResizingAllowed(false);
        sourceMapTable.getTableHeader().setReorderingAllowed(false);
        sourceMapTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            sourceMapTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        class SourceMapTableCellEditor extends AbstractCellEditor implements TableCellEditor {
            private JTable table;
            private int column;
            private JTextField textField;
            private Object originalValue;
            private String newValue;

            public SourceMapTableCellEditor(JTable table, int column) {
                super();
                this.table = table;
                this.column = column;
                textField = new JTextField();
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        textField.setCaretPosition(textField.getText().length());
                    }
                });
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt == null) {
                    return false;
                }
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                originalValue = value;
                newValue = null;
                textField.setText(String.valueOf(value));
                return textField;
            }

            @Override
            public Object getCellEditorValue() {
                if (newValue != null) {
                    return newValue;
                } else {
                    return originalValue;
                }
            }

            @Override
            public boolean stopCellEditing() {
                if (!valueChanged()) {
                    super.cancelCellEditing();
                } else {
                    newValue = textField.getText();
                }
                return super.stopCellEditing();
            }

            private boolean valueChanged() {
                String value = textField.getText();
                if (StringUtils.isBlank(value)) {
                    return false;
                }

                for (int i = 0; i < table.getRowCount(); i++) {
                    Object tableValue = table.getValueAt(i, column);
                    if (tableValue != null && String.valueOf(tableValue).equals(value)) {
                        return false;
                    }
                }

                return true;
            }
        }

        sourceMapTable.getColumnModel().getColumn(0).setCellEditor(new SourceMapTableCellEditor(sourceMapTable, 0));
        sourceMapTable.getColumnModel().getColumn(1).setCellEditor(new SourceMapTableCellEditor(sourceMapTable, 1));

        sourceMapScrollPane.setViewportView(sourceMapTable);
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

    private String getNewSourceMapKey() {
        String key;
        int num = 1;

        do {
            key = "key" + num++;
        } while (sourceMapKeyExists(key));

        return key;
    }

    private boolean sourceMapKeyExists(String key) {
        for (int row = 0; row < sourceMapTable.getRowCount(); row++) {
            if (key.equals(sourceMapTable.getValueAt(row, 0))) {
                return true;
            }
        }
        return false;
    }

    private int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    private int getSelectedColumn(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingColumn();
        } else {
            return table.getSelectedColumn();
        }
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
        newButton = new javax.swing.JButton();
        processMessageButton = new javax.swing.JButton();
        messageContent = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthTable1 = new com.mirth.connect.client.ui.components.MirthTable();
        jLabel1 = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        openBinaryFileButton = new javax.swing.JButton();
        openTextFileButton = new javax.swing.JButton();
        sourceMapLabel = new javax.swing.JLabel();
        sourceMapScrollPane = new javax.swing.JScrollPane();
        sourceMapTable = new com.mirth.connect.client.ui.components.MirthTable();
        closeButton1 = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Message");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        newButton.setText("New");
        newButton.setToolTipText("Close this message sender dialog.");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
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

        jScrollPane1.setViewportView(mirthTable1);

        jLabel1.setText("Send to the following destination(s):");

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));

        openBinaryFileButton.setText("Open Binary File...");
        openBinaryFileButton.setToolTipText("<html>Open a binary file into the editor above.<br>The file will be encoded and displayed as Base64.</html>");
        openBinaryFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBinaryFileButtonActionPerformed(evt);
            }
        });

        openTextFileButton.setText("Open Text File...");
        openTextFileButton.setToolTipText("Open a text file into the editor above.");
        openTextFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTextFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(openTextFileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openBinaryFileButton))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(openBinaryFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(openTextFileButton))
        );

        sourceMapLabel.setText("Include the following source map variables:");

        sourceMapScrollPane.setViewportView(sourceMapTable);

        closeButton1.setText("Close");
        closeButton1.setToolTipText("Close this message sender dialog.");
        closeButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Close this message sender dialog.");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageContent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sourceMapLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(sourceMapScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(processMessageButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeButton1)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageContent, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceMapLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceMapScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processMessageButton)
                    .addComponent(closeButton1))
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

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) sourceMapTable.getModel()).addRow(new Object[] { getNewSourceMapKey(),
                "" });
        sourceMapTable.setRowSelectionInterval(sourceMapTable.getRowCount() - 1, sourceMapTable.getRowCount() - 1);
        sourceMapTable.scrollRowToVisible(sourceMapTable.getRowCount() - 1);
    }//GEN-LAST:event_newButtonActionPerformed

    private void processMessageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_processMessageButtonActionPerformed
    {//GEN-HEADEREND:event_processMessageButtonActionPerformed
        ItemSelectionTableModel<Integer, String> model = (ItemSelectionTableModel<Integer, String>) mirthTable1.getModel();
        List<Integer> metaDataIds = model.getKeys(true);

        if (metaDataIds.size() == model.getRowCount()) {
            metaDataIds = null;
        }

        if (sourceMapTable.isEditing()) {
            sourceMapTable.getCellEditor(getSelectedRow(sourceMapTable), getSelectedColumn(sourceMapTable)).stopCellEditing();
        }

        Map<String, Object> sourceMap = new HashMap<String, Object>();
        for (int row = 0; row < sourceMapTable.getRowCount(); row++) {
            String key = (String) sourceMapTable.getModel().getValueAt(row, 0);
            Object value = sourceMapTable.getModel().getValueAt(row, 1);
            sourceMap.put(key, value);
        }

        parent.processMessage(channelId, new RawMessage(messageContent.getText(), metaDataIds, sourceMap));
        this.dispose();
    }//GEN-LAST:event_processMessageButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int selectedRow = getSelectedRow(sourceMapTable);
        if (selectedRow != -1 && !sourceMapTable.isEditing()) {
            ((DefaultTableModel) sourceMapTable.getModel()).removeRow(selectedRow);

            if (sourceMapTable.getRowCount() > 0) {
                if (selectedRow >= sourceMapTable.getRowCount()) {
                    sourceMapTable.setRowSelectionInterval(sourceMapTable.getRowCount() - 1, sourceMapTable.getRowCount() - 1);
                } else {
                    sourceMapTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton1;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea messageContent;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable1;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openBinaryFileButton;
    private javax.swing.JButton openTextFileButton;
    private javax.swing.JButton processMessageButton;
    private javax.swing.JLabel sourceMapLabel;
    private javax.swing.JScrollPane sourceMapScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable sourceMapTable;
    // End of variables declaration//GEN-END:variables
}
