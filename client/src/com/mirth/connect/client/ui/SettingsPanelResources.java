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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.InvalidResourceProperties;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.plugins.ResourceClientPlugin;

public class SettingsPanelResources extends AbstractSettingsPanel implements ListSelectionListener {

    public static final String TAB_NAME = "Resources";

    private static final int PROPERTIES_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int TYPE_COLUMN = 2;
    private static final int GLOBAL_SCRIPTS_COLUMN = 3;

    private MirthTable resourceTable;
    private Map<String, ResourcePropertiesPanel> propertiesPanelMap = new LinkedHashMap<String, ResourcePropertiesPanel>();
    private ResourcePropertiesPanel currentPropertiesPanel = null;
    private JPanel fillerPanel;
    private JLabel fillerLabel;
    private JScrollPane exceptionScrollPane;
    private JTextPane exceptionTextPane;
    private int selectedRow = -1;
    private int removeResourceTaskIndex;
    private int reloadResourceTaskIndex;

    public SettingsPanelResources(String tabName) {
        super(tabName);
        initComponents();

        addTask("doAddResource", "Add Resource", "Add new resource.", "", new ImageIcon(Frame.class.getResource("images/add.png")));
        removeResourceTaskIndex = addTask("doRemoveResource", "Remove Resource", "Remove selected resource.", "", new ImageIcon(Frame.class.getResource("images/delete.png")));
        reloadResourceTaskIndex = addTask("doReloadResource", "Reload Resource", "Reloads the selected resource on the server.", "", new ImageIcon(Frame.class.getResource("images/arrow_rotate_clockwise.png")));

        setVisibleTasks(removeResourceTaskIndex, removeResourceTaskIndex, false);
    }

    @Override
    public void doRefresh() {
        if (!getFrame().confirmLeave()) {
            return;
        }

        final String workingId = getFrame().startWorking("Loading resources...");
        final int selectedRow = resourceTable.getSelectedRow();

        SwingWorker<List<ResourceProperties>, Void> worker = new SwingWorker<List<ResourceProperties>, Void>() {

            @Override
            public List<ResourceProperties> doInBackground() throws ClientException {
                return getFrame().mirthClient.getResources();
            }

            @Override
            public void done() {
                try {
                    List<ResourceProperties> resources = get();

                    ResourceProperties defaultResource = null;
                    for (ResourceProperties properties : resources) {
                        if (properties.getId().equals(ResourceProperties.DEFAULT_RESOURCE_ID)) {
                            defaultResource = properties;
                        }
                    }

                    Object[][] data = new Object[resources.size()][4];

                    data[0][PROPERTIES_COLUMN] = defaultResource;
                    data[0][NAME_COLUMN] = defaultResource.getName();
                    data[0][TYPE_COLUMN] = defaultResource.getType();
                    data[0][GLOBAL_SCRIPTS_COLUMN] = defaultResource.isIncludeWithGlobalScripts();

                    int i = 1;
                    for (ResourceProperties properties : resources) {
                        if (!properties.getId().equals(ResourceProperties.DEFAULT_RESOURCE_ID)) {
                            data[i][PROPERTIES_COLUMN] = properties;
                            data[i][NAME_COLUMN] = properties.getName();
                            data[i][TYPE_COLUMN] = properties.getType();
                            data[i][GLOBAL_SCRIPTS_COLUMN] = properties.isIncludeWithGlobalScripts();
                            i++;
                        }
                    }

                    SettingsPanelResources.this.selectedRow = -1;
                    changePropertiesPanel(null);

                    ((RefreshTableModel) resourceTable.getModel()).refreshDataVector(data);
                    if (selectedRow > -1 && selectedRow < resourceTable.getRowCount()) {
                        resourceTable.setRowSelectionInterval(selectedRow, selectedRow);
                    } else if (resourceTable.getRowCount() > 0) {
                        resourceTable.setRowSelectionInterval(0, 0);
                    }

                    getFrame().setSaveEnabled(false);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertException(getFrame(), t.getStackTrace(), "Error loading resources: " + t.toString());
                } finally {
                    getFrame().stopWorking(workingId);
                }
            }
        };

        worker.execute();
    }

