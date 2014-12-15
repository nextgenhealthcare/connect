/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.plugins.LibraryClientPlugin;

public class LibraryResourcesDialog extends MirthDialog implements ListSelectionListener, TreeSelectionListener {

    private static final int SELECTED_COLUMN = 0;
    private static final int PROPERTIES_COLUMN = 1;
    private static final int TYPE_COLUMN = 2;

    private boolean saved;
    private Map<Integer, Set<String>> selectedResourceIds;
    private MirthTreeTable treeTable;
    private MirthTable resourceTable;
    private JButton okButton;
    private JButton cancelButton;

    public LibraryResourcesDialog(Channel channel) {
        super(PlatformUI.MIRTH_FRAME, true);

        selectedResourceIds = new HashMap<Integer, Set<String>>();

        Set<String> channelResourceIds = channel.getProperties().getResourceIds();
        if (channelResourceIds == null) {
            channelResourceIds = new LinkedHashSet<String>();
        }
        selectedResourceIds.put(null, new LinkedHashSet<String>(channelResourceIds));

        Set<String> sourceResourceIds = ((SourceConnectorPropertiesInterface) channel.getSourceConnector().getProperties()).getSourceConnectorProperties().getResourceIds();
        if (sourceResourceIds == null) {
            sourceResourceIds = new LinkedHashSet<String>();
        }
        selectedResourceIds.put(channel.getSourceConnector().getMetaDataId(), new LinkedHashSet<String>(sourceResourceIds));

        for (Connector destinationConnector : channel.getDestinationConnectors()) {
            Set<String> destinationResourceIds = ((DestinationConnectorPropertiesInterface) destinationConnector.getProperties()).getDestinationConnectorProperties().getResourceIds();
            if (destinationResourceIds == null) {
                destinationResourceIds = new LinkedHashSet<String>();
            }
            selectedResourceIds.put(destinationConnector.getMetaDataId(), new LinkedHashSet<String>(destinationResourceIds));
        }

        initComponents(channel);
        setPreferredSize(new Dimension(450, 444));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Library Resources");
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);

        okButton.setEnabled(false);
        final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Loading library resources...");

        SwingWorker<List<LibraryProperties>, Void> worker = new SwingWorker<List<LibraryProperties>, Void>() {

            @Override
            public List<LibraryProperties> doInBackground() throws ClientException {
                List<ResourceProperties> resourceProperties = PlatformUI.MIRTH_FRAME.mirthClient.getResources();
                List<LibraryProperties> libraryProperties = new ArrayList<LibraryProperties>();
                for (ResourceProperties resource : resourceProperties) {
                    if (resource instanceof LibraryProperties) {
                        libraryProperties.add((LibraryProperties) resource);
                    }
                }
                return libraryProperties;
            }

            @Override
            public void done() {
                try {
                    List<LibraryProperties> resources = get();
                    if (resources == null) {
                        resources = new ArrayList<LibraryProperties>();
                    }

                    Object[][] data = new Object[resources.size()][3];
                    int i = 0;

                    for (LibraryProperties properties : resources) {
                        data[i][SELECTED_COLUMN] = null;
                        data[i][PROPERTIES_COLUMN] = properties;
                        data[i][TYPE_COLUMN] = properties.getType();
                        i++;
                    }

                    ((RefreshTableModel) resourceTable.getModel()).refreshDataVector(data);

                    treeTable.getSelectionModel().setSelectionInterval(0, 0);
                    treeTable.getTreeSelectionModel().setSelectionPath(treeTable.getPathForRow(0));
                    okButton.setEnabled(true);
                } catch (Throwable t) {
                    if (t instanceof ExecutionException) {
                        t = t.getCause();
                    }
                    PlatformUI.MIRTH_FRAME.alertException(PlatformUI.MIRTH_FRAME, t.getStackTrace(), "Error loading library resources: " + t.toString());
                } finally {
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                }
            }
        };

        worker.execute();

