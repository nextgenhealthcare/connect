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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
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
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;

public class CodeTemplateImportDialog extends MirthDialog {

    private static final int IMPORT_SELECTED_COLUMN = 0;
    private static final int IMPORT_NAME_COLUMN = 1;
    private static final int IMPORT_OVERWRITE_COLUMN = 2;
    private static final int IMPORT_WARNINGS_COLUMN = 3;
    private static final int IMPORT_ERRORS_COLUMN = 4;
    private static final int IMPORT_ID_COLUMN = 5;

    private List<CodeTemplateLibrary> importLibraries;
    private Map<String, CodeTemplateLibrary> importLibraryMap;
    private Map<String, CodeTemplate> importCodeTemplateMap;
    private ImportTreeTableNode importUnassignedNode;

    private boolean showCancelAlert;
    private boolean saved;
    private Map<String, CodeTemplateLibrary> updatedLibraries;
    private Map<String, CodeTemplate> updatedCodeTemplates;

    public CodeTemplateImportDialog(Frame owner, List<CodeTemplateLibrary> importLibraries) {
        this(owner, importLibraries, false);
    }

    public CodeTemplateImportDialog(Frame owner, List<CodeTemplateLibrary> importLibraries, boolean showCancelAlert) {
        super(owner, "Import Code Templates / Libraries", true);
        this.importLibraries = importLibraries;
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
                        } else {
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
                        } else {
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
                return (column == IMPORT_OVERWRITE_COLUMN || column == IMPORT_SELECTED_COLUMN || column == IMPORT_NAME_COLUMN) && !CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID.equals(getModel().getValueAt(row, IMPORT_ID_COLUMN));
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

        DefaultTreeTableModel model = new ImportTreeTableModel();
        model.setColumnIdentifiers(Arrays.asList(new String[] { "", "Name", "Overwrite",
                "Warnings", "Errors", "Id" }));

        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode();
        importUnassignedNode = new ImportLibraryTreeTableNode(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID, CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID);
        rootNode.add(importUnassignedNode);

        Set<String> addedLibraryIds = new HashSet<String>();
        Set<String> addedCodeTemplateIds = new HashSet<String>();

        for (CodeTemplateLibrary library : importLibraries) {
            if (!addedLibraryIds.contains(library.getId())) {
                ImportTreeTableNode libraryNode;
                if (library.getId().equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
                    libraryNode = importUnassignedNode;
                } else {
                    libraryNode = new ImportLibraryTreeTableNode(library.getName(), library.getId());
                    importLibraryMap.put(library.getId(), library);
                }

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

        if (importUnassignedNode.getChildCount() == 0) {
            importUnassignedNode.removeFromParent();
        }

        model.setRoot(rootNode);

        importTreeTable.setTreeTableModel(model);
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

        HighlightPredicate highlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == IMPORT_NAME_COLUMN) {
                    TreePath path = importTreeTable.getPathForRow(adapter.row);
                    if (path != null) {
                        ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                        if ((boolean) node.getValueAt(IMPORT_SELECTED_COLUMN)) {
                            if (node instanceof ImportLibraryTreeTableNode) {
                                ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                                return libraryNode.getConflicts().isConflictByName();
                            } else {
                                ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                                return codeTemplateNode.getConflicts().isConflictByName();
                            }
                        }
                    }
                }

                return false;
            }
        };

        importTreeTable.addHighlighter(new ColorHighlighter(highlighterPredicate, UIConstants.INVALID_COLOR, Color.BLACK, UIConstants.INVALID_COLOR, Color.BLACK));

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
                if (evt.getColumn() != IMPORT_WARNINGS_COLUMN && evt.getColumn() != IMPORT_ERRORS_COLUMN) {
                    for (int row = evt.getFirstRow(); row <= evt.getLastRow() && row < importTreeTable.getRowCount(); row++) {
                        TreePath path = importTreeTable.getPathForRow(row);
                        if (path != null) {
                            ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                            if (path.getPathCount() == 2) {
                                ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) node;
                                libraryNode.setConflicts(getLibraryConflicts(node));

                                for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
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

        importTreeTable.getColumnModel().getColumn(IMPORT_WARNINGS_COLUMN).setMinWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_WARNINGS_COLUMN).setMaxWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_WARNINGS_COLUMN).setCellRenderer(new IconCellRenderer(UIConstants.ICON_WARNING));

        importTreeTable.getColumnModel().getColumn(IMPORT_ERRORS_COLUMN).setMinWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_ERRORS_COLUMN).setMaxWidth(60);
        importTreeTable.getColumnModel().getColumn(IMPORT_ERRORS_COLUMN).setCellRenderer(new IconCellRenderer(UIConstants.ICON_ERROR));