    @Override
    public boolean doSave() {
        resetInvalidProperties();
        final String errors = checkProperties().trim();
        if (StringUtils.isNotEmpty(errors)) {
            getFrame().alertError(getFrame(), "Error validating resource settings:\n\n" + errors);
            return false;
        }

        updateResource(resourceTable.getSelectedRow());

        final String workingId = getFrame().startWorking("Saving resources...");
        final List<ResourceProperties> resources = new ArrayList<ResourceProperties>();

        for (int row = 0; row < resourceTable.getRowCount(); row++) {
            resources.add((ResourceProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN));
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() throws ClientException {
                getFrame().mirthClient.setResources(resources);
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                    setSaveEnabled(false);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    getFrame().alertException(getFrame(), t.getStackTrace(), "Error saving resources: " + t.toString());
                } finally {
                    getFrame().stopWorking(workingId);
                }
            }
        };

        worker.execute();

        return true;
    }

    public void doAddResource() {
        int selectedRow = resourceTable.getSelectedRow();
        if (selectedRow >= 0) {
            resetInvalidProperties();
            final String errors = StringUtils.defaultString(checkProperties()).trim();
            if (StringUtils.isNotEmpty(errors)) {
                getFrame().alertError(getFrame(), "Error validating resource settings:\n\n" + errors);
                return;
            }

            updateResource(selectedRow);
        }

        if (propertiesPanelMap.size() > 0) {
            changePropertiesPanel(propertiesPanelMap.keySet().iterator().next());
            resetInvalidProperties();
            ResourceProperties properties = currentPropertiesPanel.getDefaults();

            int num = 1;
            do {
                properties.setName("Resource " + num++);
            } while (!checkUniqueName(properties.getName()));

            this.selectedRow = -1;
            ((RefreshTableModel) resourceTable.getModel()).addRow(new Object[] { properties,
                    properties.getName(), properties.getType(), false });
            resourceTable.getSelectionModel().setSelectionInterval(resourceTable.getRowCount() - 1, resourceTable.getRowCount() - 1);
            getFrame().setSaveEnabled(true);
        }
    }

    private boolean checkUniqueName(String name) {
        for (int row = 0; row < resourceTable.getRowCount(); row++) {
            if (((String) resourceTable.getModel().getValueAt(row, NAME_COLUMN)).equals(name)) {
                return false;
            }
        }
        return true;
    }

    public void doRemoveResource() {
        int selectedRow = resourceTable.getSelectedRow();
        if (selectedRow > 0) {
            resetInvalidProperties();
            resourceTable.getSelectionModel().removeListSelectionListener(this);
            ((RefreshTableModel) resourceTable.getModel()).removeRow(selectedRow);
            resourceTable.getSelectionModel().addListSelectionListener(this);
            int previousSelectedRow = selectedRow;
            this.selectedRow = -1;
            changePropertiesPanel(null);

            if (previousSelectedRow < resourceTable.getRowCount()) {
                resourceTable.getSelectionModel().setSelectionInterval(previousSelectedRow, previousSelectedRow);
            } else if (resourceTable.getRowCount() > 0) {
                resourceTable.getSelectionModel().setSelectionInterval(resourceTable.getRowCount() - 1, resourceTable.getRowCount() - 1);
            } else {
                setVisibleTasks(removeResourceTaskIndex, removeResourceTaskIndex, false);
            }

            getFrame().setSaveEnabled(true);
        }
    }

    public void doReloadResource() {
        final int selectedRow = resourceTable.getSelectedRow();

        if (selectedRow >= 0) {
            if (getFrame().isSaveEnabled()) {
                getFrame().alertWarning(getFrame(), "You must save before reloading any resources.");
            } else {
                final String workingId = getFrame().startWorking("Reloading resource...");
                final String resourceId = ((ResourceProperties) resourceTable.getModel().getValueAt(selectedRow, PROPERTIES_COLUMN)).getId();

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    @Override
                    public Void doInBackground() throws ClientException {
                        getFrame().mirthClient.reloadResource(resourceId);
                        return null;
                    }

                    @Override
                    public void done() {
                        try {
                            get();

                            if (resourceTable.getSelectedRow() == selectedRow && currentPropertiesPanel != null) {
                                ResourceProperties properties = (ResourceProperties) resourceTable.getModel().getValueAt(selectedRow, PROPERTIES_COLUMN);
                                properties.setName((String) resourceTable.getModel().getValueAt(selectedRow, NAME_COLUMN));
                                properties.setIncludeWithGlobalScripts((Boolean) resourceTable.getModel().getValueAt(selectedRow, GLOBAL_SCRIPTS_COLUMN));
                                currentPropertiesPanel.fillProperties(properties);
                                currentPropertiesPanel.setProperties(properties);
                            }
                        } catch (Throwable t) {
                            if (t instanceof ExecutionException) {
                                t = t.getCause();
                            }
                            getFrame().alertException(getFrame(), t.getStackTrace(), "Error reloading resource: " + t.toString());
                        } finally {
                            getFrame().stopWorking(workingId);
                        }
                    }
                };

                worker.execute();
            }
        }
    }

    private void resetInvalidProperties() {
        if (currentPropertiesPanel != null) {
            currentPropertiesPanel.resetInvalidProperties();
        }
    }

    private String checkProperties() {
        if (currentPropertiesPanel != null) {
            return currentPropertiesPanel.checkProperties();
        }
        return "";
    }

    private void updateResource(int row) {
        ResourceProperties properties = (ResourceProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);
        properties.setName((String) resourceTable.getModel().getValueAt(row, NAME_COLUMN));
        properties.setIncludeWithGlobalScripts((Boolean) resourceTable.getModel().getValueAt(row, GLOBAL_SCRIPTS_COLUMN));
        if (currentPropertiesPanel != null) {
            currentPropertiesPanel.fillProperties(properties);
        }
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill"));
        setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel resourceListPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        resourceListPanel.setBackground(getBackground());
        resourceListPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Resources", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        resourceTable = new MirthTable();
        resourceTable.setModel(new RefreshTableModel(new Object[] { "Properties", "Name", "Type",
                "Global Scripts" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (row == 0) {
                    return column == GLOBAL_SCRIPTS_COLUMN;
                } else {
                    return column == NAME_COLUMN || column == TYPE_COLUMN || column == GLOBAL_SCRIPTS_COLUMN;
                }
            }
        });
        resourceTable.setDragEnabled(false);
        resourceTable.setRowSelectionAllowed(true);
        resourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceTable.setRowHeight(UIConstants.ROW_HEIGHT);
        resourceTable.setFocusable(true);
        resourceTable.setOpaque(true);
        resourceTable.getTableHeader().setReorderingAllowed(false);
        resourceTable.setEditable(true);
        resourceTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            resourceTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        for (ResourceClientPlugin plugin : LoadedExtensions.getInstance().getResourceClientPlugins().values()) {
            propertiesPanelMap.put(plugin.getType(), plugin.getPropertiesPanel());
        }

        resourceTable.getColumnModel().getColumn(NAME_COLUMN).setCellEditor(new NameEditor());
        resourceTable.getColumnExt(NAME_COLUMN).setToolTipText("The unique name of the resource.");

        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setMinWidth(100);
        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setMaxWidth(200);
        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setCellRenderer(new ComboBoxRenderer(propertiesPanelMap.keySet().toArray()));
        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setCellEditor(new ComboBoxEditor(resourceTable, propertiesPanelMap.keySet().toArray(), 1, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        }));
        resourceTable.getColumnExt(TYPE_COLUMN).setToolTipText("The type of resource.");

        resourceTable.getColumnModel().getColumn(GLOBAL_SCRIPTS_COLUMN).setMinWidth(80);
        resourceTable.getColumnModel().getColumn(GLOBAL_SCRIPTS_COLUMN).setMaxWidth(80);
        resourceTable.getColumnModel().getColumn(GLOBAL_SCRIPTS_COLUMN).setCellRenderer(new CheckBoxRenderer());
        resourceTable.getColumnModel().getColumn(GLOBAL_SCRIPTS_COLUMN).setCellEditor(new CheckBoxEditor());
        resourceTable.getColumnExt(GLOBAL_SCRIPTS_COLUMN).setToolTipText("<html>If checked, libraries associated with the corresponding<br/>resource will be included in global script contexts.</html>");

        resourceTable.removeColumn(resourceTable.getColumnModel().getColumn(PROPERTIES_COLUMN));

        resourceTable.getSelectionModel().addListSelectionListener(this);

        resourceTable.setToolTipText("<html>Add or remove resources to use<br/>in specific channels/connectors.</html>");

        resourceListPanel.add(new JScrollPane(resourceTable), "grow, push");

        add(resourceListPanel, "grow, h 20%");

        for (ResourcePropertiesPanel panel : propertiesPanelMap.values()) {
            add(panel, "newline, grow, h 80%");
        }

        fillerPanel = new JPanel(new MigLayout("insets 5, novisualpadding, hidemode 3, fill", "", "[][grow]"));
        fillerPanel.setBackground(getBackground());
        fillerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Resource Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));
        fillerLabel = new JLabel("Select a resource from the table above.");
        fillerPanel.add(fillerLabel);

        exceptionTextPane = new JTextPane();
        exceptionTextPane.setBackground(new Color(224, 223, 227));
        exceptionTextPane.setEditable(false);
        exceptionScrollPane = new JScrollPane(exceptionTextPane);
        fillerPanel.add(exceptionScrollPane, "newline, grow");

        add(fillerPanel, "newline, grow, h 80%");
    }

    private void typeComboBoxActionPerformed(ActionEvent evt) {
        JComboBox comboBox = (JComboBox) evt.getSource();
        if (comboBox.isPopupVisible()) {
            int selectedRow = resourceTable.getSelectedRow();
            if (selectedRow >= 0) {
                String type = (String) comboBox.getSelectedItem();

                if (propertiesPanelMap.containsKey(type)) {
                    changePropertiesPanel(type);
                    ResourceProperties properties = currentPropertiesPanel.getDefaults();
                    resourceTable.getModel().setValueAt(properties, selectedRow, PROPERTIES_COLUMN);
                    currentPropertiesPanel.setProperties(properties);
                    getFrame().setSaveEnabled(true);
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            final int previousSelectedRow = selectedRow;
            if (previousSelectedRow >= 0) {
                resetInvalidProperties();
                final String errors = checkProperties().trim();
                if (StringUtils.isNotEmpty(errors)) {
                    resourceTable.getSelectionModel().removeListSelectionListener(this);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resourceTable.getSelectionModel().setSelectionInterval(previousSelectedRow, previousSelectedRow);
                            getFrame().alertError(getFrame(), "Error validating resource settings:\n\n" + errors);
                            resourceTable.getSelectionModel().addListSelectionListener(SettingsPanelResources.this);
                        }
                    });
                    return;
                }

                updateResource(previousSelectedRow);
            }

            int newSelectedRow = resourceTable.getSelectedRow();
            if (newSelectedRow >= 0) {
                ResourceProperties properties = (ResourceProperties) resourceTable.getModel().getValueAt(newSelectedRow, PROPERTIES_COLUMN);
                if (properties instanceof InvalidResourceProperties) {
                    currentPropertiesPanel = null;
                    for (Entry<String, ResourcePropertiesPanel> entry : propertiesPanelMap.entrySet()) {
                        entry.getValue().setVisible(false);
                    }
                    fillerLabel.setText("The currently selected resource is invalid. Check to make sure all resource extensions are correctly loaded.");
                    fillerPanel.setVisible(true);

                    Throwable cause = ((InvalidResourceProperties) properties).getCause();
                    if (cause != null) {
                        exceptionScrollPane.setVisible(true);
                        exceptionTextPane.setText(ExceptionUtils.getStackTrace(cause));
                        exceptionTextPane.setCaretPosition(0);
                    } else {
                        exceptionScrollPane.setVisible(false);
                    }
                } else {
                    boolean saveEnabled = getFrame().isSaveEnabled();
                    changePropertiesPanel(properties.getType());
                    resetInvalidProperties();
                    currentPropertiesPanel.setProperties(properties);
                    getFrame().setSaveEnabled(saveEnabled);
                }
            } else {
                changePropertiesPanel(null);
            }

            selectedRow = newSelectedRow;
            setVisibleTasks(removeResourceTaskIndex, removeResourceTaskIndex, selectedRow > 0);
        }
    }

    private void changePropertiesPanel(String type) {
        currentPropertiesPanel = propertiesPanelMap.get(type);
        for (Entry<String, ResourcePropertiesPanel> entry : propertiesPanelMap.entrySet()) {
            entry.getValue().setVisible(entry.getKey().equals(type));
        }

        if (currentPropertiesPanel == null) {
            fillerLabel.setText("Select a resource from the table above.");
            fillerPanel.setVisible(true);
        } else {
            fillerPanel.setVisible(false);
        }
        exceptionScrollPane.setVisible(false);
    }

    private class NameEditor extends TextFieldCellEditor {
        @Override
        protected boolean valueChanged(String value) {
            if (value.equals(getOriginalValue())) {
                return false;
            }

            for (int row = 0; row < resourceTable.getRowCount(); row++) {
                if (value.equals(resourceTable.getModel().getValueAt(row, NAME_COLUMN))) {
                    return false;
                }
            }

            getFrame().setSaveEnabled(true);
            return true;
        }
    }

    private class ComboBoxRenderer extends MirthComboBoxTableCellRenderer {

        private Object[] items;
        private JPanel panel;
        private JLabel label;

        public ComboBoxRenderer(Object[] items) {
            super(items);
            this.items = items;
            panel = new JPanel(new MigLayout("insets 0 3 0 0, novisualpadding, hidemode 3, fill"));
            label = new JLabel();
            panel.add(label, "grow");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (row == 0) {
                if (isSelected) {
                    panel.setBackground(table.getSelectionBackground());
                } else {
                    panel.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
                }
                label.setBackground(panel.getBackground());
                label.setText((String) value);
                return panel;
            }

            JComboBox comboBox = (JComboBox) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ResourceProperties properties = (ResourceProperties) table.getModel().getValueAt(row, PROPERTIES_COLUMN);

            if (properties instanceof InvalidResourceProperties && value != null) {
                List<Object> list = new ArrayList<Object>(Arrays.asList(items));
                list.add(value);
                comboBox.setModel(new DefaultComboBoxModel(list.toArray()));
            } else {
                comboBox.setModel(new DefaultComboBoxModel(items));
            }

            if (value != null) {
                comboBox.setSelectedItem(value);
            } else {
                comboBox.setSelectedIndex(-1);
            }

            return comboBox;
        }
    }

    private class ComboBoxEditor extends MirthComboBoxTableCellEditor {

        private Object[] items;

        public ComboBoxEditor(JTable table, Object[] items, int clickCount, boolean focusable, final ActionListener actionListener) {
            super(table, items, clickCount, focusable, actionListener);
            this.items = items;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JComboBox comboBox = (JComboBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            ResourceProperties properties = (ResourceProperties) table.getModel().getValueAt(row, PROPERTIES_COLUMN);

            if (properties instanceof InvalidResourceProperties && value != null) {
                List<Object> list = new ArrayList<Object>(Arrays.asList(items));
                list.add(value);
                comboBox.setModel(new DefaultComboBoxModel(list.toArray()));
            } else {
                comboBox.setModel(new DefaultComboBoxModel(items));
            }

            comboBox.setSelectedItem(value);
            return comboBox;
        }
    }

    private class CheckBoxRenderer extends JPanel implements TableCellRenderer {
        private MirthCheckBox checkBox;

        public CheckBoxRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new MirthCheckBox();
            add(checkBox, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true) && row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }
            checkBox.setBackground(getBackground());
            if (value != null) {
                checkBox.setSelected((Boolean) value);
            }
            return this;
        }
    }

    private class CheckBoxEditor extends DefaultCellEditor {
        private JPanel panel;
        private JCheckBox checkBox;

        public CheckBoxEditor() {
            super(new MirthCheckBox());
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = (JCheckBox) editorComponent;
            panel.add(checkBox, "center");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            panel.setBackground(table.getSelectionBackground());
            component.setBackground(panel.getBackground());
            return panel;
        }
    }
}