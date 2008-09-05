/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */
package com.webreach.mirth.connectors.jdbc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.connectors.ConnectorClass;
import java.util.HashMap;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class DatabaseMetadataDialog extends javax.swing.JDialog {
    private Frame parent;
    private ConnectorClass parentConnector;
    private STATEMENT_TYPE type;
    private Properties connectionProperties = null;
    private final String INCLUDED_COLUMN_NAME_COLUMN_NAME = "Table/Column Name";
    private final String INCLUDED_STATUS_COLUMN_NAME = "Include";
    private final String INCLUDED_TYPE_COLUMN_NAME = "Type";
    private final String TABLE_TYPE_COLUMN = "table";
    private final String COLUMN_TYPE_COLUMN = "column";
    
    public enum STATEMENT_TYPE {SELECT_TYPE, UPDATE_TYPE, INSERT_TYPE};

    /**
     * Creates new form ViewContentDialog
     */
    public DatabaseMetadataDialog(ConnectorClass parentConnector, STATEMENT_TYPE type, Properties connectionProperties) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.parentConnector = parentConnector;
        this.type = type;
        initComponents();
        this.connectionProperties = connectionProperties;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        pack();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        Map<String, Object> metaData = null;
        
        try { 
    		metaData = (Map<String, Object>) parent.mirthClient.invokeConnectorService("Database Reader", "getInformationSchema", connectionProperties);
        } catch (ClientException e) {
        	parent.alertError(parent, "Could not retrieve database metadata.  Please ensure that your driver, URL, username, and password are correct.");
        	return;
        }
        
        makeIncludedDestinationsTable(metaData);
        generateButton.requestFocus();
        generateButton.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                // TODO Auto-generated method stub
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    generateButtonActionPerformed(null);
                }
            }

            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub
            }
        });
        setVisible(true);
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeIncludedDestinationsTable(Map<String, Object> metaData) {
        updateIncludedDestinationsTable(metaData);

        includedMetaDataTable.setDragEnabled(false);
        includedMetaDataTable.setRowSelectionAllowed(false);
        includedMetaDataTable.setRowHeight(UIConstants.ROW_HEIGHT);
        includedMetaDataTable.setFocusable(false);
        includedMetaDataTable.setOpaque(true);
        includedMetaDataTable.getTableHeader().setReorderingAllowed(false);
        includedMetaDataTable.setSortable(false);

        includedMetaDataTable.getColumnExt(INCLUDED_STATUS_COLUMN_NAME).setMaxWidth(50);
        includedMetaDataTable.getColumnExt(INCLUDED_STATUS_COLUMN_NAME).setMinWidth(50);
        includedMetaDataTable.getColumnExt(INCLUDED_TYPE_COLUMN_NAME).setVisible(false);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            includedMetaDataTable.setHighlighters(highlighter);
        }

        includedMetaDataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
            }
        });

        includedDestinationsPane.setViewportView(includedMetaDataTable);

        // Key Listener trigger for CTRL-S
        includedMetaDataTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                    PlatformUI.MIRTH_FRAME.doSaveAlerts();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        
     // Mouse listener for trigger-button popup on the table.
        includedMetaDataTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                //showAlertPopupMenu(evt, true);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                checkTableNameSelected(evt);
            }
        });

    }

    private void checkTableNameSelected(java.awt.event.MouseEvent evt)
    {
        if (!evt.isPopupTrigger())
        {
            int row = includedMetaDataTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
            int column = includedMetaDataTable.columnAtPoint(new Point(evt.getX(), evt.getY()));
            
            if(row != -1 && column == 0) {
            	String type = (String) includedMetaDataTable.getModel().getValueAt(row, 2);
            	Boolean selected = (Boolean) includedMetaDataTable.getModel().getValueAt(row, 0);
            	
                if(type.equals(TABLE_TYPE_COLUMN)) { 
                	RefreshTableModel model = (RefreshTableModel) includedMetaDataTable.getModel();
                    boolean nextTableFound = false;
                    int tableLength = model.getRowCount();
                	int endRow = -1;
                    for(int i = row+1; !nextTableFound && i != tableLength; i++) {
                		String nextType = (String) includedMetaDataTable.getModel().getValueAt(i, 2);
                		if(nextType.equals(TABLE_TYPE_COLUMN)) {
                			endRow = i;
                			nextTableFound = true;
                		} else if (i+1 == tableLength) { 
                			endRow = i+1;
                		}
                	}
                    	
                    if(endRow == -1) { 
                    	return;
                    }
                    
                	for(int i = row+1; i < endRow; i++) { 
                		model.setValueAt(selected, i, 0);
                	}
                }
            }
        }
    }
    
    public void updateIncludedDestinationsTable(Map<String,Object> metaData) {
        Object[][] tableData = null;
        int tableSize = 0;

        if (metaData != null) {
            for(Object o : metaData.values()) { 
                tableSize++;
                List<String> l = (List<String>) o;
                tableSize += l.size();
            }
        	
            tableData = new Object[tableSize][3];
            int i = 0;
            Iterator iterator = metaData.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry entry = (Entry) iterator.next();
               
                tableData[i][0] = Boolean.FALSE;
                tableData[i][1] = "<html><b>" + entry.getKey() + "</b></html>";
                tableData[i][2] = TABLE_TYPE_COLUMN;
                i++;
                
               	List<String> columns = (List<String>) metaData.get(entry.getKey());
            
               	for(String column : columns) { 
                    tableData[i][0] = Boolean.FALSE;
                    tableData[i][1] = "     " + column;
                    tableData[i][2] = COLUMN_TYPE_COLUMN;
                    i++;
               	}
            }
        }

        if (metaData != null && includedMetaDataTable != null) {
            RefreshTableModel model = (RefreshTableModel) includedMetaDataTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            includedMetaDataTable = new MirthTable();
            includedMetaDataTable.setModel(new RefreshTableModel(tableData, new String[]{INCLUDED_STATUS_COLUMN_NAME, INCLUDED_COLUMN_NAME_COLUMN_NAME, INCLUDED_TYPE_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{true, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }
    
    public String createQueryFromMetaData(Map<String, Object> metaData) { 
        String query = "";
        
        if (metaData != null) {
            query += "SELECT ";
            int i = 0;
            
            Iterator iterator = metaData.entrySet().iterator();
            while (iterator.hasNext()) {
            	Entry entry = (Entry) iterator.next();
            	String tableName = (String)entry.getKey();
                List<String> columns = (List<String>) metaData.get(tableName);
                
                for(String column : columns) { 
                    if(i != 0) { 
                        query += ", ";
                    }
                    query += tableName + "." + column.trim() + " AS " + tableName + "_" + column.trim();
                    i++;                          
                }
            }
            
            query += "\nFROM ";

            iterator = metaData.entrySet().iterator();
            i = 0;
            while (iterator.hasNext())
            {
                Entry entry = (Entry) iterator.next();
               	if(i != 0) { 
                    query += ", ";
                }
                query += entry.getKey();
                i++;
            }
        }
        
        return query;
    }
    
    public List<String> createInsertFromMetaData(Map<String, Object> metaData) { 
        List<String> insertStatements = new LinkedList<String>();
    	
        if (metaData != null) {           
            Iterator iterator = metaData.entrySet().iterator();
            
            while (iterator.hasNext())
            {
            	String statement = "INSERT INTO ";
            	String values = "\nVALUES (";
                Entry entry = (Entry) iterator.next();
                statement += entry.getKey();
                
                List<String> columns = (List<String>) metaData.get(entry.getKey());
                statement += " (";
                int i = 0;
                for(String column : columns) { 
                    if(i != 0) { 
                        statement += ", ";
                        values += ", ";
                    }
                    statement += column.trim();
                    values += "?";
                    i++;                          
                }
                statement += ")";
                values += ")";
                statement += values;
                insertStatements.add(statement);
            }
        }
        
        return insertStatements;
    }
    
    public List<String> createUpdateFromMetaData(Map<String, Object> metaData) { 
    	List<String> insertStatements = new LinkedList<String>();
    	
        if (metaData != null) {           
            Iterator iterator = metaData.entrySet().iterator();
            
            while (iterator.hasNext())
            {
            	String statement = "UPDATE ";
                 Entry entry = (Entry) iterator.next();
                statement += entry.getKey();
                statement += "\nSET ";
                
                List<String> columns = (List<String>) metaData.get(entry.getKey());
                int i = 0;
                for(String column : columns) { 
                    if(i != 0) { 
                        statement += ", ";
                    }
                    statement += column.trim() + " = ?";

                    i++;                          
                }

                insertStatements.add(statement);
            }
        }
        
        return insertStatements;
    }
    
    public Map<String, Object> getSelectedMetaData() {
        Map<String, Object> metaData = new HashMap<String, Object>();
        String currentTableName = "";
        for (int i = 0; i < includedMetaDataTable.getModel().getRowCount(); i++) {
            String type = (String) includedMetaDataTable.getModel().getValueAt(i, 2);
        
            if(type.equals(TABLE_TYPE_COLUMN)) { 
                currentTableName = ((String) includedMetaDataTable.getModel().getValueAt(i, 1)).replaceAll("<html><b>", "").replaceAll("</b></html>", "");
            } else { 
                 if (((Boolean) includedMetaDataTable.getModel().getValueAt(i, 0)).booleanValue()) {
                    if(metaData.get(currentTableName) == null) { 
                        metaData.put(currentTableName, new LinkedList<String>());
                    } 
                    
                    ((List<String>)metaData.get(currentTableName)).add((String) includedMetaDataTable.getModel().getValueAt(i, 1));
                 }
            }
        }
        return metaData;
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
        cancelButton = new javax.swing.JButton();
        generateButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        includedDestinationsPane = new javax.swing.JScrollPane();
        includedMetaDataTable = null;

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SQL Creation");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        generateButton.setText("Generate");
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        includedDestinationsPane.setViewportView(includedMetaDataTable);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, includedDestinationsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(generateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {cancelButton, generateButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(includedDestinationsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(generateButton))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    this.dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
    if(parentConnector instanceof DatabaseReader) { 
    	if(type == STATEMENT_TYPE.SELECT_TYPE) { 
            ((DatabaseReader) parentConnector).setSelectText(createQueryFromMetaData(getSelectedMetaData()));
        } else { 
            ((DatabaseReader) parentConnector).setUpdateText(createUpdateFromMetaData(getSelectedMetaData()));
        }
    } else if(parentConnector instanceof DatabaseWriter) { 
    	((DatabaseWriter) parentConnector).setInsertText(createInsertFromMetaData(getSelectedMetaData()));
    }

    this.dispose();
}//GEN-LAST:event_generateButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton generateButton;
    private javax.swing.JScrollPane includedDestinationsPane;
    private com.webreach.mirth.client.ui.components.MirthTable includedMetaDataTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
