/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.extensionmanager;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.IgnoredComponent;
import com.mirth.connect.client.ui.BareBonesBrowserLaunch;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.UpdateInfo;

public class ExtensionUpdateDialog extends javax.swing.JDialog {

    private final String EXTENSION_NEW_COLUMN_NAME = "New Extension";
    private final String EXTENSION_INSTALL_COLUMN_NAME = "Install";
    private final String EXTENSION_TYPE_COLUMN_NAME = "Type";
    private final String EXTENSION_NAME_COLUMN_NAME = "Name";
    private final String EXTENSION_PRIORITY_COLUMN_NAME = "Priority";
    private final String EXTENSION_INSTALLED_VERSION_COLUMN_NAME = "Installed Version";
    private final String EXTENSION_UPDATE_VERSION_COLUMN_NAME = "Update Version";
    private final String EXTENSION_IGNORE_COLUMN_NAME = "Ignore";
    private final int EXTENSION_TABLE_NUMBER_OF_COLUMNS = 8;
    private final int EXTENSION_NEW_COLUMN_NUMBER = 0;
    private final int EXTENSION_INSTALL_COLUMN_NUMBER = 1;
    private final int EXTENSION_TYPE_COLUMN_NUMBER = 2;
    private final int EXTENSION_NAME_COLUMN_NUMBER = 3;
    private final int EXTENSION_PRIORITY_COLUMN_NUMBER = 4;
    private final int EXTENSION_INSTALLED_VERSION_COLUMN_NUMBER = 5;
    private final int EXTENSION_UPDATE_VERSION_COLUMN_NUMBER = 6;
    private final int EXTENSION_IGNORE_COLUMN_NUMBER = 7;
    private final String PRIORITY_OPTIONAL = "Optional";
    private final String PRIORITY_RECOMMENDED = "Recommended";
    private Map<String, MetaData> extensions = new HashMap<String, MetaData>();
    private Map<String, UpdateInfo> extensionUpdates = new HashMap<String, UpdateInfo>();
    private ExtensionUtil pluginUtil = new ExtensionUtil();
    private boolean cancel = false;
    private Frame parent;

    public ExtensionUpdateDialog() throws ClientException {
        this(null);
    }

