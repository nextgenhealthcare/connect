/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.dependencies;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ChannelDependenciesDialog;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyGraph;
import com.mirth.connect.util.ChannelDependencyUtil;
import com.mirth.connect.util.DirectedAcyclicGraphNode;

public class ChannelDependenciesPanel extends JPanel {

    private ChannelDependenciesDialog parent;
    private Channel channel;
    private Set<ChannelDependency> dependencies;
    private Map<String, String> channelNameMap;

    public ChannelDependenciesPanel(ChannelDependenciesDialog parent, Channel channel) {
        this.parent = parent;
        this.channel = channel;
        initComponents();
        initLayout();

        updateAddButton();

        channelNameMap = new HashMap<String, String>(PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelIdsAndNames());
        channelNameMap.put(channel.getId(), channel.getName());

        PlatformUI.MIRTH_FRAME.channelPanel.retrieveDependencies();
        dependencies = new HashSet<ChannelDependency>(PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelDependencies());

        updateTreeTable(true, dependencies);
        updateTreeTable(false, dependencies);
    }

    public boolean saveChanges() {
        if (!PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelDependencies().equals(dependencies)) {
            if (!PlatformUI.MIRTH_FRAME.alertOption(this, "You've made changes to deploy/start dependencies, which will be saved now. Are you sure you wish to continue?")) {
                return false;
            }

            try {
                PlatformUI.MIRTH_FRAME.mirthClient.setChannelDependencies(dependencies);
            } catch (ClientException e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
                return false;
            }
        }

        return true;
    }

    private void updateTreeTable(boolean dependency, Set<ChannelDependency> dependencies) {
        try {
            ChannelDependencyGraph dependencyGraph = ChannelDependencyUtil.getDependencyGraph(dependencies);

            DefaultTreeTableModel model = (DefaultTreeTableModel) (dependency ? dependencyTreeTable.getTreeTableModel() : dependentTreeTable.getTreeTableModel());
            DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
            model.setRoot(root);

            DirectedAcyclicGraphNode<String> node = dependencyGraph.getNode(channel.getId());

            if (node != null) {
                if (dependency) {
                    List<DirectedAcyclicGraphNode<String>> dependencyNodes = new ArrayList<DirectedAcyclicGraphNode<String>>(node.getDirectDependencyNodes());
                    Collections.sort(dependencyNodes, new NodeComparator());
                    for (DirectedAcyclicGraphNode<String> dependencyNode : dependencyNodes) {
                        addDependencyNode(root, dependencyNode);
                    }
                } else {
                    List<DirectedAcyclicGraphNode<String>> dependentNodes = new ArrayList<DirectedAcyclicGraphNode<String>>(node.getDirectDependentNodes());
                    Collections.sort(dependentNodes, new NodeComparator());
                    for (DirectedAcyclicGraphNode<String> dependentNode : dependentNodes) {
                        addDependentNode(root, dependentNode);
                    }
                }
            }
        } catch (ChannelDependencyException e) {
        }
    }

    private void addDependencyNode(MutableTreeTableNode parent, DirectedAcyclicGraphNode<String> node) {
        String channelId = node.getElement();
        String channelName = channelNameMap.get(channelId);
        if (StringUtils.isNotBlank(channelName)) {
            MutableTreeTableNode newTreeTableNode = new DefaultMutableTreeTableNode(new ImmutablePair<String, String>(channelId, channelName));

            SortableTreeTableModel model = (SortableTreeTableModel) dependencyTreeTable.getTreeTableModel();
            model.insertNodeInto(newTreeTableNode, parent);

            List<DirectedAcyclicGraphNode<String>> dependencyNodes = new ArrayList<DirectedAcyclicGraphNode<String>>(node.getDirectDependencyNodes());
            Collections.sort(dependencyNodes, new NodeComparator());
            for (DirectedAcyclicGraphNode<String> dependencyNode : dependencyNodes) {
                addDependencyNode(newTreeTableNode, dependencyNode);
            }
        }
    }

