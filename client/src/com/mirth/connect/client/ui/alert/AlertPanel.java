package com.mirth.connect.client.ui.alert;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.alert.AlertModel;

public class AlertPanel extends JPanel {

    private Frame parent;
    private static final String STATUS_COLUMN_NAME = "Status";
    private static final String NAME_COLUMN_NAME = "Name";
    private static final String ID_COLUMN_NAME = "Id";
    private static final String ENABLED_STATUS = "Enabled";
    
    public AlertPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
        makeAlertTable();
    }
    
    private void makeAlertTable() {
        
        alertTable.setModel(new RefreshTableModel(null, new String[]{ STATUS_COLUMN_NAME, NAME_COLUMN_NAME, ID_COLUMN_NAME}) {

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        
        
        alertTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        alertTable.setHorizontalScrollEnabled(true);
        
        // Must set the maximum width on columns that should be packed.
        alertTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MIN_WIDTH);
        alertTable.getColumnExt(STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        alertTable.getColumnExt(STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        alertTable.getColumnExt(STATUS_COLUMN_NAME).setToolTipText("<html><body>The status of this channel. Possible values are enabled and disabled.<br>Only enabled channels can be deployed.</body></html>");

        alertTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(150);
        alertTable.getColumnExt(NAME_COLUMN_NAME).setToolTipText("<html><body>The name of this channel.</body></html>");

        alertTable.getColumnExt(ID_COLUMN_NAME).setMinWidth(215);
        alertTable.getColumnExt(ID_COLUMN_NAME).setMaxWidth(215);
        alertTable.getColumnExt(ID_COLUMN_NAME).setToolTipText("<html><body>The unique id of this channel.</body></html>");

        alertTable.packTable(UIConstants.COL_MARGIN);
        
        alertTable.setRowHeight(UIConstants.ROW_HEIGHT);
        alertTable.setOpaque(true);
        alertTable.setRowSelectionAllowed(true);
        
        alertTable.setSortable(true);
        
        // Sort by Channel Name column
        alertTable.getRowSorter().toggleSortOrder(alertTable.getColumnModelIndex(NAME_COLUMN_NAME));

        alertTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                AlertListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        alertTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (alertTable.rowAtPoint(new Point(evt.getX(), evt.getY())) == -1) {
                    return;
                }

                if (evt.getClickCount() >= 2) {
                    parent.doEditAlert();
                }
            }
        });

        // Key Listener trigger for DEL
        alertTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteAlert();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            alertTable.setHighlighters(highlighter);
        }
    }
    
    public void updateAlertTable(List<AlertModel> alerts) {
        Object[][] tableData = null;
        
        if (alerts != null) {
            tableData = new Object[alerts.size()][3];
            
            for (int i = 0; i < alerts.size(); i++) {
                AlertModel alert = alerts.get(i);
                
                if (alert.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }
                tableData[i][1] = alert.getName();
                tableData[i][2] = alert.getId();
            }
        }

        RefreshTableModel model = (RefreshTableModel) alertTable.getModel();
        model.refreshDataVector(tableData);
        if (tableData.length == 0) {
            alertTable.clearSelection();
        }
    }
    
    /** The action called when an alert is selected. Sets tasks as well. */
    private void AlertListSelected(ListSelectionEvent evt) {
        int[] rows = alertTable.getSelectedModelRows();
        int column = alertTable.getColumnModelIndex(STATUS_COLUMN_NAME);

        if (rows.length > 0) {
            parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 3, -1, true);

            if (rows.length > 1) {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 7, false); // hide edit, enable, and disable
            } else {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 7, false); // hide enable and disable
            }

            for (int i = 0; i < rows.length; i++) {
                if (((CellData) alertTable.getModel().getValueAt(rows[i], column)).getText().equals(ENABLED_STATUS)) {
                    parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, true); // show disable if any selected are enabled
                } else {
                    parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 6, true); // show enable if any selected are disabled
                }
            }
        }
    }
    
    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = alertTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                if (!alertTable.isRowSelected(row)) {
                    alertTable.setRowSelectionInterval(row, row);
                }
            }
            parent.alertPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    private void deselectRows() {
        alertTable.clearSelection();
        parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 0, -1, true);
        parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 4, -1, false);
    }
    
    public List<String> getSelectedAlertIds() {
        int[] selectedRows = alertTable.getSelectedModelRows();
        List<String> selectedAlertIds = new ArrayList<String>();
        for (int i = 0; i < selectedRows.length; i++) {
            String alertId = (String) alertTable.getModel().getValueAt(selectedRows[i], alertTable.getColumnModelIndex(ID_COLUMN_NAME));
            selectedAlertIds.add(alertId);
        }

        return selectedAlertIds;
    }
    
    public void setSelectedAlertIds(List<String> alertIds) {
        TableModel model = alertTable.getModel();
        int rowCount = model.getRowCount();
        int idColumn = alertTable.getColumnModelIndex(ID_COLUMN_NAME);
        
        for (String alertId : alertIds) {
            for (int i = 0; i < rowCount; i++) {
                if (alertId.equals(model.getValueAt(i, idColumn))) {
                    int row = alertTable.convertRowIndexToView(i);
                    alertTable.addRowSelectionInterval(row, row);
                }
            }
        }
    }
    
    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new MigLayout("fill, insets 0"));
        
        
        alertTable = new MirthTable();
        
        alertPane = new JScrollPane(alertTable);
        alertPane.setBorder(BorderFactory.createEmptyBorder());
        
        
        add(alertPane, "grow");
    }
    
    private JScrollPane alertPane;
    private MirthTable alertTable;
}
