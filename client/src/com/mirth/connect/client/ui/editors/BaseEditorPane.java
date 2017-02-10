/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorElement;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.plugins.FilterTransformerTypePlugin;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.ScriptBuilderException;

public abstract class BaseEditorPane<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends JPanel implements DropTargetListener {

    private static final int TASK_ADD = 0;
    private static final int TASK_DELETE = 1;
    private static final int TASK_IMPORT = 2;
    private static final int TASK_EXPORT = 3;
    private static final int TASK_VALIDATE = 4;
    private static final int TASK_VALIDATE_ELEMENT = 5;
    private static final int TASK_MOVE_UP = 6;
    private static final int TASK_MOVE_DOWN = 7;

    protected int numColumn;
    protected int operatorColumn = -1;
    protected int nameColumn;
    protected int typeColumn;
    protected int columnCount = 3;

    private JXTaskPane viewTasks;
    private JXTaskPane editorTasks;
    private JPopupMenu editorPopupMenu;

    private Connector connector;
    private boolean response;
    private T originalProperties;
    private AtomicBoolean updating = new AtomicBoolean(false);
    private DropTarget dropTarget;

    public BaseEditorPane() {
        initComponents();
        initLayout();
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    protected abstract Class<?> getContainerClass();

    protected abstract String getContainerName();

    protected boolean allowCellEdit(int rowIndex, int columnIndex) {
        if (columnIndex == nameColumn) {
            try {
                return getPlugins().get(treeTable.getValueAt(rowIndex, typeColumn)).isNameEditable();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return columnIndex == typeColumn;
    }

    protected abstract void onTableLoad();

    protected abstract void updateTable();

    protected abstract String getElementName();

    protected abstract boolean useOperatorColumn();

    protected abstract Object getOperator(C element);

    protected abstract void setOperator(C element, Object value);

    protected abstract Map<String, FilterTransformerTypePlugin<C>> getPlugins();

    public void accept() {
        accept(true);
    }

    public void accept(boolean returning) {
        saveData();
        boolean saveEnabled = PlatformUI.MIRTH_FRAME.isSaveEnabled();
        T properties = getProperties();

        if (StringUtils.isNotBlank(validateAll())) {
            return;
        }

        doAccept(connector, properties, response);

        if (returning) {
            PlatformUI.MIRTH_FRAME.channelEditPanel.setDestinationVariableList();
            PlatformUI.MIRTH_FRAME.setCurrentContentPage(PlatformUI.MIRTH_FRAME.channelEditPanel);
            PlatformUI.MIRTH_FRAME.setFocus(PlatformUI.MIRTH_FRAME.channelEditTasks);
            PlatformUI.MIRTH_FRAME.setPanelName("Edit Channel - " + PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getName());
            if (isModified(properties)) {
                saveEnabled = true;
            }
            PlatformUI.MIRTH_FRAME.channelEditPanel.updateComponentShown();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(saveEnabled);
        }
    }

    protected abstract void doAccept(Connector connector, T properties, boolean response);

    public boolean isModified() {
        return isModified(getProperties());
    }

    protected boolean isModified(T properties) {
        return !Objects.equals(originalProperties, getProperties());
    }

    public abstract T getProperties();

    public void setProperties(Connector connector, T properties, boolean response) {
        setProperties(connector, properties, response, true);
    }

    @SuppressWarnings("unchecked")
    public void setProperties(Connector connector, T properties, boolean response, boolean overwriteOriginal) {
        boolean saveEnabled = PlatformUI.MIRTH_FRAME.isSaveEnabled();
        PlatformUI.MIRTH_FRAME.setCurrentContentPage((JPanel) this);

        this.connector = connector;
        this.response = response;
        properties = (T) properties.clone();
        if (overwriteOriginal) {
            originalProperties = (T) properties.clone();
        }

        updating.set(true);
        try {
            propertiesScrollPane.setViewportView(null);
            propertiesContainer.removeAll();
            tabPane.updateUI();

            if (connector.getMode() == Connector.Mode.SOURCE) {
                templatePanel.setSourceView();
            } else if (connector.getMode() == Connector.Mode.DESTINATION) {
                templatePanel.setDestinationView(response);
            }
            templatePanel.setDefaultComponent();

            doSetProperties(connector, properties, response, overwriteOriginal);

            PlatformUI.MIRTH_FRAME.setFocus(new JXTaskPane[] { viewTasks,
                    editorTasks }, false, true);
            PlatformUI.MIRTH_FRAME.setSaveEnabled(saveEnabled);

            updateTemplateVariables();
            updateTaskPane();
            updateSequenceNumbers();
            updateTable();
        } finally {
            updating.set(false);
        }

        if (treeTable.getRowCount() > 0) {
            treeTable.setRowSelectionInterval(0, 0);
        }
        treeTable.expandAll();
    }

    protected abstract void doSetProperties(Connector connector, T properties, boolean response, boolean overwriteOriginal);

    @SuppressWarnings("unchecked")
    public List<C> getElements() {
        List<C> elements = new ArrayList<C>();
        for (Enumeration<? extends TreeTableNode> en = treeTableModel.getRoot().children(); en.hasMoreElements();) {
            elements.add(((FilterTransformerTreeTableNode<T, C>) en.nextElement()).getElementWithChildren());
        }
        return elements;
    }

    public void setElements(List<C> elements) {
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        for (C element : elements) {
            root.add(createTreeTableNode(element));
        }
        treeTableModel.setRoot(root);
        treeTable.expandPath(new TreePath(treeTableModel.getPathToRoot(treeTableModel.getRoot())));
    }

    protected abstract FilterTransformerTreeTableNode<T, C> createTreeTableNode(C element);

    public String getInboundTemplate() {
        return templatePanel.getIncomingMessage();
    }

    public void setInboundTemplate(String inboundTemplate) {
        templatePanel.setIncomingMessage(inboundTemplate);
    }

    public String getOutboundTemplate() {
        return templatePanel.getOutgoingMessage();
    }

    public void setOutboundTemplate(String outboundTemplate) {
        templatePanel.setOutgoingMessage(outboundTemplate);
    }

    public String getInboundDataType() {
        return PlatformUI.MIRTH_FRAME.displayNameToDataType.get(templatePanel.getIncomingDataType());
    }

    public void setInboundDataType(String inboundDataType) {
        templatePanel.setIncomingDataType(PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(inboundDataType));
    }

    public String getOutboundDataType() {
        return PlatformUI.MIRTH_FRAME.displayNameToDataType.get(templatePanel.getOutgoingDataType());
    }

    public void setOutboundDataType(String outboundDataType) {
        templatePanel.setOutgoingDataType(PlatformUI.MIRTH_FRAME.dataTypeToDisplayName.get(outboundDataType));
    }

    public DataTypeProperties getInboundDataTypeProperties() {
        return templatePanel.getIncomingDataProperties();
    }

    public void setInboundDataTypeProperties(DataTypeProperties properties) {
        templatePanel.setIncomingDataProperties(properties);
    }

    public DataTypeProperties getOutboundDataTypeProperties() {
        return templatePanel.getOutgoingDataProperties();
    }

    public void setOutboundDataTypeProperties(DataTypeProperties properties) {
        templatePanel.setOutgoingDataProperties(properties);
    }

    public void doAddNewElement() {
        addNewElement();
    }

    public abstract void addNewElement();

    public void addNewElement(String name, String variable, String mapping, String type) {
        try {
            int rowCount = treeTable.getRowCount();
            int selectedRow = treeTable.getSelectedRow();
            saveData(selectedRow);

            FilterTransformerTypePlugin<C> plugin = getPlugins().get(type);
            C element = plugin.newObject(variable, mapping);
            element.setName(name);
            addRow(element);
            updateSequenceNumbers();

            treeTable.setRowSelectionInterval(rowCount, rowCount);
            treeTable.scrollRowToVisible(rowCount);
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
        }
    }

    private void addRow(C element) {
        treeTableModel.insertNodeInto(createTreeTableNode(element), (MutableTreeTableNode) treeTableModel.getRoot(), treeTableModel.getRoot().getChildCount());
    }

    public void doDeleteElement() {
        stopTableEditing();
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(selectedRow);

            updating.set(true);
            try {
                treeTableModel.removeNodeFromParent(node);
            } finally {
                updating.set(false);
            }

            if (isValidViewRow(selectedRow)) {
                treeTable.setRowSelectionInterval(selectedRow, selectedRow);
            } else if (isValidViewRow(selectedRow - 1)) {
                treeTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            } else {
                propertiesScrollPane.setViewportView(null);
                propertiesContainer.removeAll();
                tabPane.updateUI();
            }

            updateTaskPane();
            updateGeneratedCode();
            updateTemplateVariables();
            updateSequenceNumbers();
            updateTable();
        }
    }

    public void doImport() {
        String content = PlatformUI.MIRTH_FRAME.browseForFileString("XML");

        if (content != null) {
            importFilterTransformer(content);
        }
    }

    @SuppressWarnings("unchecked")
    private void importFilterTransformer(String content) {
        String containerName = getContainerName().toLowerCase();
        String elementName = getElementName().toLowerCase();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        T properties = null;

        try {
            /*
             * Note: Filters/Transformers generated prior to version 3.0.0 cannot be imported and
             * migrated, because the lack of a version field poses problems with migration. However
             * they can still be imported as part of a connector.
             */
            properties = (T) serializer.deserialize(content, getContainerClass());
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertError(this, "Invalid " + containerName + " file.");
            return;
        }

        boolean append = (tableModel.getRowCount() > 0 && PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Would you like to append the " + elementName + "s from the imported " + containerName + " into the existing " + containerName + "?"));

        /*
         * When appending, we merely add the elements from the filter/transformer being imported.
         * When not appending, we replace the entire filter/transformer with the one being imported.
         */
        if (append) {
            for (C element : properties.getElements()) {
                addRow(element);
            }
            updateSequenceNumbers();
            updateTable();
        } else {
            setProperties(connector, properties, response, false);
        }
    }

    public void doExport() {
        saveData();
        PlatformUI.MIRTH_FRAME.exportFile(ObjectXMLSerializer.getInstance().serialize(getProperties()), null, "XML", getContainerName());
    }

    public void doValidate() {
        saveData();
        if (StringUtils.isBlank(validateAll())) {
            PlatformUI.MIRTH_FRAME.alertInformation(this, "Validation successful.");
        }
    }

    @SuppressWarnings("unchecked")
    private String validateAll() {
        int selectedRow = treeTable.getSelectedRow();
        String selectedSequenceNumber = null;
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);
            selectedSequenceNumber = ((FilterTransformerTreeTableNode<T, C>) treeTable.getPathForRow(selectedRow).getLastPathComponent()).getElement().getSequenceNumber();
        }

        String containerName = getContainerName().toLowerCase();
        String elementName = getElementName().toLowerCase();

        T properties = getProperties();
        StringBuilder builder = new StringBuilder();
        for (C element : properties.getElements()) {
            validateElementRecursive(element, builder, selectedSequenceNumber);
        }
        String errors = builder.toString();

        if (StringUtils.isBlank(errors)) {
            try {
                StringBuilder script = new StringBuilder();
                for (C element : properties.getElements()) {
                    script.append(element.getScript(false)).append('\n');
                }
                errors = JavaScriptSharedUtil.validateScript(script.toString());
            } catch (Exception e) {
                errors = "Exception occurred during validation.";
            }
        }

        if (StringUtils.isNotBlank(errors)) {
            PlatformUI.MIRTH_FRAME.alertCustomError(PlatformUI.MIRTH_FRAME, errors, "Error validating " + containerName + " " + elementName + "s.");
        }

        return errors;
    }

    @SuppressWarnings("unchecked")
    private void validateElementRecursive(C element, StringBuilder builder, String selectedSequenceNumber) {
        String validationMessage = validateElement(element, StringUtils.equals(element.getSequenceNumber(), selectedSequenceNumber), false);
        if (StringUtils.isNotBlank(validationMessage)) {
            builder.append("Error in connector \"").append(connector.getName()).append("\" at ").append(response ? "response " : "").append(getContainerName().toLowerCase()).append(' ').append(getElementName().toLowerCase()).append(' ').append(element.getSequenceNumber()).append(" (\"").append(element.getName()).append("\"):\n").append(validationMessage).append("\n\n");
        }
        if (element instanceof IteratorElement) {
            for (C child : ((IteratorElement<C>) element).getProperties().getChildren()) {
                validateElementRecursive(child, builder, selectedSequenceNumber);
            }
        }
    }

    public void doValidateElement() {
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            String type = (String) treeTable.getValueAt(selectedRow, typeColumn);
            try {
                FilterTransformerTypePlugin<C> plugin = getPlugins().get(type);
                String validationMessage = validateElement(plugin.getProperties());
                if (StringUtils.isBlank(validationMessage)) {
                    PlatformUI.MIRTH_FRAME.alertInformation(this, "Validation successful.");
                } else {
                    PlatformUI.MIRTH_FRAME.alertInformation(this, validationMessage);
                }
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            }
        }
    }