        importTreeTable.getColumnModel().removeColumn(importTreeTable.getColumnModel().getColumn(IMPORT_ID_COLUMN));
        importTreeTable.getColumnModel().removeColumn(importTreeTable.getColumnModel().getColumn(IMPORT_ERRORS_COLUMN));

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
                        for (Enumeration<? extends TreeTableNode> codeTemplateNodes = ((ImportLibraryTreeTableNode) libraryNodes.nextElement()).children(); codeTemplateNodes.hasMoreElements();) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();

                            if ((boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                                CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                                if (conflicts.isConflictByDifferentLibrary() || conflicts.isUnassignedWarning()) {
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
                case IMPORT_WARNINGS_COLUMN: return Boolean.class;
                case IMPORT_ERRORS_COLUMN: return Boolean.class;
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

        private boolean selectionValue;
        private String name;
        private Boolean overwrite;
        private boolean warnings;
        private boolean errors;
        private String id;

        public ImportTreeTableNode(String name, String id) {
            this.selectionValue = true;
            this.name = name;
            this.overwrite = null;
            this.warnings = false;
            this.errors = false;
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
                case IMPORT_WARNINGS_COLUMN: return warnings;
                case IMPORT_ERRORS_COLUMN: return errors;
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
                case IMPORT_WARNINGS_COLUMN:
                    warnings = (boolean) value;
                    break;
                case IMPORT_ERRORS_COLUMN:
                    errors = (boolean) value;
                    break;
                case IMPORT_ID_COLUMN:
                    id = (String) value;
                    break;
            }
        }
    }

    private class ImportCodeTemplateTreeTableNode extends ImportTreeTableNode {

        private CodeTemplateConflicts conflicts;

        public ImportCodeTemplateTreeTableNode(String name, String id) {
            super(name, id);
        }

        public CodeTemplateConflicts getConflicts() {
            return conflicts;
        }

        public void setConflicts(CodeTemplateConflicts conflicts) {
            this.conflicts = conflicts;
            setValueAt((boolean) getValueAt(IMPORT_SELECTED_COLUMN) && conflicts.isConflictByName(), IMPORT_ERRORS_COLUMN);
            setValueAt((boolean) getValueAt(IMPORT_SELECTED_COLUMN) && (conflicts.isConflictByDifferentLibrary() || conflicts.isUnassignedWarning()), IMPORT_WARNINGS_COLUMN);
        }
    }

    private class ImportLibraryTreeTableNode extends ImportTreeTableNode {

        private LibraryConflicts conflicts;

        public ImportLibraryTreeTableNode(String name, String id) {
            super(name, id);
        }

        public LibraryConflicts getConflicts() {
            return conflicts;
        }

        public void setConflicts(LibraryConflicts conflicts) {
            this.conflicts = conflicts;
            setValueAt((boolean) getValueAt(IMPORT_SELECTED_COLUMN) && conflicts.isConflictByName(), IMPORT_ERRORS_COLUMN);
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
                if (node.getValueAt(IMPORT_ID_COLUMN).equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
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
                if (node.getValueAt(IMPORT_ID_COLUMN).equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
                    visible = false;
                }
            }
            checkBox.setVisible(visible);

            return panel;
        }
    }

    private class NameCellRenderer extends JLabel implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            setEnabled(true);
            if (selected) {
                setBackground(importTreeTable.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
            }

            if (value != null && value instanceof ImportTreeTableNode) {
                ImportTreeTableNode node = (ImportTreeTableNode) value;
                setText((String) node.getValueAt(IMPORT_NAME_COLUMN));

                if (!(boolean) node.getValueAt(IMPORT_SELECTED_COLUMN)) {
                    setEnabled(false);
                }
            }

            return this;
        }

    }

    private class NameCellEditor extends DefaultCellEditor {

        private OffsetPanel panel;
        private JTextField field;

