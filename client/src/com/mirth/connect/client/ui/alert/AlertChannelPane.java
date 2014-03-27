/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.alert.AlertChannels;

public class AlertChannelPane extends JPanel {

    public AlertChannelPane() {
        initComponents();

        makeChannelTable();
    }

    private void makeChannelTable() {
        ChannelTreeTableModel model = new ChannelTreeTableModel();
        model.setColumnIdentifiers(Arrays.asList(new String[] { "test" }));

        channelTreeTable.setTreeTableModel(model);
        channelTreeTable.setDoubleBuffered(true);
        channelTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        channelTreeTable.setHorizontalScrollEnabled(true);
        channelTreeTable.packTable(UIConstants.COL_MARGIN);
        channelTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        channelTreeTable.setOpaque(true);
        channelTreeTable.setRowSelectionAllowed(true);
        channelTreeTable.putClientProperty("JTree.lineStyle", "Horizontal");
        channelTreeTable.setAutoCreateColumnsFromModel(false);
        channelTreeTable.setShowGrid(true, true);
        channelTreeTable.setTableHeader(null);
        channelTreeTable.setDragEnabled(false);

        channelTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean enabled = channelTreeTable.getSelectedRowCount() > 0;

                    enableButton.setEnabled(enabled);
                    disableButton.setEnabled(enabled);
                }
            }

        });

        channelTreeTable.setTreeCellRenderer(new TreeCellRenderer() {

            private JLabel label = new JLabel();

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value != null) {
                    if (value instanceof ChannelTreeTableNode) {
                        ChannelTreeTableNode channelNode = (ChannelTreeTableNode) value;
                        ImageIcon icon;

                        if (channelNode.getChildCount() == 0) {
                            // node is a channel node with no children
                            icon = channelNode.isEnabled() ? UIConstants.ICON_BULLET_GREEN : UIConstants.ICON_BULLET_RED;
                        } else {
                            // node is a channel node with children
                            boolean hasDisabled = false;
                            boolean hasEnabled = false;

                            for (int i = 0; i < channelNode.getChildCount(); i++) {
                                ConnectorTreeTableNode connectorNode = (ConnectorTreeTableNode) channelNode.getChildAt(i);

                                if (connectorNode.isEnabled()) {
                                    hasEnabled = true;
                                } else {
                                    hasDisabled = true;
                                }
                            }

                            if (hasEnabled && hasDisabled) {
                                icon = UIConstants.ICON_BULLET_YELLOW;
                            } else if (hasDisabled) {
                                icon = UIConstants.ICON_BULLET_RED;
                            } else if (hasEnabled) {
                                icon = UIConstants.ICON_BULLET_GREEN;
                            } else {
                                // This should never occur because a channel should never be a leaf.
                                icon = UIConstants.ICON_BULLET_RED;
                            }
                        }

                        label.setText(channelNode.getChannelName());
                        label.setIcon(icon);
                    } else if (value instanceof ConnectorTreeTableNode) {
                        ConnectorTreeTableNode connectorNode = (ConnectorTreeTableNode) value;
                        ImageIcon icon = connectorNode.isEnabled() ? UIConstants.ICON_BULLET_GREEN : UIConstants.ICON_BULLET_RED;
                        label.setText(connectorNode.getConnectorName());
                        label.setIcon(icon);
                    }
                }
                return label;
            }

        });
        channelTreeTable.expandAll();
    }

    private void toggleSelectedRows(boolean enabled) {
        int[] selectedRows = channelTreeTable.getSelectedModelRows();

        for (int i = 0; i < selectedRows.length; i++) {
            TreePath path = channelTreeTable.getPathForRow(selectedRows[i]);
            AbstractChannelTreeTableNode node = (AbstractChannelTreeTableNode) path.getLastPathComponent();

            node.setEnabled(enabled);

            for (int j = 0; j < node.getChildCount(); j++) {
                ((AbstractChannelTreeTableNode) node.getChildAt(j)).setEnabled(enabled);
            }
        }

        channelTreeTable.repaint();
    }

    public AlertChannels getChannels() {
        return ((ChannelTreeTableModel) channelTreeTable.getTreeTableModel()).getAlertChannels();
    }

    public void setChannels(AlertChannels alertChannels, boolean includeConnectors) {
        if (PlatformUI.MIRTH_FRAME.channelStatuses != null) {
            TreeMap<String, Channel> channelMap = new TreeMap<String, Channel>(String.CASE_INSENSITIVE_ORDER);

            // Sort the channels by channel name
            for (ChannelStatus channelStatus : PlatformUI.MIRTH_FRAME.channelStatuses.values()) {
                Channel channel = channelStatus.getChannel();
                channelMap.put(channel.getName(), channel);
            }

            ChannelTreeTableModel model = (ChannelTreeTableModel) channelTreeTable.getTreeTableModel();
            model.addChannels(channelMap.values(), alertChannels, includeConnectors);
        }

        enableButton.setEnabled(false);
        disableButton.setEnabled(false);
    }

    private void updateFilter(String filterString) {
        ChannelTreeTableModel model = (ChannelTreeTableModel) channelTreeTable.getTreeTableModel();
        model.setFilter(filterString);
        channelTreeTable.expandAll();
    }

    private class ChannelTreeTableModel extends SortableTreeTableModel {
        private AbstractChannelTreeTableNode modelRoot;
        private AbstractChannelTreeTableNode viewRoot;

        public ChannelTreeTableModel() {
            modelRoot = new AbstractChannelTreeTableNode(false) {
                @Override
                public Object getValueAt(int column) {
                    return null;
                }

                @Override
                public int getColumnCount() {
                    return 0;
                }
            };

            viewRoot = new AbstractChannelTreeTableNode(false) {
                @Override
                public Object getValueAt(int column) {
                    return null;
                }

                @Override
                public int getColumnCount() {
                    return 0;
                }
            };

            setRoot(viewRoot);
        }

        @Override
        public int getHierarchicalColumn() {
            return 0;
        }

        public AlertChannels getAlertChannels() {
            AlertChannels channels = new AlertChannels();

            ChannelTreeTableNode channelNode = (ChannelTreeTableNode) modelRoot.getModelChildAt(0);
            channels.setNewChannel(((ConnectorTreeTableNode) channelNode.getModelChildAt(0)).isEnabled(), ((ConnectorTreeTableNode) channelNode.getModelChildAt(1)).isEnabled());

            for (int i = 1; i < modelRoot.getModelChildCount(); i++) {
                channelNode = (ChannelTreeTableNode) modelRoot.getModelChildAt(i);
                String channelId = channelNode.getChannelId();

                Map<Integer, Boolean> connectors = new HashMap<Integer, Boolean>();

                for (int j = 0; j < channelNode.getModelChildCount(); j++) {
                    ConnectorTreeTableNode connectorNode = (ConnectorTreeTableNode) channelNode.getModelChildAt(j);

                    connectors.put(connectorNode.getMetaDataId(), connectorNode.isEnabled());
                }

                channels.addChannel(channelId, connectors);
            }

            return channels;
        }

        public void setFilter(String filter) {
            filter = filter.toLowerCase();

            clearView();

            for (int i = 0; i < modelRoot.getModelChildCount(); i++) {
                boolean matchesFilter = false;

                ChannelTreeTableNode channelNode = (ChannelTreeTableNode) modelRoot.getModelChildAt(i);
                if (StringUtils.isBlank(filter) || channelNode.getChannelName().toLowerCase().contains(filter)) {
                    matchesFilter = true;
                } else {
                    for (int j = 0; j < channelNode.getModelChildCount(); j++) {
                        ConnectorTreeTableNode connectorNode = (ConnectorTreeTableNode) channelNode.getModelChildAt(j);
                        if (connectorNode.getConnectorName().toLowerCase().contains(filter)) {
                            matchesFilter = true;
                            break;
                        }
                    }
                }

                if (matchesFilter) {
                    insertNodeInto(channelNode, viewRoot);

                    for (int j = 0; j < channelNode.getModelChildCount(); j++) {
                        insertNodeInto(channelNode.getModelChildAt(j), channelNode);
                    }
                }
            }
        }

        public void addChannels(Collection<Channel> channels, AlertChannels alertChannels, boolean includeConnectors) {
            clearView();
            modelRoot.removeAllModelChildren();

            AbstractChannelTreeTableNode channelNode = new ChannelTreeTableNode("[New Channels]", null, alertChannels.isChannelEnabled(null));
            modelRoot.addModelChild(channelNode);

            if (includeConnectors) {
                channelNode.addModelChild(new ConnectorTreeTableNode("Source", 0, alertChannels.isConnectorEnabled(null, 0)));
                channelNode.addModelChild(new ConnectorTreeTableNode("[New Destinations]", null, alertChannels.isConnectorEnabled(null, null)));
            }

            for (Channel channel : channels) {
                String channelId = channel.getId();

                channelNode = new ChannelTreeTableNode(channel.getName(), channelId, alertChannels.isChannelEnabled(channelId));
                modelRoot.addModelChild(channelNode);

                if (includeConnectors) {
                    channelNode.addModelChild(new ConnectorTreeTableNode("Source", 0, alertChannels.isConnectorEnabled(channelId, 0)));

                    // add each status as a new child node of parent
                    for (Connector destinationConnector : channel.getDestinationConnectors()) {
                        Integer metaDataId = destinationConnector.getMetaDataId();
                        channelNode.addModelChild(new ConnectorTreeTableNode(destinationConnector.getName(), metaDataId, alertChannels.isConnectorEnabled(channelId, metaDataId)));
                    }

                    channelNode.addModelChild(new ConnectorTreeTableNode("[New Destinations]", null, alertChannels.isConnectorEnabled(channelId, null)));
                }
            }

            setFilter("");
        }

        private void clearView() {
            int childCount = viewRoot.getChildCount();

            for (int i = 0; i < childCount; i++) {
                removeNodeFromParent((MutableTreeTableNode) viewRoot.getChildAt(0));
            }
        }

    }

    private abstract class AbstractChannelTreeTableNode extends AbstractSortableTreeTableNode {
        private boolean enabled;
        private List<AbstractChannelTreeTableNode> modelChildren = new ArrayList<AbstractChannelTreeTableNode>();

        public AbstractChannelTreeTableNode(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void addModelChild(AbstractChannelTreeTableNode child) {
            modelChildren.add(child);
        }

        public int getModelChildCount() {
            return modelChildren.size();
        }

        public AbstractChannelTreeTableNode getModelChildAt(int index) {
            return modelChildren.get(index);
        }

        public void removeAllModelChildren() {
            modelChildren.clear();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
    }

    private class ChannelTreeTableNode extends AbstractChannelTreeTableNode {

        private String channelName;
        private String channelId;

        public ChannelTreeTableNode(String channelName, String channelId, boolean enabled) {
            super(enabled);
            this.channelName = channelName;
            this.channelId = channelId;
        }

        public String getChannelName() {
            return channelName;
        }

        public String getChannelId() {
            return channelId;
        }

        @Override
        public Object getValueAt(int column) {
            return channelName;
        }
    }

    private class ConnectorTreeTableNode extends AbstractChannelTreeTableNode {

        private String connectorName;
        private Integer metaDataId;

        public ConnectorTreeTableNode(String connectorName, Integer metaDataId, boolean enabled) {
            super(enabled);
            this.connectorName = connectorName;
            this.metaDataId = metaDataId;
        }

        public String getConnectorName() {
            return connectorName;
        }

        public Integer getMetaDataId() {
            return metaDataId;
        }

        @Override
        public Object getValueAt(int column) {
            return connectorName;
        }
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Channels"));
        setLayout(new MigLayout("insets 0", "[grow][grow]", "[][][grow]"));

        filterLabel = new JLabel("Filter: ");
        filterTextField = new JTextField();
        filterTextField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                updateFilter(filterTextField.getText());
            }

        });

        expandLabel = new JLabel("<html><u>Expand All</u></html>");
        expandLabel.setForeground(Color.blue);
        expandLabel.setToolTipText("Expand all nodes below.");
        expandLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        expandLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                channelTreeTable.expandAll();
            }
        });

        collapseLabel = new JLabel("<html><u>Collapse All</u></html>");
        collapseLabel.setForeground(Color.blue);
        collapseLabel.setToolTipText("Collapse all nodes below.");
        collapseLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        collapseLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                channelTreeTable.collapseAll();
            }
        });

        enableButton = new JButton("Enable");
        enableButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectedRows(true);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }

        });

        disableButton = new JButton("Disable");
        disableButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelectedRows(false);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }

        });

        channelTreeTable = new MirthTreeTable();

        channelScrollPane = new JScrollPane(channelTreeTable);

        add(filterLabel, "span 2, split");
        add(filterTextField, "growx, span, wrap");
        add(enableButton, "split 2, alignx left, width 50");
        add(disableButton, "alignx left, width 50");
        add(expandLabel, "split 2, alignx right");
        add(collapseLabel, "alignx right, wrap");
        add(channelScrollPane, "height 100:100:, width 100:100:, grow, span");
    }

    private JLabel filterLabel;
    private JTextField filterTextField;
    private JLabel expandLabel;
    private JLabel collapseLabel;
    private JButton enableButton;
    private JButton disableButton;
    private JScrollPane channelScrollPane;
    private MirthTreeTable channelTreeTable;

}