    public String validateElement(C element) {
        return validateElement(element, true, true);
    }

    public String validateElement(C element, boolean highlight, boolean checkSyntax) {
        try {
            FilterTransformerTypePlugin<C> plugin = getPlugins().get(element.getType());
            String validationMessage = plugin.checkProperties(element, highlight);
            if (checkSyntax && StringUtils.isBlank(validationMessage)) {
                validationMessage = JavaScriptSharedUtil.validateScript(element.getScript(false));
            }
            return validationMessage;
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            return "Exception occurred during validation.";
        }
    }

    public void doMoveElementUp() {
        moveElement(true);
    }

    public void doMoveElementDown() {
        moveElement(false);
    }

    private void moveElement(boolean up) {
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(selectedRow);
            MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();

            int childCount = parent.getChildCount();
            int index = parent.getIndex(node);
            if (up && index > 0 || !up && index + 1 < childCount) {
                saveData(selectedRow);
                updating.set(true);
                try {
                    treeTableModel.removeNodeFromParent(node);
                    treeTableModel.insertNodeInto(node, parent, index + (up ? -1 : 1));
                    selectedRow = treeTable.getRowForPath(new TreePath(treeTableModel.getPathToRoot(node)));
                    treeTable.setRowSelectionInterval(selectedRow, selectedRow);
                    updateSequenceNumbers();
                    updateTaskPane();
                    updateTable();
                    updateTemplateVariables(selectedRow);
                } finally {
                    updating.set(false);
                }
            }
        }
    }

    public void resizePanes() {
        verticalSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 3.5));
        horizontalSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 2 + PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 6.7));
        templatePanel.resizePanes();
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder());
        setPreferredSize(new Dimension(0, 0));

        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setBorder(BorderFactory.createEmptyBorder());
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane.setContinuousLayout(true);

        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setBorder(BorderFactory.createEmptyBorder());
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setContinuousLayout(true);

        boolean useOperatorColumn = useOperatorColumn();
        int columnIndex = 0;
        List<String> columnNames = new ArrayList<String>();
        columnNames.add("#");
        numColumn = columnIndex++;
        if (useOperatorColumn) {
            columnNames.add("Operator");
            operatorColumn = columnIndex++;
            columnCount++;
        }
        columnNames.add("Name");
        nameColumn = columnIndex++;
        columnNames.add("Type");
        typeColumn = columnIndex++;

        final TableCellRenderer numCellRenderer = new LeftCellRenderer();
        final TableCellEditor nameCellEditor = new DefaultCellEditor(new JTextField());

        treeTable = new MirthTreeTable() {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == numColumn) {
                    return numCellRenderer;
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (isHierarchical(column)) {
                    return nameCellEditor;
                } else {
                    return super.getCellEditor(row, column);
                }
            }
        };

        treeTableModel = new SortableTreeTableModel() {
            @Override
            public int getHierarchicalColumn() {
                return nameColumn;
            }

            @Override
            public boolean isCellEditable(Object node, int column) {
                int row = treeTable.getRowForPath(new TreePath(getPathToRoot((TreeTableNode) node)));
                return allowCellEdit(row, column);
            }
        };
        treeTableModel.setColumnIdentifiers(columnNames);

        treeTable.setBorder(BorderFactory.createEmptyBorder());
        treeTable.setTreeTableModel(treeTableModel);
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.setDoubleBuffered(true);
        treeTable.setFocusable(true);
        treeTable.setEditable(true);
        treeTable.setAutoCreateColumnsFromModel(false);
        treeTable.setShowGrid(true, true);
        treeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        treeTable.setCustomEditorControls(true);
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.getTreeSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeTable.setRowHeight(UIConstants.ROW_HEIGHT);
        treeTable.packTable(UIConstants.COL_MARGIN);
        treeTable.setSortable(false);
        treeTable.setOpaque(true);
        treeTable.setRowSelectionAllowed(true);
        treeTable.setDragEnabled(false);
        treeTable.getTableHeader().setReorderingAllowed(false);
        treeTable.putClientProperty("JTree.lineStyle", "Horizontal");

        treeTable.setTreeCellRenderer(new NameRenderer());

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            treeTable.setHighlighters(highlighter);
        }

        List<String> types = new ArrayList<String>();
        for (String pluginPointName : getPlugins().keySet()) {
            types.add(pluginPointName);
        }
        String[] typeArray = types.toArray(new String[types.size()]);

        MirthComboBoxTableCellEditor typeEditor = new MirthComboBoxTableCellEditor(treeTable, typeArray, 2, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        treeTable.getColumnExt(numColumn).setMaxWidth(UIConstants.MAX_WIDTH);
        treeTable.getColumnExt(numColumn).setPreferredWidth(36);
        treeTable.getColumnExt(numColumn).setCellRenderer(new LeftCellRenderer());

        treeTable.getColumnExt(typeColumn).setMaxWidth(UIConstants.MAX_WIDTH);
        treeTable.getColumnExt(typeColumn).setMinWidth(155);
        treeTable.getColumnExt(typeColumn).setPreferredWidth(155);
        treeTable.getColumnExt(typeColumn).setCellRenderer(new MirthComboBoxTableCellRenderer(typeArray));
        treeTable.getColumnExt(typeColumn).setCellEditor(typeEditor);

        treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                tableListSelectionChanged(evt);
            }
        });

        treeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        treeTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                    doDeleteElement();
                }
            }
        });

        treeTable.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}

            @Override
            public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException {
                if (!updating.getAndSet(true)) {
                    try {
                        saveData(treeTable.getSelectedRow());
                    } finally {
                        updating.set(false);
                    }
                }
            }
        });

        onTableLoad();

        treeTableScrollPane = new JScrollPane(treeTable);
        treeTableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        tabPane = new JTabbedPane();
        tabPane.setBorder(BorderFactory.createEmptyBorder());
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                if (tabPane.getSelectedIndex() == 1) {
                    updateGeneratedCode();
                }
            }
        });

        propertiesContainer = new JPanel();
        propertiesContainer.setBorder(BorderFactory.createEmptyBorder());
        tabPane.addTab(getElementName(), propertiesContainer);

        propertiesScrollPane = new JScrollPane();
        propertiesScrollPane.setBorder(BorderFactory.createEmptyBorder());

        generatedScriptTextArea = new MirthRTextScrollPane(null, true, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, false);
        generatedScriptTextArea.setBackground(new Color(204, 204, 204));
        generatedScriptTextArea.setBorder(BorderFactory.createEtchedBorder());
        generatedScriptTextArea.getTextArea().setEditable(false);
        generatedScriptTextArea.getTextArea().setDropTarget(null);
        tabPane.addTab("Generated Script", generatedScriptTextArea);

        templatePanel = new TabbedTemplatePanel();
        templatePanel.setBorder(BorderFactory.createEmptyBorder());

        ActionListener nameActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!updating.get()) {
                    int selectedRow = treeTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        treeTableModel.setValueAt(evt.getActionCommand(), getNodeAtRow(selectedRow), nameColumn);
                    }
                }
            }
        };
        for (FilterTransformerTypePlugin<C> plugin : getPlugins().values()) {
            plugin.getPanel().addNameActionListener(nameActionListener);
        }

        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);
        viewTasks.add(initActionCallback("accept", "Return back to channel.", ActionFactory.createBoundAction("accept", "Back to Channel", "B"), new ImageIcon(Frame.class.getResource("images/resultset_previous.png"))));
        PlatformUI.MIRTH_FRAME.setNonFocusable(viewTasks);
        viewTasks.setVisible(false);
        PlatformUI.MIRTH_FRAME.taskPaneContainer.add(viewTasks, PlatformUI.MIRTH_FRAME.taskPaneContainer.getComponentCount() - 1);

        String containerName = getContainerName().toLowerCase();
        String containerNameCap = WordUtils.capitalize(containerName);
        String elementName = getElementName().toLowerCase();
        String elementNameCap = WordUtils.capitalize(elementName);

        editorTasks = new JXTaskPane();
        editorTasks.setTitle(containerNameCap + " Tasks");
        editorTasks.setFocusable(false);
        editorPopupMenu = new JPopupMenu();

        editorTasks.add(initActionCallback("doAddNewElement", "Add a new " + containerName + " " + elementName + ".", ActionFactory.createBoundAction("doAddNewElement", "Add New " + elementNameCap, "N"), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewElementItem = new JMenuItem("Add New " + elementNameCap);
        addNewElementItem.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        addNewElementItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doAddNewElement();
            }
        });
        editorPopupMenu.add(addNewElementItem);

        editorTasks.add(initActionCallback("doDeleteElement", "Delete the currently selected " + containerName + " " + elementName + ".", ActionFactory.createBoundAction("doDeleteElement", "Delete " + elementNameCap, "X"), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem doDeleteElementItem = new JMenuItem("Delete " + elementNameCap);
        doDeleteElementItem.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        doDeleteElementItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doDeleteElement();
            }
        });
        editorPopupMenu.add(doDeleteElementItem);

        editorTasks.add(initActionCallback("doImport", "Import a " + containerName + " from an XML file.", ActionFactory.createBoundAction("doImport", "Import " + containerNameCap, "I"), new ImageIcon(Frame.class.getResource("images/report_go.png"))));
        JMenuItem doImportItem = new JMenuItem("Import " + containerNameCap);
        doImportItem.setIcon(new ImageIcon(Frame.class.getResource("images/report_go.png")));
        doImportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doImport();
            }
        });
        editorPopupMenu.add(doImportItem);

        editorTasks.add(initActionCallback("doExport", "Export the " + containerName + " to an XML file.", ActionFactory.createBoundAction("doExport", "Export " + containerNameCap, "E"), new ImageIcon(Frame.class.getResource("images/report_disk.png"))));
        JMenuItem doExportItem = new JMenuItem("Export " + containerNameCap);
        doExportItem.setIcon(new ImageIcon(Frame.class.getResource("images/report_disk.png")));
        doExportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doExport();
            }
        });
        editorPopupMenu.add(doExportItem);

        editorTasks.add(initActionCallback("doValidate", "Validate the entire " + containerName + " and all " + elementName + "s.", ActionFactory.createBoundAction("doValidate", "Validate " + containerNameCap, "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem doValidateItem = new JMenuItem("Validate " + containerNameCap);
        doValidateItem.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        doValidateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doValidateElement();
            }
        });
        editorPopupMenu.add(doValidateItem);

        editorTasks.add(initActionCallback("doValidateElement", "Validate the current " + elementName + ".", ActionFactory.createBoundAction("doValidateElement", "Validate " + elementNameCap, "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem doValidateElementItem = new JMenuItem("Validate " + elementNameCap);
        doValidateElementItem.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        doValidateElementItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doValidateElement();
            }
        });
        editorPopupMenu.add(doValidateElementItem);

        editorTasks.add(initActionCallback("doMoveElementUp", "Move the currently selected " + elementName + " up.", ActionFactory.createBoundAction("doMoveElementUp", "Move " + elementNameCap + " Up", "P"), new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem doMoveElementUpItem = new JMenuItem("Move " + elementNameCap + " Up");
        doMoveElementUpItem.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_up.png")));
        doMoveElementUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doMoveElementUp();
            }
        });
        editorPopupMenu.add(doMoveElementUpItem);

        editorTasks.add(initActionCallback("doMoveElementDown", "Move the currently selected " + elementName + " down.", ActionFactory.createBoundAction("doMoveElementDown", "Move " + elementNameCap + " Down", "D"), new ImageIcon(Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem doMoveElementDownItem = new JMenuItem("Move " + elementNameCap + " Down");
        doMoveElementDownItem.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_down.png")));
        doMoveElementDownItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doMoveElementDown();
            }
        });
        editorPopupMenu.add(doMoveElementDownItem);

        // add the tasks to the taskpane, and the taskpane to the mirth client
        PlatformUI.MIRTH_FRAME.setNonFocusable(editorTasks);
        editorTasks.setVisible(false);
        PlatformUI.MIRTH_FRAME.taskPaneContainer.add(editorTasks, PlatformUI.MIRTH_FRAME.taskPaneContainer.getComponentCount() - 1);

        dropTarget = new DropTarget(this, this);
        treeTable.setDropTarget(dropTarget);
        treeTableScrollPane.setDropTarget(dropTarget);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        propertiesContainer.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        verticalSplitPane.setTopComponent(treeTableScrollPane);
        verticalSplitPane.setBottomComponent(tabPane);

        horizontalSplitPane.setLeftComponent(verticalSplitPane);
        horizontalSplitPane.setRightComponent(templatePanel);

        add(horizontalSplitPane, "grow");
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void checkSelectionAndPopupMenu(MouseEvent evt) {
        int row = treeTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                treeTable.setRowSelectionInterval(row, row);
            }
            editorPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void tableListSelectionChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting() && !updating.getAndSet(true)) {
            try {
                int selectedRow = treeTable.getSelectedRow();
                int previousRow = selectedRow == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();

                if (previousRow != selectedRow) {
                    saveData(previousRow);
                }
                loadData(selectedRow);
                updateTaskPane();
                updateTable();
                updateGeneratedCode();
            } finally {
                updating.set(false);
            }
        }
    }

    private void loadData(int viewRow) {
        if (isValidViewRow(viewRow)) {
            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(viewRow);
            C element = node.getElement();
            String type = element.getType();

            try {
                FilterTransformerTypePlugin<C> plugin = getPlugins().get(type);
                plugin.setProperties(connector.getMode(), response, element);

                propertiesContainer.removeAll();
                if (plugin.includesScrollPane()) {
                    propertiesScrollPane.setViewportView(null);
                    propertiesContainer.add(plugin.getPanel(), "grow");
                } else {
                    propertiesScrollPane.setViewportView(plugin.getPanel());
                    propertiesContainer.add(propertiesScrollPane, "grow");
                }
                tabPane.updateUI();
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            }
        } else {
            propertiesScrollPane.setViewportView(null);
            propertiesContainer.removeAll();
        }

        updateTemplateVariables(viewRow);
    }

    private void updateTemplateVariables() {
        updateTemplateVariables(0);
    }

    private void updateTemplateVariables(int viewRow) {
        if (connector.getMode() == Connector.Mode.SOURCE) {
            Set<String> concatenatedRules = new LinkedHashSet<String>();
            Set<String> concatenatedSteps = new LinkedHashSet<String>();
            getRuleVariables(connector, concatenatedRules, true);
            getStepVariables(connector, concatenatedSteps, true, viewRow);
            templatePanel.updateVariables(concatenatedRules, concatenatedSteps);
        } else {
            templatePanel.updateVariables(getRuleVariables(), getStepVariables(viewRow));
            templatePanel.populateConnectors(PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel.getDestinationConnectors());
        }
    }

    protected abstract void getRuleVariables(Connector connector, Set<String> concatenatedRules, boolean includeLocalVars);

    protected abstract void getStepVariables(Connector connector, Set<String> concatenatedSteps, boolean includeLocalVars, int viewRow);

    private Set<String> getRuleVariables() {
        Channel channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        Set<String> concatenatedRules = new LinkedHashSet<String>();
        VariableListUtil.getRuleVariables(concatenatedRules, channel.getSourceConnector().getFilter(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                getRuleVariables(connector, concatenatedRules, true);
                seenCurrent = true;
            } else if (!seenCurrent) {
                VariableListUtil.getRuleVariables(concatenatedRules, destination.getFilter(), false);
                concatenatedRules.add(destination.getName());
            }
        }
        return concatenatedRules;
    }

    private Set<String> getStepVariables(int viewRow) {
        Channel channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        Set<String> concatenatedSteps = new LinkedHashSet<String>();
        VariableListUtil.getStepVariables(concatenatedSteps, channel.getSourceConnector().getTransformer(), false);

        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext()) {
            Connector destination = it.next();
            if (connector == destination) {
                if (response) {
                    VariableListUtil.getStepVariables(concatenatedSteps, connector.getTransformer(), true);
                }
                getStepVariables(connector, concatenatedSteps, true, viewRow);
                seenCurrent = true;
            } else if (!seenCurrent) {
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getTransformer(), false);
                VariableListUtil.getStepVariables(concatenatedSteps, destination.getResponseTransformer(), false);
                concatenatedSteps.add(destination.getName());
            }
        }
        return concatenatedSteps;
    }

    private void saveData() {
        saveData(treeTable.getSelectedRow());
    }

    private void saveData(int viewRow) {
        stopTableEditing();
        if (isValidViewRow(viewRow)) {
            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(viewRow);
            String type = node.getElement().getType();
            try {
                C element = getPlugins().get(type).getProperties();
                element.setSequenceNumber(node.getElement().getSequenceNumber());
                if (useOperatorColumn()) {
                    setOperator(element, getOperator(node.getElement()));
                }
                element.setName(node.getElement().getName());

                node.setElement(element);
                treeTableModel.setValueAt(element.getSequenceNumber(), node, numColumn);
                if (useOperatorColumn()) {
                    treeTableModel.setValueAt(getOperator(element), node, operatorColumn);
                }
                treeTableModel.setValueAt(element.getName(), node, nameColumn);
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            }
        }
    }

    private void stopTableEditing() {
        if (treeTable.isEditing()) {
            treeTable.getCellEditor(treeTable.getEditingRow(), treeTable.getEditingColumn()).stopCellEditing();
        }
    }

    private boolean isValidViewRow(int viewRow) {
        return viewRow >= 0 && viewRow < treeTable.getRowCount();
    }

    @SuppressWarnings("unchecked")
    private FilterTransformerTreeTableNode<T, C> getNodeAtRow(int viewRow) {
        return (FilterTransformerTreeTableNode<T, C>) treeTable.getPathForRow(viewRow).getLastPathComponent();
    }

    private BoundAction initActionCallback(String callbackMethod, String toolTip, BoundAction boundAction, ImageIcon icon) {
        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }

    private void updateTaskPane() {
        int rowCount = treeTable.getRowCount();
        int selectedRow = treeTable.getSelectedRow();

        if (rowCount <= 0) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_DELETE, -1, false);
        } else if (rowCount == 1) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
        } else {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);

            TreePath path = treeTable.getPathForRow(selectedRow);
            if (path != null) {
                TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
                TreeTableNode parent = node.getParent();
                int childIndex = parent.getIndex(node);

                if (childIndex == 0) {
                    PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, TASK_MOVE_UP, false);
                }
                if (childIndex == parent.getChildCount() - 1) {
                    PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_DOWN, TASK_MOVE_DOWN, false);
                }
            } else {
                PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
            }
        }

        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_IMPORT, TASK_EXPORT, true);
        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_VALIDATE, TASK_VALIDATE, true);
        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_VALIDATE_ELEMENT, TASK_VALIDATE_ELEMENT, selectedRow >= 0);
        if (selectedRow < 0) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_DELETE, TASK_DELETE, false);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateGeneratedCode() {
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            TreePath path = treeTable.getPathForRow(selectedRow);
            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) path.getLastPathComponent();
            C element = node.getElementWithChildren();
            try {
                generatedScriptTextArea.setText(JavaScriptSharedUtil.prettyPrint(element.getScript(false)));
            } catch (ScriptBuilderException e) {
                e.printStackTrace();
                generatedScriptTextArea.setText("");
            }
        } else {
            generatedScriptTextArea.setText("");
        }
    }

    private void typeComboBoxActionPerformed(ActionEvent evt) {
        int selectedRow = treeTable.getSelectedRow();

        if (selectedRow >= 0 && !updating.getAndSet(true)) {
            try {
                FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(selectedRow);

                String selectedType = ((JComboBox<?>) evt.getSource()).getSelectedItem().toString();
                String previousType = (String) treeTableModel.getValueAt(node, typeColumn);

                if (!StringUtils.equalsIgnoreCase(selectedType, previousType)) {
                    FilterTransformerTypePlugin<C> plugin = getPlugins().get(previousType);
                    C selectedElement = plugin.getProperties();

                    String containerName = getContainerName().toLowerCase();
                    String elementName = getElementName().toLowerCase();
                    if (!EqualsBuilder.reflectionEquals(selectedElement, plugin.getDefaults(), "name", "sequenceNumber") && !PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you would like to change this " + containerName + " " + elementName + " and lose all of the current data?")) {
                        ((JComboBox<?>) evt.getSource()).getModel().setSelectedItem(previousType);
                        return;
                    }

                    plugin = getPlugins().get(selectedType);
                    C newElement = plugin.getDefaults();
                    node.setElement(newElement);
                    treeTableModel.setValueAt(newElement.getName(), node, nameColumn);
                    plugin.setProperties(connector.getMode(), response, newElement);

                    propertiesContainer.removeAll();
                    if (plugin.includesScrollPane()) {
                        propertiesScrollPane.setViewportView(null);
                        propertiesContainer.add(plugin.getPanel(), "grow");
                    } else {
                        propertiesScrollPane.setViewportView(plugin.getPanel());
                        propertiesContainer.add(propertiesScrollPane, "grow");
                    }
                    tabPane.updateUI();

                    updateTaskPane();
                    updateSequenceNumbers();
                    updateTable();
                    updateGeneratedCode();
                }
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
            } finally {
                updating.set(false);
            }
        }
    }

    private void updateSequenceNumbers() {
        updateSequenceNumbers(treeTableModel.getRoot(), "");
    }

    private void updateSequenceNumbers(TreeTableNode node, String prefix) {
        if (StringUtils.isNotEmpty(prefix)) {
            prefix += "-";
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeTableNode child = node.getChildAt(i);
            String num = prefix + i;
            treeTableModel.setValueAt(num, child, numColumn);
            updateSequenceNumbers(child, num);
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);

                List<?> fileList = (List<?>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<?> iterator = fileList.iterator();
                if (iterator.hasNext() && fileList.size() == 1) {
                    String fileName = ((File) iterator.next()).getName();
                    if (!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml")) {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            } else if (!handleDragEnter(dtde, tr)) {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    protected abstract boolean handleDragEnter(DropTargetDragEvent dtde, Transferable tr) throws UnsupportedFlavorException, IOException;

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> iterator = fileList.iterator();

                if (fileList.size() == 1) {
                    File file = (File) iterator.next();
                    importFilterTransformer(PlatformUI.MIRTH_FRAME.readFileToString(file));
                }
            } else {
                handleDrop(dtde, tr);
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }
    }

    protected abstract void handleDrop(DropTargetDropEvent dtde, Transferable tr) throws UnsupportedFlavorException, IOException;

    private class LeftCellRenderer extends DefaultTableCellRenderer {

        public LeftCellRenderer() {
            setHorizontalAlignment(LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (StringUtils.isNotBlank((String) value)) {
                value = " " + value;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private class NameRenderer extends JLabel implements TreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (row >= 0 && value instanceof FilterTransformerTreeTableNode) {
                FilterTransformerTreeTableNode<?, ?> node = (FilterTransformerTreeTableNode<?, ?>) value;
                setText(node.getElement().getName());

                if (node.getElement() instanceof IteratorElement) {
                    setIcon(UIConstants.ICON_BULLET_YELLOW);
                } else {
                    setIcon(UIConstants.ICON_BULLET_GREEN);
                }

                if (selected) {
                    setBackground(treeTable.getSelectionBackground());
                } else if (row % 2 == 0) {
                    setBackground(UIConstants.HIGHLIGHTER_COLOR);
                } else {
                    setBackground(treeTable.getBackground());
                }
            }

            return this;
        }
    }

    private JSplitPane horizontalSplitPane;

    private JSplitPane verticalSplitPane;
    protected MirthTreeTable treeTable;
    private JScrollPane treeTableScrollPane;
    protected TableModel tableModel;
    protected DefaultTreeTableModel treeTableModel;
    private JTabbedPane tabPane;
    private JPanel propertiesContainer;
    private JScrollPane propertiesScrollPane;
    private MirthRTextScrollPane generatedScriptTextArea;

    public TabbedTemplatePanel templatePanel;
}