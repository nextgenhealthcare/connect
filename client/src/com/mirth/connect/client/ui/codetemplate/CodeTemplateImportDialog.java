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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;

public class CodeTemplateImportDialog extends MirthDialog {

    private static final int IMPORT_SELECTED_COLUMN = 0;
    private static final int IMPORT_NAME_COLUMN = 1;
    private static final int IMPORT_OVERWRITE_COLUMN = 2;
    private static final int IMPORT_CONFLICTS_COLUMN = 3;
    private static final int IMPORT_ID_COLUMN = 4;

    private List<CodeTemplateLibrary> importLibraries;
    private Map<String, CodeTemplateLibrary> importLibraryMap;
    private Map<String, CodeTemplate> importCodeTemplateMap;

    private boolean unassignedCodeTemplates;
    private boolean showCancelAlert;
    private boolean saved;
    private Map<String, CodeTemplateLibrary> updatedLibraries;
    private Map<String, CodeTemplate> updatedCodeTemplates;

    public CodeTemplateImportDialog(Frame owner, List<CodeTemplateLibrary> importLibraries, boolean unassignedCodeTemplates) {
        this(owner, importLibraries, unassignedCodeTemplates, false);
    }

    public CodeTemplateImportDialog(Frame owner, List<CodeTemplateLibrary> importLibraries, boolean unassignedCodeTemplates, boolean showCancelAlert) {
        super(owner, "Import Code Templates / Libraries", true);

        // Remove skeleton code templates if they exist
        for (CodeTemplateLibrary library : importLibraries) {
            for (Iterator<CodeTemplate> it = library.getCodeTemplates().iterator(); it.hasNext();) {
                if (it.next().getProperties() == null) {
                    it.remove();
                }
            }
        }

        this.importLibraries = importLibraries;
        this.unassignedCodeTemplates = unassignedCodeTemplates;
        this.showCancelAlert = showCancelAlert;

        importLibraryMap = new HashMap<String, CodeTemplateLibrary>();
        importCodeTemplateMap = new HashMap<String, CodeTemplate>();

        initComponents();
        initLayout();

        if (showCancelAlert) {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    if (confirmClose()) {
                        dispose();
                    }
                }
            });

            ActionListener closeAction = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (confirmClose()) {
                        dispose();
                    }
                }
            };

            getRootPane().registerKeyboardAction(closeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        } else {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        updateErrorsAndWarnings();

        setPreferredSize(new Dimension(420, 420));
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }

    public boolean wasSaved() {
        return saved;
    }

    public Map<String, CodeTemplateLibrary> getUpdatedLibraries() {
        return updatedLibraries;
    }

    public Map<String, CodeTemplate> getUpdatedCodeTemplates() {
        return updatedCodeTemplates;
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());

        topPanel = new JPanel();
        topPanel.setBackground(getBackground());

        linkPanel = new JPanel();
        linkPanel.setBackground(topPanel.getBackground());

        linkLeftPanel = new JPanel();
        linkLeftPanel.setBackground(linkPanel.getBackground());

        linkLeftSelectAllLabel = new JLabel("<html><u>All</u></html>");
        linkLeftSelectAllLabel.setForeground(Color.BLUE);
        linkLeftSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLeftSelectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    importTreeTable.getModel().setValueAt(true, row, IMPORT_SELECTED_COLUMN);
                }
            }
        });

        linkLeftDeselectAllLabel = new JLabel("<html><u>None</u></html>");
        linkLeftDeselectAllLabel.setForeground(Color.BLUE);
        linkLeftDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLeftDeselectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    importTreeTable.getModel().setValueAt(false, row, IMPORT_SELECTED_COLUMN);
                }
            }
        });

        linkRightPanel = new JPanel();
        linkRightPanel.setBackground(linkPanel.getBackground());

        linkRightOverwriteAllLabel = new JLabel("<html><u>All</u></html>");
        linkRightOverwriteAllLabel.setForeground(Color.BLUE);
        linkRightOverwriteAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkRightOverwriteAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    TreePath path = importTreeTable.getPathForRow(row);
                    if (path != null) {
                        ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();
                        if (node instanceof ImportLibraryTreeTableNode) {
                            ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                            if (libraryNode.getConflicts().getMatchingLibrary() != null) {
                                importTreeTable.getModel().setValueAt(true, row, IMPORT_OVERWRITE_COLUMN);
                            }
                        } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                            if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null) {
                                importTreeTable.getModel().setValueAt(true, row, IMPORT_OVERWRITE_COLUMN);
                            }
                        }
                    }
                }
            }
        });

        linkRightOverwriteNoneLabel = new JLabel("<html><u>None</u></html>");
        linkRightOverwriteNoneLabel.setForeground(Color.BLUE);
        linkRightOverwriteNoneLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkRightOverwriteNoneLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    TreePath path = importTreeTable.getPathForRow(row);
                    if (path != null) {
                        ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();
                        if (node instanceof ImportLibraryTreeTableNode) {
                            ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                            if (libraryNode.getConflicts().getMatchingLibrary() != null) {
                                importTreeTable.getModel().setValueAt(false, row, IMPORT_OVERWRITE_COLUMN);
                            }
                        } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                            if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null) {
                                importTreeTable.getModel().setValueAt(false, row, IMPORT_OVERWRITE_COLUMN);
                            }
                        }
                    }
                }
            }
        });

        final TableCellEditor templateCellEditor = new NameCellEditor();

        importTreeTable = new JXTreeTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return (column == IMPORT_OVERWRITE_COLUMN || column == IMPORT_SELECTED_COLUMN || column == IMPORT_NAME_COLUMN);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (isHierarchical(column)) {
                    return templateCellEditor;
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };

        importTreeTable.setLargeModel(true);
        DefaultTreeTableModel model = new ImportTreeTableModel();
        model.setColumnIdentifiers(Arrays.asList(new String[] { "", "Name", "Overwrite",
                "Conflicts", "Id" }));

        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode();
        model.setRoot(rootNode);
        importTreeTable.setTreeTableModel(model);

        Set<String> addedCodeTemplateIds = new HashSet<String>();

        if (unassignedCodeTemplates) {
            ImportTreeTableNode libraryNode = new ImportUnassignedLibraryTreeTableNode("Select a library", "");
            CodeTemplateLibrary library = importLibraries.get(0);

            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                if (!addedCodeTemplateIds.contains(codeTemplate.getId())) {
                    libraryNode.add(new ImportCodeTemplateTreeTableNode(codeTemplate.getName(), codeTemplate.getId()));
                    addedCodeTemplateIds.add(codeTemplate.getId());
                    importCodeTemplateMap.put(codeTemplate.getId(), codeTemplate);
                }
            }

            rootNode.add(libraryNode);
        } else {
            Set<String> addedLibraryIds = new HashSet<String>();

            for (CodeTemplateLibrary library : importLibraries) {
                if (!addedLibraryIds.contains(library.getId())) {
                    ImportTreeTableNode libraryNode = new ImportLibraryTreeTableNode(library.getName(), library.getId());
                    importLibraryMap.put(library.getId(), library);

                    for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                        if (!addedCodeTemplateIds.contains(codeTemplate.getId())) {
                            libraryNode.add(new ImportCodeTemplateTreeTableNode(codeTemplate.getName(), codeTemplate.getId()));
                            addedCodeTemplateIds.add(codeTemplate.getId());
                            importCodeTemplateMap.put(codeTemplate.getId(), codeTemplate);
                        }
                    }

                    rootNode.add(libraryNode);
                    addedLibraryIds.add(library.getId());
                }
            }
        }

        importTreeTable.setOpenIcon(null);
        importTreeTable.setClosedIcon(null);
        importTreeTable.setLeafIcon(null);
        importTreeTable.setRootVisible(false);
        importTreeTable.setDoubleBuffered(true);
        importTreeTable.setDragEnabled(false);
        importTreeTable.setRowSelectionAllowed(true);
        importTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        importTreeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        importTreeTable.setFocusable(true);
        importTreeTable.setOpaque(true);
        importTreeTable.getTableHeader().setReorderingAllowed(false);
        importTreeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        importTreeTable.setEditable(true);
        importTreeTable.setSortable(false);
        importTreeTable.setAutoCreateColumnsFromModel(false);
        importTreeTable.setShowGrid(true, true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            importTreeTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        importTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelection(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelection(evt);
            }

            private void checkSelection(MouseEvent evt) {
                int row = importTreeTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

                if (row < 0) {
                    importTreeTable.clearSelection();
                }
            }
        });

        importTreeTable.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                throw new ExpandVetoException(event);
            }
        });

        importTreeTable.setTreeCellRenderer(new NameCellRenderer());

        importTreeTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                if (evt.getColumn() != IMPORT_CONFLICTS_COLUMN) {
                    for (int row = evt.getFirstRow(); row <= evt.getLastRow() && row < importTreeTable.getRowCount(); row++) {
                        TreePath path = importTreeTable.getPathForRow(row);
                        if (path != null) {
                            ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                            if (path.getPathCount() == 2) {
                                if (node instanceof ImportUnassignedLibraryTreeTableNode) {
                                    String libraryName = (String) node.getValueAt(IMPORT_NAME_COLUMN);
                                    String libraryId = null;
                                    for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                                        if (library.getName().equals(libraryName)) {
                                            libraryId = library.getId();
                                            break;
                                        }
                                    }
                                    node.setValueAt(libraryId, IMPORT_ID_COLUMN);
                                } else if (node instanceof ImportLibraryTreeTableNode) {
                                    ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                                    libraryNode.setConflicts(getLibraryConflicts(node));
                                }

                                for (Enumeration<? extends TreeTableNode> codeTemplateNodes = node.children(); codeTemplateNodes.hasMoreElements();) {
                                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                                    codeTemplateNode.setConflicts(getCodeTemplateConflicts(codeTemplateNode));
                                }

                                importTreeTable.updateUI();
                            } else if (path.getPathCount() == 3) {
                                ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                                codeTemplateNode.setConflicts(getCodeTemplateConflicts(node));
                            }
                        }
                    }
                }

                updateImportButton();
                updateErrorsAndWarnings();
            }
        });

        importTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    updateImportButton();
                    updateErrorsAndWarnings();
                }
            }
        });

        importTreeTable.expandAll();

        importTreeTable.getColumnModel().getColumn(IMPORT_SELECTED_COLUMN).setMinWidth(20);
        importTreeTable.getColumnModel().getColumn(IMPORT_SELECTED_COLUMN).setMaxWidth(20);
        importTreeTable.getColumnModel().getColumn(IMPORT_SELECTED_COLUMN).setCellRenderer(new ImportSelectedCellRenderer());
        importTreeTable.getColumnModel().getColumn(IMPORT_SELECTED_COLUMN).setCellEditor(new ImportSelectedCellEditor());

        importTreeTable.getColumnModel().getColumn(IMPORT_OVERWRITE_COLUMN).setMinWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_OVERWRITE_COLUMN).setMaxWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_OVERWRITE_COLUMN).setCellRenderer(new OverwriteCellRenderer());
        importTreeTable.getColumnModel().getColumn(IMPORT_OVERWRITE_COLUMN).setCellEditor(new OverwriteCellEditor());

        importTreeTable.getColumnModel().getColumn(IMPORT_CONFLICTS_COLUMN).setMinWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_CONFLICTS_COLUMN).setMaxWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_CONFLICTS_COLUMN).setCellRenderer(new IconCellRenderer());

        importTreeTable.getColumnModel().removeColumn(importTreeTable.getColumnModel().getColumn(IMPORT_ID_COLUMN));

        importTreeTableScrollPane = new JScrollPane(importTreeTable);
        importTreeTableScrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0x6E6E6E)));

        warningsPanel = new JPanel();
        warningsPanel.setBackground(getBackground());
        warningsPanel.setVisible(false);

        warningsLabel = new JLabel(UIConstants.ICON_WARNING);

        warningsTextArea = new JTextArea();
        warningsTextArea.setLineWrap(true);
        warningsTextArea.setWrapStyleWord(true);

        errorsPanel = new JPanel();
        errorsPanel.setBackground(getBackground());
        errorsPanel.setVisible(false);

        errorsLabel = new JLabel(UIConstants.ICON_ERROR);

        errorsTextArea = new JTextArea();
        errorsTextArea.setLineWrap(true);
        errorsTextArea.setWrapStyleWord(true);

        separator = new JSeparator();

        buttonPanel = new JPanel();
        buttonPanel.setBackground(getBackground());

        importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    boolean warnings = false;
                    for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                        for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNodes.nextElement().children(); codeTemplateNodes.hasMoreElements();) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();

                            if ((boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                                CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                                if (conflicts.getMatchingCodeTemplate() != null) {
                                    warnings = true;
                                    break;
                                }
                            }
                        }

                        if (warnings) {
                            break;
                        }
                    }

                    if (!warnings || PlatformUI.MIRTH_FRAME.alertOption(CodeTemplateImportDialog.this, "Some selected rows have warnings. Are you sure you wish to continue?")) {
                        save();
                        dispose();
                    }
                } catch (Exception e) {
                    PlatformUI.MIRTH_FRAME.alertThrowable(CodeTemplateImportDialog.this, e, "Unable to import: " + e.getMessage());
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (confirmClose()) {
                    dispose();
                }
            }
        });

        updateImportButton();
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));

        topPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        linkPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        linkLeftPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        linkLeftPanel.add(new JLabel("Select:"));
        linkLeftPanel.add(linkLeftSelectAllLabel);
        linkLeftPanel.add(new JLabel("|"));
        linkLeftPanel.add(linkLeftDeselectAllLabel);
        linkPanel.add(linkLeftPanel, "left");

        linkRightPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        linkRightPanel.add(new JLabel("Overwrite:"));
        linkRightPanel.add(linkRightOverwriteAllLabel);
        linkRightPanel.add(new JLabel("|"));
        linkRightPanel.add(linkRightOverwriteNoneLabel);
        linkPanel.add(linkRightPanel, "right");

        topPanel.add(linkPanel, "grow");

        topPanel.add(importTreeTableScrollPane, "newline, grow, push");
        add(topPanel, "grow");

        errorsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        errorsPanel.add(errorsLabel, "top");
        errorsPanel.add(errorsTextArea, "grow, push");
        add(errorsPanel, "newline, grow");

        warningsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        warningsPanel.add(warningsLabel, "top");
        warningsPanel.add(warningsTextArea, "grow, push");
        add(warningsPanel, "newline, grow");

        add(separator, "newline, growx, pushx");

        buttonPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, "newline, right");
    }

    private enum ConflictValue {
        NONE, WARNING, ERROR;
    }

    private class ImportTreeTableModel extends DefaultTreeTableModel {

        @Override
        public int getHierarchicalColumn() {
            return IMPORT_NAME_COLUMN;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            // @formatter:off
            switch(column) {
                case IMPORT_SELECTED_COLUMN: return Boolean.class;
                case IMPORT_NAME_COLUMN: return String.class;
                case IMPORT_OVERWRITE_COLUMN: return Boolean.class;
                case IMPORT_CONFLICTS_COLUMN: return ConflictValue.class;
                case IMPORT_ID_COLUMN: return String.class;
                default: return String.class;
            }
            // @formatter:on
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return column == IMPORT_SELECTED_COLUMN || column == IMPORT_NAME_COLUMN || column == IMPORT_OVERWRITE_COLUMN;
        }
    }

    private abstract class ImportTreeTableNode extends AbstractMutableTreeTableNode {

        protected boolean selectionValue;
        private String name;
        private Boolean overwrite;
        private ConflictValue conflictValue;
        private String id;

        public ImportTreeTableNode(String name, String id) {
            this.selectionValue = true;
            this.name = name;
            this.overwrite = null;
            this.conflictValue = ConflictValue.NONE;
            this.id = id;
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int column) {
            // @formatter:off
            switch (column) {
                case IMPORT_SELECTED_COLUMN: return selectionValue;
                case IMPORT_NAME_COLUMN: return name;
                case IMPORT_OVERWRITE_COLUMN: return overwrite;
                case IMPORT_CONFLICTS_COLUMN: return conflictValue;
                case IMPORT_ID_COLUMN: return id;
                default: return null;
            }
            // @formatter:on
        }

        @Override
        public void setValueAt(Object value, int i) {
            switch (i) {
                case IMPORT_SELECTED_COLUMN:
                    selectionValue = (boolean) value;
                    break;
                case IMPORT_NAME_COLUMN:
                    name = (String) value;
                    break;
                case IMPORT_OVERWRITE_COLUMN:
                    overwrite = (Boolean) value;
                    break;
                case IMPORT_CONFLICTS_COLUMN:
                    conflictValue = (ConflictValue) value;
                    break;
                case IMPORT_ID_COLUMN:
                    id = (String) value;
                    break;
            }
        }
    }

    private class ImportCodeTemplateTreeTableNode extends ImportTreeTableNode {

        private CodeTemplateConflicts conflicts = new CodeTemplateConflicts();

        public ImportCodeTemplateTreeTableNode(String name, String id) {
            super(name, id);
        }

        public CodeTemplateConflicts getConflicts() {
            return conflicts;
        }

        public void setConflicts(CodeTemplateConflicts conflicts) {
            this.conflicts = conflicts;
            if (selectionValue) {
                if (conflicts.isConflictByName() || conflicts.isUnassignedConflict()) {
                    setValueAt(ConflictValue.ERROR, IMPORT_CONFLICTS_COLUMN);
                } else if (conflicts.getMatchingCodeTemplate() != null) {
                    setValueAt(ConflictValue.WARNING, IMPORT_CONFLICTS_COLUMN);
                } else {
                    setValueAt(ConflictValue.NONE, IMPORT_CONFLICTS_COLUMN);
                }
            } else {
                setValueAt(ConflictValue.NONE, IMPORT_CONFLICTS_COLUMN);
            }
        }
    }

    private class ImportLibraryTreeTableNode extends ImportTreeTableNode {

        private LibraryConflicts conflicts = new LibraryConflicts();

        public ImportLibraryTreeTableNode(String name, String id) {
            super(name, id);
        }

        public LibraryConflicts getConflicts() {
            return conflicts;
        }

        public void setConflicts(LibraryConflicts conflicts) {
            this.conflicts = conflicts;

            if (selectionValue && conflicts.isConflictByName()) {
                setValueAt(ConflictValue.ERROR, IMPORT_CONFLICTS_COLUMN);
            } else {
                setValueAt(ConflictValue.NONE, IMPORT_CONFLICTS_COLUMN);
            }
        }
    }

    private class ImportUnassignedLibraryTreeTableNode extends ImportTreeTableNode {

        public ImportUnassignedLibraryTreeTableNode(String name, String id) {
            super(name, id);
        }

        @Override
        public void setValueAt(Object value, int i) {
            if (i != IMPORT_SELECTED_COLUMN) {
                super.setValueAt(value, i);
            }
        }
    }

    private class ImportSelectedCellRenderer extends JPanel implements TableCellRenderer {

        private JCheckBox checkBox;

        public ImportSelectedCellRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new JCheckBox();
            add(checkBox, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }
            checkBox.setBackground(getBackground());
            checkBox.setSelected(value != null && (boolean) value);

            boolean visible = true;
            TreePath path = importTreeTable.getPathForRow(row);
            if (path != null) {
                ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();
                if (node instanceof ImportUnassignedLibraryTreeTableNode) {
                    visible = false;
                }
            }
            checkBox.setVisible(visible);

            return this;
        }
    }

    private class ImportSelectedCellEditor extends DefaultCellEditor {

        private JPanel panel;
        private JCheckBox checkBox;

        public ImportSelectedCellEditor() {
            super(new JCheckBox());
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = (JCheckBox) editorComponent;
            panel.add(checkBox, "center");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            super.getTableCellEditorComponent(table, value, isSelected, row, column);
            panel.setBackground(table.getSelectionBackground());
            checkBox.setBackground(panel.getBackground());
            checkBox.setSelected(value != null && (boolean) value);

            boolean visible = true;
            TreePath path = importTreeTable.getPathForRow(row);
            if (path != null) {
                ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();
                if (node instanceof ImportUnassignedLibraryTreeTableNode) {
                    visible = false;
                }
            }
            checkBox.setVisible(visible);

            return panel;
        }
    }

    private class NameCellRenderer extends JPanel implements TreeCellRenderer {

        private JLabel label;
        private JComboBox<String> comboBox;

        public NameCellRenderer() {
            setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            label = new JLabel();
            label.setVisible(false);
            add(label);

            comboBox = new JComboBox<String>();
            List<String> libraryNames = new ArrayList<String>();
            for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                libraryNames.add(library.getName());
            }
            comboBox.setModel(new DefaultComboBoxModel<String>(libraryNames.toArray(new String[libraryNames.size()])));

            comboBox.setVisible(false);
            add(comboBox);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (selected) {
                setBackground(importTreeTable.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }

            if (value != null && value instanceof ImportTreeTableNode) {
                ImportTreeTableNode node = (ImportTreeTableNode) value;

                if (node instanceof ImportUnassignedLibraryTreeTableNode) {
                    label.setVisible(false);
                    comboBox.setVisible(true);

                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4515838
                    // Workaround to remove the border around the comboBox
                    for (int i = 0; i < comboBox.getComponentCount(); i++) {
                        if (comboBox.getComponent(i) instanceof AbstractButton) {
                            ((AbstractButton) comboBox.getComponent(i)).setBorderPainted(false);
                        }
                    }

                    comboBox.setBackground(getBackground());

                    for (int i = 0; i < comboBox.getComponentCount(); i++) {
                        if (comboBox.getComponent(i) instanceof AbstractButton) {
                            comboBox.getComponent(i).setBackground(comboBox.getBackground());
                        }
                    }

                    String name = (String) node.getValueAt(IMPORT_NAME_COLUMN);

                    List<String> libraryNames = new ArrayList<String>();
                    for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                        libraryNames.add(library.getName());
                    }
                    if (!libraryNames.contains(name)) {
                        libraryNames.add(0, name);
                    }
                    comboBox.setModel(new DefaultComboBoxModel<String>(libraryNames.toArray(new String[libraryNames.size()])));

                    comboBox.setSelectedItem(name);
                } else {
                    label.setVisible(true);
                    comboBox.setVisible(false);

                    label.setEnabled(true);
                    label.setBackground(getBackground());
                    label.setText((String) node.getValueAt(IMPORT_NAME_COLUMN));

                    if (!(boolean) node.getValueAt(IMPORT_SELECTED_COLUMN)) {
                        label.setEnabled(false);
                    }
                }
            }

            return this;
        }

    }

    private class NameCellEditor extends DefaultCellEditor {

        private OffsetPanel panel;
        private JTextField field;
        private JComboBox<String> comboBox;

        public NameCellEditor() {
            super(new JTextField());
            panel = new OffsetPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            field = (JTextField) editorComponent;
            field.setBackground(UIConstants.BACKGROUND_COLOR);
            field.setVisible(false);
            panel.add(field, "grow, push");

            comboBox = new JComboBox<String>();
            List<String> libraryNames = new ArrayList<String>();
            for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                libraryNames.add(library.getName());
            }
            comboBox.setModel(new DefaultComboBoxModel<String>(libraryNames.toArray(new String[libraryNames.size()])));

            comboBox.setMaximumRowCount(20);
            comboBox.setRenderer(new DataTypeListCellRenderer());

            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4515838
            // Workaround to remove the border around the comboBox
            for (int i = 0; i < comboBox.getComponentCount(); i++) {
                if (comboBox.getComponent(i) instanceof AbstractButton) {
                    ((AbstractButton) comboBox.getComponent(i)).setBorderPainted(false);
                }
            }

            comboBox.setVisible(false);
            panel.add(comboBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JXTreeTable treeTable = (JXTreeTable) table;
            JTree tree = (JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn());
            panel.setOffset(tree.getRowBounds(row).x);
            panel.setBackground(table.getSelectionBackground());

            ImportTreeTableNode node = (ImportTreeTableNode) treeTable.getPathForRow(row).getLastPathComponent();

            if (node instanceof ImportUnassignedLibraryTreeTableNode) {
                field.setVisible(false);
                comboBox.setVisible(true);

                comboBox.setBackground(panel.getBackground());

                for (int i = 0; i < comboBox.getComponentCount(); i++) {
                    if (comboBox.getComponent(i) instanceof AbstractButton) {
                        comboBox.getComponent(i).setBackground(comboBox.getBackground());
                    }
                }

                String name = (String) value;

                List<String> libraryNames = new ArrayList<String>();
                for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
                    libraryNames.add(library.getName());
                }
                if (!libraryNames.contains(name)) {
                    libraryNames.add(0, name);
                }
                comboBox.setModel(new DefaultComboBoxModel<String>(libraryNames.toArray(new String[libraryNames.size()])));

                comboBox.setSelectedItem(name);

                comboBox.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {}

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
                        fireEditingStopped();
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent evt) {
                        fireEditingCanceled();
                    }
                });
            } else {
                field.setVisible(true);
                comboBox.setVisible(false);

                field.setText((String) value);
            }

            return panel;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            int selectedRow = importTreeTable.getSelectedRow();
            if (selectedRow >= 0) {
                TreePath selectedPath = importTreeTable.getPathForRow(selectedRow);
                if (selectedPath != null && !(selectedPath.getLastPathComponent() instanceof ImportUnassignedLibraryTreeTableNode) && !(boolean) importTreeTable.getModel().getValueAt(selectedRow, IMPORT_SELECTED_COLUMN)) {
                    return false;
                }
            }

            return evt != null && evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2;
        }

        @Override
        public Object getCellEditorValue() {
            if (field.isVisible()) {
                return StringUtils.trim((String) super.getCellEditorValue());
            } else {
                return (String) comboBox.getSelectedItem();
            }
        }

        @Override
        public boolean stopCellEditing() {
            String value = (String) getCellEditorValue();
            boolean valid = true;

            if (StringUtils.isBlank(value)) {
                valid = false;
            }

            if (valid) {
                for (Enumeration<? extends MutableTreeTableNode> libraryNodes = ((MutableTreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                    ImportTreeTableNode libraryNode = (ImportTreeTableNode) libraryNodes.nextElement();
                    if (libraryNode.getValueAt(IMPORT_NAME_COLUMN).equals(value)) {
                        valid = false;
                        break;
                    }

                    for (Enumeration<? extends MutableTreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                        ImportTreeTableNode codeTemplateNode = (ImportTreeTableNode) codeTemplateNodes.nextElement();
                        if (codeTemplateNode.getValueAt(IMPORT_NAME_COLUMN).equals(value)) {
                            valid = false;
                            break;
                        }
                    }

                    if (!valid) {
                        break;
                    }
                }
            }

            if (!valid) {
                super.cancelCellEditing();
            }

            return super.stopCellEditing();
        }

        private class OffsetPanel extends JPanel {

            private int offset;

            public OffsetPanel(LayoutManager layout) {
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

        private class DataTypeListCellRenderer extends DefaultListCellRenderer {

            public DataTypeListCellRenderer() {}

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (index >= 0) {
                    if (!isSelected) {
                        component.setBackground(UIConstants.BACKGROUND_COLOR);
                    }
                }

                return component;
            }
        }
    }

    private class OverwriteCellRenderer extends JPanel implements TableCellRenderer {

        private JCheckBox checkBox;

        public OverwriteCellRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = new JCheckBox();
            add(checkBox, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }
            checkBox.setBackground(getBackground());
            checkBox.setSelected(value != null && (Boolean) value);
            checkBox.setEnabled(true);

            boolean visible = false;
            TreePath path = importTreeTable.getPathForRow(row);
            if (path != null) {
                ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();
                checkBox.setEnabled((boolean) node.getValueAt(IMPORT_SELECTED_COLUMN));

                if (node instanceof ImportLibraryTreeTableNode) {
                    ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                    LibraryConflicts conflicts = libraryNode.getConflicts();
                    if (conflicts.getMatchingLibrary() != null) {
                        visible = true;
                    }
                } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                    CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                    if (conflicts.getMatchingCodeTemplate() != null && conflicts.getMatchingLibrary() != null && conflicts.getMatchingLibrary().getId().equals(node.getParent().getValueAt(IMPORT_ID_COLUMN))) {
                        visible = true;
                    }
                }
            }
            checkBox.setVisible(visible);

            return this;
        }
    }

    private class OverwriteCellEditor extends DefaultCellEditor {

        private JPanel panel;
        private JCheckBox checkBox;

        public OverwriteCellEditor() {
            super(new JCheckBox());
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            checkBox = (JCheckBox) editorComponent;
            panel.add(checkBox, "center");
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            int selectedRow = importTreeTable.getSelectedRow();
            return super.isCellEditable(anEvent) && (selectedRow < 0 || (boolean) importTreeTable.getModel().getValueAt(selectedRow, IMPORT_SELECTED_COLUMN));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            super.getTableCellEditorComponent(table, value, isSelected, row, column);

            panel.setBackground(table.getSelectionBackground());
            checkBox.setBackground(panel.getBackground());
            checkBox.setSelected(value != null && (Boolean) value);

            boolean visible = false;
            TreePath path = importTreeTable.getPathForRow(row);
            if (path != null) {
                ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                if (node instanceof ImportLibraryTreeTableNode) {
                    ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                    LibraryConflicts conflicts = libraryNode.getConflicts();
                    if (conflicts.getMatchingLibrary() != null) {
                        visible = true;
                    }
                } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                    CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                    if (conflicts.getMatchingCodeTemplate() != null && conflicts.getMatchingLibrary() != null && conflicts.getMatchingLibrary().getId().equals(node.getParent().getValueAt(IMPORT_ID_COLUMN))) {
                        visible = true;
                    }
                }
            }
            checkBox.setVisible(visible);

            return panel;
        }
    }

    private class IconCellRenderer extends JPanel implements TableCellRenderer {

        private JLabel label;

        public IconCellRenderer() {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            label = new JLabel();
            add(label, "center");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }
            label.setBackground(getBackground());
            if (value != null) {
                ConflictValue conflictValue = (ConflictValue) value;
                label.setVisible(conflictValue != ConflictValue.NONE);
                label.setIcon(conflictValue == ConflictValue.ERROR ? UIConstants.ICON_ERROR : UIConstants.ICON_WARNING);
            } else {
                label.setVisible(false);
            }
            return this;
        }

    }

    private class EffectiveNames {
        private Map<String, String> libraryNameMap;
        private Map<String, Map<String, String>> codeTemplateNameMap;

        /*
         * The new names maps reflect entities that either have no matching cached entry, or that do
         * have a matching cached entry but are not being overwritten.
         */
        private Map<String, String> newLibraryNames;
        private Map<String, Map<String, String>> newCodeTemplateNames;

        public EffectiveNames(Map<String, String> libraryNameMap, Map<String, Map<String, String>> codeTemplateNameMap, Map<String, String> newLibraryNames, Map<String, Map<String, String>> newCodeTemplateNames) {
            this.libraryNameMap = libraryNameMap;
            this.codeTemplateNameMap = codeTemplateNameMap;
            this.newLibraryNames = newLibraryNames;
            this.newCodeTemplateNames = newCodeTemplateNames;
        }

        public Map<String, String> getLibraryNameMap() {
            return libraryNameMap;
        }

        public Map<String, Map<String, String>> getCodeTemplateNameMap() {
            return codeTemplateNameMap;
        }

        public Map<String, String> getNewLibraryNames() {
            return newLibraryNames;
        }

        public Map<String, Map<String, String>> getNewCodeTemplateNames() {
            return newCodeTemplateNames;
        }
    }

    private EffectiveNames getEffectiveNames() {
        Map<String, String> libraryNameMap = new HashMap<String, String>();
        Map<String, Map<String, String>> codeTemplateNameMap = new HashMap<String, Map<String, String>>();
        Map<String, String> newLibraryNames = new HashMap<String, String>();
        Map<String, Map<String, String>> newCodeTemplateNames = new HashMap<String, Map<String, String>>();

        for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            libraryNameMap.put(library.getId(), library.getName().toLowerCase());

            Map<String, String> codeTemplateMap = new HashMap<String, String>();
            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                codeTemplate = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().get(codeTemplate.getId());
                codeTemplateMap.put(codeTemplate.getId(), codeTemplate.getName().toLowerCase());
            }
            codeTemplateNameMap.put(library.getId(), codeTemplateMap);
        }

        for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
            ImportTreeTableNode libraryNode = (ImportTreeTableNode) libraryNodes.nextElement();
            String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
            String libraryName = (String) libraryNode.getValueAt(IMPORT_NAME_COLUMN);
            boolean librarySelected = (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN);
            Boolean libraryOverwrite = (Boolean) libraryNode.getValueAt(IMPORT_OVERWRITE_COLUMN);

            if (libraryNode instanceof ImportLibraryTreeTableNode && librarySelected) {
                CodeTemplateLibrary matchingLibrary = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().get(libraryId);

                if (matchingLibrary != null && libraryOverwrite != null && libraryOverwrite) {
                    libraryNameMap.put(libraryId, libraryName.toLowerCase());
                } else {
                    newLibraryNames.put(libraryId, libraryName.toLowerCase());
                    newCodeTemplateNames.put(libraryId, new HashMap<String, String>());
                }
            }

            for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                ImportTreeTableNode codeTemplateNode = (ImportTreeTableNode) codeTemplateNodes.nextElement();
                String codeTemplateId = (String) codeTemplateNode.getValueAt(IMPORT_ID_COLUMN);
                String codeTemplateName = (String) codeTemplateNode.getValueAt(IMPORT_NAME_COLUMN);
                boolean codeTemplateSelected = (boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN);
                Boolean codeTemplateOverwrite = (Boolean) codeTemplateNode.getValueAt(IMPORT_OVERWRITE_COLUMN);

                if (codeTemplateSelected) {
                    CodeTemplate matchingCodeTemplate = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().get(codeTemplateId);

                    if (matchingCodeTemplate != null && codeTemplateOverwrite != null && codeTemplateOverwrite) {
                        Map<String, String> codeTemplateMap = codeTemplateNameMap.get(libraryId);
                        if (codeTemplateMap == null) {
                            codeTemplateMap = new HashMap<String, String>();
                            codeTemplateNameMap.put(libraryId, codeTemplateMap);
                        }
                        codeTemplateMap.put(codeTemplateId, codeTemplateName.toLowerCase());
                    } else {
                        Map<String, String> codeTemplateMap = newCodeTemplateNames.get(libraryId);
                        if (codeTemplateMap == null) {
                            codeTemplateMap = new HashMap<String, String>();
                            newCodeTemplateNames.put(libraryId, codeTemplateMap);
                        }
                        codeTemplateMap.put(codeTemplateId, codeTemplateName.toLowerCase());
                    }
                }
            }
        }

        return new EffectiveNames(libraryNameMap, codeTemplateNameMap, newLibraryNames, newCodeTemplateNames);
    }

    private LibraryConflicts getLibraryConflicts(ImportTreeTableNode libraryNode) {
        LibraryConflicts conflicts = new LibraryConflicts();
        String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
        String libraryName = ((String) libraryNode.getValueAt(IMPORT_NAME_COLUMN)).toLowerCase();
        Boolean libraryOverwrite = (Boolean) libraryNode.getValueAt(IMPORT_OVERWRITE_COLUMN);
        boolean overwrite = libraryOverwrite != null && libraryOverwrite;

        // Determine if it conflicts by ID
        CodeTemplateLibrary matchingLibrary = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().get(libraryId);
        conflicts.setMatchingLibrary(matchingLibrary);

        // Determine if it conflicts by name
        EffectiveNames effectiveNames = getEffectiveNames();

        /*
         * Always conflict when it's the same name but a different ID. If the same name and same ID,
         * only conflict if there's a matching cached library and it's not being overwritten.
         */
        for (Entry<String, String> entry : effectiveNames.getLibraryNameMap().entrySet()) {
            if (entry.getValue().equals(libraryName) && (!entry.getKey().equals(libraryId) || matchingLibrary != null && !overwrite)) {
                conflicts.setConflictByName(true);
            }
        }

        // If the name is found in the new library names map, it's only a conflict if it's a different ID
        for (Entry<String, String> entry : effectiveNames.getNewLibraryNames().entrySet()) {
            if (entry.getValue().equals(libraryName) && !entry.getKey().equals(libraryId)) {
                conflicts.setConflictByName(true);
            }
        }

        return conflicts;
    }

    private CodeTemplateConflicts getCodeTemplateConflicts(ImportTreeTableNode codeTemplateNode) {
        CodeTemplateConflicts conflicts = new CodeTemplateConflicts();
        String codeTemplateId = (String) codeTemplateNode.getValueAt(IMPORT_ID_COLUMN);
        String codeTemplateName = ((String) codeTemplateNode.getValueAt(IMPORT_NAME_COLUMN)).toLowerCase();
        Boolean codeTemplateOverwrite = (Boolean) codeTemplateNode.getValueAt(IMPORT_OVERWRITE_COLUMN);
        boolean overwrite = codeTemplateOverwrite != null && codeTemplateOverwrite;

        // Determine if it conflicts by ID
        CodeTemplate matchingCodeTemplate = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().get(codeTemplateId);
        conflicts.setMatchingCodeTemplate(matchingCodeTemplate);

        ImportTreeTableNode libraryNode = (ImportTreeTableNode) codeTemplateNode.getParent();
        String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
        boolean librarySelected = (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN);

        // Determine if it conflicts by name
        EffectiveNames effectiveNames = getEffectiveNames();

        /*
         * Always conflict when it's the same name but a different ID. If the same name and same ID,
         * only conflict if there's a matching cached template and it's not being overwritten.
         */
        if (effectiveNames.getCodeTemplateNameMap().containsKey(libraryId)) {
            for (Entry<String, String> entry : effectiveNames.getCodeTemplateNameMap().get(libraryId).entrySet()) {
                if (entry.getValue().equals(codeTemplateName) && (!entry.getKey().equals(codeTemplateId) || matchingCodeTemplate != null && !overwrite)) {
                    conflicts.setConflictByName(true);
                }
            }
        }

        // If the name is found in the new code template names map, it's only a conflict if it's a different ID
        if (effectiveNames.getNewCodeTemplateNames().containsKey(libraryId)) {
            for (Entry<String, String> entry : effectiveNames.getNewCodeTemplateNames().get(libraryId).entrySet()) {
                if (entry.getValue().equals(codeTemplateName) && !entry.getKey().equals(codeTemplateId)) {
                    conflicts.setConflictByName(true);
                }
            }
        }

        // Find the cached library that should contain this code template, if applicable
        CodeTemplateLibrary matchingLibrary = null;
        for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                if (codeTemplate.getId().equals(codeTemplateId)) {
                    matchingLibrary = library;
                    break;
                }
            }

            if (matchingLibrary != null) {
                break;
            }
        }

        if (matchingLibrary == null) {
            matchingLibrary = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().get(libraryId);
        }

        conflicts.setMatchingLibrary(matchingLibrary);

        if (StringUtils.isBlank(libraryId) || (!librarySelected && (matchingLibrary == null || !overwrite))) {
            conflicts.setUnassignedConflict(true);
        }

        return conflicts;
    }

    private class LibraryConflicts {
        private CodeTemplateLibrary matchingLibrary;
        private boolean conflictByName;

        public CodeTemplateLibrary getMatchingLibrary() {
            return matchingLibrary;
        }

        public void setMatchingLibrary(CodeTemplateLibrary matchingLibrary) {
            this.matchingLibrary = matchingLibrary;
        }

        public boolean isConflictByName() {
            return conflictByName;
        }

        public void setConflictByName(boolean conflictByName) {
            this.conflictByName = conflictByName;
        }
    }

    private class CodeTemplateConflicts {
        private CodeTemplate matchingCodeTemplate;
        private CodeTemplateLibrary matchingLibrary;
        private boolean conflictByName;
        private boolean unassignedConflict;

        public CodeTemplate getMatchingCodeTemplate() {
            return matchingCodeTemplate;
        }

        public void setMatchingCodeTemplate(CodeTemplate matchingCodeTemplate) {
            this.matchingCodeTemplate = matchingCodeTemplate;
        }

        public CodeTemplateLibrary getMatchingLibrary() {
            return matchingLibrary;
        }

        public void setMatchingLibrary(CodeTemplateLibrary matchingLibrary) {
            this.matchingLibrary = matchingLibrary;
        }

        public boolean isConflictByName() {
            return conflictByName;
        }

        public void setConflictByName(boolean conflictByName) {
            this.conflictByName = conflictByName;
        }

        public boolean isUnassignedConflict() {
            return unassignedConflict;
        }

        public void setUnassignedConflict(boolean unassignedConflict) {
            this.unassignedConflict = unassignedConflict;
        }
    }

    private void updateImportButton() {
        if (importButton != null) {
            boolean enabled = true;
            boolean noneSelected = true;

            for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                ImportTreeTableNode libraryNode = (ImportTreeTableNode) libraryNodes.nextElement();

                if (libraryNode instanceof ImportLibraryTreeTableNode && (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                    noneSelected = false;

                    if (((ImportLibraryTreeTableNode) libraryNode).getConflicts().isConflictByName()) {
                        enabled = false;
                        break;
                    }
                }

                for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                    if ((boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                        noneSelected = false;

                        if (codeTemplateNode.getConflicts().isConflictByName() || codeTemplateNode.getConflicts().isUnassignedConflict()) {
                            enabled = false;
                            break;
                        }
                    }
                }

                if (!enabled) {
                    break;
                }
            }

            if (noneSelected) {
                enabled = false;
            }

            importButton.setEnabled(enabled);
        }
    }

    private void updateErrorsAndWarnings() {
        if (warningsPanel != null && warningsPanel != null) {
            errorsPanel.setVisible(false);
            warningsPanel.setVisible(false);

            int selectedRow = importTreeTable.getSelectedRow();
            if (selectedRow >= 0) {
                TreePath selectedPath = importTreeTable.getPathForRow(selectedRow);

                if (selectedPath != null) {
                    ImportTreeTableNode node = (ImportTreeTableNode) selectedPath.getLastPathComponent();

                    if ((boolean) node.getValueAt(IMPORT_SELECTED_COLUMN)) {
                        String name = (String) node.getValueAt(IMPORT_NAME_COLUMN);
                        Boolean overwrite = (Boolean) node.getValueAt(IMPORT_OVERWRITE_COLUMN);
                        overwrite = overwrite != null && overwrite;

                        if (node instanceof ImportLibraryTreeTableNode) {
                            ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;

                            if (libraryNode.getConflicts().isConflictByName()) {
                                errorsPanel.setVisible(true);
                                StringBuilder text = new StringBuilder();

                                if (libraryNode.getConflicts().getMatchingLibrary() != null && StringUtils.equalsIgnoreCase(libraryNode.getConflicts().getMatchingLibrary().getName(), name) && !overwrite) {
                                    text.append("The selected library already exists. Edit its name, or select overwrite.");
                                } else {
                                    text.append("Another library (with a different ID) is already using the name \"");
                                    text.append(name);
                                    text.append("\". Please enter a new name.");
                                }

                                errorsTextArea.setText(text.toString());
                            }
                        } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;

                            if (codeTemplateNode.getConflicts().isConflictByName()) {
                                errorsPanel.setVisible(true);
                                StringBuilder text = new StringBuilder();

                                if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null && StringUtils.equalsIgnoreCase(codeTemplateNode.getConflicts().getMatchingCodeTemplate().getName(), name) && !overwrite) {
                                    if (unassignedCodeTemplates) {
                                        text.append("The selected code template already exists. Edit its name, select overwrite, or switch the library.");
                                    } else {
                                        text.append("The selected code template already exists. Edit its name, or select overwrite.");
                                    }
                                } else {
                                    text.append("Another code template (with a different ID) is already using the name \"");
                                    text.append(name);
                                    text.append("\". Please enter a new name");
                                    if (unassignedCodeTemplates) {
                                        text.append(", or switch the library");
                                    }
                                    text.append('.');
                                }

                                errorsTextArea.setText(text.toString());
                            } else if (codeTemplateNode.getConflicts().isUnassignedConflict()) {
                                errorsPanel.setVisible(true);

                                if (unassignedCodeTemplates) {
                                    errorsTextArea.setText("Please select a parent library in order to import the selected code template.");
                                } else {
                                    ImportTreeTableNode tableLibraryNode = (ImportTreeTableNode) codeTemplateNode.getParent();

                                    StringBuilder text = new StringBuilder("The parent library \"");
                                    text.append((String) tableLibraryNode.getValueAt(IMPORT_NAME_COLUMN));
                                    text.append("\" does not currently exist, so it must be imported in order to import the selected code template.");

                                    errorsTextArea.setText(text.toString());
                                }
                            }

                            if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null) {
                                warningsPanel.setVisible(true);
                                if (codeTemplateNode.getConflicts().getMatchingLibrary() != null) {
                                    warningsTextArea.setText("The selected code template already exists in library \"" + codeTemplateNode.getConflicts().getMatchingLibrary().getName() + "\".");
                                } else {
                                    warningsTextArea.setText("The selected code template already exists.");
                                }
                            }
                        }
                    }
                }
            } else {
                boolean errors = false;
                boolean warnings = false;

                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    TreePath path = importTreeTable.getPathForRow(row);

                    if (path != null) {
                        ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                        if ((boolean) node.getValueAt(IMPORT_SELECTED_COLUMN)) {
                            if (node instanceof ImportLibraryTreeTableNode) {
                                LibraryConflicts conflicts = ((ImportLibraryTreeTableNode) node).getConflicts();
                                if (conflicts.isConflictByName()) {
                                    errors = true;
                                    break;
                                }
                            } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                                CodeTemplateConflicts conflicts = ((ImportCodeTemplateTreeTableNode) node).getConflicts();
                                if (conflicts.isConflictByName() || conflicts.isUnassignedConflict()) {
                                    errors = true;
                                    break;
                                }

                                if (conflicts.getMatchingCodeTemplate() != null) {
                                    warnings = true;
                                }
                            }
                        }
                    }
                }

                if (errors) {
                    errorsPanel.setVisible(true);
                    if (unassignedCodeTemplates) {
                        errorsTextArea.setText("One or more libraries / code templates have name conflicts. Edit their names, select overwrite, or switch the library.");
                    } else {
                        errorsTextArea.setText("One or more libraries / code templates have name conflicts. Edit their names, or select overwrite.");
                    }
                } else if (warnings) {
                    warningsPanel.setVisible(true);
                    warningsTextArea.setText("One or more libraries / code templates have warnings.");
                }
            }
        }
    }

    private void save() throws Exception {
        updatedLibraries = new HashMap<String, CodeTemplateLibrary>();
        updatedCodeTemplates = new HashMap<String, CodeTemplate>();

        for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            updatedLibraries.put(library.getId(), new CodeTemplateLibrary(library));
        }

        for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
            ImportTreeTableNode libraryNode = (ImportTreeTableNode) libraryNodes.nextElement();
            String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
            String libraryName = ((String) libraryNode.getValueAt(IMPORT_NAME_COLUMN));
            boolean librarySelected = (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN);
            Boolean libraryOverwrite = (Boolean) libraryNode.getValueAt(IMPORT_OVERWRITE_COLUMN);
            libraryOverwrite = libraryOverwrite != null && libraryOverwrite;

            CodeTemplateLibrary library = updatedLibraries.get(libraryId);
            Set<String> libraryCodeTemplateIds = new HashSet<String>();

            // If the library is existing, first add the existing code template IDs
            if (library != null) {
                for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                    libraryCodeTemplateIds.add(codeTemplate.getId());
                }
            }

            if (librarySelected && !unassignedCodeTemplates) {
                library = new CodeTemplateLibrary(importLibraryMap.get(libraryId));
                library.setName(libraryName);

                CodeTemplateLibrary matchingLibrary = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().get(libraryId);

                if (matchingLibrary != null) {
                    if (libraryOverwrite) {
                        // Merge the enabled/disabled channel IDs since we're overwriting
                        library.getEnabledChannelIds().addAll(matchingLibrary.getEnabledChannelIds());
                        library.getDisabledChannelIds().addAll(matchingLibrary.getDisabledChannelIds());
                        library.getDisabledChannelIds().removeAll(library.getEnabledChannelIds());
                    } else {
                        // Reset the ID and revision since it's a new library
                        library.setId(PlatformUI.MIRTH_FRAME.mirthClient.getGuid());
                        library.setRevision(0);
                    }
                }

                updatedLibraries.put(library.getId(), library);
            }

            for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                String codeTemplateId = (String) codeTemplateNode.getValueAt(IMPORT_ID_COLUMN);
                String codeTemplateName = ((String) codeTemplateNode.getValueAt(IMPORT_NAME_COLUMN));
                boolean codeTemplateSelected = (boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN);
                Boolean codeTemplateOverwrite = (Boolean) codeTemplateNode.getValueAt(IMPORT_OVERWRITE_COLUMN);
                codeTemplateOverwrite = codeTemplateOverwrite != null && codeTemplateOverwrite && codeTemplateNode.getConflicts().getMatchingLibrary() != null && codeTemplateNode.getConflicts().getMatchingLibrary().getId().equals(libraryId);

                if (codeTemplateSelected) {
                    CodeTemplate codeTemplate = new CodeTemplate(importCodeTemplateMap.get(codeTemplateId));
                    codeTemplate.setName(codeTemplateName);

                    if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null && !codeTemplateOverwrite) {
                        // Reset the ID and revision since it's a new code template
                        codeTemplate.setId(PlatformUI.MIRTH_FRAME.mirthClient.getGuid());
                        codeTemplate.setRevision(0);
                    }

                    // Add the code template ID to the library
                    libraryCodeTemplateIds.add(codeTemplate.getId());

                    updatedCodeTemplates.put(codeTemplate.getId(), codeTemplate);
                }
            }

            // Set the library code template IDs
            if (library != null) {
                List<CodeTemplate> libraryCodeTemplates = new ArrayList<CodeTemplate>();
                for (String libraryCodeTemplateId : libraryCodeTemplateIds) {
                    libraryCodeTemplates.add(new CodeTemplate(libraryCodeTemplateId));
                }
                library.setCodeTemplates(libraryCodeTemplates);
            }
        }

        saved = true;
    }

    private boolean confirmClose() {
        return !showCancelAlert || PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you wish to continue without importing any code templates / libraries?");
    }

    private JPanel topPanel;

    private JPanel linkPanel;
    private JPanel linkLeftPanel;
    private JLabel linkLeftSelectAllLabel;
    private JLabel linkLeftDeselectAllLabel;
    private JPanel linkRightPanel;
    private JLabel linkRightOverwriteAllLabel;
    private JLabel linkRightOverwriteNoneLabel;

    private JXTreeTable importTreeTable;
    private JScrollPane importTreeTableScrollPane;

    private JPanel errorsPanel;
    private JLabel errorsLabel;
    private JTextArea errorsTextArea;

    private JPanel warningsPanel;
    private JLabel warningsLabel;
    private JTextArea warningsTextArea;

    private JSeparator separator;
    private JPanel buttonPanel;
    private JButton importButton;
    private JButton cancelButton;
}