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
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
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
import com.mirth.connect.util.CodeTemplateUtil;
import com.mirth.connect.util.MirthXmlUtil;

public class CodeTemplateLibrariesPanel extends JPanel {

    private ChannelDependenciesDialog parent;
    private Map<String, CodeTemplateLibrary> libraryMap = new HashMap<String, CodeTemplateLibrary>();
    private String channelId;
    private boolean changed = false;

    public CodeTemplateLibrariesPanel(ChannelDependenciesDialog parent, Channel channel) {
        this.parent = parent;
        this.channelId = channel.getId();
        initComponents(channel);
        initLayout();
    }

    public void initialize() {
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

                libraryTreeTable.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent evt) {
                        for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((DefaultMutableTreeTableNode) libraryTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                            Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) libraryNodes.nextElement().getUserObject();

                            CodeTemplateLibrary library = libraryMap.get(triple.getLeft());
                            if (triple.getRight()) {
                                library.getDisabledChannelIds().remove(channelId);
                                library.getEnabledChannelIds().add(channelId);
                            } else {
                                library.getDisabledChannelIds().add(channelId);
                                library.getEnabledChannelIds().remove(channelId);
                            }
                        }
                    }
                });

                parent.codeTemplateLibrariesReady();
            }
        });
    }

    public Map<String, CodeTemplateLibrary> getLibraryMap() {
        return libraryMap;
    }

    public boolean wasChanged() {
        return changed;
    }

    private void initComponents(Channel channel) {
        setBackground(UIConstants.BACKGROUND_COLOR);

        selectAllLabel = new JLabel("<html><u>Select All</u></html>");
        selectAllLabel.setForeground(Color.BLUE);
        selectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((MutableTreeTableNode) libraryTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                    MutableTreeTableNode libraryNode = libraryNodes.nextElement();
                    Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) libraryNode.getUserObject();
                    libraryTreeTable.getTreeTableModel().setValueAt(new MutableTriple<String, String, Boolean>(triple.getLeft(), triple.getMiddle(), true), libraryNode, libraryTreeTable.getHierarchicalColumn());
                }
                libraryTreeTable.updateUI();
                changed = true;
            }
        });

        selectSeparatorLabel = new JLabel("|");

        deselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        deselectAllLabel.setForeground(Color.BLUE);
        deselectAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((MutableTreeTableNode) libraryTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                    MutableTreeTableNode libraryNode = libraryNodes.nextElement();
                    Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) libraryNode.getUserObject();
                    libraryTreeTable.getTreeTableModel().setValueAt(new MutableTriple<String, String, Boolean>(triple.getLeft(), triple.getMiddle(), false), libraryNode, libraryTreeTable.getHierarchicalColumn());
                }
                libraryTreeTable.updateUI();
                changed = true;
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

        expandSeparatorLabel = new JLabel("|");

        collapseAllLabel = new JLabel("<html><u>Collapse All</u></html>");
        collapseAllLabel.setForeground(Color.BLUE);
        collapseAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                libraryTreeTable.collapseAll();
            }
        });

        final TableCellEditor libraryCellEditor = new LibraryTreeCellEditor();

        libraryTreeTable = new MirthTreeTable() {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (isHierarchical(column)) {
                    return libraryCellEditor;
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };

        DefaultTreeTableModel model = new SortableTreeTableModel();
        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode();
        model.setRoot(rootNode);

        libraryTreeTable.setLargeModel(true);
        libraryTreeTable.setTreeTableModel(model);
        libraryTreeTable.setOpenIcon(null);
        libraryTreeTable.setClosedIcon(null);
        libraryTreeTable.setLeafIcon(null);
        libraryTreeTable.setRootVisible(false);
        libraryTreeTable.setDoubleBuffered(true);
        libraryTreeTable.setDragEnabled(false);
        libraryTreeTable.setRowSelectionAllowed(true);
        libraryTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        libraryTreeTable.setFocusable(true);
        libraryTreeTable.setOpaque(true);
        libraryTreeTable.setEditable(true);
        libraryTreeTable.setSortable(false);
        libraryTreeTable.setAutoCreateColumnsFromModel(false);
        libraryTreeTable.setShowGrid(true, true);
        libraryTreeTable.setTableHeader(null);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            libraryTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        libraryTreeTable.setTreeCellRenderer(new LibraryTreeCellRenderer());

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
                    boolean visible = false;
                    int selectedRow = libraryTreeTable.getSelectedRow();

                    if (selectedRow >= 0) {
                        TreePath selectedPath = libraryTreeTable.getPathForRow(selectedRow);
                        if (selectedPath != null) {
                            visible = true;
                            Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) ((MutableTreeTableNode) selectedPath.getLastPathComponent()).getUserObject();
                            String description = "";

                            if (selectedPath.getPathCount() == 2) {
                                description = libraryMap.get(triple.getLeft()).getDescription();
                            } else if (selectedPath.getPathCount() == 3) {
                                description = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().get(triple.getLeft()).getDescription();
                            }

                            if (StringUtils.isBlank(description) || StringUtils.equals(description, CodeTemplateUtil.getDocumentation(CodeTemplate.DEFAULT_CODE).getDescription())) {
                                descriptionTextPane.setText("<html><body class=\"code-template-libraries-panel\"><i>No description.</i></body></html>");
                            } else {
                                descriptionTextPane.setText("<html><body class=\"code-template-libraries-panel\">" + MirthXmlUtil.encode(description) + "</body></html>");
                            }
                        }
                    }

                    descriptionScrollPane.setVisible(visible);
                    updateUI();
                }
            }
        });

        libraryTreeTableScrollPane = new JScrollPane(libraryTreeTable);

        descriptionTextPane = new JTextPane();
        descriptionTextPane.setContentType("text/html");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(".code-template-libraries-panel {font-family:\"Tahoma\";font-size:11;text-align:top}");
        descriptionTextPane.setEditorKit(editorKit);
        descriptionTextPane.setEditable(false);
        descriptionScrollPane = new JScrollPane(descriptionTextPane);
        descriptionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setVisible(false);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        add(selectAllLabel, "split 3");
        add(selectSeparatorLabel);
        add(deselectAllLabel);
        add(expandAllLabel, "right, split, w 51!");
        add(expandSeparatorLabel);
        add(collapseAllLabel, "w 60!");
        add(libraryTreeTableScrollPane, "newline, grow, sx, push");
        add(descriptionScrollPane, "newline, grow, sx, h 90!");
    }

    private class LibraryTreeCellRenderer extends JPanel implements TreeCellRenderer {

        private JCheckBox checkBox;
        private JLabel filler;
        private JLabel label;

        public LibraryTreeCellRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new JCheckBox();
            add(checkBox);
            filler = new JLabel();
            add(filler, "w 13!");
            label = new JLabel();
            add(label, "grow, push");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (selected) {
                setBackground(libraryTreeTable.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }
            checkBox.setBackground(getBackground());
            label.setBackground(getBackground());

            if (value != null) {
                MutableTreeTableNode node = (MutableTreeTableNode) value;

                if (node.getUserObject() != null) {
                    Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) node.getUserObject();
                    label.setText(triple.getMiddle());
                    checkBox.setSelected(triple.getRight());
                    if (node.getParent().getParent() == null) {
                        checkBox.setVisible(true);
                        filler.setVisible(false);
                    } else {
                        checkBox.setVisible(false);
                        filler.setVisible(true);
                    }
                }
            }
            return this;
        }
    }

    private class LibraryTreeCellEditor extends DefaultCellEditor {

        private CheckBoxPanel panel;
        private JCheckBox checkBox;
        private JLabel filler;
        private JLabel label;
        private String id;

        public LibraryTreeCellEditor() {
            super(new JCheckBox());
            panel = new CheckBoxPanel(new MigLayout("insets 1 0 0 0, novisualpadding, hidemode 3, fill"));
            checkBox = (JCheckBox) editorComponent;
            panel.add(checkBox);
            filler = new JLabel();
            panel.add(filler, "w 13!");
            label = new JLabel();
            panel.add(label, "grow, push");

            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    changed = true;
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return new ImmutableTriple<String, String, Boolean>(id, label.getText(), checkBox.isSelected());
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) value;
            if (triple != null) {
                id = triple.getLeft();
                label.setText(triple.getMiddle());
                checkBox.setSelected(triple.getRight());
            }

            MirthTreeTable treeTable = (MirthTreeTable) table;
            JTree tree = (JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn());
            panel.setOffset(tree.getRowBounds(row).x);
            panel.setBackground(table.getSelectionBackground());
            checkBox.setBackground(panel.getBackground());
            label.setBackground(panel.getBackground());

            TreePath path = treeTable.getPathForRow(row);
            if (path != null && path.getPathCount() == 2) {
                checkBox.setVisible(true);
                filler.setVisible(false);
            } else {
                checkBox.setVisible(false);
                filler.setVisible(true);
            }

            return panel;
        }

        private class CheckBoxPanel extends JPanel {

            private int offset;

            public CheckBoxPanel(LayoutManager layout) {
                super(layout);
            }

            public void setOffset(int offset) {
                this.offset = offset;
            }

            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x + offset, y, width - offset, height);
            }
        }
    }

    private JLabel selectAllLabel;
    private JLabel selectSeparatorLabel;
    private JLabel deselectAllLabel;
    private JLabel expandAllLabel;
    private JLabel expandSeparatorLabel;
    private JLabel collapseAllLabel;
    private MirthTreeTable libraryTreeTable;
    private JScrollPane libraryTreeTableScrollPane;
    private JTextPane descriptionTextPane;
    private JScrollPane descriptionScrollPane;
}