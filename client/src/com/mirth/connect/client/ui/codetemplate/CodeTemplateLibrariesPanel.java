/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.ui.ChannelDependenciesDialog;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;

public class CodeTemplateLibrariesPanel extends JPanel {

    private Map<String, CodeTemplateLibrary> libraryMap = new HashMap<String, CodeTemplateLibrary>();
    private String channelId;

    public CodeTemplateLibrariesPanel(final ChannelDependenciesDialog parent, Channel channel) {
        this.channelId = channel.getId();
        initComponents(channel);
        initLayout();

        PlatformUI.MIRTH_FRAME.codeTemplatePanel.doRefreshCodeTemplates(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                    libraryMap.put(library.getId(), new CodeTemplateLibrary(library));
                }
                Map<String, CodeTemplate> codeTemplateMap = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates();

                DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode();

                for (CodeTemplateLibrary library : libraryMap.values()) {
                    boolean enabled = library.getEnabledChannelIds().contains(channelId) || (library.isIncludeNewChannels() && !library.getDisabledChannelIds().contains(channelId));
                    DefaultMutableTreeTableNode libraryNode = new DefaultMutableTreeTableNode(new ImmutableTriple<String, String, Boolean>(library.getId(), library.getName(), enabled));

                    for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                        codeTemplate = codeTemplateMap.get(codeTemplate.getId());
                        if (codeTemplate != null) {
                            libraryNode.add(new DefaultMutableTreeTableNode(new ImmutableTriple<String, String, Boolean>(codeTemplate.getId(), codeTemplate.getName(), enabled)));
                        }
                    }

                    rootNode.add(libraryNode);
                }

                ((DefaultTreeTableModel) libraryTreeTable.getTreeTableModel()).setRoot(rootNode);
                libraryTreeTable.expandAll();
                parent.codeTemplateLibrariesReady();
            }
        });
    }

    public Map<String, CodeTemplateLibrary> getLibraryMap() {
        return libraryMap;
    }

    private void initComponents(Channel channel) {
        setBackground(UIConstants.BACKGROUND_COLOR);

        enableButton = new JButton("Enable");
        enableButton.setEnabled(false);
        enableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setLibraryEnabled(true);
            }
        });

        disableButton = new JButton("Disable");
        disableButton.setEnabled(false);
        disableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setLibraryEnabled(false);
            }
        });

        expandAllLabel = new JLabel("<html><u>Expand All</u></html>");
        expandAllLabel.setForeground(Color.BLUE);
        expandAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                libraryTreeTable.expandAll();
            }
        });

        separatorLabel = new JLabel("|");

        collapseAllLabel = new JLabel("<html><u>Collapse All</u></html>");
        collapseAllLabel.setForeground(Color.BLUE);
        collapseAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                libraryTreeTable.collapseAll();
            }
        });

        libraryTreeTable = new MirthTreeTable();

        DefaultTreeTableModel model = new SortableTreeTableModel();
        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode();
        model.setRoot(rootNode);

        libraryTreeTable.setTreeTableModel(model);
        libraryTreeTable.setOpenIcon(null);
        libraryTreeTable.setClosedIcon(null);
        libraryTreeTable.setLeafIcon(null);
        libraryTreeTable.setRootVisible(false);
        libraryTreeTable.setDoubleBuffered(true);
        libraryTreeTable.setDragEnabled(false);
        libraryTreeTable.setRowSelectionAllowed(true);
        libraryTreeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        libraryTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        libraryTreeTable.setFocusable(true);
        libraryTreeTable.setOpaque(true);
        libraryTreeTable.setEditable(false);
        libraryTreeTable.setSortable(false);
        libraryTreeTable.setAutoCreateColumnsFromModel(false);
        libraryTreeTable.setShowGrid(true, true);
        libraryTreeTable.setTableHeader(null);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            libraryTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        libraryTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                if (libraryTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY())) < 0) {
                    libraryTreeTable.clearSelection();
                }
            }
        });

        libraryTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    enableButton.setEnabled(libraryTreeTable.getSelectedRowCount() > 0);
                    disableButton.setEnabled(libraryTreeTable.getSelectedRowCount() > 0);
                }
            }
        });

        libraryTreeTable.setTreeCellRenderer(new LibraryTreeCellRenderer());

        libraryTreeTableScrollPane = new JScrollPane(libraryTreeTable);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        add(enableButton, "left, split 2, w 48!");
        add(disableButton, "w 48!");
        add(expandAllLabel, "right, split");
        add(separatorLabel);
        add(collapseAllLabel);
        add(libraryTreeTableScrollPane, "newline, grow, sx, push");
    }

    private class LibraryTreeCellRenderer extends JLabel implements TreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value != null) {
                MutableTreeTableNode node = (MutableTreeTableNode) value;

                if (node.getUserObject() != null) {
                    Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) node.getUserObject();
                    setText(triple.getMiddle());
                    setIcon(triple.getRight() ? UIConstants.ICON_BULLET_GREEN : UIConstants.ICON_BULLET_RED);
                }
            }
            return this;
        }
    }

    private void setLibraryEnabled(boolean enabled) {
        for (int selectedRow : libraryTreeTable.getSelectedRows()) {
            TreePath selectedPath = libraryTreeTable.getPathForRow(selectedRow);
            if (selectedPath != null) {
                if (selectedPath.getPathCount() == 3) {
                    selectedPath = selectedPath.getParentPath();
                }

                MutableTreeTableNode selectedNode = (MutableTreeTableNode) selectedPath.getLastPathComponent();
                Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) ((MutableTreeTableNode) selectedPath.getLastPathComponent()).getUserObject();
                String libraryId = triple.getLeft();

                libraryTreeTable.getTreeTableModel().setValueAt(new ImmutableTriple<String, String, Boolean>(triple.getLeft(), triple.getMiddle(), enabled), selectedNode, 0);

                for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = selectedNode.children(); codeTemplateNodes.hasMoreElements();) {
                    MutableTreeTableNode codeTemplateNode = codeTemplateNodes.nextElement();
                    triple = (Triple<String, String, Boolean>) codeTemplateNode.getUserObject();
                    libraryTreeTable.getTreeTableModel().setValueAt(new ImmutableTriple<String, String, Boolean>(triple.getLeft(), triple.getMiddle(), enabled), codeTemplateNode, 0);
                }

                CodeTemplateLibrary library = libraryMap.get(libraryId);
                if (enabled) {
                    library.getDisabledChannelIds().remove(channelId);
                    library.getEnabledChannelIds().add(channelId);
                } else {
                    library.getDisabledChannelIds().add(channelId);
                    library.getEnabledChannelIds().remove(channelId);
                }
            }
        }
    }

    private JButton enableButton;
    private JButton disableButton;
    private JLabel expandAllLabel;
    private JLabel separatorLabel;
    private JLabel collapseAllLabel;
    private MirthTreeTable libraryTreeTable;
    private JScrollPane libraryTreeTableScrollPane;
}