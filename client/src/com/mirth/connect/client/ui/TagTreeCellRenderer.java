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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.ui.tag.ChannelTagLabelCache;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;

public class TagTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private static final int GAP = 4;
    private JLabel label;
    private JPanel tagPanel;
    private Dimension screenSize;

    private boolean renderTags = false;
    private boolean tagTextMode = false;

    public TagTreeCellRenderer(boolean renderTags, boolean tagTextMode) {
        super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap " + GAP));
        this.renderTags = renderTags;
        this.tagTextMode = tagTextMode;
        setOpaque(false);
        label = new JLabel();
        label.setOpaque(false);
        add(label);
        tagPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, gap " + GAP));
        tagPanel.setOpaque(false);
        add(tagPanel, "growx, pushx");
    }

    @Override
    public Dimension getPreferredSize() {
        // Return more width than needed to allow tags to scroll off regardless of column width
        Dimension size = super.getPreferredSize();
        if (screenSize == null) {
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        }
        return new Dimension(size.width + screenSize.width, size.height);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        // Adjust for the label width and layout gap
        p.translate((int) -label.getPreferredSize().getWidth() - GAP, 0);
        int tagLocX = 0;
        for (Component tagComp : tagPanel.getComponents()) {
            JLabel tagLabel = (JLabel) tagComp;
            Icon tagIcon = tagLabel.getIcon();
            if (tagIcon != null) {
                // Using getComponentAt won't work because the layout dimensions are zero
                if (p.x >= tagLocX && p.x < tagLocX + tagIcon.getIconWidth() && p.y >= 0 && p.y < tagIcon.getIconHeight()) {
                    return tagLabel.getToolTipText();
                }

                tagLocX += tagIcon.getIconWidth();
            }
        }

        return null;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String name = "";
        String channelId = "";
        ImageIcon icon = UIConstants.ICON_CHANNEL;
        boolean channel = false;

        if (value instanceof AbstractDashboardTableNode) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) value;

            if (node.isGroupNode()) {
                name = node.getGroupStatus().getGroup().getName();
                icon = UIConstants.ICON_GROUP;
            } else {
                DashboardStatus status = node.getDashboardStatus();
                name = status.getName();
                channelId = status.getChannelId();
                if (status.getStatusType() == StatusType.CHANNEL) {
                    icon = UIConstants.ICON_CHANNEL;
                    channel = true;
                }
            }
        } else if (value instanceof ChannelTableNode) {
            ChannelTableNode node = (ChannelTableNode) value;
            if (node.isGroupNode()) {
                name = node.getGroupStatus().getGroup().getName();
                icon = UIConstants.ICON_GROUP;
            } else {
                name = node.getChannelStatus().getChannel().getName();
                channelId = node.getChannelStatus().getChannel().getId();
                channel = true;
            }
        }

        label.setIcon(icon);
        label.setText(name);
        tagPanel.removeAll();
        if (renderTags && channel) {
            List<ChannelTag> tags = new ArrayList<ChannelTag>();
            for (ChannelTag tag : PlatformUI.MIRTH_FRAME.getCachedChannelTags()) {
                if (tag.getChannelIds().contains(channelId)) {
                    tags.add(tag);
                }
            }

            if (CollectionUtils.isNotEmpty(tags)) {
                tags.sort(new Comparator<ChannelTag>() {
                    @Override
                    public int compare(ChannelTag tag1, ChannelTag tag2) {
                        return tag1.getName().compareToIgnoreCase(tag2.getName());
                    }
                });

                String constraints = tagTextMode ? "h 16!, growx" : "";
                for (ChannelTag tag : tags) {
                    tagPanel.add(ChannelTagLabelCache.getInstance().getLabel(tag, tagTextMode), constraints);
                }
            }
        }
        return this;
    }
}