    public ExtensionUpdateDialog(List<UpdateInfo> updateInfoList) throws ClientException {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();
        extensions.putAll(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        extensions.putAll(PlatformUI.MIRTH_FRAME.getConnectorMetaData());

        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                closeButtonActionPerformed(null);
            }
        });

        progressBar.setVisible(false);

        makeLoadedExtensionsTable();

        if (updateInfoList == null) {
            checkForUpdatesButtonActionPerformed(null);
        } else {
            extensionUpdates = new HashMap<String, UpdateInfo>();
            for (UpdateInfo updateInfo : updateInfoList) {
                extensionUpdates.put(updateInfo.getName(), updateInfo);
            }

            updateExtensionsTable();
        }

        setVisible(true);
    }

    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedExtensionsTable() {

        loadedExtensionTable = new MirthTable();
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[]{EXTENSION_NEW_COLUMN_NAME, EXTENSION_INSTALL_COLUMN_NAME, EXTENSION_TYPE_COLUMN_NAME, EXTENSION_NAME_COLUMN_NAME, EXTENSION_PRIORITY_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME, EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_IGNORE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{false, true, false, false, false, false, false, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        loadedExtensionTable.setDragEnabled(false);
        loadedExtensionTable.setRowSelectionAllowed(true);
        loadedExtensionTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedExtensionTable.setFocusable(false);
        loadedExtensionTable.setOpaque(true);
        loadedExtensionTable.getTableHeader().setReorderingAllowed(true);
        loadedExtensionTable.setSortable(true);
        loadedExtensionTable.setSelectionMode(0);

        loadedExtensionTable.getColumnExt(EXTENSION_NEW_COLUMN_NAME).setVisible(false);

        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMinWidth(50);

        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMaxWidth(75);
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMinWidth(75);

        loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMinWidth(75);

        loadedExtensionTable.getColumnExt(EXTENSION_PRIORITY_COLUMN_NAME).setMaxWidth(80);
        loadedExtensionTable.getColumnExt(EXTENSION_PRIORITY_COLUMN_NAME).setMinWidth(80);

        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMinWidth(90);

        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMinWidth(90);

        loadedExtensionTable.getColumnExt(EXTENSION_IGNORE_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_IGNORE_COLUMN_NAME).setMinWidth(50);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            loadedExtensionTable.setHighlighters(highlighter);
        }
        loadedExtensionTable.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                    if (row > -1 && extensionUpdates != null) {
                        UpdateInfo updateInfo = extensionUpdates.get(loadedExtensionTable.getModel().getValueAt(row, EXTENSION_NAME_COLUMN_NUMBER));
                        String type = updateInfo.getType().toString();

                        String priority = PRIORITY_RECOMMENDED;
                        if (updateInfo.isOptional()) {
                            priority = PRIORITY_OPTIONAL;
                        }

                        String name = updateInfo.getName();
                        String version = updateInfo.getVersion();
                        String author = updateInfo.getAuthor();
                        String url = updateInfo.getUri();
                        String description = updateInfo.getDescription();

                        new ExtensionInfoDialog(name, type, priority, author, version, url, description);
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
            }
        });
        loadedExtensionTable.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                loadedExtensionScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

        updateTableFilters();
    }

    public void installUpdates() {
        installSelectedButton.setEnabled(false);
        checkForUpdatesButton.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>() {

            private boolean installedUpdates = false;

            public Void doInBackground() {
                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++) {
                    if (loadedExtensionTable.getModel().getValueAt(i, EXTENSION_TYPE_COLUMN_NUMBER).toString().equalsIgnoreCase(UpdateInfo.Type.SERVER.toString())) {
                        if (((Boolean) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_INSTALL_COLUMN_NUMBER)).booleanValue()) {
                            String serverName = (String) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
                            String serverUrl = extensionUpdates.get(serverName).getUri();
                            boolean downloadServer = parent.alertOkCancel(progressBar, "The server cannot be automatically upgraded. Press OK to download it now from:\n" + serverUrl);

                            if (downloadServer) {
                                BareBonesBrowserLaunch.openURL(serverUrl);
                            }
                        }
                    }

                }

                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++) {
                    boolean install = ((Boolean) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_INSTALL_COLUMN_NUMBER)).booleanValue();
                    boolean server = loadedExtensionTable.getModel().getValueAt(i, EXTENSION_TYPE_COLUMN_NUMBER).toString().equalsIgnoreCase(UpdateInfo.Type.SERVER.toString());

                    if (install && !server) {
                        String name = (String) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
                        UpdateInfo extension = extensionUpdates.get(name);
                        statusLabel.setText("Downloading extension: " + extension.getName());
                        if (cancel) {
                            break;
                        }
                        progressBar.setVisible(true);
                        File file = pluginUtil.downloadFileToDisk(extension.getUri(), statusLabel, progressBar);
                        progressBar.setVisible(false);
                        if (cancel) {
                            break;
                        }
                        if (file == null) {
                            parent.alertError(progressBar, "Could not download " + name + " from:\n" + extension.getUri());
                        } else {
                            statusLabel.setText("Updating extension: " + extension.getName());
                            parent.installExtension(file);
                            installedUpdates = true;
                        }
                    }
                }

                return null;
            }

            public void done() {
                checkForUpdatesButton.setEnabled(true);
                installSelectedButton.setEnabled(true);
                if (installedUpdates) {
                    statusLabel.setText("Updates installed!");
                    parent.finishExtensionInstall();
                    dispose();
                }
            }
        };

        worker.execute();
    }

    public void updateExtensionsListAndTable() {

        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        statusLabel.setText("Checking for updates...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                extensionUpdates = new HashMap<String, UpdateInfo>();

                List<UpdateInfo> updateInfoList = new ArrayList<UpdateInfo>();

                try {
                    updateInfoList = PlatformUI.MIRTH_FRAME.getUpdateClient(progressBar).getUpdates();
                } catch (ClientException e) {
                    parent.alertError(progressBar, "Could not contact update server.");
                }

                for (UpdateInfo updateInfo : updateInfoList) {
                    extensionUpdates.put(updateInfo.getName(), updateInfo);
                }

                return null;
            }

            public void done() {
                updateExtensionsTable();
                progressBar.setIndeterminate(false);
            }
        };

        worker.execute();
    }

    private void updateExtensionsTable() {
        Object[][] tableData = null;
        int tableSize = 0;

        if (extensionUpdates.size() > 0) {
            statusLabel.setText("Ready to install updates!");
            installSelectedButton.setEnabled(true);
        } else {
            statusLabel.setText("No updates found.");
        }

        tableSize = extensionUpdates.size();
        progressBar.setVisible(false);
        tableData = new Object[tableSize][EXTENSION_TABLE_NUMBER_OF_COLUMNS];

        int i = 0;
        for (UpdateInfo updateInfo : extensionUpdates.values()) {
            tableData[i][EXTENSION_NEW_COLUMN_NUMBER] = updateInfo.isNew();
            tableData[i][EXTENSION_INSTALL_COLUMN_NUMBER] = !updateInfo.isIgnored();
            String type = updateInfo.getType().toString();
            if (type.length() > 1) {
                type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
            }
            tableData[i][EXTENSION_TYPE_COLUMN_NUMBER] = type;
            tableData[i][EXTENSION_NAME_COLUMN_NUMBER] = updateInfo.getName();

            String priority = PRIORITY_RECOMMENDED;
            if (updateInfo.isOptional()) {
                priority = PRIORITY_OPTIONAL;
            }
            tableData[i][EXTENSION_PRIORITY_COLUMN_NUMBER] = priority;

            String installedVersion = "New";
            if (updateInfo.getType().equals(UpdateInfo.Type.SERVER)) {
                installedVersion = PlatformUI.SERVER_VERSION;
            } else if (!updateInfo.isNew()) {
                for (MetaData metaData : extensions.values()) {
                    if (metaData.getPath().equalsIgnoreCase(updateInfo.getPath())) {
                        installedVersion = extensions.get(metaData.getName()).getPluginVersion();
                    }
                }
            }

            tableData[i][EXTENSION_INSTALLED_VERSION_COLUMN_NUMBER] = installedVersion;
            tableData[i][EXTENSION_UPDATE_VERSION_COLUMN_NUMBER] = updateInfo.getVersion();
            tableData[i][EXTENSION_IGNORE_COLUMN_NUMBER] = updateInfo.isIgnored();
            i++;
        }

        if (loadedExtensionTable != null) {
            RefreshTableModel model = (RefreshTableModel) loadedExtensionTable.getModel();
            model.refreshDataVector(tableData);
        } else {
        }
        progressBar.setValue(0);
    }

    private void updateTableFilters() {
        RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {

            @Override
            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                boolean include = true;
                if (updatesCheckBox.isSelected() && newCheckBox.isSelected()) {
                    // leave true
                } else if (updatesCheckBox.isSelected()) {
                    if (entry.getStringValue(EXTENSION_NEW_COLUMN_NUMBER).equalsIgnoreCase("true")) {
                        include = false;
                    }
                } else if (newCheckBox.isSelected()) {
                    if (entry.getStringValue(EXTENSION_NEW_COLUMN_NUMBER).equalsIgnoreCase("false")) {
                        include = false;
                    }
                } else {
                    include = false;
                }

                if (!optionalCheckBox.isSelected()) {
                    if (!entry.getStringValue(EXTENSION_PRIORITY_COLUMN_NUMBER).equalsIgnoreCase(PRIORITY_RECOMMENDED)) {
                        include = false;
                    }
                }

                if (!ignoredCheckBox.isSelected()) {
                    if (entry.getStringValue(EXTENSION_IGNORE_COLUMN_NUMBER).equalsIgnoreCase("true")) {
                        include = false;
                    }
                }

                return include;
            }
        };

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(loadedExtensionTable.getModel());
        sorter.setRowFilter(filter);
        loadedExtensionTable.setRowSorter(sorter);
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
        statusLabel = new javax.swing.JLabel();
        checkForUpdatesButton = new javax.swing.JButton();
        installSelectedButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        loadedExtensionScrollPane = new javax.swing.JScrollPane();
        loadedExtensionTable = new com.mirth.connect.client.ui.components.MirthTable();
        progressBar = new javax.swing.JProgressBar();
        ignoredCheckBox = new javax.swing.JCheckBox();
        newCheckBox = new javax.swing.JCheckBox();
        updatesCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        optionalCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mirth Updater");
        setModal(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(null);
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 400));

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Idle");

        checkForUpdatesButton.setText("Check for Updates");
        checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesButtonActionPerformed(evt);
            }
        });

        installSelectedButton.setText("Install Selected");
        installSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installSelectedButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Mirth Updates");

        loadedExtensionScrollPane.setMaximumSize(null);
        loadedExtensionScrollPane.setMinimumSize(null);
        loadedExtensionScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));

        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] { EXTENSION_INSTALL_COLUMN_NAME, EXTENSION_TYPE_COLUMN_NAME, EXTENSION_NAME_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME,  EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_IGNORE_COLUMN_NAME }));
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

        progressBar.setMinimumSize(new java.awt.Dimension(100, 18));

        ignoredCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoredCheckBox.setSelected(true);
        ignoredCheckBox.setText("Ignored");
        ignoredCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoredCheckBoxActionPerformed(evt);
            }
        });

        newCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        newCheckBox.setSelected(true);
        newCheckBox.setText("New");
        newCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCheckBoxActionPerformed(evt);
            }
        });

        updatesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        updatesCheckBox.setSelected(true);
        updatesCheckBox.setText("Updates");
        updatesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updatesCheckBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Show:");

        optionalCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        optionalCheckBox.setSelected(true);
        optionalCheckBox.setText("Optional");
        optionalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionalCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updatesCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optionalCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoredCheckBox))
                    .addComponent(loadedExtensionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(installSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkForUpdatesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoredCheckBox)
                    .addComponent(newCheckBox)
                    .addComponent(updatesCheckBox)
                    .addComponent(jLabel1)
                    .addComponent(optionalCheckBox)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadedExtensionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(closeButton)
                        .addComponent(checkForUpdatesButton)
                        .addComponent(installSelectedButton))
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void checkForUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkForUpdatesButtonActionPerformed
        checkForUpdatesButton.setEnabled(false);
        installSelectedButton.setEnabled(false);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                updateExtensionsListAndTable();
                return null;
            }

            public void done() {
                checkForUpdatesButton.setEnabled(true);
            }
        };

        worker.execute();

}//GEN-LAST:event_checkForUpdatesButtonActionPerformed

    private void installSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installSelectedButtonActionPerformed
        installUpdates();
}//GEN-LAST:event_installSelectedButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        cancel = true;

        try {
            List<IgnoredComponent> ignoredComponents = PlatformUI.MIRTH_FRAME.getUpdateClient(this).getIgnoredComponents();

            for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++) {
                String componentName = (String) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_NAME_COLUMN_NUMBER);
                String componentVersion = (String) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_UPDATE_VERSION_COLUMN_NUMBER);
                IgnoredComponent component = new IgnoredComponent(componentName, componentVersion);
                if ((Boolean) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_IGNORE_COLUMN_NUMBER) && !ignoredComponents.contains(component)) {
                    ignoredComponents.add(component);
                } else if (!(Boolean) loadedExtensionTable.getModel().getValueAt(i, EXTENSION_IGNORE_COLUMN_NUMBER) && ignoredComponents.contains(component)) {
                    ignoredComponents.remove(component);
                }
            }

            PlatformUI.MIRTH_FRAME.getUpdateClient(this).setIgnoredComponents(ignoredComponents);
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

private void updatesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatesCheckBoxActionPerformed
    updateTableFilters();
}//GEN-LAST:event_updatesCheckBoxActionPerformed

private void newCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCheckBoxActionPerformed
    updateTableFilters();
}//GEN-LAST:event_newCheckBoxActionPerformed

private void ignoredCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoredCheckBoxActionPerformed
    updateTableFilters();
}//GEN-LAST:event_ignoredCheckBoxActionPerformed

private void optionalCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionalCheckBoxActionPerformed
    updateTableFilters();
}//GEN-LAST:event_optionalCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkForUpdatesButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox ignoredCheckBox;
    private javax.swing.JButton installSelectedButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane loadedExtensionScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable loadedExtensionTable;
    private javax.swing.JCheckBox newCheckBox;
    private javax.swing.JCheckBox optionalCheckBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JCheckBox updatesCheckBox;
    // End of variables declaration//GEN-END:variables
}
