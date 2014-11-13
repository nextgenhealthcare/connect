/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CenterCellRenderer;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RuleDropData;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.EditorTableCellEditor;
import com.mirth.connect.client.ui.editors.MirthEditorPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.FilterRulePlugin;

public class FilterPane extends MirthEditorPane implements DropTargetListener {
    // used to load this pane

    private Filter filter;
    private Transformer transformer;
    // fields
    private MirthTable filterTable;
    private DefaultTableModel filterTableModel;
    private JScrollPane filterTablePane;
    private JSplitPane hSplitPane;
    private JSplitPane vSplitPane;
    private boolean updating; // allow the selection listener to breathe
    private boolean addingNewRow;
    private boolean removingRow;
    JXTaskPane viewTasks;
    JXTaskPane filterTasks;
    JPopupMenu filterPopupMenu;
    // this little sucker is used to track the last row that had
    // focus after a new row is selected
    private Connector connector;
    // panels using CardLayout
    protected BasePanel rulePanel; // the card holder
    protected BasePanel blankPanel;
    public static final int NUMBER_OF_COLUMNS = 5;
    public static final String BLANK_TYPE = "";
    public static final String JAVASCRIPT = "JavaScript";
    public static final String RULE_BUILDER = "Rule Builder";
    private String[] comboBoxValues = new String[] { Rule.Operator.AND.toString(),
            Rule.Operator.OR.toString() };
    private Channel channel;
    private DropTarget dropTarget;

    /**
     * CONSTRUCTOR
     */
    public FilterPane() {
        prevSelRow = -1;
        modified = false;
        new DropTarget(this, this);
        initComponents();
    }

    public void reload(Connector c, Filter f) {
        connector = c;
        filter = f;
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
    }

