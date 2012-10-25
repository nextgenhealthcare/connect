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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.components.MirthComboBoxCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;

public class CustomMetaDataDialog extends javax.swing.JDialog {

    private Frame parent;
    private final String METADATA_NAME_COLUMN_NAME = "Column Name";
    private final String METADATA_TYPE_COLUMN_NAME = "Type";
    private final String METADATA_MAPPING_COLUMN_NAME = "Variable Mapping";
    
    public static final int DATA_TYPE_COLUMN_NUMBER = 1;

    public CustomMetaDataDialog() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initMetaDataTable();
        
        updateMetaDataTable();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                updateMetaDataColumns();
            }
            
        });
        
        setVisible(true);
    }
    
    private void initMetaDataTable() {        
        metaDataTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableModel model = new DefaultTableModel(new Object [][] {}, new String[] { METADATA_NAME_COLUMN_NAME, METADATA_TYPE_COLUMN_NAME, METADATA_MAPPING_COLUMN_NAME }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
            
            public void setValue(Object value, int row, int column) {
                if (getColumnName(column).equals(METADATA_NAME_COLUMN_NAME)) {
                    
                } else if (getColumnName(column).equals(METADATA_MAPPING_COLUMN_NAME)) {
                    
                }
                
                super.setValueAt(value, row, column);
            }
        };
        
        model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    parent.setSaveEnabled(true);
                }
            }
            
        });
        
        class AlphaNumericCellEditor extends TextFieldCellEditor {
            
            public AlphaNumericCellEditor() {
                super();
                MirthFieldConstraints constraints = new MirthFieldConstraints("^[a-zA-Z_0-9]*$");
                constraints.setLimit(30);
                getTextField().setDocument(constraints);
            }

            @Override
            protected boolean valueChanged(String value) {
                return true;
            }
            
        }
        
        metaDataTable.setSortable(false);
        metaDataTable.getTableHeader().setReorderingAllowed(false);
        metaDataTable.setModel(model);
        
        metaDataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteMetaDataButton.setEnabled(metaDataTable.getSelectedRow() != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            metaDataTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }
        
        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_NAME_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());
        metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_MAPPING_COLUMN_NAME)).setCellEditor(new AlphaNumericCellEditor());
        
        TableColumn column = metaDataTable.getColumnModel().getColumn(metaDataTable.getColumnModel().getColumnIndex(METADATA_TYPE_COLUMN_NAME));
        column.setCellEditor(new DefaultCellEditor(new JComboBox(MetaDataColumnType.values())));
        column.setCellRenderer(new MirthComboBoxCellRenderer(MetaDataColumnType.values()));
        column.setMinWidth(100);
        column.setMaxWidth(100);
        
        deleteMetaDataButton.setEnabled(false);
    }
    
    private void updateMetaDataTable() {
        DefaultTableModel model = (DefaultTableModel)metaDataTable.getModel();
        model.setNumRows(0);

        for (MetaDataColumn column : parent.channelEditPanel.currentChannel.getProperties().getMetaDataColumns()) {
            model.addRow(new Object[]{column.getName(), column.getType(), column.getMappingName()});
        }
    }
    
    public void updateMetaDataColumns() {
        DefaultTableModel model = (DefaultTableModel)metaDataTable.getModel();
        
        List<MetaDataColumn> metaDataColumns = parent.channelEditPanel.currentChannel.getProperties().getMetaDataColumns();
        metaDataColumns.clear();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            MetaDataColumn column = new MetaDataColumn();
            column.setName((String) model.getValueAt(i, 0));
            column.setType((MetaDataColumnType) model.getValueAt(i, 1));
            column.setMappingName((String)model.getValueAt(i, 2));
            
            metaDataColumns.add(column);
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
        closeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        metaDataTablePane = new javax.swing.JScrollPane();
        metaDataTable = new com.mirth.connect.client.ui.components.MirthTable();
        deleteMetaDataButton = new javax.swing.JButton();
        addMetaDataButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set MetaData");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom MetaData"));

        metaDataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        metaDataTablePane.setViewportView(metaDataTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(metaDataTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(metaDataTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        deleteMetaDataButton.setText("Delete");
        deleteMetaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaDataButtonActionPerformed(evt);
            }
        });

        addMetaDataButton.setText("New");
        addMetaDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaDataButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(deleteMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addMetaDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(addMetaDataButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteMetaDataButton)
                        .addGap(0, 257, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        jPanel2.getAccessibleContext().setAccessibleName("MetaData Pane");

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

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
    this.dispose();
}//GEN-LAST:event_closeButtonActionPerformed

    private void addMetaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMetaDataButtonActionPerformed
        DefaultTableModel model = ((DefaultTableModel) metaDataTable.getModel());
        int row = model.getRowCount();
        
        model.addRow(new Object[]{"", MetaDataColumnType.STRING, ""});
        
        metaDataTable.setRowSelectionInterval(row, row);
        
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_addMetaDataButtonActionPerformed

    private void deleteMetaDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMetaDataButtonActionPerformed
        int selectedRow = metaDataTable.getSelectedRow();

        if (selectedRow != -1 && !metaDataTable.isEditing()) {
            ((DefaultTableModel) metaDataTable.getModel()).removeRow(selectedRow);
        }
        
        int rowCount = metaDataTable.getRowCount();
        
        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }
            
            metaDataTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
        
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_deleteMetaDataButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMetaDataButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteMetaDataButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.components.MirthTable metaDataTable;
    private javax.swing.JScrollPane metaDataTablePane;
    // End of variables declaration//GEN-END:variables
}
