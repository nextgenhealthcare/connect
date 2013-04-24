package com.mirth.connect.client.ui.alert;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;

public class AlertChannelPane extends JPanel {
    
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;

    public AlertChannelPane() {
        initComponents();
        
        makeChannelTree();
    }
    
    private void makeChannelTree() {
        root = new DefaultMutableTreeNode(new ChannelInfo(false, "All Channels", null));
        model = new DefaultTreeModel(root);
        
        channelTree.setModel(model);
    }
    
    public void updateChannelTree(Map<String, List<Integer>> enabledChannels) {
        root.removeAllChildren();
        
        for (Channel channel : PlatformUI.MIRTH_FRAME.channels.values()) {
            boolean enabled = true;
            if (enabledChannels != null && enabledChannels.containsKey(channel.getId())) {
                enabled = enabledChannels.containsKey(channel.getId());
            }
            
            DefaultMutableTreeNode channelNode = new DefaultMutableTreeNode(new ChannelInfo(enabled, channel.getName(), channel.getId()));
            
            root.add(channelNode);
            
            DefaultMutableTreeNode sourceNode = new DefaultMutableTreeNode(new ConnectorInfo(false, channel.getSourceConnector().getName(), channel.getSourceConnector().getMetaDataId()));
            channelNode.add(sourceNode);
            
            for (Connector connector : channel.getDestinationConnectors()) {
                DefaultMutableTreeNode destinationNode = new DefaultMutableTreeNode(new ConnectorInfo(false, connector.getName(), connector.getMetaDataId()));
            }
        }
        
        model.reload();
        channelTree.expandAll();
    }
    
    public Map<String, List<Integer>> getChannels() {
        return null;
    }
    
    public void setChannels(Map<String, List<Integer>> enabledChannels) {
        updateChannelTree(enabledChannels);
    }
    
    private abstract class Info {
        private boolean enabled;
        private String name;
        
        public Info(boolean enabled, String name) {
            this.enabled = enabled;
            this.name = name;
        }
        
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class ChannelInfo extends Info {
        private String channelId;
        
        public ChannelInfo(boolean enabled, String name, String channelId) {
            super(enabled, name);
            this.channelId = channelId;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }
    }
    
    private class ConnectorInfo extends Info {
        private Integer metaDataId;
        
        public ConnectorInfo(boolean enabled, String name, Integer metaDataId) {
            super(enabled, name);
            this.metaDataId = metaDataId;
        }

        public Integer getMetaDataId() {
            return metaDataId;
        }

        public void setMetaDataId(Integer metaDataId) {
            this.metaDataId = metaDataId;
        }
    }
    
    private class ChannelTreeCellRenderer implements TreeCellRenderer {
        
        private MirthCheckBox checkBox;
        
        public ChannelTreeCellRenderer() {
            checkBox = new MirthCheckBox();
            checkBox.setBackground(UIConstants.BACKGROUND_COLOR);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Info info = (Info) ((DefaultMutableTreeNode) value).getUserObject();
            
            checkBox.setText(info.getName());
            checkBox.setSelected(info.isEnabled());
            
            return checkBox;
        }
        
    }
    
    private class ChannelTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
        
        private MirthCheckBox checkBox;
        private Info info;
        
        public ChannelTreeCellEditor() {
            checkBox = new MirthCheckBox();
            checkBox.setBackground(UIConstants.BACKGROUND_COLOR);
            checkBox.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (info != null) {
                        info.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                    }
                }
                
            });
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            info = (Info) ((DefaultMutableTreeNode) value).getUserObject();
            
            checkBox.setText(info.getName());
            checkBox.setSelected(info.isEnabled());
            
            return checkBox;
        }

        @Override
        public Object getCellEditorValue() {
            return info;
        }
        
    }
    
    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Channels"));
        setLayout(new BorderLayout());
        
        channelTree = new MirthTree();
        channelTree.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        channelTree.setCellRenderer(new ChannelTreeCellRenderer());
        channelTree.setCellEditor(new ChannelTreeCellEditor());
        channelTree.setEditable(true);
        
        channelScrollPane = new JScrollPane(channelTree);
        
        add(channelScrollPane);
    }
    
    private JScrollPane channelScrollPane;
    private MirthTree channelTree;
}
