/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.util.ConfigurationProperty;

public class SettingsPanelMap extends AbstractSettingsPanel {

    public static final String TAB_NAME = "Configuration Map";

    public SettingsPanelMap(String tabName) {
        super(tabName);

        initComponents();

        addTask(TaskConstants.SETTINGS_CONFIGURATION_MAP_IMPORT, "Import Map", "Import a properties file into the configuration map. This will remove and replace any existing map values.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")));
        addTask(TaskConstants.SETTINGS_CONFIGURATION_MAP_EXPORT, "Export Map", "Export the configuration map to a properties file.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")));
    
        setVisibleTasks(2, 3, true);
    }

    public void doRefresh() {
        final String workingId = getFrame().startWorking("Loading " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            Map<String, ConfigurationProperty> configurationMap = null;

            public Void doInBackground() {
                try {
                    if (getFrame().confirmLeave()) {
                        configurationMap = getFrame().mirthClient.getConfigurationMap();
                    }
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            @Override
            public void done() {
                // null if it failed to get the map settings or if confirmLeave returned false
                if (configurationMap != null) {
                    updateConfigurationTable(configurationMap);
                }
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public boolean doSave() {
        final Map<String, ConfigurationProperty> configurationMap = new HashMap<String, ConfigurationProperty>();
        RefreshTableModel model = (RefreshTableModel) configurationMapTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String key = (String) model.getValueAt(i, 0);
            String value = (String) model.getValueAt(i, 1);
            String comment = (String) model.getValueAt(i, 2);

            if (StringUtils.isNotBlank(key)) {
                configurationMap.put(key, new ConfigurationProperty(value, comment));
            } else {
                if (StringUtils.isNotBlank(value) || StringUtils.isNotBlank(comment)) {
                    getFrame().alertWarning(this, "Blank keys are not allowed.");
                    return false;
                }
            }
        }

        final String workingId = getFrame().startWorking("Saving " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    getFrame().mirthClient.setConfigurationMap(configurationMap);
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }

                return null;
            }

            @Override
            public void done() {
                setSaveEnabled(false);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();

        return true;
    }

    public void doImportMap() {
        File file = getFrame().browseForFile("PROPERTIES");

        if (file != null) {
            try {
                PropertiesConfiguration properties = new PropertiesConfiguration(file);
                Map<String, ConfigurationProperty> configurationMap = new HashMap<String, ConfigurationProperty>();
                Iterator<String> iterator = properties.getKeys();

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String value = properties.getString(key);
                    String comment = properties.getLayout().getCanonicalComment(key, false);

                    configurationMap.put(key, new ConfigurationProperty(value, comment));
                }

                updateConfigurationTable(configurationMap);
                setSaveEnabled(true);
            } catch (Exception e) {
                getFrame().alertException(getFrame(), e.getStackTrace(), "Error importing configuration map");
            }
        }
    }

    public void doExportMap() {
        if (isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings first?");

            if (option == JOptionPane.YES_OPTION) {
                if (!doSave()) {
                    return;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }

        final String workingId = getFrame().startWorking("Exporting " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            private Map<String, ConfigurationProperty> configurationMap;

            public Void doInBackground() {
                try {
                    File file = getFrame().createFileForExport(null, "PROPERTIES");
                    if (file != null) {
                        PropertiesConfiguration properties = new PropertiesConfiguration(file);
                        properties.clear();
                        PropertiesConfigurationLayout layout = properties.getLayout();

                        configurationMap = getFrame().mirthClient.getConfigurationMap();
                        Map<String, ConfigurationProperty> sortedMap = new TreeMap<String, ConfigurationProperty>(String.CASE_INSENSITIVE_ORDER);
                        sortedMap.putAll(configurationMap);

                        for (Entry<String, ConfigurationProperty> entry : sortedMap.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue().getValue();
                            String comment = entry.getValue().getComment();

                            if (StringUtils.isNotBlank(key)) {
                                properties.setProperty(key, value);
                                layout.setComment(key, StringUtils.isBlank(comment) ? null : comment);
                            }
                        }

                        properties.save();
                    }
                } catch (Exception e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }

                return null;
            }

            @Override
            public void done() {
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }

    private void updateConfigurationTable(Map<String, ConfigurationProperty> map) {
        RefreshTableModel model = (RefreshTableModel) configurationMapTable.getModel();
        String[][] data = new String[map.size()][3];
        Map<String, ConfigurationProperty> sortedMap = new TreeMap<String, ConfigurationProperty>(String.CASE_INSENSITIVE_ORDER);
        sortedMap.putAll(map);

        int index = 0;
        for (Entry<String, ConfigurationProperty> entry : sortedMap.entrySet()) {
            data[index][0] = entry.getKey();
            data[index][1] = entry.getValue().getValue();
            data[index++][2] = entry.getValue().getComment();
        }

        model.refreshDataVector(data);
    }

    private void initComponents() {
        setBackground(Color.WHITE);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setLayout(new MigLayout("insets 12, fill"));

        configurationMapTable = new MirthTable();
        configurationMapTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        configurationMapTable.getTableHeader().setReorderingAllowed(false);
        configurationMapTable.setSortable(false);
        configurationMapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurationMapTable.setModel(new RefreshTableModel(new String[][] {}, new String[] {
                "Key", "Value", "Comment" }));
        TableCellEditor cellEditor = new TextFieldCellEditor() {

            @Override
            protected boolean valueChanged(String value) {
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                return true;
            }

        };
        configurationMapTable.getColumnExt("Key").setCellEditor(cellEditor);
        configurationMapTable.getColumnExt("Value").setCellEditor(cellEditor);
        configurationMapTable.getColumnExt("Comment").setCellEditor(cellEditor);
        configurationMapTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                int selectedRow;
                if (configurationMapTable.isEditing()) {
                    selectedRow = configurationMapTable.getEditingRow();
                } else {
                    selectedRow = configurationMapTable.getSelectedRow();
                }
                removeButton.setEnabled(selectedRow != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            configurationMapTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        configurationMapScrollPane = new JScrollPane();
        configurationMapScrollPane.setViewportView(configurationMapTable);

        addButton = new MirthButton("Add");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((RefreshTableModel) configurationMapTable.getModel()).addRow(new String[] { "", "" });

                if (configurationMapTable.getRowCount() == 1) {
                    configurationMapTable.setRowSelectionInterval(0, 0);
                }

                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }

        });
        removeButton = new MirthButton("Remove");
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (configurationMapTable.getSelectedModelIndex() != -1 && !configurationMapTable.isEditing()) {
                    Integer selectedModelIndex = configurationMapTable.getSelectedModelIndex();

                    RefreshTableModel model = (RefreshTableModel) configurationMapTable.getModel();

                    int newViewIndex = configurationMapTable.convertRowIndexToView(selectedModelIndex);
                    if (newViewIndex == (model.getRowCount() - 1)) {
                        newViewIndex--;
                    }

                    // must set lastModelRow to -1 so that when setting the new
                    // row selection below the old data won't try to be saved.
                    model.removeRow(selectedModelIndex);

                    if (model.getRowCount() != 0) {
                        configurationMapTable.setRowSelectionInterval(newViewIndex, newViewIndex);
                    }

                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }

        });

        configurationMapPanel = new JPanel();
        configurationMapPanel.setBackground(Color.WHITE);
        configurationMapPanel.setLayout(new MigLayout("fill, flowy, insets 0", "[grow][]", "grow"));
        configurationMapPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Configuration Map", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        configurationMapPanel.add(configurationMapScrollPane, "grow, wrap");
        configurationMapPanel.add(addButton, "growx, aligny top, split");
        configurationMapPanel.add(removeButton, "growx, aligny top");

        add(configurationMapPanel, "grow, height 100px:100%:100%, wrap");
    }

    private JPanel configurationMapPanel;
    private JScrollPane configurationMapScrollPane;
    private MirthTable configurationMapTable;
    private MirthButton addButton;
    private MirthButton removeButton;
}