    /**
     * load( Filter f )
     */
    public boolean load(Connector c, Filter f, Transformer t, boolean channelHasBeenChanged) {
        if (alertUnsupportedRuleTypes(f)) {
            return false;
        }

        prevSelRow = -1;
        filter = f;
        transformer = t;
        connector = c;
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;

        // we need to clear all the old data before we load the new
        makeFilterTable();

        parent.setCurrentContentPage((JPanel) this);
        parent.setFocus(new JXTaskPane[] { viewTasks, filterTasks }, false, true);

        // add any existing rules to the model
        List<Rule> list = filter.getRules();
        ListIterator<Rule> li = list.listIterator();
        while (li.hasNext()) {
            Rule s = li.next();
            setRowData(s, s.getSequenceNumber(), false);
        }

        tabTemplatePanel.setDefaultComponent();
        tabTemplatePanel.setFilterView();

        int rowCount = filterTableModel.getRowCount();
        // select the first row if there is one
        if (rowCount > 0) {
            filterTable.setRowSelectionInterval(0, 0);
            prevSelRow = 0;
        } else {
            rulePanel.showCard(BLANK_TYPE);

            for (FilterRulePlugin plugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
                plugin.getPanel().setData(null);
            }

            loadData(-1);
        }

        if (connector.getMode() == Connector.Mode.SOURCE) {
            tabTemplatePanel.setSourceView();
            tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(channel.getSourceConnector().getTransformer().getInboundDataType()));
        } else if (connector.getMode() == Connector.Mode.DESTINATION) {
            tabTemplatePanel.setDestinationView(false);
            if (channel.getSourceConnector().getTransformer().getOutboundDataType() != null) {
                tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(channel.getSourceConnector().getTransformer().getOutboundDataType()));
            } else {
                tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(channel.getSourceConnector().getTransformer().getInboundDataType()));
            }
        }

        tabTemplatePanel.setIncomingDataProperties(transformer.getInboundProperties());

        tabTemplatePanel.setIncomingMessage(transformer.getInboundTemplate());

        updateRuleNumbers();
        if (filterTableModel.getRowCount() > 0) {
            updateTaskPane((String) filterTableModel.getValueAt(0, RULE_TYPE_COL));
        }

        if (channelHasBeenChanged) {
            modified = true;
        } else {
            modified = false;
        }

        return true;
    }

    /**
     * @return Returns true if the filter has unsupported rule types and an alert was generated,
     *         false otherwise.
     */
    private boolean alertUnsupportedRuleTypes(Filter filter) {
        if (LoadedExtensions.getInstance().getFilterRulePlugins().values().size() == 0) {
            parent.alertError(this, "No filter rule plugins loaded.\r\nPlease install plugins and try again.");
            return true;
        }

        Set<String> types = new HashSet<String>();

        for (Rule rule : filter.getRules()) {
            types.add(rule.getType());
        }

        types.removeAll(LoadedExtensions.getInstance().getFilterRulePlugins().keySet());

        if (!types.isEmpty()) {
            if (types.size() == 1) {
                parent.alertError(this, "The \"" + types.toArray()[0] + "\" rule plugin is required by this filter. Please install this plugin and try again.");
            } else {
                parent.alertError(this, "The following rule type plugins are required by this filter: " + StringUtils.join(types, ", ") + ". Please install these plugins and try again.");
            }

            return true;
        }

        return false;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                if (iterator.hasNext() && fileList.size() == 1) {
                    String fileName = ((File) iterator.next()).getName();
                    if (!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml")) {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            } else if (tr.isDataFlavorSupported(TreeTransferable.RULE_DATA_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {}

    public void dropActionChanged(DropTargetDragEvent dtde) {}

    public void dragExit(DropTargetEvent dte) {}

    public void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable tr = dtde.getTransferable();

            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();

                if (fileList.size() == 1) {
                    File file = (File) iterator.next();
                    importFilter(parent.readFileToString(file));
                }
            } else if (tr.isDataFlavorSupported(TreeTransferable.RULE_DATA_FLAVOR)) {
                Object transferData = tr.getTransferData(TreeTransferable.RULE_DATA_FLAVOR);

                if (transferData instanceof RuleDropData) {
                    RuleDropData data = (RuleDropData) transferData;
                    addNewRule(MirthTree.constructNodeDescription(data.getNode()), data.getMapping());
                }
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    public void initComponents() {

        // the available panels (cards)
        rulePanel = new BasePanel();
        blankPanel = new BasePanel();

        for (FilterRulePlugin filterRulePlugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
            filterRulePlugin.initialize(this);
        }

        // establish the cards to use in the Transformer
        rulePanel.addCard(blankPanel, BLANK_TYPE);
        for (FilterRulePlugin plugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
            rulePanel.addCard(plugin.getPanel(), plugin.getPluginPointName());
        }

        filterTablePane = new JScrollPane();

        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);

        filterPopupMenu = new JPopupMenu();

        viewTasks.add(initActionCallback("accept", "Return back to channel.", ActionFactory.createBoundAction("accept", "Back to Channel", "B"), new ImageIcon(Frame.class.getResource("images/resultset_previous.png"))));
        parent.setNonFocusable(viewTasks);
        viewTasks.setVisible(false);
        parent.taskPaneContainer.add(viewTasks, parent.taskPaneContainer.getComponentCount() - 1);

        filterTasks = new JXTaskPane();
        filterTasks.setTitle("Filter Tasks");
        filterTasks.setFocusable(false);

        // add new rule task
        filterTasks.add(initActionCallback("addNewRule", "Add a new filter rule.", ActionFactory.createBoundAction("addNewRule", "Add New Rule", "N"), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewRule = new JMenuItem("Add New Rule");
        addNewRule.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        addNewRule.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addNewRule();
            }
        });
        filterPopupMenu.add(addNewRule);

        // delete rule task
        filterTasks.add(initActionCallback("deleteRule", "Delete the currently selected filter rule.", ActionFactory.createBoundAction("deleteRule", "Delete Rule", "X"), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteRule = new JMenuItem("Delete Rule");
        deleteRule.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        deleteRule.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                deleteRule();
            }
        });
        filterPopupMenu.add(deleteRule);

        filterTasks.add(initActionCallback("doImport", "Import a filter from an XML file.", ActionFactory.createBoundAction("doImport", "Import Filter", "I"), new ImageIcon(Frame.class.getResource("images/report_go.png"))));
        JMenuItem importFilter = new JMenuItem("Import Filter");
        importFilter.setIcon(new ImageIcon(Frame.class.getResource("images/report_go.png")));
        importFilter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doImport();
            }
        });
        filterPopupMenu.add(importFilter);

        filterTasks.add(initActionCallback("doExport", "Export the filter to an XML file.", ActionFactory.createBoundAction("doExport", "Export Filter", "E"), new ImageIcon(Frame.class.getResource("images/report_disk.png"))));
        JMenuItem exportFilter = new JMenuItem("Export Filter");
        exportFilter.setIcon(new ImageIcon(Frame.class.getResource("images/report_disk.png")));
        exportFilter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doExport();
            }
        });
        filterPopupMenu.add(exportFilter);

        filterTasks.add(initActionCallback("doValidate", "Validate the currently viewed script.", ActionFactory.createBoundAction("doValidate", "Validate Script", "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem validateStep = new JMenuItem("Validate Script");
        validateStep.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        validateStep.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doValidate();
            }
        });
        filterPopupMenu.add(validateStep);

        // move rule up task
        filterTasks.add(initActionCallback("moveRuleUp", "Move the currently selected rule up.", ActionFactory.createBoundAction("moveRuleUp", "Move Rule Up", "P"), new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveRuleUp = new JMenuItem("Move Rule Up");
        moveRuleUp.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_up.png")));
        moveRuleUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveRuleUp();
            }
        });
        filterPopupMenu.add(moveRuleUp);

        // move rule down task
        filterTasks.add(initActionCallback("moveRuleDown", "Move the currently selected rule down.", ActionFactory.createBoundAction("moveRuleDown", "Move Rule Down", "D"), new ImageIcon(Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveRuleDown = new JMenuItem("Move Rule Down");
        moveRuleDown.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_down.png")));
        moveRuleDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveRuleDown();
            }
        });
        filterPopupMenu.add(moveRuleDown);

        // add the tasks to the taskpane, and the taskpane to the mirth client
        parent.setNonFocusable(filterTasks);
        filterTasks.setVisible(false);
        parent.taskPaneContainer.add(filterTasks, parent.taskPaneContainer.getComponentCount() - 1);

        makeFilterTable();

        // BGN LAYOUT
        filterTablePane.setBorder(BorderFactory.createEmptyBorder());
        rulePanel.setBorder(BorderFactory.createEmptyBorder());

        hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filterTablePane, rulePanel);
        hSplitPane.setContinuousLayout(true);
        hSplitPane.setOneTouchExpandable(true);
        vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hSplitPane, refPanel);
        vSplitPane.setContinuousLayout(true);
        vSplitPane.setOneTouchExpandable(true);

        this.setLayout(new BorderLayout());
        this.add(vSplitPane, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder());
        vSplitPane.setBorder(BorderFactory.createEmptyBorder());
        hSplitPane.setBorder(BorderFactory.createEmptyBorder());
        resizePanes();
        // END LAYOUT

    } // END initComponents()

    public void makeFilterTable() {
        filterTable = new MirthTable();

        filterTable.setModel(new DefaultTableModel(new String[] { "#", "Operator", "Name", "Type",
                "Data" }, 0) {

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                boolean[] canEdit;
                FilterRulePlugin plugin;
                try {
                    plugin = getPlugin((String) filterTableModel.getValueAt(rowIndex, RULE_TYPE_COL));
                    canEdit = new boolean[] { false, true, plugin.isNameEditable(), true, true };
                } catch (Exception e) {
                    canEdit = new boolean[] { false, true, true, true, true };
                }
                return canEdit[columnIndex];
            }
        });

        filterTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        filterTableModel = (DefaultTableModel) filterTable.getModel();

        filterTable.getColumnModel().getColumn(RULE_NAME_COL).setCellEditor(new EditorTableCellEditor(this));
        filterTable.setCustomEditorControls(true);

        // Set the combobox editor on the operator column, and add action
        // listener
        MirthComboBoxTableCellEditor comboBoxOp = new MirthComboBoxTableCellEditor(filterTable, comboBoxValues, 2, true, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                modified = true;
                updateOperations();
            }
        });

        // Set the combobox editor on the type column, and add action listener
        String[] defaultComboBoxValues = new String[LoadedExtensions.getInstance().getFilterRulePlugins().size()];
        FilterRulePlugin[] pluginArray = LoadedExtensions.getInstance().getFilterRulePlugins().values().toArray(new FilterRulePlugin[0]);
        for (int i = 0; i < pluginArray.length; i++) {
            defaultComboBoxValues[i] = pluginArray[i].getPluginPointName();
        }

        MirthComboBoxTableCellEditor comboBoxType = new MirthComboBoxTableCellEditor(filterTable, defaultComboBoxValues, 2, true, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (filterTable.getEditingRow() != -1) {
                    int row = getSelectedRow();
                    String selectedType = ((JComboBox) evt.getSource()).getSelectedItem().toString();
                    String previousType = (String) filterTable.getValueAt(row, RULE_TYPE_COL);

                    if (selectedType.equalsIgnoreCase(previousType)) {
                        return;
                    }

                    modified = true;
                    FilterRulePlugin plugin;
                    try {
                        if (rulePanel.isModified() && !PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you would like to change this filter rule and lose all of the current filter data?")) {
                            ((JComboBox) evt.getSource()).getModel().setSelectedItem(previousType);
                            return;
                        }

                        plugin = getPlugin(selectedType);
                        plugin.initData();
                        filterTableModel.setValueAt(plugin.getNewName(), row, RULE_NAME_COL);
                        rulePanel.showCard(selectedType);
                        updateTaskPane(selectedType);
                    } catch (Exception e) {
                        parent.alertException(parent, e.getStackTrace(), e.getMessage());
                    }

                }
            }
        });

        filterTable.setSelectionMode(0); // only select one row at a time

        filterTable.getColumnExt(RULE_NUMBER_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        filterTable.getColumnExt(RULE_OP_COL).setMaxWidth(UIConstants.MAX_WIDTH);

        filterTable.getColumnExt(RULE_NUMBER_COL).setPreferredWidth(30);
        filterTable.getColumnExt(RULE_OP_COL).setPreferredWidth(60);

        filterTable.getColumnExt(RULE_NUMBER_COL).setCellRenderer(new CenterCellRenderer());
        filterTable.getColumnExt(RULE_OP_COL).setCellEditor(comboBoxOp);
        filterTable.getColumnExt(RULE_OP_COL).setCellRenderer(new MirthComboBoxTableCellRenderer(comboBoxValues) {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof String && value.equals("")) {
                    value = null;
                } else if (value != null) {
                    value = value.toString();
                }

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        filterTable.getColumnExt(RULE_TYPE_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        filterTable.getColumnExt(RULE_TYPE_COL).setMinWidth(120);
        filterTable.getColumnExt(RULE_TYPE_COL).setPreferredWidth(120);
        filterTable.getColumnExt(RULE_TYPE_COL).setCellEditor(comboBoxType);
        filterTable.getColumnExt(RULE_TYPE_COL).setCellRenderer(new MirthComboBoxTableCellRenderer(defaultComboBoxValues));

        filterTable.getColumnExt(RULE_DATA_COL).setVisible(false);

        filterTable.setRowHeight(UIConstants.ROW_HEIGHT);
        filterTable.packTable(UIConstants.COL_MARGIN);
        filterTable.setSortable(false);
        filterTable.setOpaque(true);
        filterTable.setRowSelectionAllowed(true);
        filterTable.setDragEnabled(false);
        filterTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            filterTable.setHighlighters(highlighter);
        }

        filterTable.setDropTarget(dropTarget);
        filterTablePane.setDropTarget(dropTarget);

        filterTable.setBorder(BorderFactory.createEmptyBorder());
        filterTablePane.setBorder(BorderFactory.createEmptyBorder());

        filterTablePane.setViewportView(filterTable);

        filterTable.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        filterTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (!updating && !evt.getValueIsAdjusting()) {
                    FilterListSelected(evt);
                }
            }
        });
        filterTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteRule();
                }
            }

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void checkSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = filterTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                filterTable.setRowSelectionInterval(row, row);
            }
            filterPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    // for the task pane
    public BoundAction initActionCallback(String callbackMethod, String toolTip, BoundAction boundAction, ImageIcon icon) {

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }

        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }

    // called whenever a table row is (re)selected
    private void FilterListSelected(ListSelectionEvent evt) {
        updating = true;

        int row = filterTable.getSelectedRow();
        int last = evt.getLastIndex();

        saveData(prevSelRow);

        if (isValid(row)) {
            loadData(row);
        } else if (isValid(last)) {
            loadData(last);
            row = last;
        }

        String type = (String) filterTable.getValueAt(row, RULE_TYPE_COL);
        rulePanel.showCard(type);
        filterTable.setRowSelectionInterval(row, row);
        prevSelRow = row;
        updateTaskPane(type);

        updating = false;
    }

    // returns true if the row is a valid index in the existing model
    private boolean isValid(int row) {
        return (row >= 0 && row < filterTableModel.getRowCount());
    }

    // sets the data from the previously used panel into the
    // previously selected Rule object
    private void saveData(int row) {
        if (filterTable.isEditing()) {
            filterTable.getCellEditor(filterTable.getEditingRow(), filterTable.getEditingColumn()).stopCellEditing();
        }

        updating = true;

        if (isValid(row)) {
            String type = (String) filterTable.getValueAt(row, RULE_TYPE_COL);

            Map<Object, Object> data;
            try {
                data = getPlugin(type).getData(row);
                filterTableModel.setValueAt(data, row, RULE_DATA_COL);
                List<Rule> list = buildRuleList(new ArrayList<Rule>(), filterTable.getRowCount());
                filter.setRules(list);
            } catch (Exception e) {
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }
        }

        updating = false;
    }

    // loads the data object from the currently selected row
    // into the correct panel
    private void loadData(int row) {
        if (isValid(row)) {
            String type = (String) filterTableModel.getValueAt(row, RULE_TYPE_COL);
            Map<Object, Object> data = (Map<Object, Object>) filterTableModel.getValueAt(row, RULE_DATA_COL);

            setPanelData(type, data);
        }

        if (connector.getMode() == Connector.Mode.SOURCE) {
            Set<String> concatenatedRules = new LinkedHashSet<String>();
            VariableListUtil.getRuleVariables(concatenatedRules, connector, true, row);
            tabTemplatePanel.updateVariables(concatenatedRules, null);
        } else {
            tabTemplatePanel.updateVariables(getRuleVariables(row), getGlobalStepVariables(row));
            tabTemplatePanel.populateConnectors(channel.getDestinationConnectors());
        }
    }

    private void setPanelData(String type, Map<Object, Object> data) {
        FilterRulePlugin plugin;
        try {
            plugin = getPlugin(type);
            plugin.setData(data);
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    // display a rule in the table
    private void setRowData(Rule rule, int row, boolean selectRow) {
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];

        tableData[RULE_NUMBER_COL] = rule.getSequenceNumber();
        tableData[RULE_OP_COL] = rule.getOperator();

        FilterRulePlugin plugin;
        try {
            plugin = getPlugin(rule.getType());
            String ruleName = rule.getName();
            if (ruleName == null || ruleName.equals("") || plugin.isProvideOwnStepName()) {
                plugin.setData((Map<Object, Object>) rule.getData());
                ruleName = plugin.getStepName();
            }
            tableData[RULE_NAME_COL] = ruleName;
            tableData[RULE_TYPE_COL] = rule.getType();
            tableData[RULE_DATA_COL] = rule.getData();

            updating = true;
            filterTableModel.addRow(tableData);
            if (selectRow) {
                filterTable.setRowSelectionInterval(row, row);
            }
            updating = false;
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    public void updateName(int row, String name) {
        /*
         * Make sure a row is not being added or removed when updating name. The removingRow check
         * was added to force Rule Builder rows to disappear when they are removed. They stopped
         * disappearing sometime after 1.8.2, possibly with swingx changes.
         */
        if (row > -1 && !addingNewRow && !removingRow) {
            updating = true;
            filterTableModel.setValueAt(name, row, RULE_NAME_COL);
            updating = false;
        }
    }

    public Frame getParentFrame() {
        return parent;
    }

    /**
     * void addNewRule() add a new rule to the end of the list
     */
    public void addNewRule() {
        addNewRule("New Rule", "");
    }

    /**
     * void addNewRule() add a new rule to the end of the list
     */
    public void addNewRule(String name, String mapping) {
        modified = true;
        addingNewRow = true;
        int rowCount = filterTable.getRowCount();
        Rule rule = new Rule();

        saveData(filterTable.getSelectedRow());

        rule.setSequenceNumber(rowCount);
        rule.setScript("return true;");
        rule.setName(name);
        rule.setData(null);

        if (rowCount == 0) {
            rule.setOperator(Rule.Operator.NONE); // NONE operator by default
        } // on row 0
        else {
            rule.setOperator(Rule.Operator.AND); // AND operator by default
        } // elsewhere

        if (LoadedExtensions.getInstance().getFilterRulePlugins().containsKey(RULE_BUILDER)) {
            rule.setType(RULE_BUILDER); // graphical rule type by default,
                                        // inbound
            FilterRulePlugin plugin = LoadedExtensions.getInstance().getFilterRulePlugins().get(RULE_BUILDER);
            plugin.initData();
            LinkedHashMap<Object, Object> data = new LinkedHashMap<Object, Object>();
            data.put("Field", mapping);
            data.put("Equals", UIConstants.EXISTS_OPTION);
            data.put("Values", new ArrayList());
            data.put("Accept", UIConstants.YES_OPTION);
            if (name.equals("New Rule")) {
                name = "";
            }
            data.put("Name", name);
            data.put("OriginalField", mapping);
            rule.setData(data);

            if (plugin.isProvideOwnStepName()) {
                plugin.setData(data);
                rule.setName(plugin.getStepName());
                plugin.clearData();
            }
        } else {
            System.out.println("Rule Builder not found");
            rule.setType(LoadedExtensions.getInstance().getFilterRulePlugins().keySet().iterator().next());
        }

        setRowData(rule, rowCount, true);
        prevSelRow = rowCount;
        updateRuleNumbers();
        filterTable.setRowSelectionInterval(rowCount, rowCount);
        filterTablePane.getViewport().setViewPosition(new Point(0, filterTable.getRowHeight() * rowCount));
        addingNewRow = false;
    }

    /**
     * void deleteRule(MouseEvent evt) delete all selected rows
     */
    public void deleteRule() {
        modified = true;
        if (filterTable.isEditing()) {
            filterTable.getCellEditor(filterTable.getEditingRow(), filterTable.getEditingColumn()).stopCellEditing();
        }

        updating = true;
        removingRow = true;

        int row = filterTable.getSelectedRow();

        if (isValid(row + 1)) {
            prepData(row + 1);
        }

        if (isValid(row)) {
            filterTableModel.removeRow(row);
        }

        removingRow = false;
        updating = false;

        if (isValid(row)) {
            filterTable.setRowSelectionInterval(row, row);
        } else if (isValid(row - 1)) {
            filterTable.setRowSelectionInterval(row - 1, row - 1);
        } else {
            rulePanel.showCard(BLANK_TYPE);
            for (FilterRulePlugin plugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
                plugin.clearData();
            }
        }

        updateRuleNumbers();
    }

    /**
     * prepData( int row ) works to move the data in a panel for moves or deletes
     */
    private void prepData(int row) {
        Map<Object, Object> d = (Map<Object, Object>) filterTableModel.getValueAt(row, RULE_DATA_COL);
        String type = (String) filterTableModel.getValueAt(row, RULE_TYPE_COL);
        setPanelData(type, d);
    }

    private FilterRulePlugin getPlugin(String name) throws Exception {
        FilterRulePlugin plugin = LoadedExtensions.getInstance().getFilterRulePlugins().get(name);
        if (plugin == null) {
            String message = "Unable to find Filter Rule Plugin: " + name;
            Exception e = new Exception(message);
            parent.alertError(this, message);
            throw new Exception(e);
        } else {
            return plugin;
        }
    }

    /**
     * void moveRule( int i ) move the selected row i places
     */
    public void moveRuleUp() {
        moveRule(-1);
    }

    public void moveRuleDown() {
        moveRule(1);
    }

    public void moveRule(int i) {
        modified = true;
        int selRow = filterTable.getSelectedRow();
        int moveTo = selRow + i;

        // we can't move past the first or last row
        if (moveTo >= 0 && moveTo < filterTable.getRowCount()) {
            for (FilterRulePlugin plugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
                plugin.moveStart();
            }

            saveData(selRow);
            loadData(moveTo);
            filterTableModel.moveRow(selRow, selRow, moveTo);
            filterTable.setRowSelectionInterval(moveTo, moveTo);

            for (FilterRulePlugin plugin : LoadedExtensions.getInstance().getFilterRulePlugins().values()) {
                plugin.moveEnd();
            }
        }

        updateRuleNumbers();
    }

    /*
     * Import a filter.
     */
    public void doImport() {
        String content = parent.browseForFileString("XML");

        if (content != null) {
            importFilter(content);
        }
    }

    public void importFilter(String content) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        Filter importFilter = null;

        try {
            importFilter = serializer.deserialize(content, Filter.class);
        } catch (Exception e) {
            parent.alertError(this, "Invalid filter file.");
            return;
        }

        if (alertUnsupportedRuleTypes(importFilter)) {
            return;
        }

        prevSelRow = -1;
        modified = true;

        boolean append = (filterTableModel.getRowCount() > 0 && parent.alertOption(parent, "Would you like to append the rules from the imported filter to the existing filter?"));

        /*
         * When appending, we merely add the rules from the filter being imported. When not
         * appending, we replace the entire filter with the one being imported.
         */
        if (append) {
            /*
             * MIRTH-2746 When appending filter rules from an import, the first rule may not have an
             * operator. To prevent an error in the JavaScript, we default it to AND.
             */
            switch (importFilter.getRules().get(0).getOperator()) {
                case AND:
                case OR:
                    break;

                default:
                    importFilter.getRules().get(0).setOperator(Operator.AND);
                    break;
            }

            int row = filterTableModel.getRowCount();

            for (Rule rule : importFilter.getRules()) {
                setRowData(rule, row++, false);
            }

            updateRuleNumbers();
        } else {
            connector.setFilter(importFilter);

            /*
             * We don't need to check the boolean return value from load() because we already
             * checked for unsupported rule types earlier in this method.
             */
            load(connector, importFilter, transformer, modified);
        }
    }

    /*
     * Export the filter.
     */
    public void doExport() {
        accept(false);

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        String filterXML = serializer.serialize(filter);

        parent.exportFile(filterXML, null, "XML", "Filter");
    }

    /*
     * Validate the current rule
     */
    public void doValidate() {
        String type = (String) filterTable.getValueAt(filterTable.getSelectedRow(), RULE_TYPE_COL);
        try {
            FilterRulePlugin rulePlugin = getPlugin(type);
            int selectedRule = filterTable.getSelectedRow();
            saveData(selectedRule);
            String validationMessage = rulePlugin.doValidate(rulePlugin.getData(selectedRule));

            if (validationMessage == null) {
                parent.alertInformation(this, "Validation successful.");
            } else {
                parent.alertInformation(this, validationMessage);
            }
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    /**
     * Run a specific rule's validator.
     * 
     * @param
     * @return
     */
    public String validateRule(Rule rule) {
        try {
            FilterRulePlugin rulePlugin = getPlugin(rule.getType());
            return rulePlugin.doValidate((Map<Object, Object>) rule.getData());
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
            return "Exception occurred during validation.";
        }
    }

    public List<Rule> buildRuleList(List<Rule> list, int endingRow) {
        for (int i = 0; i < endingRow; i++) {
            Rule rule = new Rule();
            rule.setSequenceNumber(Integer.parseInt(filterTable.getValueAt(i, RULE_NUMBER_COL).toString()));

            String operator = filterTableModel.getValueAt(i, RULE_OP_COL).toString();
            if (i == 0 || StringUtils.isBlank(operator)) {
                rule.setOperator(Rule.Operator.NONE);
            } else {
                rule.setOperator(Rule.Operator.valueOf(operator));
            }

            rule.setData((LinkedHashMap<Object, Object>) filterTableModel.getValueAt(i, RULE_DATA_COL));
            rule.setName((String) filterTableModel.getValueAt(i, RULE_NAME_COL));
            rule.setType((String) filterTableModel.getValueAt(i, RULE_TYPE_COL));

            Map<Object, Object> map = (Map<Object, Object>) rule.getData();
            try {
                rule.setScript(getPlugin(rule.getType()).getScript(map));
            } catch (Exception e) {
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }

            list.add(rule);
        }
        return list;
    }

    private Set<String> getRuleVariables(int row) {
        Set<String> concatenatedRules = new LinkedHashSet<String>();
        VariableListUtil.getRuleVariables(concatenatedRules, channel.getSourceConnector(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                VariableListUtil.getRuleVariables(concatenatedRules, destination, true, row);
                seenCurrent = true;
            } else if (!seenCurrent) {
                VariableListUtil.getRuleVariables(concatenatedRules, destination, false);
                concatenatedRules.add(destination.getName());
            }
        }
        return concatenatedRules;
    }

    private Set<String> getGlobalStepVariables(int row) {
        Set<String> concatenatedSteps = new LinkedHashSet<String>();
        VariableListUtil.getStepVariables(concatenatedSteps, channel.getSourceConnector().getTransformer(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                seenCurrent = true;
                // VariableListUtil.getStepVariables(concatenatedSteps,
                // destination, true, row);
            } else if (!seenCurrent) {
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), false);
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getResponseTransformer(), false);
                concatenatedSteps.add(destination.getName());
            }
        }
        return concatenatedSteps;
    }

    /**
     * void accept(MouseEvent evt) returns a vector of vectors to the caller of this.
     */
    public void accept() {
        accept(true);
    }

    public void accept(boolean returning) {
        saveData(filterTable.getSelectedRow());

        List<Rule> list = buildRuleList(new ArrayList<Rule>(), filterTable.getRowCount());
        filter.setRules(list);

        transformer.setInboundTemplate(tabTemplatePanel.getIncomingMessage());
        transformer.setInboundProperties(tabTemplatePanel.getIncomingDataProperties());

        // reset the task pane and content to channel edit page
        if (returning) {
            parent.channelEditPanel.setDestinationVariableList();
            parent.setCurrentContentPage(parent.channelEditPanel);
            parent.setFocus(parent.channelEditTasks);
            parent.setPanelName("Edit Channel - " + parent.channelEditPanel.currentChannel.getName());
            if (modified) {
                parent.setSaveEnabled(true);
            }

            parent.channelEditPanel.updateComponentShown();
            modified = false;
        }
    }

    /**
     * void updateRuleNumbers() traverses the table and updates all data numbers, both in the model
     * and the view, after any change to the table
     */
    private void updateRuleNumbers() {
        updating = true;

        int rowCount = filterTableModel.getRowCount();
        int selRow = filterTable.getSelectedRow();

        for (int i = 0; i < rowCount; i++) {
            filterTableModel.setValueAt(i, i, RULE_NUMBER_COL);
        }

        updateOperations();
        String type = new String();

        if (isValid(selRow)) {
            filterTable.setRowSelectionInterval(selRow, selRow);
            loadData(selRow);
            type = filterTableModel.getValueAt(selRow, RULE_TYPE_COL).toString();
            rulePanel.showCard(type);
        } else if (rowCount > 0) {
            filterTable.setRowSelectionInterval(0, 0);
            loadData(0);
            type = filterTableModel.getValueAt(0, RULE_TYPE_COL).toString();
            rulePanel.showCard(type);
        }

        updateTaskPane(type);
        updating = false;
    }

    /**
     * updateTaskPane() configure the task pane so that it shows only relevant tasks
     */
    public void updateTaskPane(String newType) {
        int rowCount = filterTableModel.getRowCount();
        if (rowCount <= 0) {
            parent.setVisibleTasks(filterTasks, filterPopupMenu, 1, -1, false);
        } else if (rowCount == 1) {
            parent.setVisibleTasks(filterTasks, filterPopupMenu, 0, -1, true);
            parent.setVisibleTasks(filterTasks, filterPopupMenu, 2, -1, false);
        } else {
            parent.setVisibleTasks(filterTasks, filterPopupMenu, 0, -1, true);

            int selRow = filterTable.getSelectedRow();
            if (selRow == 0) // hide move up
            {
                parent.setVisibleTasks(filterTasks, filterPopupMenu, 5, 5, false);
            } else if (selRow == rowCount - 1) // hide move down
            {
                parent.setVisibleTasks(filterTasks, filterPopupMenu, 6, 6, false);
            }
        }
        parent.setVisibleTasks(filterTasks, filterPopupMenu, 2, 3, true);

        try {
            if (newType != null && !newType.equals("")) {
                parent.setVisibleTasks(filterTasks, filterPopupMenu, 4, 4, getPlugin(newType).showValidateTask());
            } else {
                parent.setVisibleTasks(filterTasks, filterPopupMenu, 4, 4, false);
            }
        } catch (Exception e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    /**
     * updateOperations() goes through all existing rules, enforcing rule 0 to be a
     * Rule.Operator.NONE, and any other NONEs to ANDs.
     */
    private void updateOperations() {
        for (int i = 0; i < filterTableModel.getRowCount(); i++) {
            if (i == 0) {
                filterTableModel.setValueAt("", i, RULE_OP_COL);
            } else if (filterTableModel.getValueAt(i, RULE_OP_COL).toString().equals("")) {
                filterTableModel.setValueAt(Rule.Operator.AND.toString(), i, RULE_OP_COL);
            }
        }
    }

    public int getSelectedRow() {
        return filterTable.getSelectedRow();
    }

    public DefaultTableModel getTableModel() {
        return filterTableModel;
    }

    public void resizePanes() {
        hSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 3.5));
        vSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 2 + PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 6.7));
        tabTemplatePanel.resizePanes();
    }
}
