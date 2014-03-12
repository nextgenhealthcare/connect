/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;

public class DatabaseMetadataDialog extends MirthDialog {
    /**
     * The maximum string length of column aliases in generated SELECT queries. Some databases
     * impose a limit as low as 30.
     */
    private final static int MAX_ALIAS_LENGTH = 30;

    private Frame parent;
    private ConnectorSettingsPanel parentConnector;
    private STATEMENT_TYPE type;
    private DatabaseConnectionInfo databaseConnectionInfo = null;
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

    public DatabaseMetadataDialog(ConnectorSettingsPanel parentConnector, STATEMENT_TYPE type, DatabaseConnectionInfo databaseConnectionInfo) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.parentConnector = parentConnector;
        this.type = type;
        initComponents();
        this.databaseConnectionInfo = databaseConnectionInfo;
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

        makeIncludedMetaDataTable(null);
        filterTableTextField.requestFocus();
        setVisible(true);
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should be added as well.
     */
    public void makeIncludedMetaDataTable(Set<Table> metaData) {
        updateIncludedMetaDataTable(metaData);

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

            public void valueChanged(ListSelectionEvent e) {}
        });

        includedMetaDataPane.setViewportView(includedMetaDataTable);

        // Mouse listener for trigger-button popup on the table.
        includedMetaDataTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {}

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

    public void updateIncludedMetaDataTable(Set<Table> metaData) {
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
            includedMetaDataTable.setModel(new RefreshTableModel(tableData, new String[] {
                    INCLUDED_STATUS_COLUMN_NAME, INCLUDED_COLUMN_NAME_COLUMN_NAME,
                    INCLUDED_TYPE_COLUMN_NAME, INCLUDED_COLUMN_TYPE_NAME }) {

                boolean[] canEdit = new boolean[] { true, false, false, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    /**
     * Generates a SELECT query from a map of table names with column names.
     * 
     * @param metaData
     *            A map of String table names to List<String> column names
     * @return The generated SELECT query
     */
    @SuppressWarnings("unchecked")
    public String createQueryFromMetaData(Map<String, Object> metaData) {
        if (metaData == null) {
            return null;
        }

        Set<String> tables = new LinkedHashSet<String>();
        Set<String> aliases = new LinkedHashSet<String>();
        Set<String> columns = new LinkedHashSet<String>();

        for (Entry<String, Object> entry : metaData.entrySet()) {
            String table = entry.getKey().trim();

            for (String column : (List<String>) entry.getValue()) {
                column = column.trim();
                String alias = table + "_" + column;

                if (alias.length() > MAX_ALIAS_LENGTH) {
                    alias = column;
                }

                if (alias.length() > MAX_ALIAS_LENGTH) {
                    alias = StringUtils.substring(alias, 0, MAX_ALIAS_LENGTH);
                }

                int i = 2;
                String originalAlias = alias;

                /*
                 * If the column alias already exists, then append a counter to the end of the
                 * alias, keeping it under MAX_ALIAS_LENGTH, until we have a new unique alias.
                 */
                while (aliases.contains(alias)) {
                    alias = originalAlias + i;

                    if (alias.length() > MAX_ALIAS_LENGTH) {
                        alias = StringUtils.substring(originalAlias, 0, MAX_ALIAS_LENGTH - String.valueOf(i).length()) + i;
                    }

                    i++;
                }

                tables.add(table);
                aliases.add(alias);
                columns.add(table + "." + column + " AS " + alias);
            }
        }

        return "SELECT " + StringUtils.join(columns, ", ") + "\nFROM " + StringUtils.join(tables, ", ");
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
        includedMetaDataPane = new javax.swing.JScrollPane();
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

        includedMetaDataPane.setViewportView(includedMetaDataTable);

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
                    .addComponent(includedMetaDataPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
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
                .addComponent(includedMetaDataPane, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
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
     * Action to send request to server and attempt to retrieve the tables based on the filter
     * criteria.
     * 
     * @param evt
     *            Action event triggered
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        // retrieve the table pattern filter
        databaseConnectionInfo.setTableNamePatternExpression(filterTableTextField.getText());

        final String workingId = parent.startWorking("Retrieving tables...");

        // Cancel any previous workers that had been called.
        if (metaDataWorker != null) {
            metaDataWorker.cancel(true);
        }

        metaDataWorker = new SwingWorker<Void, Void>() {
            Set<Table> metaData;

            public Void doInBackground() {
                try {
                    // method "getInformationSchema" will return Set<Table>
                    metaData = (Set<Table>) parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), "Database Reader", "getInformationSchema", databaseConnectionInfo);
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
                        makeIncludedMetaDataTable(metaData);
                    }
                }

                parent.stopWorking(workingId);
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
    private javax.swing.JScrollPane includedMetaDataPane;
    private com.mirth.connect.client.ui.components.MirthTable includedMetaDataTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel tableFilterNamePanel;
    // End of variables declaration//GEN-END:variables
}