        public NameCellEditor() {
            super(new JTextField());
            panel = new OffsetPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            field = (JTextField) editorComponent;
            field.setDocument(new MirthFieldConstraints(0, false, true, true));
            panel.add(field, "grow, push");
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JXTreeTable treeTable = (JXTreeTable) table;
            JTree tree = (JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn());
            panel.setOffset(tree.getRowBounds(row).x);
            field.setText((String) value);
            return panel;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            int selectedRow = importTreeTable.getSelectedRow();
            if (selectedRow >= 0 && !(boolean) importTreeTable.getModel().getValueAt(selectedRow, IMPORT_SELECTED_COLUMN)) {
                return false;
            }

            return evt != null && evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2;
        }

        @Override
        public Object getCellEditorValue() {
            return StringUtils.trim((String) super.getCellEditorValue());
        }

        @Override
        public boolean stopCellEditing() {
            String value = (String) getCellEditorValue();
            boolean valid = true;

            if (StringUtils.isBlank(value) || !value.matches("^[a-zA-Z_0-9\\-\\s]*$")) {
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
                } else {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                    CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                    if (conflicts.getMatchingCodeTemplate() != null) {
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
                } else {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;
                    CodeTemplateConflicts conflicts = codeTemplateNode.getConflicts();
                    if (conflicts.getMatchingCodeTemplate() != null) {
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

        public IconCellRenderer(ImageIcon icon) {
            super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
            label = new JLabel(icon);
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
            label.setVisible(value != null && (boolean) value);
            return this;
        }

    }

    private class EffectiveNames {
        private Map<String, String> libraryNameMap;
        private Map<String, String> codeTemplateNameMap;

        /*
         * The new names maps reflect entities that either have no matching cached entry, or that do
         * have a matching cached entry but are not being overwritten.
         */
        private Map<String, String> newLibraryNames;
        private Map<String, String> newCodeTemplateNames;

        public EffectiveNames(Map<String, String> libraryNameMap, Map<String, String> codeTemplateNameMap, Map<String, String> newLibraryNames, Map<String, String> newCodeTemplateNames) {
            this.libraryNameMap = libraryNameMap;
            this.codeTemplateNameMap = codeTemplateNameMap;
            this.newLibraryNames = newLibraryNames;
            this.newCodeTemplateNames = newCodeTemplateNames;
        }

        public Map<String, String> getLibraryNameMap() {
            return libraryNameMap;
        }

        public Map<String, String> getCodeTemplateNameMap() {
            return codeTemplateNameMap;
        }

        public Map<String, String> getNewLibraryNames() {
            return newLibraryNames;
        }

        public Map<String, String> getNewCodeTemplateNames() {
            return newCodeTemplateNames;
        }
    }

    private EffectiveNames getEffectiveNames() {
        Map<String, String> libraryNameMap = new HashMap<String, String>();
        Map<String, String> codeTemplateNameMap = new HashMap<String, String>();
        Map<String, String> newLibraryNames = new HashMap<String, String>();
        Map<String, String> newCodeTemplateNames = new HashMap<String, String>();

        for (CodeTemplateLibrary library : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().values()) {
            libraryNameMap.put(library.getId(), library.getName().toLowerCase());
        }

        for (CodeTemplate codeTemplate : PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplates().values()) {
            codeTemplateNameMap.put(codeTemplate.getId(), codeTemplate.getName().toLowerCase());
        }

        for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
            ImportTreeTableNode libraryNode = (ImportTreeTableNode) libraryNodes.nextElement();
            String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
            String libraryName = (String) libraryNode.getValueAt(IMPORT_NAME_COLUMN);
            boolean librarySelected = (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN);
            Boolean libraryOverwrite = (Boolean) libraryNode.getValueAt(IMPORT_OVERWRITE_COLUMN);

            if (librarySelected) {
                CodeTemplateLibrary matchingLibrary = PlatformUI.MIRTH_FRAME.codeTemplatePanel.getCachedCodeTemplateLibraries().get(libraryId);

                if (matchingLibrary != null && libraryOverwrite != null && libraryOverwrite) {
                    libraryNameMap.put(libraryId, libraryName.toLowerCase());
                } else {
                    newLibraryNames.put(libraryId, libraryName.toLowerCase());
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
                        codeTemplateNameMap.put(codeTemplateId, codeTemplateName.toLowerCase());
                    } else {
                        newCodeTemplateNames.put(codeTemplateId, codeTemplateName.toLowerCase());
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

        if (effectiveNames.getCodeTemplateNameMap().values().contains(libraryName) || effectiveNames.getNewCodeTemplateNames().values().contains(libraryName)) {
            conflicts.setConflictByName(true);
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

        // Determine if it conflicts by name
        EffectiveNames effectiveNames = getEffectiveNames();

        /*
         * Always conflict when it's the same name but a different ID. If the same name and same ID,
         * only conflict if there's a matching cached template and it's not being overwritten.
         */
        for (Entry<String, String> entry : effectiveNames.getCodeTemplateNameMap().entrySet()) {
            if (entry.getValue().equals(codeTemplateName) && (!entry.getKey().equals(codeTemplateId) || matchingCodeTemplate != null && !overwrite)) {
                conflicts.setConflictByName(true);
            }
        }

        // If the name is found in the new code template names map, it's only a conflict if it's a different ID
        for (Entry<String, String> entry : effectiveNames.getNewCodeTemplateNames().entrySet()) {
            if (entry.getValue().equals(codeTemplateName) && !entry.getKey().equals(codeTemplateId)) {
                conflicts.setConflictByName(true);
            }
        }

        if (effectiveNames.getLibraryNameMap().values().contains(codeTemplateName) || effectiveNames.getNewLibraryNames().values().contains(codeTemplateName)) {
            conflicts.setConflictByName(true);
        }

        ImportTreeTableNode libraryNode = (ImportTreeTableNode) codeTemplateNode.getParent();
        String libraryId = (String) libraryNode.getValueAt(IMPORT_ID_COLUMN);
        boolean librarySelected = (boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN);

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
        conflicts.setMatchingLibrary(matchingLibrary);

        if (librarySelected) {
            // If the parent library node is selected, check whether the cached code template belongs to a different library
            if (matchingLibrary != null) {
                if (overwrite && !matchingLibrary.getId().equals(libraryId)) {
                    conflicts.setConflictByDifferentLibrary(true);
                }
            }
        } else {
            /*
             * If the parent library node isn't selected, and there's no matching library, then the
             * code template will be unassigned when imported.
             */
            if (matchingLibrary == null && !libraryId.equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
                conflicts.setUnassignedWarning(true);
            }
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
        private boolean conflictByDifferentLibrary;
        private boolean unassignedWarning;

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

        public boolean isConflictByDifferentLibrary() {
            return conflictByDifferentLibrary;
        }

        public void setConflictByDifferentLibrary(boolean conflictByDifferentLibrary) {
            this.conflictByDifferentLibrary = conflictByDifferentLibrary;
        }

        public boolean isUnassignedWarning() {
            return unassignedWarning;
        }

        public void setUnassignedWarning(boolean unassignedWarning) {
            this.unassignedWarning = unassignedWarning;
        }
    }

    private void updateImportButton() {
        if (importButton != null) {
            boolean enabled = true;
            boolean noneSelected = true;

            for (Enumeration<? extends TreeTableNode> libraryNodes = ((TreeTableNode) importTreeTable.getTreeTableModel().getRoot()).children(); libraryNodes.hasMoreElements();) {
                ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) libraryNodes.nextElement();
                if ((boolean) libraryNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                    if (!libraryNode.getValueAt(IMPORT_ID_COLUMN).equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
                        noneSelected = false;
                    }

                    if (libraryNode.getConflicts().isConflictByName()) {
                        enabled = false;
                        break;
                    }
                }

                for (Enumeration<? extends TreeTableNode> codeTemplateNodes = libraryNode.children(); codeTemplateNodes.hasMoreElements();) {
                    ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) codeTemplateNodes.nextElement();
                    if ((boolean) codeTemplateNode.getValueAt(IMPORT_SELECTED_COLUMN)) {
                        noneSelected = false;

                        if (codeTemplateNode.getConflicts().isConflictByName()) {
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
                                    text.append("The selected library already exists but is not being overwritten. Either enter a new name by double-clicking on the highlighted column, or overwrite the existing library.");
                                } else {
                                    text.append("Another library or code template (with a different ID) is already using the name \"");
                                    text.append(name);
                                    text.append("\". Please enter a new name by double-clicking on the highlighted column.");
                                }

                                errorsTextArea.setText(text.toString());
                            }
                        } else if (node instanceof ImportCodeTemplateTreeTableNode) {
                            ImportCodeTemplateTreeTableNode codeTemplateNode = (ImportCodeTemplateTreeTableNode) node;

                            if (codeTemplateNode.getConflicts().isConflictByName()) {
                                errorsPanel.setVisible(true);
                                StringBuilder text = new StringBuilder();

                                if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null && StringUtils.equalsIgnoreCase(codeTemplateNode.getConflicts().getMatchingCodeTemplate().getName(), name) && !overwrite) {
                                    text.append("The selected code template already exists but is not being overwritten. Either enter a new name by double-clicking on the highlighted column, or overwrite the existing code template.");
                                } else {
                                    text.append("Another library or code template (with a different ID) is already using the name \"");
                                    text.append(name);
                                    text.append("\". Please enter a new name by double-clicking on the highlighted column.");
                                }

                                errorsTextArea.setText(text.toString());
                            }

                            if (codeTemplateNode.getConflicts().isConflictByDifferentLibrary()) {
                                warningsPanel.setVisible(true);
                                ImportLibraryTreeTableNode tableLibraryNode = (ImportLibraryTreeTableNode) codeTemplateNode.getParent();

                                StringBuilder text = new StringBuilder("The selected code template already exists in the library \"");
                                text.append(codeTemplateNode.getConflicts().getMatchingLibrary().getName());
                                text.append("\". It will be imported, but not under the library \"");
                                text.append((String) tableLibraryNode.getValueAt(IMPORT_NAME_COLUMN));
                                text.append("\".");

                                warningsTextArea.setText(text.toString());
                            } else if (codeTemplateNode.getConflicts().isUnassignedWarning()) {
                                warningsPanel.setVisible(true);
                                warningsTextArea.setText("The selected code template will be imported, but its parent library, which doesn't already exist, is not selected. The code template will be unassigned by default.");
                            }
                        }
                    }
                }
            }

            if (!errorsPanel.isVisible()) {
                for (int row = 0; row < importTreeTable.getRowCount(); row++) {
                    TreePath path = importTreeTable.getPathForRow(row);

                    if (path != null) {
                        ImportTreeTableNode node = (ImportTreeTableNode) path.getLastPathComponent();

                        if ((boolean) node.getValueAt(IMPORT_SELECTED_COLUMN) && (node instanceof ImportLibraryTreeTableNode && ((ImportLibraryTreeTableNode) node).getConflicts().isConflictByName() || node instanceof ImportCodeTemplateTreeTableNode && ((ImportCodeTemplateTreeTableNode) node).getConflicts().isConflictByName())) {
                            errorsPanel.setVisible(true);
                            errorsTextArea.setText("One or more libraries / code templates have name conflicts. Please either enter new names for them by double-clicking on the highlighted columns, or if they match an existing entry, you may also choose to overwrite them.");
                            break;
                        }
                    }
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
            ImportLibraryTreeTableNode libraryNode = (ImportLibraryTreeTableNode) libraryNodes.nextElement();
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

            if (librarySelected && !libraryId.equals(CodeTemplateLibrary.UNASSIGNED_LIBRARY_ID)) {
                library = new CodeTemplateLibrary(importLibraryMap.get(libraryId));
                library.setName(libraryName);

                if (libraryNode.getConflicts().getMatchingLibrary() != null) {
                    if (libraryOverwrite) {
                        // Merge the enabled/disabled channel IDs since we're overwriting
                        library.getEnabledChannelIds().addAll(libraryNode.getConflicts().getMatchingLibrary().getEnabledChannelIds());
                        library.getDisabledChannelIds().addAll(libraryNode.getConflicts().getMatchingLibrary().getDisabledChannelIds());
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
                codeTemplateOverwrite = codeTemplateOverwrite != null && codeTemplateOverwrite;

                if (codeTemplateSelected) {
                    CodeTemplate codeTemplate = new CodeTemplate(importCodeTemplateMap.get(codeTemplateId));
                    codeTemplate.setName(codeTemplateName);

                    if (codeTemplateNode.getConflicts().getMatchingCodeTemplate() != null && !codeTemplateOverwrite) {
                        // Reset the ID and revision since it's a new code template
                        codeTemplate.setId(PlatformUI.MIRTH_FRAME.mirthClient.getGuid());
                        codeTemplate.setRevision(0);
                    }

                    if (!codeTemplateNode.getConflicts().isConflictByDifferentLibrary()) {
                        // Add the code template ID to the library
                        libraryCodeTemplateIds.add(codeTemplate.getId());
                    }

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