    private void addDependentNode(MutableTreeTableNode parent, DirectedAcyclicGraphNode<String> node) {
        String channelId = node.getElement();
        String channelName = channelNameMap.get(channelId);
        if (StringUtils.isNotBlank(channelName)) {
            MutableTreeTableNode newTreeTableNode = new DefaultMutableTreeTableNode(new ImmutablePair<String, String>(channelId, channelName));

            SortableTreeTableModel model = (SortableTreeTableModel) dependentTreeTable.getTreeTableModel();
            model.insertNodeInto(newTreeTableNode, parent);

            List<DirectedAcyclicGraphNode<String>> dependentNodes = new ArrayList<DirectedAcyclicGraphNode<String>>(node.getDirectDependentNodes());
            Collections.sort(dependentNodes, new NodeComparator());
            for (DirectedAcyclicGraphNode<String> dependentNode : dependentNodes) {
                addDependentNode(newTreeTableNode, dependentNode);
            }
        }
    }

    private Map<String, String> getAllowedChannels(boolean dependency) {
        Map<String, String> allowedChannelsMap = new HashMap<String, String>();

        try {
            ChannelDependencyGraph dependencyGraph = ChannelDependencyUtil.getDependencyGraph(dependencies);
            DirectedAcyclicGraphNode<String> node = dependencyGraph.getNode(channel.getId());

            Map<String, String> channelIdsAndNames = PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelIdsAndNames();

            for (Entry<String, String> entry : channelIdsAndNames.entrySet()) {
                String channelId = entry.getKey();
                String channelName = entry.getValue();

                if (!StringUtils.equals(channelId, channel.getId())) {
                    boolean allowed = false;

                    if (node != null) {
                        if (dependency) {
                            if (node.findDependentNode(channelId) == null && !node.getDirectDependencyElements().contains(channelId)) {
                                allowed = true;
                            }
                        } else {
                            if (node.findDependencyNode(channelId) == null && !node.getDirectDependentElements().contains(channelId)) {
                                allowed = true;
                            }
                        }
                    } else {
                        allowed = true;
                    }

                    if (allowed) {
                        allowedChannelsMap.put(channelId, channelName);
                    }
                }
            }
        } catch (ChannelDependencyException e) {
        }

        return allowedChannelsMap;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        dependencyLabel = new JLabel("This channel depends upon:");

        dependencyTreeTable = new MirthTreeTable();

        DefaultTreeTableModel dependencyModel = new SortableTreeTableModel();
        DefaultMutableTreeTableNode dependencyRootNode = new DefaultMutableTreeTableNode();
        dependencyModel.setRoot(dependencyRootNode);

        dependencyTreeTable.setTreeTableModel(dependencyModel);
        dependencyTreeTable.setRootVisible(false);
        dependencyTreeTable.setDoubleBuffered(true);
        dependencyTreeTable.setDragEnabled(false);
        dependencyTreeTable.setRowSelectionAllowed(true);
        dependencyTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dependencyTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        dependencyTreeTable.setFocusable(true);
        dependencyTreeTable.setOpaque(true);
        dependencyTreeTable.setEditable(false);
        dependencyTreeTable.setSortable(true);
        dependencyTreeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        dependencyTreeTable.setAutoCreateColumnsFromModel(false);
        dependencyTreeTable.setShowGrid(true, true);
        dependencyTreeTable.setTableHeader(null);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            dependencyTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        dependencyTreeTable.setTreeCellRenderer(new DependencyTreeCellRenderer(dependencyTreeTable));

        dependencyTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (dependencyTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    dependencyTreeTable.clearSelection();
                }
            }
        });

        dependencyTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    boolean removeEnabled = false;
                    int[] rows = dependencyTreeTable.getSelectedModelRows();

                    if (rows.length == 1) {
                        TreePath selectedPath = dependencyTreeTable.getPathForRow(rows[0]);

                        if (selectedPath != null) {
                            if (selectedPath.getPathCount() == 2) {
                                removeEnabled = true;
                            }
                        }
                    }

                    dependencyRemoveButton.setEnabled(removeEnabled);
                }
            }
        });

        dependencyScrollPane = new JScrollPane(dependencyTreeTable);

        dependencyExpandAllLabel = new JLabel("<html><u>Expand All</u></html>");
        dependencyExpandAllLabel.setForeground(Color.BLUE);
        dependencyExpandAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dependencyExpandAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                dependencyTreeTable.expandAll();
            }
        });

        dependencyCollapseAllLabel = new JLabel("<html><u>Collapse All</u></html>");
        dependencyCollapseAllLabel.setForeground(Color.BLUE);
        dependencyCollapseAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dependencyCollapseAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                dependencyTreeTable.collapseAll();
            }
        });

        dependencyAddButton = new JButton("Add");
        dependencyAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Map<String, String> allowedChannelsMap = getAllowedChannels(true);

                if (MapUtils.isNotEmpty(allowedChannelsMap)) {
                    AddDialog dialog = new AddDialog(allowedChannelsMap, true);

                    if (dialog.wasSaved()) {
                        for (String dependencyId : dialog.getSelectedChannelIds()) {
                            dependencies.add(new ChannelDependency(channel.getId(), dependencyId));
                        }

                        updateTreeTable(true, dependencies);
                        updateAddButton();
                    }
                }
            }
        });

        dependencyRemoveButton = new JButton("Remove");
        dependencyRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int[] rows = dependencyTreeTable.getSelectedModelRows();

                if (rows.length == 1) {
                    TreePath selectedPath = dependencyTreeTable.getPathForRow(rows[0]);

                    if (selectedPath != null && selectedPath.getPathCount() == 2) {
                        Pair<String, String> pair = (Pair<String, String>) ((MutableTreeTableNode) selectedPath.getLastPathComponent()).getUserObject();

                        for (Iterator<ChannelDependency> it = dependencies.iterator(); it.hasNext();) {
                            ChannelDependency dependency = it.next();
                            if (StringUtils.equals(channel.getId(), dependency.getDependentId()) && StringUtils.equals(pair.getLeft(), dependency.getDependencyId())) {
                                it.remove();
                            }
                        }

                        updateTreeTable(true, dependencies);
                        updateAddButton();
                    }
                }
            }
        });
        dependencyRemoveButton.setEnabled(false);

        dependentLabel = new JLabel("This channel is depended upon by:");

        dependentTreeTable = new MirthTreeTable();

        DefaultTreeTableModel dependentModel = new SortableTreeTableModel();
        DefaultMutableTreeTableNode dependentRootNode = new DefaultMutableTreeTableNode();
        dependentModel.setRoot(dependentRootNode);

        dependentTreeTable.setTreeTableModel(dependentModel);
        dependentTreeTable.setRootVisible(false);
        dependentTreeTable.setDoubleBuffered(true);
        dependentTreeTable.setDragEnabled(false);
        dependentTreeTable.setRowSelectionAllowed(true);
        dependentTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dependentTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        dependentTreeTable.setFocusable(true);
        dependentTreeTable.setOpaque(true);
        dependentTreeTable.setEditable(false);
        dependentTreeTable.setSortable(true);
        dependentTreeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        dependentTreeTable.setAutoCreateColumnsFromModel(false);
        dependentTreeTable.setShowGrid(true, true);
        dependentTreeTable.setTableHeader(null);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            dependentTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        dependentTreeTable.setTreeCellRenderer(new DependencyTreeCellRenderer(dependentTreeTable));

        dependentTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (dependentTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    dependentTreeTable.clearSelection();
                }
            }
        });

        dependentTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    boolean removeEnabled = false;
                    int[] rows = dependentTreeTable.getSelectedModelRows();

                    if (rows.length == 1) {
                        TreePath selectedPath = dependentTreeTable.getPathForRow(rows[0]);

                        if (selectedPath != null) {
                            if (selectedPath.getPathCount() == 2) {
                                removeEnabled = true;
                            }
                        }
                    }

                    dependentRemoveButton.setEnabled(removeEnabled);
                }
            }
        });

        dependentScrollPane = new JScrollPane(dependentTreeTable);

        dependentExpandAllLabel = new JLabel("<html><u>Expand All</u></html>");
        dependentExpandAllLabel.setForeground(Color.BLUE);
        dependentExpandAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dependentExpandAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                dependentTreeTable.expandAll();
            }
        });

        dependentCollapseAllLabel = new JLabel("<html><u>Collapse All</u></html>");
        dependentCollapseAllLabel.setForeground(Color.BLUE);
        dependentCollapseAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dependentCollapseAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                dependentTreeTable.collapseAll();
            }
        });

        dependentAddButton = new JButton("Add");
        dependentAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Map<String, String> allowedChannelsMap = getAllowedChannels(false);

                if (MapUtils.isNotEmpty(allowedChannelsMap)) {
                    AddDialog dialog = new AddDialog(allowedChannelsMap, false);

                    if (dialog.wasSaved()) {
                        for (String dependentId : dialog.getSelectedChannelIds()) {
                            dependencies.add(new ChannelDependency(dependentId, channel.getId()));
                        }

                        updateTreeTable(false, dependencies);
                        updateAddButton();
                    }
                }
            }
        });

        dependentRemoveButton = new JButton("Remove");
        dependentRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int[] rows = dependentTreeTable.getSelectedModelRows();

                if (rows.length == 1) {
                    TreePath selectedPath = dependentTreeTable.getPathForRow(rows[0]);

                    if (selectedPath != null && selectedPath.getPathCount() == 2) {
                        Pair<String, String> pair = (Pair<String, String>) ((MutableTreeTableNode) selectedPath.getLastPathComponent()).getUserObject();

                        for (Iterator<ChannelDependency> it = dependencies.iterator(); it.hasNext();) {
                            ChannelDependency dependency = it.next();
                            if (StringUtils.equals(pair.getLeft(), dependency.getDependentId()) && StringUtils.equals(channel.getId(), dependency.getDependencyId())) {
                                it.remove();
                            }
                        }

                        updateTreeTable(false, dependencies);
                        updateAddButton();
                    }
                }
            }
        });
        dependentRemoveButton.setEnabled(false);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        add(dependencyLabel);
        add(dependencyExpandAllLabel, "right, split 3");
        add(new JLabel("|"));
        add(dependencyCollapseAllLabel);
        add(dependencyScrollPane, "newline, grow, push, span 2");
        add(dependencyAddButton, "top, flowy, split 2, w 60!");
        add(dependencyRemoveButton, "w 60!");

        add(dependentLabel, "newline");
        add(dependentExpandAllLabel, "right, split 3");
        add(new JLabel("|"));
        add(dependentCollapseAllLabel);
        add(dependentScrollPane, "newline, grow, push, span 2");
        add(dependentAddButton, "top, flowy, split 2, w 60!");
        add(dependentRemoveButton, "w 60!");
    }

    private void updateAddButton() {
        dependencyAddButton.setEnabled(MapUtils.isNotEmpty(getAllowedChannels(true)));
        dependentAddButton.setEnabled(MapUtils.isNotEmpty(getAllowedChannels(false)));
    }

    private class NodeComparator implements Comparator<DirectedAcyclicGraphNode<String>> {
        @Override
        public int compare(DirectedAcyclicGraphNode<String> o1, DirectedAcyclicGraphNode<String> o2) {
            String name1 = channelNameMap.get(o1.getElement());
            String name2 = channelNameMap.get(o2.getElement());

            if (name1 == null && name2 == null) {
                return 0;
            } else if (name1 == null) {
                return -1;
            } else if (name2 == null) {
                return 1;
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        }
    }

    private class DependencyTreeCellRenderer extends JLabel implements TreeCellRenderer {

        private MirthTreeTable treeTable;

        public DependencyTreeCellRenderer(MirthTreeTable treeTable) {
            this.treeTable = treeTable;
            setIcon(UIConstants.ICON_CHANNEL);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (selected) {
                setBackground(treeTable.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }

            if (value != null) {
                MutableTreeTableNode node = (MutableTreeTableNode) value;

                if (node.getUserObject() != null) {
                    Pair<String, String> pair = (Pair<String, String>) node.getUserObject();
                    setText(pair.getRight());
                }

                setEnabled(node.getParent() == treeTable.getTreeTableModel().getRoot());
            }

            return this;
        }
    }

    private class AddDialog extends MirthDialog {

        private boolean saved = false;

        public boolean wasSaved() {
            return saved;
        }

        public AddDialog(Map<String, String> allowedChannelMap, boolean dependency) {
            super(parent, true);

            initComponents(allowedChannelMap, dependency);
            initLayout();

            setPreferredSize(new Dimension(365, 230));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setTitle("Add " + (dependency ? "Dependency" : "Dependent"));
            pack();
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        public Set<String> getSelectedChannelIds() {
            Set<String> selectedChannelIds = new HashSet<String>();

            for (int row = 0; row < table.getModel().getRowCount(); row++) {
                if ((boolean) table.getModel().getValueAt(row, 0)) {
                    selectedChannelIds.add((String) table.getModel().getValueAt(row, 2));
                }
            }

            return selectedChannelIds;
        }

        private void initComponents(Map<String, String> allowedChannelMap, final boolean dependency) {
            setBackground(UIConstants.BACKGROUND_COLOR);
            getContentPane().setBackground(getBackground());

            label = new JLabel("Select the " + (dependency ? "dependency" : "dependent") + " channel(s) to add.");

            selectAllLabel = new JLabel("<html><u>Select All</u></html>");
            selectAllLabel.setForeground(Color.BLUE);
            selectAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            selectAllLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent evt) {
                    for (int row = 0; row < table.getModel().getRowCount(); row++) {
                        table.getModel().setValueAt(true, row, 0);
                    }
                }
            });

            deselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
            deselectAllLabel.setForeground(Color.BLUE);
            deselectAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deselectAllLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent evt) {
                    for (int row = 0; row < table.getModel().getRowCount(); row++) {
                        table.getModel().setValueAt(false, row, 0);
                    }
                }
            });

            Object[][] data = new Object[allowedChannelMap.size()][3];
            int i = 0;
            for (Entry<String, String> entry : allowedChannelMap.entrySet()) {
                data[i][0] = false;
                data[i][1] = entry.getValue();
                data[i][2] = entry.getKey();
                i++;
            }

            table = new MirthTable();
            table.setModel(new RefreshTableModel(data, new Object[] { "", "Name", "Id" }) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0;
                }
            });
            table.setDragEnabled(false);
            table.setRowSelectionAllowed(false);
            table.setRowHeight(UIConstants.ROW_HEIGHT);
            table.setFocusable(false);
            table.setOpaque(true);
            table.getTableHeader().setReorderingAllowed(false);
            table.setEditable(true);
            table.setSortable(true);

            if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
                table.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
            }

            table.getColumnModel().getColumn(0).setMinWidth(20);
            table.getColumnModel().getColumn(0).setMaxWidth(20);
            table.getColumnExt(2).setVisible(false);

            scrollPane = new JScrollPane(table);

            separator = new JSeparator(SwingConstants.HORIZONTAL);

            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (CollectionUtils.isEmpty(getSelectedChannelIds())) {
                        PlatformUI.MIRTH_FRAME.alertError(AddDialog.this, "You must select at least one " + (dependency ? "dependency" : "dependent") + " channel.");
                        return;
                    }

                    saved = true;
                    dispose();
                }
            });

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
        }

        private void initLayout() {
            setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));

            add(label);
            add(selectAllLabel, "right, split 3");
            add(new JLabel("|"));
            add(deselectAllLabel);
            add(scrollPane, "newline, grow, push, sx");
            add(separator, "newline, growx, sx");
            add(okButton, "newline, sx, split 2, right, w 50!");
            add(cancelButton, "w 50!");
        }

        private JLabel label;
        private JLabel selectAllLabel;
        private JLabel deselectAllLabel;
        private MirthTable table;
        private JScrollPane scrollPane;
        private JSeparator separator;
        private JButton okButton;
        private JButton cancelButton;
    }

    private JLabel dependencyLabel;
    private MirthTreeTable dependencyTreeTable;
    private JScrollPane dependencyScrollPane;
    private JLabel dependencyExpandAllLabel;
    private JLabel dependencyCollapseAllLabel;
    private JButton dependencyAddButton;
    private JButton dependencyRemoveButton;

    private JLabel dependentLabel;
    private MirthTreeTable dependentTreeTable;
    private JScrollPane dependentScrollPane;
    private JLabel dependentExpandAllLabel;
    private JLabel dependentCollapseAllLabel;
    private JButton dependentAddButton;
    private JButton dependentRemoveButton;
}