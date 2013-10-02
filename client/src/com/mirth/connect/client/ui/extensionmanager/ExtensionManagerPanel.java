/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.extensionmanager;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.CellData;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.ImageCellRenderer;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;

public class ExtensionManagerPanel extends javax.swing.JPanel {

    private final String PLUGIN_STATUS_COLUMN_NAME = "Status";
    private final String PLUGIN_NAME_COLUMN_NAME = "Name";
    private final String PLUGIN_AUTHOR_COLUMN_NAME = "Author";
    private final String PLUGIN_URL_COLUMN_NAME = "URL";
    private final String PLUGIN_VERSION_COLUMN_NAME = "Version";
    private final int PLUGIN_STATUS_COLUMN_NUMBER = 0;
    private final int PLUGIN_NAME_COLUMN_NUMBER = 1;
    private final int NUMBER_OF_COLUMNS = 5;
    private int lastConnectorRow = -1;
    private int lastPluginRow = -1;
    private final String ENABLED_STATUS = "Enabled";
    private Map<String, PluginMetaData> pluginData = null;
    private Map<String, ConnectorMetaData> connectorData = null;
    private Frame parent;

    public ExtensionManagerPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        setRestartRequired(false);
        makeLoadedConnectorsTable();
        makeLoadedPluginsTable();
    }

    /**
     * Gets the selected extension index that corresponds to the saved extensions
     * list
     */
    public MetaData getSelectedExtension() {
        if (loadedConnectorsTable.getSelectedRowCount() > 0) {
            int selectedRow = loadedConnectorsTable.getSelectedRow();

            if (selectedRow != -1) {
                String extensionName = (String) loadedConnectorsTable.getModel().getValueAt(loadedConnectorsTable.convertRowIndexToModel(selectedRow), PLUGIN_NAME_COLUMN_NUMBER);
                return connectorData.get(extensionName);
            }
        } else if (loadedPluginsTable.getSelectedRowCount() > 0) {
            int selectedRow = loadedPluginsTable.getSelectedRow();

            if (selectedRow != -1) {
                String extensionName = (String) loadedPluginsTable.getModel().getValueAt(loadedPluginsTable.convertRowIndexToModel(selectedRow), PLUGIN_NAME_COLUMN_NUMBER);
                return pluginData.get(extensionName);
            }
        }

        return null;
    }

    public void setSelectedExtensionEnabled(boolean enabled) {
        CellData enabledCellData = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
        CellData disabledCellData = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");

        if (loadedConnectorsTable.getSelectedRowCount() > 0) {
            int selectedRow = loadedConnectorsTable.getSelectedRow();

            if (selectedRow != -1) {
                if (enabled) {
                    loadedConnectorsTable.getModel().setValueAt(enabledCellData, loadedConnectorsTable.convertRowIndexToModel(selectedRow), PLUGIN_STATUS_COLUMN_NUMBER);
                } else {
                    loadedConnectorsTable.getModel().setValueAt(disabledCellData, loadedConnectorsTable.convertRowIndexToModel(selectedRow), PLUGIN_STATUS_COLUMN_NUMBER);
                }
            }
        } else if (loadedPluginsTable.getSelectedRowCount() > 0) {
            int selectedRow = loadedPluginsTable.getSelectedRow();

            if (selectedRow != -1) {
                if (enabled) {
                    loadedPluginsTable.getModel().setValueAt(enabledCellData, loadedPluginsTable.convertRowIndexToModel(selectedRow), PLUGIN_STATUS_COLUMN_NUMBER);
                } else {
                    loadedPluginsTable.getModel().setValueAt(disabledCellData, loadedPluginsTable.convertRowIndexToModel(selectedRow), PLUGIN_STATUS_COLUMN_NUMBER);
                }
            }
        }
        
        // Change the current task to reflect the new status
        if (enabled) {
            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, 2, false);
            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 3, 3, true);
        } else {
            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, 2, true);
            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 3, 3, false);
        }
    }

    public void showExtensionProperties() {
        MetaData metaData = getSelectedExtension();
        if (metaData != null) {
            String type = "";
            if (metaData instanceof ConnectorMetaData) {
                type = "Connector";
            } else if (metaData instanceof PluginMetaData) {
                type = "Plugin";
            }

            String name = metaData.getName();
            String version = metaData.getPluginVersion();
            String author = metaData.getAuthor();
            String url = metaData.getUrl();
            String description = metaData.getDescription();

            new ExtensionInfoDialog(name, type, "Installed", author, version, url, description);
        }
    }

    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedConnectorsTable() {
        updateLoadedConnectorsTable();

        loadedConnectorsTable.setSelectionMode(0);
        loadedConnectorsTable.setDragEnabled(false);
        loadedConnectorsTable.setRowSelectionAllowed(true);
        loadedConnectorsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedConnectorsTable.setOpaque(true);
        loadedConnectorsTable.getTableHeader().setReorderingAllowed(true);
        loadedConnectorsTable.setSortable(true);

        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);

        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            loadedConnectorsTable.setHighlighters(highlighter);
        }

        loadedConnectorsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                ConnectorListSelected(evt);
            }
        });

        // listen for trigger button and double click to edit channel.
        loadedConnectorsTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkConnectorSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkConnectorSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showExtensionProperties();
                }
            }
        });
        loadedConnectorsTable.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                loadedConnectorsScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });
        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);
    }

    public void setConnectorData(Map<String, ConnectorMetaData> connectorData) {
        this.connectorData = connectorData;
        updateLoadedConnectorsTable();
    }

    public void updateLoadedConnectorsTable() {
        Object[][] tableData = null;
        int tableSize = 0;

        if (connectorData != null) {
            tableSize = connectorData.size();
            tableData = new Object[tableSize][NUMBER_OF_COLUMNS];

            int i = 0;
            for (ConnectorMetaData metaData : connectorData.values()) {
                boolean enabled = false;
                try {
                    enabled = parent.mirthClient.isExtensionEnabled(metaData.getName());
                } catch (ClientException e) {
                    // Show a plugin as disabled if the status cannot be retrieved
                }
                
                if (enabled) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }

                tableData[i][1] = metaData.getName();
                tableData[i][2] = metaData.getAuthor();
                tableData[i][3] = metaData.getUrl();
                tableData[i][4] = metaData.getPluginVersion();

                i++;
            }
        }

        if (connectorData != null && loadedConnectorsTable != null) {
            lastConnectorRow = loadedConnectorsTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) loadedConnectorsTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            loadedConnectorsTable = new MirthTable();
            loadedConnectorsTable.setModel(new RefreshTableModel(tableData, new String[]{PLUGIN_STATUS_COLUMN_NAME, PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastConnectorRow >= 0 && lastConnectorRow < loadedConnectorsTable.getRowCount()) {
            loadedConnectorsTable.setRowSelectionInterval(lastConnectorRow, lastConnectorRow);
        } else {
            lastConnectorRow = UIConstants.ERROR_CONSTANT;
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            loadedConnectorsTable.setHighlighters(highlighter);
        }

    }

    /** The action called when a connector is selected. Sets tasks as well. */
    private void ConnectorListSelected(ListSelectionEvent evt) {
        int row = loadedConnectorsTable.getSelectedRow();

        if (row >= 0 && row < loadedConnectorsTable.getRowCount()) {
            loadedPluginsTable.clearSelection();

            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, -1, true);

            int columnNumber = loadedConnectorsTable.getColumnViewIndex(PLUGIN_STATUS_COLUMN_NAME);
            if (((CellData) loadedConnectorsTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS)) {
                parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, 2, false);
            } else {
                parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 3, 3, false);
            }
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkConnectorSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = loadedConnectorsTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectConnectorRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                loadedConnectorsTable.setRowSelectionInterval(row, row);
            }
            parent.extensionsPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    public void deselectConnectorRows() {
        loadedConnectorsTable.clearSelection();
        parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, -1, false);
    }

    public void makeLoadedPluginsTable() {
        updateLoadedPluginsTable();

        loadedPluginsTable.setSelectionMode(0);
        loadedPluginsTable.setDragEnabled(false);
        loadedPluginsTable.setRowSelectionAllowed(true);
        loadedPluginsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedPluginsTable.setOpaque(true);
        loadedPluginsTable.getTableHeader().setReorderingAllowed(true);
        loadedPluginsTable.setSortable(true);

        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);

        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            loadedPluginsTable.setHighlighters(highlighter);
        }

        loadedPluginsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                PluginListSelected(evt);
            }
        });

        // listen for trigger button and double click to edit channel.
        loadedPluginsTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkPluginSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkPluginSelectionAndPopupMenu(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showExtensionProperties();
                }
            }
        });

        loadedPluginsTable.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                loadedPluginsScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
        });
        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);
    }

    public void setPluginData(Map<String, PluginMetaData> pluginData) {
        this.pluginData = pluginData;
        updateLoadedPluginsTable();
    }

    public void updateLoadedPluginsTable() {
        Object[][] tableData = null;
        int tableSize = 0;

        if (pluginData != null) {
            tableSize = pluginData.size();
            tableData = new Object[tableSize][NUMBER_OF_COLUMNS];

            int i = 0;
            for (PluginMetaData metaData : pluginData.values()) {
                boolean enabled = false;
                try {
                    enabled = parent.mirthClient.isExtensionEnabled(metaData.getName());
                } catch (ClientException e) {
                    // Show a plugin as disabled if the status cannot be retrieved
                }
                
                if (enabled) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                }

                tableData[i][1] = metaData.getName();
                tableData[i][2] = metaData.getAuthor();
                tableData[i][3] = metaData.getUrl();
                tableData[i][4] = metaData.getPluginVersion();

                i++;
            }
        }

        if (pluginData != null && loadedPluginsTable != null) {
            lastPluginRow = loadedPluginsTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) loadedPluginsTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            loadedPluginsTable = new MirthTable();
            loadedPluginsTable.setModel(new RefreshTableModel(tableData, new String[]{PLUGIN_STATUS_COLUMN_NAME, PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, false, false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        if (lastPluginRow >= 0 && lastPluginRow < loadedPluginsTable.getRowCount()) {
            loadedPluginsTable.setRowSelectionInterval(lastPluginRow, lastPluginRow);
        } else {
            lastPluginRow = UIConstants.ERROR_CONSTANT;
        }

        // Set highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            loadedPluginsTable.setHighlighters(highlighter);
        }

    }

    /** The action called when a plugin is selected. Sets tasks as well. */
    private void PluginListSelected(ListSelectionEvent evt) {
        int row = loadedPluginsTable.getSelectedRow();

        if (row >= 0 && row < loadedPluginsTable.getRowCount()) {
            loadedConnectorsTable.clearSelection();

            parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, -1, true);

            int columnNumber = loadedPluginsTable.getColumnViewIndex(PLUGIN_STATUS_COLUMN_NAME);
            if (((CellData) loadedPluginsTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS)) {
                parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, 2, false);
            } else {
                parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 3, 3, false);
            }
        }
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.  Deselects the rows if no row was selected.
     */
    private void checkPluginSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = loadedPluginsTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (row == -1) {
            deselectPluginRows();
        }

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                loadedPluginsTable.setRowSelectionInterval(row, row);
            }
            parent.extensionsPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    public void deselectPluginRows() {
        loadedPluginsTable.clearSelection();
        parent.setVisibleTasks(parent.extensionsTasks, parent.extensionsPopupMenu, 2, -1, false);
    }
    
    public void setRestartRequired(boolean restartRequired) {
        restartRequiredPanel.setVisible(restartRequired);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loadedPluginsPanel = new javax.swing.JPanel();
        loadedPluginsScrollPane = new javax.swing.JScrollPane();
        loadedPluginsTable = null;
        installExtensionPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fileText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        installButton = new javax.swing.JButton();
        loadedConnectorsPanel = new javax.swing.JPanel();
        loadedConnectorsScrollPane = new javax.swing.JScrollPane();
        loadedConnectorsTable = null;
        restartRequiredPanel = new javax.swing.JPanel();
        restartRequiredLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        loadedPluginsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedPluginsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Installed Plugins", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);

        javax.swing.GroupLayout loadedPluginsPanelLayout = new javax.swing.GroupLayout(loadedPluginsPanel);
        loadedPluginsPanel.setLayout(loadedPluginsPanelLayout);
        loadedPluginsPanelLayout.setHorizontalGroup(
            loadedPluginsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadedPluginsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        loadedPluginsPanelLayout.setVerticalGroup(
            loadedPluginsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadedPluginsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
        );

        installExtensionPanel.setBackground(new java.awt.Color(255, 255, 255));
        installExtensionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Install Extension from File System", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel1.setText("File:");

        browseButton.setText("Browse...");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        installButton.setText("Install");
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout installExtensionPanelLayout = new javax.swing.GroupLayout(installExtensionPanel);
        installExtensionPanel.setLayout(installExtensionPanelLayout);
        installExtensionPanelLayout.setHorizontalGroup(
            installExtensionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installExtensionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileText, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installButton))
        );
        installExtensionPanelLayout.setVerticalGroup(
            installExtensionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(installExtensionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(installButton)
                .addComponent(browseButton)
                .addComponent(fileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        loadedConnectorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedConnectorsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Installed Connectors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);

        javax.swing.GroupLayout loadedConnectorsPanelLayout = new javax.swing.GroupLayout(loadedConnectorsPanel);
        loadedConnectorsPanel.setLayout(loadedConnectorsPanelLayout);
        loadedConnectorsPanelLayout.setHorizontalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadedConnectorsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        loadedConnectorsPanelLayout.setVerticalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadedConnectorsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
        );

        restartRequiredPanel.setBackground(new java.awt.Color(255, 255, 204));

        restartRequiredLabel.setForeground(new java.awt.Color(204, 0, 0));
        restartRequiredLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        restartRequiredLabel.setText("The Mirth Connect Server and Administrator must be restarted before your changes will take effect.");

        javax.swing.GroupLayout restartRequiredPanelLayout = new javax.swing.GroupLayout(restartRequiredPanel);
        restartRequiredPanel.setLayout(restartRequiredPanelLayout);
        restartRequiredPanelLayout.setHorizontalGroup(
            restartRequiredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(restartRequiredPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(restartRequiredLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                .addContainerGap())
        );
        restartRequiredPanelLayout.setVerticalGroup(
            restartRequiredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(restartRequiredLabel)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(restartRequiredPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadedConnectorsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(installExtensionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadedPluginsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loadedConnectorsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadedPluginsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(restartRequiredPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installExtensionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_installButtonActionPerformed
    {//GEN-HEADEREND:event_installButtonActionPerformed
        final String workingId = parent.startWorking("Installing Extension...");
        installButton.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            private boolean retVal = false;

            public Void doInBackground() {
                retVal = parent.installExtension(new File(fileText.getText()));
                return null;
            }

            public void done() {
                parent.stopWorking(workingId);
                installButton.setEnabled(true);
                if (retVal) {
                    setRestartRequired(true);
                    fileText.setText("");
                }
            }
        };

        worker.execute();


    }//GEN-LAST:event_installButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        File pluginFile = PlatformUI.MIRTH_FRAME.browseForFile("ZIP");

        if (pluginFile != null) {
            fileText.setText(pluginFile.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField fileText;
    private javax.swing.JButton installButton;
    private javax.swing.JPanel installExtensionPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel loadedConnectorsPanel;
    private javax.swing.JScrollPane loadedConnectorsScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable loadedConnectorsTable;
    private javax.swing.JPanel loadedPluginsPanel;
    private javax.swing.JScrollPane loadedPluginsScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable loadedPluginsTable;
    private javax.swing.JLabel restartRequiredLabel;
    private javax.swing.JPanel restartRequiredPanel;
    // End of variables declaration//GEN-END:variables
}
