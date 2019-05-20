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
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.DriverInfo;

public class DatabaseDriversDialog extends MirthDialog {

    private List<DriverInfo> currentDrivers;
    private boolean saved;

    public DatabaseDriversDialog(Window owner, List<DriverInfo> currentDrivers) {
        super(owner, "Database Drivers", true);

        if (currentDrivers == null) {
            currentDrivers = new ArrayList<DriverInfo>();
        } else {
            currentDrivers = new ArrayList<DriverInfo>(currentDrivers);
        }
        if (currentDrivers.size() > 0) {
            if (StringUtils.equals(currentDrivers.get(0).getName(), DatabaseReceiverProperties.DRIVER_DEFAULT)) {
                currentDrivers.remove(0);
            }
            if (StringUtils.equals(currentDrivers.get(currentDrivers.size() - 1).getName(), DatabaseReceiverProperties.DRIVER_CUSTOM)) {
                currentDrivers.remove(currentDrivers.size() - 1);
            }
        }
        this.currentDrivers = currentDrivers;

        initComponents();
        initToolTips();
        initLayout();

        setDrivers(currentDrivers);

        saveButton.setEnabled(false);
        final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Retrieving database drivers...");
        SwingWorker<List<DriverInfo>, Void> worker = new SwingWorker<List<DriverInfo>, Void>() {
            @Override
            protected List<DriverInfo> doInBackground() throws Exception {
                return PlatformUI.MIRTH_FRAME.mirthClient.getDatabaseDrivers();
            }

            @Override
            protected void done() {
                try {
                    setDrivers(get());
                    saveButton.setEnabled(true);
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                } catch (Exception e) {
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                    PlatformUI.MIRTH_FRAME.alertThrowable(DatabaseDriversDialog.this, e);
                }
            }
        };
        worker.execute();

        setPreferredSize(new Dimension(950, 216));
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public boolean wasSaved() {
        return saved;
    }

    public List<DriverInfo> getDrivers() {
        List<DriverInfo> drivers = new ArrayList<DriverInfo>();

        for (int row = 0; row < driversTable.getModel().getRowCount(); row++) {
            String name = StringUtils.trim((String) driversTable.getModel().getValueAt(row, 0));
            String className = StringUtils.trim((String) driversTable.getModel().getValueAt(row, 1));
            String template = StringUtils.trim((String) driversTable.getModel().getValueAt(row, 2));
            String selectLimit = StringUtils.trim((String) driversTable.getModel().getValueAt(row, 3));
            List<String> alternativeClassNames = new ArrayList<String>(Arrays.asList(StringUtils.split(StringUtils.trim((String) driversTable.getModel().getValueAt(row, 4)), ',')));
            drivers.add(new DriverInfo(name, className, template, selectLimit, alternativeClassNames));
        }

        return drivers;
    }

    private void setDrivers(List<DriverInfo> drivers) {
        if (drivers == null) {
            drivers = new ArrayList<DriverInfo>();
        }

        Object[][] data = new Object[drivers.size()][5];

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo info = drivers.get(i);
            data[i][0] = StringUtils.trim(StringUtils.defaultString(info.getName()));
            data[i][1] = StringUtils.trim(StringUtils.defaultString(info.getClassName()));
            data[i][2] = StringUtils.trim(StringUtils.defaultString(info.getTemplate()));
            data[i][3] = StringUtils.trim(StringUtils.defaultString(info.getSelectLimit()));

            String alternativeClassNamesStr = "";
            List<String> alternativeClassNames = info.getAlternativeClassNames();
            if (CollectionUtils.isNotEmpty(alternativeClassNames)) {
                alternativeClassNamesStr = StringUtils.join(alternativeClassNames, ',');
            }
            data[i][4] = alternativeClassNamesStr;
        }

        ((RefreshTableModel) driversTable.getModel()).refreshDataVector(data);
    }

