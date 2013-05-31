package com.mirth.connect.client.ui.alert;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.NumberCellComparator;
import com.mirth.connect.client.ui.NumberCellRenderer;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.alert.AlertStatus;

public class DefaultAlertPanel extends AlertPanel {

    private Frame parent;
    private static final String STATUS_COLUMN_NAME = "Status";
    private static final String NAME_COLUMN_NAME = "Name";
    private static final String ID_COLUMN_NAME = "Id";
    private static final String ALERTED_COLUMN_NAME = "Alerted";
    private static final String ENABLED_STATUS = "Enabled";
    private static final int NAME_COLUMN_NUMBER = 1;
    private static final int ID_COLUMN_NUMBER = 2;

    public DefaultAlertPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        makeAlertTable();
    }

    private void makeAlertTable() {

        alertTable.setModel(new RefreshTableModel(null, new String[] { STATUS_COLUMN_NAME,
                NAME_COLUMN_NAME, ID_COLUMN_NAME, ALERTED_COLUMN_NAME }) {

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
        alertTable.getColumnExt(STATUS_COLUMN_NAME).setToolTipText("<html><body>The status of this alert. Possible values are enabled and disabled.</body></html>");

        alertTable.getColumnExt(NAME_COLUMN_NAME).setMinWidth(150);
        alertTable.getColumnExt(NAME_COLUMN_NAME).setToolTipText("<html><body>The name of this alert.</body></html>");

        alertTable.getColumnExt(ID_COLUMN_NAME).setMinWidth(215);
        alertTable.getColumnExt(ID_COLUMN_NAME).setMaxWidth(215);
        alertTable.getColumnExt(ID_COLUMN_NAME).setToolTipText("<html><body>The unique id of this alert.</body></html>");
        
        alertTable.getColumnExt(ALERTED_COLUMN_NAME).setCellRenderer(new NumberCellRenderer());
        alertTable.getColumnExt(ALERTED_COLUMN_NAME).setComparator(new NumberCellComparator());
        alertTable.getColumnExt(ALERTED_COLUMN_NAME).setMaxWidth(UIConstants.MIN_WIDTH);
        alertTable.getColumnExt(ALERTED_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        alertTable.getColumnExt(ALERTED_COLUMN_NAME).setToolTipText("<html><body>The number of times alerts have been sent.</body></html>");

        alertTable.packTable(UIConstants.COL_MARGIN);

        alertTable.setRowHeight(UIConstants.ROW_HEIGHT);
        alertTable.setOpaque(true);
        alertTable.setRowSelectionAllowed(true);

        alertTable.setSortable(true);
        // Sort by Alert Name column
        alertTable.getRowSorter().toggleSortOrder(alertTable.getColumnModelIndex(NAME_COLUMN_NAME));

        class CustomTransferHandler extends TransferHandler {

            @Override
            protected Transferable createTransferable(JComponent c) {
                MirthTable table = (MirthTable) c;
                int[] rows = table.getSelectedModelRows();

                // Don't put anything on the clipboard if no rows are selected
                if (rows.length == 0) {
                    return null;
                }

                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < rows.length; i++) {
                    builder.append(table.getModel().getValueAt(rows[i], NAME_COLUMN_NUMBER));
                    builder.append(" (");
                    builder.append(table.getModel().getValueAt(rows[i], ID_COLUMN_NUMBER));
                    builder.append(")");

                    if (i != rows.length - 1) {
                        builder.append("\n");
                    }
                }

                return new StringSelection(builder.toString());
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (canImport(support)) {
                    try {
                        List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        boolean showAlerts = (fileList.size() == 1);

                        for (File file : fileList) {
                            if (FilenameUtils.isExtension(file.getName(), "xml")) {
                                parent.importAlert(parent.readFileToString(file), showAlerts);
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        // Let it return false
                    }
                }

                return false;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                        for (File file : fileList) {
                            if (!FilenameUtils.isExtension(file.getName(), "xml")) {
                                return false;
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        // Return true anyway until this bug is fixed:
                        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6759788
                        return true;
                    }
                }

                return false;
            }
        }

        alertTable.setDragEnabled(true);
        alertTable.setDropMode(DropMode.ON);
        alertTable.setTransferHandler(new CustomTransferHandler());

        alertTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                AlertListSelected(evt);
            }
        });

        // listen for trigger button and double click to edit alert.
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

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            alertTable.setHighlighters(highlighter);
        }
    }

    @Override
    public void updateAlertTable(List<AlertStatus> alertStatusList) {
        Object[][] tableData = null;

        if (alertStatusList != null) {
            tableData = new Object[alertStatusList.size()][4];

            for (int i = 0; i < alertStatusList.size(); i++) {
                AlertStatus alertStatus = alertStatusList.get(i);

                if (alertStatus.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }
                tableData[i][1] = alertStatus.getName();
                tableData[i][2] = alertStatus.getId();
                
                if (alertStatus.getAlertedCount() != null) {
                    tableData[i][3] = alertStatus.getAlertedCount();
                }
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
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 4, 4, false); // Hide export
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 8, false); // hide edit, enable, and disable
            } else {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 8, false); // hide enable and disable
            }

            for (int i = 0; i < rows.length; i++) {
                if (((CellData) alertTable.getModel().getValueAt(rows[i], column)).getText().equals(ENABLED_STATUS)) {
                    parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 8, 8, true); // show disable if any selected are enabled
                } else {
                    parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, true); // show enable if any selected are disabled
                }
            }
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed. Deselects the rows if no row was selected.
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

    @Override
    public Map<String, String> getAlertNames() {
        Map<String, String> alertNames = new HashMap<String, String>();
        for (int i = 0; i < alertTable.getRowCount(); i++) {
            String alertId = (String) alertTable.getModel().getValueAt(i, alertTable.getColumnModelIndex(ID_COLUMN_NAME));
            String alertName = (String) alertTable.getModel().getValueAt(i, alertTable.getColumnModelIndex(NAME_COLUMN_NAME));
            alertNames.put(alertId, alertName);
        }

        return alertNames;
    }

    @Override
    public List<String> getSelectedAlertIds() {
        int[] selectedRows = alertTable.getSelectedModelRows();
        List<String> selectedAlertIds = new ArrayList<String>();
        for (int i = 0; i < selectedRows.length; i++) {
            String alertId = (String) alertTable.getModel().getValueAt(selectedRows[i], alertTable.getColumnModelIndex(ID_COLUMN_NAME));
            selectedAlertIds.add(alertId);
        }

        return selectedAlertIds;
    }

    @Override
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