        setVisible(true);
    }

    public boolean wasSaved() {
        return saved;
    }

    public Map<Integer, Set<String>> getSelectedResourceIds() {
        return selectedResourceIds;
    }

    private void initComponents(Channel channel) {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "", "[][]8[]8[]"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        AbstractMutableTreeTableNode channelNode = new DefaultMutableTreeTableNode(new ConnectorEntry("Channel", -1, null));

        AbstractMutableTreeTableNode channelScriptsNode = new DefaultMutableTreeTableNode(new ConnectorEntry("Channel Scripts", null, null));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Deploy Script", null, null, false)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Undeploy Script", null, null, false)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Preprocessor Script", null, null, false)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Postprocessor Script", null, null, false)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Attachment Script", null, null, false)));
        channelScriptsNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Batch Script", null, null, false)));
        channelNode.add(channelScriptsNode);

        AbstractMutableTreeTableNode sourceConnectorNode = new DefaultMutableTreeTableNode(new ConnectorEntry("Source Connector", 0, channel.getSourceConnector().getTransportName()));
        sourceConnectorNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Receiver", channel.getSourceConnector().getMetaDataId(), channel.getSourceConnector().getTransportName(), false)));
        sourceConnectorNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Filter / Transformer Script", channel.getSourceConnector().getMetaDataId(), channel.getSourceConnector().getTransportName(), false)));
        channelNode.add(sourceConnectorNode);

        for (Connector destinationConnector : channel.getDestinationConnectors()) {
            AbstractMutableTreeTableNode destinationConnectorNode = new DefaultMutableTreeTableNode(new ConnectorEntry(destinationConnector.getName(), destinationConnector.getMetaDataId(), destinationConnector.getTransportName()));
            destinationConnectorNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Filter / Transformer Script", destinationConnector.getMetaDataId(), destinationConnector.getTransportName(), false)));
            destinationConnectorNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Dispatcher", destinationConnector.getMetaDataId(), destinationConnector.getTransportName(), false)));
            destinationConnectorNode.add(new DefaultMutableTreeTableNode(new ConnectorEntry("Response Transformer Script", destinationConnector.getMetaDataId(), destinationConnector.getTransportName(), false)));
            channelNode.add(destinationConnectorNode);
        }

        treeTable = new MirthTreeTable();

        DefaultTreeTableModel model = new SortableTreeTableModel(channelNode);
        model.setColumnIdentifiers(Arrays.asList(new String[] { "Library Context" }));
        treeTable.setTreeTableModel(model);

        treeTable.setRootVisible(true);
        treeTable.setDragEnabled(false);
        treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.getTreeSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        treeTable.setFocusable(true);
        treeTable.setOpaque(true);
        treeTable.getTableHeader().setReorderingAllowed(false);
        treeTable.setEditable(false);
        treeTable.setSortable(false);
        treeTable.addTreeSelectionListener(this);
        treeTable.getSelectionModel().addListSelectionListener(this);
        treeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        treeTable.setShowGrid(true, true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            treeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        final String toolTipText = "<html>Select which context(s) to include library resources in.<br/>Libraries can be included everywhere (the Channel node),<br/>on channel-level scripts (the Channel Scripts node), and<br/>on specific source or destination connectors.</html>";

        treeTable.setTreeCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                setToolTipText(toolTipText);
                setEnabled(((ConnectorEntry) ((AbstractMutableTreeTableNode) value).getUserObject()).enabled);
                return this;
            }
        });

        treeTable.setOpenIcon(null);
        treeTable.setClosedIcon(null);
        treeTable.setLeafIcon(null);
        treeTable.getColumnExt(0).setToolTipText(toolTipText);

        add(new JScrollPane(treeTable), "grow, h 60%");

        resourceTable = new MirthTable();
        resourceTable.setModel(new RefreshTableModel(new Object[] { "", "Name", "Type" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == SELECTED_COLUMN;
            }
        });
        resourceTable.setDragEnabled(false);
        resourceTable.setRowSelectionAllowed(false);
        resourceTable.setRowHeight(UIConstants.ROW_HEIGHT);
        resourceTable.setFocusable(false);
        resourceTable.setOpaque(true);
        resourceTable.getTableHeader().setReorderingAllowed(false);
        resourceTable.setEditable(true);
        resourceTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            resourceTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        resourceTable.getColumnModel().getColumn(SELECTED_COLUMN).setMinWidth(20);
        resourceTable.getColumnModel().getColumn(SELECTED_COLUMN).setMaxWidth(20);
        resourceTable.getColumnModel().getColumn(SELECTED_COLUMN).setCellRenderer(new CheckBoxRenderer());
        resourceTable.getColumnModel().getColumn(SELECTED_COLUMN).setCellEditor(new CheckBoxEditor());

        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setMinWidth(75);
        resourceTable.getColumnModel().getColumn(TYPE_COLUMN).setMaxWidth(200);

        add(new JScrollPane(resourceTable), "newline, grow, h 40%");

        add(new JSeparator(), "newline, grow");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        buttonPanel.setBackground(getBackground());

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed();
            }
        });
        buttonPanel.add(okButton, "w 48!");

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });
        buttonPanel.add(cancelButton, "w 48!");

        add(buttonPanel, "newline, right");
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        ((AbstractTableModel) resourceTable.getModel()).fireTableDataChanged();
    }

    @Override
    public void valueChanged(TreeSelectionEvent evt) {
        MutableTreeTableNode node = (MutableTreeTableNode) evt.getPath().getLastPathComponent();
        ConnectorEntry entry = (ConnectorEntry) node.getUserObject();

        if (node.equals(treeTable.getTreeTableModel().getRoot())) {
            resourceTable.setEnabled(true);
            for (int row = 0; row < resourceTable.getRowCount(); row++) {
                LibraryProperties properties = (LibraryProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);

                boolean allChildrenChecked = true;
                boolean allChildrenUnchecked = true;
                for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                    if (selectedResourceIds.get(((ConnectorEntry) en.nextElement().getUserObject()).metaDataId).contains(properties.getId())) {
                        allChildrenUnchecked = false;
                    } else {
                        allChildrenChecked = false;
                    }
                }

                Boolean value = null;
                if (allChildrenChecked) {
                    value = true;
                } else if (allChildrenUnchecked) {
                    value = false;
                }
                resourceTable.getModel().setValueAt(value, row, SELECTED_COLUMN);
            }
        } else {
            resourceTable.setEnabled(node.getParent().equals(treeTable.getTreeTableModel().getRoot()));
            for (int row = 0; row < resourceTable.getRowCount(); row++) {
                LibraryProperties properties = (LibraryProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);
                resourceTable.getModel().setValueAt(selectedResourceIds.get(entry.metaDataId).contains(properties.getId()), row, SELECTED_COLUMN);
            }
        }
    }

    private void okButtonActionPerformed() {
        saved = true;
        dispose();
    }

    private void cancelButtonActionPerformed() {
        dispose();
    }

    private class ConnectorEntry {
        public String name;
        public Integer metaDataId;
        public String transportName;
        public boolean enabled;

        public ConnectorEntry(String name, Integer metaDataId, String transportName) {
            this(name, metaDataId, transportName, true);
        }

        public ConnectorEntry(String name, Integer metaDataId, String transportName, boolean enabled) {
            this.name = name;
            this.metaDataId = metaDataId;
            this.transportName = transportName;
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return name + (enabled && transportName != null ? " (" + transportName + ")" : "");
        }
    }

    private class CheckBoxRenderer extends JPanel implements TableCellRenderer {
        private MirthTriStateCheckBox checkBox;

        public CheckBoxRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new MirthTriStateCheckBox();
            add(checkBox, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
                checkBox.setBackground(getBackground());
            }
            boolean enabled = table.isEnabled();

            if (treeTable.getSelectedRow() >= 0) {
                LibraryProperties properties = (LibraryProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);
                LibraryClientPlugin plugin = LoadedExtensions.getInstance().getLibraryClientPlugins().get(properties.getPluginPointName());
                if (plugin != null) {
                    ConnectorEntry entry = (ConnectorEntry) treeTable.getValueAt(treeTable.getSelectedRow(), 0);
                    if (entry.transportName != null && ArrayUtils.contains(plugin.getUnselectableTransportNames(), entry.transportName)) {
                        enabled = false;
                    }
                }
            }

            checkBox.setEnabled(enabled);
            checkBox.setState(value == null ? MirthTriStateCheckBox.PARTIAL : (Boolean) value ? MirthTriStateCheckBox.CHECKED : MirthTriStateCheckBox.UNCHECKED);
            return this;
        }
    }

    private class CheckBoxEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private MirthTriStateCheckBox checkBox;

        public CheckBoxEditor() {
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new MirthTriStateCheckBox();
            checkBox.addActionListener(this);
            panel.add(checkBox, "center");
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (treeTable.getSelectedRow() >= 0 && evt instanceof MouseEvent) {
                LibraryProperties properties = (LibraryProperties) resourceTable.getModel().getValueAt(resourceTable.rowAtPoint(((MouseEvent) evt).getPoint()), PROPERTIES_COLUMN);
                LibraryClientPlugin plugin = LoadedExtensions.getInstance().getLibraryClientPlugins().get(properties.getPluginPointName());
                if (plugin != null) {
                    ConnectorEntry entry = (ConnectorEntry) treeTable.getValueAt(treeTable.getSelectedRow(), 0);
                    if (entry.transportName != null && ArrayUtils.contains(plugin.getUnselectableTransportNames(), entry.transportName)) {
                        return false;
                    }
                }
            }

            return super.isCellEditable(evt);
        }

        @Override
        public Object getCellEditorValue() {
            return checkBox.getState() == MirthTriStateCheckBox.PARTIAL ? null : checkBox.getState() == MirthTriStateCheckBox.CHECKED;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
                panel.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
                checkBox.setBackground(panel.getBackground());
            }
            checkBox.setState(value == null ? MirthTriStateCheckBox.PARTIAL : (Boolean) value ? MirthTriStateCheckBox.CHECKED : MirthTriStateCheckBox.UNCHECKED);
            return panel;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            MutableTreeTableNode node = (MutableTreeTableNode) treeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();
            ConnectorEntry entry = (ConnectorEntry) node.getUserObject();
            LibraryProperties properties = (LibraryProperties) resourceTable.getModel().getValueAt(resourceTable.getEditingRow(), PROPERTIES_COLUMN);
            LibraryClientPlugin plugin = LoadedExtensions.getInstance().getLibraryClientPlugins().get(properties.getPluginPointName());

            boolean selected = ((JCheckBox) evt.getSource()).isSelected();

            if (node.equals(treeTable.getTreeTableModel().getRoot())) {
                for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                    ConnectorEntry child = (ConnectorEntry) en.nextElement().getUserObject();

                    if (plugin == null || child.transportName == null || !ArrayUtils.contains(plugin.getUnselectableTransportNames(), child.transportName)) {
                        if (selected) {
                            selectedResourceIds.get(child.metaDataId).add(properties.getId());
                        } else {
                            selectedResourceIds.get(child.metaDataId).remove(properties.getId());
                        }
                    }
                }
            } else {
                if (selected) {
                    selectedResourceIds.get(entry.metaDataId).add(properties.getId());
                } else {
                    selectedResourceIds.get(entry.metaDataId).remove(properties.getId());
                }
            }

            if (plugin != null && plugin.singleSelectionOnly()) {
                for (int row = 0; row < resourceTable.getRowCount(); row++) {
                    LibraryProperties props = (LibraryProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);

                    if (row != resourceTable.getEditingRow() && props.getType().equals(properties.getType())) {
                        if (node.equals(treeTable.getTreeTableModel().getRoot())) {
                            for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                                ConnectorEntry child = (ConnectorEntry) en.nextElement().getUserObject();

                                if (child.transportName == null || !ArrayUtils.contains(plugin.getUnselectableTransportNames(), child.transportName)) {
                                    selectedResourceIds.get(child.metaDataId).remove(props.getId());
                                }
                            }
                        } else {
                            selectedResourceIds.get(entry.metaDataId).remove(props.getId());
                        }
                    }
                }
            }

            for (int row = 0; row < resourceTable.getRowCount(); row++) {
                LibraryProperties props = (LibraryProperties) resourceTable.getModel().getValueAt(row, PROPERTIES_COLUMN);
                Boolean newValue;

                if (node.equals(treeTable.getTreeTableModel().getRoot())) {
                    boolean allChecked = true;
                    boolean allUnchecked = true;

                    for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                        if (selectedResourceIds.get(((ConnectorEntry) en.nextElement().getUserObject()).metaDataId).contains(props.getId())) {
                            allUnchecked = false;
                        } else {
                            allChecked = false;
                        }
                    }

                    if (allChecked) {
                        newValue = true;
                    } else if (allUnchecked) {
                        newValue = false;
                    } else {
                        newValue = null;
                    }
                } else {
                    newValue = selectedResourceIds.get(entry.metaDataId).contains(props.getId());
                }

                resourceTable.getModel().setValueAt(newValue, row, SELECTED_COLUMN);
            }

            ((AbstractTableModel) resourceTable.getModel()).fireTableDataChanged();
            cancelCellEditing();
        }
    }
}