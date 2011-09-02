/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;

public class DatabaseMetadataDialog extends javax.swing.JDialog {

    private Frame parent;
    private ConnectorClass parentConnector;
    private STATEMENT_TYPE type;
    private Properties connectionProperties = null;
    private final String INCLUDED_COLUMN_NAME_COLUMN_NAME = "Table/Column Name";
    private final String INCLUDED_STATUS_COLUMN_NAME = "Include";
    private final String INCLUDED_TYPE_COLUMN_NAME = "Type";
    private final String INCLUDED_COLUMN_TYPE_NAME = "Column Type";
    private final String TABLE_TYPE_COLUMN = "table";
    private final String COLUMN_TYPE_COLUMN = "column";
    
    private SwingWorker<Void, Void> metaDataWorker = null;

    public enum STATEMENT_TYPE {

        SELECT_TYPE, UPDATE_TYPE, INSERT_TYPE
    };

    public DatabaseMetadataDialog(ConnectorClass parentConnector, STATEMENT_TYPE type, Properties connectionProperties) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.parentConnector = parentConnector;
        this.type = type;
        initComponents();
        this.connectionProperties = connectionProperties;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelButtonActionPerformed(null);
            }
        });
        
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

        makeIncludedDestinationsTable(null);
        filterTableTextField.requestFocus();
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
    public void makeIncludedDestinationsTable(Set<Table> metaData) {
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
        includedMetaDataTable.getColumnExt(INCLUDED_TYPE_COLUMN_NAME).setMinWidth(5);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
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
                boolean isAccelerated = (e.getModifiers() & java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0;
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MIRTH_FRAME.doSaveAlerts();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        // Mouse listener for trigger-button popup on the table.
        includedMetaDataTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkTableNameSelected(evt);
            }
        });

    }

    private void checkTableNameSelected(java.awt.event.MouseEvent evt) {
        if (!evt.isPopupTrigger()) {
            int row = includedMetaDataTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
            int column = includedMetaDataTable.columnAtPoint(new Point(evt.getX(), evt.getY()));

            if (row != -1 && column == 0) {
                String type = (String) includedMetaDataTable.getModel().getValueAt(row, 2);
                Boolean selected = (Boolean) includedMetaDataTable.getModel().getValueAt(row, 0);

                if (type.equals(TABLE_TYPE_COLUMN)) {
                    RefreshTableModel model = (RefreshTableModel) includedMetaDataTable.getModel();
                    boolean nextTableFound = false;
                    int tableLength = model.getRowCount();
                    int endRow = -1;
                    for (int i = row + 1; !nextTableFound && i != tableLength; i++) {
                        String nextType = (String) includedMetaDataTable.getModel().getValueAt(i, 2);
                        if (nextType.equals(TABLE_TYPE_COLUMN)) {
                            endRow = i;
                            nextTableFound = true;
                        } else if (i + 1 == tableLength) {
                            endRow = i + 1;
                        }
                    }

                    if (endRow == -1) {
                        return;
                    }

                    for (int i = row + 1; i < endRow; i++) {
                        model.setValueAt(selected, i, 0);
                    }
                }
            }
        }
    }

    public void updateIncludedDestinationsTable(Set<Table> metaData) {
        Object[][] tableData = null;
        int tableSize = 0;
        
        if (metaData != null) {
            for (Table table : metaData) {
            	int numOfColumns = table.getColumns() != null ? table.getColumns().size() : 0;
                tableSize += 1 + numOfColumns;
            }

            tableData = new Object[tableSize][4];
            int i = 0;
            for (Table table : metaData) {                
                tableData[i][0] = Boolean.FALSE;
                tableData[i][1] = "<html><b>" + table.getName() + "</b></html>";
                tableData[i][2] = TABLE_TYPE_COLUMN;
                tableData[i][3] = "";
                i++;

                List<Column> columns = (List<Column>) table.getColumns();

                for (Column column : columns) {
                    tableData[i][0] = Boolean.FALSE;
                    tableData[i][1] = "     " + column.getName();
                    tableData[i][2] = COLUMN_TYPE_COLUMN;
                    tableData[i][3] = column.getType() + "(" + column.getPrecision() + ")";
                    i++;
                }
            }
        }

        if (metaData != null && includedMetaDataTable != null) {
            RefreshTableModel model = (RefreshTableModel) includedMetaDataTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            includedMetaDataTable = new MirthTable();
            includedMetaDataTable.setModel(new RefreshTableModel(tableData, new String[]{INCLUDED_STATUS_COLUMN_NAME, INCLUDED_COLUMN_NAME_COLUMN_NAME, INCLUDED_TYPE_COLUMN_NAME, INCLUDED_COLUMN_TYPE_NAME}) {

                boolean[] canEdit = new boolean[]{true, false, false, false};

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
                String tableName = (String) entry.getKey();
                List<String> columns = (List<String>) metaData.get(tableName);

                for (String column : columns) {
                    if (i != 0) {
                        query += ", ";
                    }
                    query += tableName + "." + column.trim() + " AS " + tableName + "_" + column.trim();
                    i++;
                }
            }

            query += "\nFROM ";

            iterator = metaData.entrySet().iterator();
            i = 0;
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                if (i != 0) {
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

            while (iterator.hasNext()) {
                String statement = "INSERT INTO ";
                String values = "\nVALUES (";
                Entry entry = (Entry) iterator.next();
                statement += entry.getKey();

                List<String> columns = (List<String>) metaData.get(entry.getKey());
                statement += " (";
                int i = 0;
                for (String column : columns) {
                    if (i != 0) {
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

            while (iterator.hasNext()) {
                String statement = "UPDATE ";
                Entry entry = (Entry) iterator.next();
                statement += entry.getKey();
                statement += "\nSET ";

                List<String> columns = (List<String>) metaData.get(entry.getKey());
                int i = 0;
                for (String column : columns) {
                    if (i != 0) {
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

            if (type.equals(TABLE_TYPE_COLUMN)) {
                currentTableName = ((String) includedMetaDataTable.getModel().getValueAt(i, 1)).replaceAll("<html><b>", "").replaceAll("</b></html>", "");
            } else {
                if (((Boolean) includedMetaDataTable.getModel().getValueAt(i, 0)).booleanValue()) {
                    if (metaData.get(currentTableName) == null) {
                        metaData.put(currentTableName, new LinkedList<String>());
                    }

                    ((List<String>) metaData.get(currentTableName)).add((String) includedMetaDataTable.getModel().getValueAt(i, 1));
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
        tableFilterNamePanel = new javax.swing.JPanel();
        filterByLabel = new javax.swing.JLabel();
        filterTableTextField = new javax.swing.JTextField();
        filterButton = new javax.swing.JButton();

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

        filterByLabel.setText("Filter by:");

        filterTableTextField.setToolTipText("<html>Enter an optional table name filter before querying the <br/>\ndatabase to limit the number of tables returned.<br/>\nExample: rad*,table*test</html>");

        filterButton.setText("Get Tables");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tableFilterNamePanelLayout = new javax.swing.GroupLayout(tableFilterNamePanel);
        tableFilterNamePanel.setLayout(tableFilterNamePanelLayout);
        tableFilterNamePanelLayout.setHorizontalGroup(
            tableFilterNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableFilterNamePanelLayout.createSequentialGroup()
                .addComponent(filterByLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterTableTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterButton))
        );
        tableFilterNamePanelLayout.setVerticalGroup(
            tableFilterNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableFilterNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableFilterNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterByLabel)
                    .addComponent(filterTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(includedDestinationsPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(generateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(tableFilterNamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, generateButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(tableFilterNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(includedDestinationsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(generateButton))
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

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
	    if (metaDataWorker != null) {
	        metaDataWorker.cancel(true);
        }
	    this.dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_generateButtonActionPerformed
		if (parentConnector instanceof DatabaseReader) {
			if (type == STATEMENT_TYPE.SELECT_TYPE) {
				((DatabaseReader) parentConnector).setSelectText(createQueryFromMetaData(getSelectedMetaData()));
			} else {
				((DatabaseReader) parentConnector).setUpdateText(createUpdateFromMetaData(getSelectedMetaData()));
			}
		} else if (parentConnector instanceof DatabaseWriter) {
			((DatabaseWriter) parentConnector).setInsertText(createInsertFromMetaData(getSelectedMetaData()));
		}

		this.dispose();
	}// GEN-LAST:event_generateButtonActionPerformed

	/**
	 * Action to send request to server and attempt to retrieve the tables based on the filter criteria.
	 * 
	 * @param evt Action event triggered
	 */
	private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
		// retrieve the table pattern filter
		connectionProperties.put(DatabaseReaderProperties.DATABASE_TABLE_NAME_PATTERN_EXPRESSION, filterTableTextField.getText());
		
		parent.setWorking("Retrieving tables...", true);
		
		// Cancel any previous workers that had been called.
		if (metaDataWorker != null) {
		    metaDataWorker.cancel(true);
		}
		
		metaDataWorker = new SwingWorker<Void, Void>() {
		    Set<Table> metaData;

            public Void doInBackground() {
                try {
                    // method "getInformationSchema" will return Set<Table>
                    metaData = (Set<Table>) parent.mirthClient.invokeConnectorService("Database Reader", "getInformationSchema", connectionProperties);
                } catch (ClientException e) {
                    // Handle in the done method
                }
                return null;
            }

            public void done() {
                // If the worker was canceled, don't display an error
                if (!isCancelled()) {
	                if (metaData == null) {
	                    parent.alertError(parent, "Could not retrieve database metadata.  Please ensure that your driver, URL, username, and password are correct.");
	                } else {
	                    // format table information into presentation
	                    makeIncludedDestinationsTable(metaData);
	                }
                }
                
                parent.setWorking("", false);
            }
        };

        metaDataWorker.execute();
	}//GEN-LAST:event_filterButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton filterButton;
    private javax.swing.JLabel filterByLabel;
    private javax.swing.JTextField filterTableTextField;
    private javax.swing.JButton generateButton;
    private javax.swing.JScrollPane includedDestinationsPane;
    private com.mirth.connect.client.ui.components.MirthTable includedMetaDataTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel tableFilterNamePanel;
    // End of variables declaration//GEN-END:variables
}