    private boolean checkDrivers(List<DriverInfo> drivers) {
        String errors = "";

        if (CollectionUtils.isEmpty(drivers)) {
            errors += "You must have at least one driver entry.";
        } else {
            Set<String> names = new HashSet<String>();

            for (int i = 0; i < drivers.size(); i++) {
                DriverInfo driver = drivers.get(i);

                if (StringUtils.isBlank(driver.getName())) {
                    errors += "Row " + (i + 1) + ": Name cannot be blank.\n";
                }
                if (!names.add(driver.getName())) {
                    errors += "Row " + (i + 1) + ": Name must be unique.\n";
                }
                if (StringUtils.isBlank(driver.getClassName())) {
                    errors += "Row " + (i + 1) + ": Driver Class cannot be blank.\n";
                }
                if (StringUtils.isBlank(driver.getTemplate())) {
                    errors += "Row " + (i + 1) + ": JDBC URL Template cannot be blank.\n";
                }
            }
        }

        if (errors.isEmpty()) {
            return true;
        } else {
            PlatformUI.MIRTH_FRAME.alertError(this, errors);
            return false;
        }
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        driversTable = new MirthTable();
        driversTable.setModel(new RefreshTableModel(new Object[] { "Name", "Driver Class",
                "JDBC URL Template", "Select with Limit Query", "Legacy Driver Classes" }, 0));
        driversTable.setDragEnabled(false);
        driversTable.setRowSelectionAllowed(true);
        driversTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driversTable.setRowHeight(UIConstants.ROW_HEIGHT);
        driversTable.setFocusable(true);
        driversTable.setOpaque(true);
        driversTable.getTableHeader().setReorderingAllowed(false);
        driversTable.setEditable(true);
        driversTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            driversTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        driversTable.getSelectionModel().addListSelectionListener(evt -> removeButton.setEnabled(getSelectedRow() >= 0));

        driversTable.getColumnExt(0).setPreferredWidth(101);
        driversTable.getColumnExt(1).setPreferredWidth(162);
        driversTable.getColumnExt(2).setPreferredWidth(269);
        driversTable.getColumnExt(3).setPreferredWidth(200);
        driversTable.getColumnExt(4).setPreferredWidth(137);

        driversScrollPane = new JScrollPane(driversTable);

        buttonPanel = new JPanel();
        buttonPanel.setBackground(getBackground());

        addButton = new JButton("Add");
        addButton.addActionListener(evt -> addDriver());

        removeButton = new JButton("Remove");
        removeButton.addActionListener(evt -> removeDriver());
        removeButton.setEnabled(getSelectedRow() >= 0);

        separator = new JSeparator();

        saveButton = new JButton("Save");
        saveButton.addActionListener(evt -> save());

        closeButton = new JButton("Close");
        closeButton.addActionListener(evt -> close());
        getRootPane().registerKeyboardAction(evt -> close(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void initToolTips() {
        driversTable.getColumnExt(0).setToolTipText("<html>The name of the driver entry. This will appear in the<br/>drop-down menu for the Database Reader/Writer connectors.</html>");
        driversTable.getColumnExt(1).setToolTipText("<html>The fully-qualified Java class name for the JDBC driver.</html>");
        driversTable.getColumnExt(2).setToolTipText("<html>The template for the JDBC connection URL that can be<br/>auto-populated from the Database Reader/Writer settings.</html>");
        driversTable.getColumnExt(3).setToolTipText("<html>A select query (with limit 1) that can be used to<br/>retrieve column metadata. If empty the driver-specific<br/>generic query will be used, which could be slow.</html>");
        driversTable.getColumnExt(4).setToolTipText("<html>A comma-separated list of alternate or legacy JDBC driver class names.<br/>Any Database Reader/Writer connector using one of these driver classes<br/>will have the corresponding entry selected in the Driver drop-down menu.<br/>The driver will be updated to the primary value upon next channel save.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill"));

        add(driversScrollPane, "grow, push");

        buttonPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        buttonPanel.add(addButton, "sg");
        buttonPanel.add(removeButton, "newline, sg");
        add(buttonPanel, "top");

        add(separator, "newline, sx, growx");
        add(saveButton, "newline, sx, right, split 2");
        add(closeButton);
    }

    private void addDriver() {
        int selectedRow = getSelectedRow();
        if (selectedRow >= 0) {
            ((RefreshTableModel) driversTable.getModel()).insertRow(selectedRow + 1, new Object[] {
                    "", "", "", "", "" });
            driversTable.getSelectionModel().setSelectionInterval(selectedRow + 1, selectedRow + 1);
            driversTable.scrollRowToVisible(selectedRow + 1);
        } else {
            ((RefreshTableModel) driversTable.getModel()).addRow(new Object[] { "", "", "", "",
                    "" });
            driversTable.getSelectionModel().setSelectionInterval(driversTable.getRowCount() - 1, driversTable.getRowCount() - 1);
            driversTable.scrollRowToVisible(driversTable.getRowCount() - 1);
        }
    }

    private void removeDriver() {
        int selectedRow = getSelectedRow();
        if (selectedRow >= 0) {
            ((RefreshTableModel) driversTable.getModel()).removeRow(selectedRow);

            if (selectedRow < driversTable.getRowCount()) {
                driversTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            } else if (driversTable.getRowCount() > 0) {
                driversTable.getSelectionModel().setSelectionInterval(driversTable.getRowCount() - 1, driversTable.getRowCount() - 1);
            } else {
                removeButton.setEnabled(false);
            }
        }
    }

    private void save() {
        final List<DriverInfo> drivers = getDrivers();

        if (checkDrivers(drivers)) {
            final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Updating database drivers...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    PlatformUI.MIRTH_FRAME.mirthClient.setDatabaseDrivers(drivers);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                        saved = true;
                        dispose();
                    } catch (Exception e) {
                        PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                        PlatformUI.MIRTH_FRAME.alertThrowable(DatabaseDriversDialog.this, e);
                        saveButton.setEnabled(true);
                    }
                }
            };

            saveButton.setEnabled(false);
            worker.execute();
        }
    }

    private void close() {
        if (Objects.equals(currentDrivers, getDrivers()) || PlatformUI.MIRTH_FRAME.alertOkCancel(this, "<html>The driver information has changed.<br/>Are you sure you want to close without saving?</html>")) {
            dispose();
        }
    }

    private int getSelectedRow() {
        if (driversTable.isEditing()) {
            return driversTable.getEditingRow();
        } else {
            return driversTable.getSelectedRow();
        }
    }

    private MirthTable driversTable;
    private JScrollPane driversScrollPane;
    private JPanel buttonPanel;
    private JButton addButton;
    private JButton removeButton;
    private JSeparator separator;
    private JButton saveButton;
    private JButton closeButton;
}
