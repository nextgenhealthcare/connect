/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.dependencies;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.client.ui.Frame.ChannelTask;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyUtil;
import com.mirth.connect.util.ChannelDependencyUtil.OrderedChannels;

public class ChannelDependenciesWarningDialog extends MirthDialog {

    private int result = JOptionPane.CLOSED_OPTION;

    public ChannelDependenciesWarningDialog(ChannelTask task, Set<ChannelDependency> dependencies, Set<String> selectedChannelIds, Set<String> additionalChannelIds) throws ChannelDependencyException {
        super(PlatformUI.MIRTH_FRAME, true);

        initComponents(task, dependencies, selectedChannelIds, additionalChannelIds);
        initLayout();

        setPreferredSize(new Dimension(344, 207));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Select An Option");
        pack();
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        includeCheckBox.requestFocus();
        setVisible(true);
    }

    public int getResult() {
        return result;
    }

    public boolean isIncludeOtherChannels() {
        return includeCheckBox.isSelected();
    }

    private void initComponents(final ChannelTask task, Set<ChannelDependency> dependencies, final Set<String> selectedChannelIds, Set<String> additionalChannelIds) throws ChannelDependencyException {
        additionalChannelIds.removeAll(selectedChannelIds);
        final OrderedChannels orderedChannels = ChannelDependencyUtil.getOrderedChannels(dependencies, new HashSet<String>(CollectionUtils.union(selectedChannelIds, additionalChannelIds)));
        final List<Set<String>> orderedChannelIds = orderedChannels.getOrderedIds();
        if (task.isForwardOrder()) {
            Collections.reverse(orderedChannelIds);
        }

        descriptionLabel = new JLabel("<html>There are additional channels in the dependency chain.<br/><b>Bolded</b> channels will be " + task.getFuturePassive() + " in the following order:</html>");

        channelsPane = new JTextPane();
        channelsPane.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule("div {font-family:\"Tahoma\";font-size:11;text-align:top}");
        channelsPane.setEditorKit(editorKit);
        channelsPane.setEditable(false);
        channelsPane.setBackground(getBackground());

        setTextPane(task, orderedChannels, orderedChannelIds, selectedChannelIds, false);

        descriptionScrollPane = new JScrollPane(channelsPane);

        includeCheckBox = new JCheckBox(WordUtils.capitalize(task.toString()) + " " + additionalChannelIds.size() + " additional channel" + (additionalChannelIds.size() == 1 ? "" : "s"));
        includeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setTextPane(task, orderedChannels, orderedChannelIds, selectedChannelIds, includeCheckBox.isSelected());
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                result = JOptionPane.OK_OPTION;
                dispose();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        });
    }

    private void setTextPane(ChannelTask task, OrderedChannels orderedChannels, List<Set<String>> orderedChannelIds, Set<String> selectedChannelIds, boolean boldAdditional) {
        Map<String, DashboardStatus> statusMap = null;
        if (task != ChannelTask.DEPLOY) {
            statusMap = new HashMap<String, DashboardStatus>();
            if (PlatformUI.MIRTH_FRAME.status != null) {
                for (DashboardStatus dashboardStatus : PlatformUI.MIRTH_FRAME.status) {
                    statusMap.put(dashboardStatus.getChannelId(), dashboardStatus);
                }
            }
        }

        StringBuilder builder = new StringBuilder("<html><div>");

        if (CollectionUtils.isNotEmpty(orderedChannels.getUnorderedIds())) {
            builder.append("&nbsp;Independent:&nbsp;");

            for (Iterator<String> it = orderedChannels.getUnorderedIds().iterator(); it.hasNext();) {
                String channelId = it.next();
                String channelName = null;
                builder.append("<b>");

                if (task != ChannelTask.DEPLOY) {
                    DashboardStatus dashboardStatus = statusMap.get(channelId);
                    if (dashboardStatus != null) {
                        channelName = dashboardStatus.getName();
                    }
                } else {
                    channelName = PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelIdsAndNames().get(channelId);
                }

                if (channelName != null) {
                    builder.append(channelName.replace(" ", "&nbsp;"));
                } else {
                    builder.append(channelId.replace(" ", "&nbsp;"));
                }

                builder.append("</b>");

                if (it.hasNext()) {
                    builder.append(",&nbsp;");
                }
            }

            builder.append("<br/>");
        }

        for (int i = 0; i < orderedChannelIds.size(); i++) {
            Set<String> set = orderedChannelIds.get(i);

            builder.append("&nbsp;").append(i + 1).append(".&nbsp;");

            for (Iterator<String> it = set.iterator(); it.hasNext();) {
                String channelId = it.next();
                String channelName = null;

                if (boldAdditional || selectedChannelIds.contains(channelId)) {
                    builder.append("<b>");
                }

                if (task != ChannelTask.DEPLOY) {
                    DashboardStatus dashboardStatus = statusMap.get(channelId);
                    if (dashboardStatus != null) {
                        channelName = dashboardStatus.getName();
                    }
                } else {
                    channelName = PlatformUI.MIRTH_FRAME.channelPanel.getCachedChannelIdsAndNames().get(channelId);
                }

                if (channelName != null) {
                    builder.append(channelName.replace(" ", "&nbsp;"));
                } else {
                    builder.append(channelId.replace(" ", "&nbsp;"));
                }

                if (boldAdditional || selectedChannelIds.contains(channelId)) {
                    builder.append("</b>");
                }

                if (it.hasNext()) {
                    builder.append(",&nbsp;");
                }
            }

            if (i < orderedChannelIds.size() - 1) {
                builder.append("<br/>");
            }
        }

        builder.append("</div></html>");
        channelsPane.setText(builder.toString());
        channelsPane.setCaretPosition(0);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));

        add(descriptionLabel);
        add(descriptionScrollPane, "newline, grow, push");
        add(includeCheckBox, "newline");
        add(okButton, "newline, center, gaptop 12, w 75!, split 2");
        add(cancelButton, "w 75!");
    }

    private JLabel descriptionLabel;
    private JTextPane channelsPane;
    private JScrollPane descriptionScrollPane;
    private JCheckBox includeCheckBox;
    private JButton okButton;
    private JButton cancelButton;
}