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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
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
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.Rule.Operator;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.plugins.FilterTransformerTypePlugin;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.ScriptBuilderException;

public abstract class BaseEditorPane<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends JPanel implements DropTargetListener {

    private static final int TASK_ADD = 0;
    private static final int TASK_DELETE = 1;
    private static final int TASK_ASSIGN_TO_ITERATOR = 2;
    private static final int TASK_REMOVE_FROM_ITERATOR = 3;
    private static final int TASK_IMPORT = 4;
    private static final int TASK_EXPORT = 5;
    private static final int TASK_VALIDATE = 6;
    private static final int TASK_VALIDATE_ELEMENT = 7;
    private static final int TASK_MOVE_UP = 8;
    private static final int TASK_MOVE_DOWN = 9;

    protected int numColumn;
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
    private Preferences userPreferences = Preferences.userNodeForPackage(Mirth.class);

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

    protected boolean allowNameEdit(int rowIndex, int columnIndex) {
        try {
            return getPlugins().get(treeTable.getValueAt(rowIndex, typeColumn)).isNameEditable();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean allowOperatorEdit(int rowIndex, int columnIndex) {
        return false;
    }

    private boolean allowCellEdit(int rowIndex, int columnIndex) {
        if (columnIndex == nameColumn) {
            return allowNameEdit(rowIndex, columnIndex) || allowOperatorEdit(rowIndex, columnIndex);
        }
        return columnIndex == typeColumn;
    }

    protected abstract void updateTable();

    protected abstract String getElementName();

    protected abstract boolean useOperatorColumn();

    protected abstract Object getOperator(C element);

    protected abstract void setOperator(C element, Object value);

    protected abstract Map<String, FilterTransformerTypePlugin<T, C>> getPlugins();

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

            treeTable.expandAll();
            if (treeTable.getRowCount() > 0) {
                treeTable.setRowSelectionInterval(0, 0);
                loadData(0);
            }

            updateTemplateVariables();
            updateTaskPane();
            updateSequenceNumbers();
            updateTable();
            updateGeneratedCode();
        } finally {
            updating.set(false);
        }

        if (treeTable.getRowCount() > 0) {
            SwingUtilities.invokeLater(() -> {
                treeTable.setRowSelectionInterval(0, 0);
            });
        }
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
        addNewElement(name, variable, mapping, type, false);
    }

    @SuppressWarnings("unchecked")
    public void addNewElement(String name, String variable, String mapping, String type, boolean showIteratorWizard) {
        updating.set(true);
        try {
            int selectedRow = treeTable.getSelectedRow();
            saveData(selectedRow);

            FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(type);
            if (plugin == null) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Could not find plugin of type: " + type);
                return;
            }

            MutableTreeTableNode parent = (MutableTreeTableNode) treeTableModel.getRoot();
            if (isValidViewRow(selectedRow)) {
                TreePath path = treeTable.getPathForRow(selectedRow);
                if (path != null) {
                    parent = (MutableTreeTableNode) path.getLastPathComponent();
                    if (!((FilterTransformerTreeTableNode<T, C>) parent).isIteratorNode()) {
                        parent = (MutableTreeTableNode) parent.getParent();
                    }
                }
            }

            if (parent instanceof FilterTransformerTreeTableNode) {
                variable = IteratorUtil.replaceIteratorVariables(JavaScriptSharedUtil.removeNumberLiterals(variable), parent);
                mapping = IteratorUtil.replaceIteratorVariables(JavaScriptSharedUtil.removeNumberLiterals(mapping), parent);
            }

            Pair<String, String> info = plugin.getIteratorInfo(variable, mapping);
            String target = info.getLeft();
            String outbound = info.getRight();

            if (showIteratorWizard && !JavaScriptSharedUtil.getExpressionParts(target, false).isEmpty() && userPreferences.getBoolean("filterTransformerShowIteratorDialog", true)) {
                String text = "Would you like to create a new Iterator for this " + getContainerName().toLowerCase() + " " + getElementName().toLowerCase() + "?";
                JCheckBox checkBox = new JCheckBox("Do not show this dialog again (may be re-enabled in the Administrator settings)");
                Object params = new Object[] { text, checkBox };
                int result = JOptionPane.showConfirmDialog(PlatformUI.MIRTH_FRAME, params, "Select An Option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (checkBox.isSelected()) {
                    userPreferences.putBoolean("filterTransformerShowIteratorDialog", false);
                }

                if (result == JOptionPane.YES_OPTION) {
                    IteratorWizardDialog<T, C> dialog = new IteratorWizardDialog<T, C>(target, null, parent, treeTableModel, true, outbound);
                    if (!dialog.wasAccepted()) {
                        return;
                    }

                    FilterTransformerTypePlugin<T, C> iteratorPlugin = getPlugins().get(IteratorProperties.PLUGIN_POINT);
                    IteratorElement<C> iteratorElement = (IteratorElement<C>) iteratorPlugin.getDefaults();
                    dialog.fillIteratorProperties(iteratorElement.getProperties());
                    ((IteratorPanel<C>) iteratorPlugin.getPanel()).setName(iteratorElement);

                    variable = IteratorUtil.replaceIteratorVariables(JavaScriptSharedUtil.removeNumberLiterals(variable), iteratorElement);
                    mapping = IteratorUtil.replaceIteratorVariables(JavaScriptSharedUtil.removeNumberLiterals(mapping), iteratorElement);

                    parent = insertNode(parent, (C) iteratorElement);
                    replaceIteratorVariables((FilterTransformerTreeTableNode<T, C>) parent);
                } else if (result != JOptionPane.NO_OPTION) {
                    return;
                }
            }

            C element = plugin.newObject(variable, mapping);
            element.setName(name);
            FilterTransformerTreeTableNode<T, C> node = insertNode(parent, element);
            replaceIteratorVariables(node);

            TreePath path = new TreePath(treeTableModel.getPathToRoot(node));
            treeTable.expandPath(path);
            treeTable.getTreeSelectionModel().setSelectionPath(path);
            treeTable.scrollPathToVisible(path);
            loadData(treeTable.getRowForPath(path));
            updateTaskPane();
            updateTable();
            updateGeneratedCode();
            updateSequenceNumbers();
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
        } finally {
            updating.set(false);
        }
    }

    public String replaceIteratorVariables(String expression) {
        return IteratorUtil.replaceIteratorVariables(expression, treeTable);
    }

    private FilterTransformerTreeTableNode<T, C> insertNode(C element) {
        return insertNode((MutableTreeTableNode) treeTableModel.getRoot(), element);
    }

    private FilterTransformerTreeTableNode<T, C> insertNode(MutableTreeTableNode parent, C element) {
        return insertNode(parent, element, parent.getChildCount());
    }

    private FilterTransformerTreeTableNode<T, C> insertNode(MutableTreeTableNode parent, C element, int index) {
        FilterTransformerTreeTableNode<T, C> node = createTreeTableNode(element);
        treeTableModel.insertNodeInto(node, parent, index);
        return node;
    }

    public void doDeleteElement() {
        stopTableEditing();
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(selectedRow);
            if (node.getChildCount() > 0 && !PlatformUI.MIRTH_FRAME.alertOkCancel(PlatformUI.MIRTH_FRAME, "All child " + getElementName().toLowerCase() + "s will be removed along with the Iterator. Are you sure you wish to continue?")) {
                return;
            }

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

    @SuppressWarnings("unchecked")
    private boolean hasIteratorNodes(MutableTreeTableNode node, FilterTransformerTreeTableNode<T, C> excluded) {
        if (!Objects.equals(node, excluded)) {
            if (node instanceof FilterTransformerTreeTableNode && ((FilterTransformerTreeTableNode<T, C>) node).isIteratorNode()) {
                return true;
            }
            for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                if (hasIteratorNodes(en.nextElement(), excluded)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void doAssignToIterator() {
        updating.set(true);
        try {
            int selectedRow = treeTable.getSelectedRow();
            if (!isValidViewRow(selectedRow)) {
                return;
            }
            saveData(selectedRow);

            TreePath path = treeTable.getPathForRow(selectedRow);
            if (path == null) {
                return;
            }

            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) path.getLastPathComponent();
            MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();
            int childIndex = parent.getIndex(node);
            String type = node.getElement().getType();

            FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(type);
            if (plugin == null) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Could not find plugin of type: " + type);
                return;
            }

            // Remove iterator variables before detaching
            removeIteratorVariables(node);

            Pair<String, String> iteratorInfo = plugin.getIteratorInfo(node.getElement());
            String target = JavaScriptSharedUtil.removeNumberLiterals(iteratorInfo.getLeft());
            String outbound = JavaScriptSharedUtil.removeNumberLiterals(iteratorInfo.getRight());

            FilterTransformerTypePlugin<T, C> iteratorPlugin = getPlugins().get(IteratorProperties.PLUGIN_POINT);

            if (JavaScriptSharedUtil.getExpressionParts(target, false).isEmpty() && !hasIteratorNodes((MutableTreeTableNode) treeTableModel.getRoot(), node)) {
                /*
                 * If there is no discernable target, and no other iterators to choose from, the
                 * most we can do is add a new default iterator and add the node to that.
                 */
                IteratorElement<C> iteratorElement = (IteratorElement<C>) iteratorPlugin.getDefaults();
                iteratorElement.getProperties().setIndexVariable(IteratorUtil.getValidIndexVariable(parent, node));

                treeTableModel.removeNodeFromParent(node);
                parent = insertNode(parent, (C) iteratorElement, childIndex);
                iteratorElement = (IteratorElement<C>) ((FilterTransformerTreeTableNode<T, C>) parent).getElement();
                replaceIteratorVariables((FilterTransformerTreeTableNode<T, C>) parent);
                ((IteratorPanel<C>) iteratorPlugin.getPanel()).setName(iteratorElement);
            } else {
                IteratorWizardDialog<T, C> dialog = new IteratorWizardDialog<T, C>(target, node, parent, treeTableModel, false, outbound);
                if (!dialog.wasAccepted()) {
                    return;
                }

                treeTableModel.removeNodeFromParent(node);

                if (dialog.isCreateNew()) {
                    IteratorElement<C> iteratorElement = (IteratorElement<C>) iteratorPlugin.getDefaults();
                    dialog.fillIteratorProperties(iteratorElement.getProperties());

                    parent = insertNode(parent, (C) iteratorElement, childIndex);
                    iteratorElement = (IteratorElement<C>) ((FilterTransformerTreeTableNode<T, C>) parent).getElement();
                    replaceIteratorVariables((FilterTransformerTreeTableNode<T, C>) parent);
                    ((IteratorPanel<C>) iteratorPlugin.getPanel()).setName(iteratorElement);
                } else {
                    target = IteratorUtil.removeIteratorVariables(target, parent);
                    outbound = IteratorUtil.removeIteratorVariables(outbound, parent);

                    parent = dialog.getSelectedParent();
                }
            }

            target = IteratorUtil.replaceIteratorVariables(target, parent);
            outbound = IteratorUtil.replaceIteratorVariables(outbound, parent);

            C element = node.getElementWithChildren();
            plugin.setIteratorInfo(element, target, outbound);

            // Replace iterator variables after reattaching
            node = insertNode(parent, element);
            replaceIteratorVariables(node);

            TreePath newPath = new TreePath(treeTableModel.getPathToRoot(node));
            treeTable.expandPath(newPath);
            treeTable.getTreeSelectionModel().setSelectionPath(newPath);
            treeTable.scrollPathToVisible(newPath);
            loadData(treeTable.getRowForPath(newPath));
            updateTaskPane();
            updateTable();
            updateGeneratedCode();
            updateSequenceNumbers();
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
        } finally {
            updating.set(false);
        }
    }

    @SuppressWarnings("unchecked")
    public void doRemoveFromIterator() {
        updating.set(true);
        try {
            int selectedRow = treeTable.getSelectedRow();
            if (!isValidViewRow(selectedRow)) {
                return;
            }
            saveData(selectedRow);

            TreePath path = treeTable.getPathForRow(selectedRow);
            if (path == null) {
                return;
            }

            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) path.getLastPathComponent();
            MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();
            if (!(node.getParent() instanceof FilterTransformerTreeTableNode)) {
                return;
            }

            // Remove iterator variables before detaching
            removeIteratorVariables(node);
            treeTableModel.removeNodeFromParent(node);

            // Replace iterator variables after reattaching
            node = insertNode((MutableTreeTableNode) parent.getParent(), node.getElementWithChildren());
            replaceIteratorVariables(node);

            TreePath newPath = new TreePath(treeTableModel.getPathToRoot(node));
            treeTable.expandPath(newPath);
            treeTable.getTreeSelectionModel().setSelectionPath(newPath);
            treeTable.scrollPathToVisible(newPath);
            loadData(treeTable.getRowForPath(newPath));
            updateTaskPane();
            updateTable();
            updateGeneratedCode();
            updateSequenceNumbers();
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
        } finally {
            updating.set(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceIteratorVariables(FilterTransformerTreeTableNode<T, C> node) {
        if (node.getParent() instanceof FilterTransformerTreeTableNode) {
            FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(node.getElement().getType());
            if (plugin == null) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Could not find plugin of type: " + node.getElement().getType());
                return;
            }

            plugin.replaceIteratorVariables(node.getElement(), (FilterTransformerTreeTableNode<T, C>) node.getParent());
        }

        for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
            replaceIteratorVariables((FilterTransformerTreeTableNode<T, C>) en.nextElement());
        }
    }

    @SuppressWarnings("unchecked")
    private void removeIteratorVariables(FilterTransformerTreeTableNode<T, C> node) {
        for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
            removeIteratorVariables((FilterTransformerTreeTableNode<T, C>) en.nextElement());
        }

        if (node.getParent() instanceof FilterTransformerTreeTableNode) {
            FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(node.getElement().getType());
            if (plugin == null) {
                PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Could not find plugin of type: " + node.getElement().getType());
                return;
            }

            plugin.removeIteratorVariables(node.getElement(), (FilterTransformerTreeTableNode<T, C>) node.getParent());
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

        boolean append = (treeTableModel.getRoot().getChildCount() > 0 && PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Would you like to append the " + elementName + "s from the imported " + containerName + " into the existing " + containerName + "?"));

        /*
         * When appending, we merely add the elements from the filter/transformer being imported.
         * When not appending, we replace the entire filter/transformer with the one being imported.
         */
        if (append) {
            for (C element : properties.getElements()) {
                insertNode(element);
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

    private void validateElementRecursive(C element, StringBuilder builder, String selectedSequenceNumber) {
        validateElementRecursive(element, builder, selectedSequenceNumber, new LinkedList<String>());
    }

    @SuppressWarnings("unchecked")
    private void validateElementRecursive(C element, StringBuilder builder, String selectedSequenceNumber, Deque<String> indexVariables) {
        String validationMessage = validateElement(element, StringUtils.equals(element.getSequenceNumber(), selectedSequenceNumber), false);
        if (StringUtils.isNotBlank(validationMessage)) {
            builder.append("Error in connector \"").append(connector.getName()).append("\" at ").append(response ? "response " : "").append(getContainerName().toLowerCase()).append(' ').append(getElementName().toLowerCase()).append(' ').append(element.getSequenceNumber()).append(" (\"").append(element.getName()).append("\"):\n").append(validationMessage).append("\n\n");
        }

        if (element instanceof IteratorElement) {
            IteratorElement<C> iterator = (IteratorElement<C>) element;
            String indexVariable = iterator.getProperties().getIndexVariable();

            if (StringUtils.isNotBlank(indexVariable) && indexVariables.contains(indexVariable)) {
                builder.append("Error in connector \"").append(connector.getName()).append("\" at ").append(response ? "response " : "").append(getContainerName().toLowerCase()).append(' ').append(getElementName().toLowerCase()).append(' ').append(element.getSequenceNumber()).append(" (\"").append(element.getName()).append("\"):\nDuplicate Iterator index variable ").append(indexVariable).append(" found.\n\n");
            }

            indexVariables.push(indexVariable);
            for (C child : iterator.getProperties().getChildren()) {
                validateElementRecursive(child, builder, selectedSequenceNumber, indexVariables);
            }
            indexVariables.pop();
        }
    }

    public void doValidateElement() {
        int selectedRow = treeTable.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            String type = (String) treeTable.getValueAt(selectedRow, typeColumn);
            try {
                FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(type);
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
            FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(element.getType());
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
            saveData(selectedRow);

            MutableTreeTableNode targetParent = null;
            int targetIndex = -1;

            FilterTransformerTreeTableNode<T, C> node = getNodeAtRow(selectedRow);
            MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();
            int index = parent.getIndex(node);

            if (up && index > 0 || !up && index + 1 < parent.getChildCount()) {
                targetParent = parent;
                targetIndex = index + (up ? -1 : 1);
            } else if (parent.getParent() != null && PlatformUI.MIRTH_FRAME.alertOkCancel(PlatformUI.MIRTH_FRAME, "This will move the " + getElementName().toLowerCase() + " out of its parent Iterator. Are you sure you wish to continue?")) {
                targetParent = (MutableTreeTableNode) parent.getParent();
                targetIndex = targetParent.getIndex(parent) + (up ? 0 : 1);
            }

            if (targetParent != null) {
                updating.set(true);
                try {
                    // Remove iterator variables before detaching
                    removeIteratorVariables(node);
                    treeTableModel.removeNodeFromParent(node);

                    // Replace iterator variables after reattaching
                    treeTableModel.insertNodeInto(node, targetParent, targetIndex);
                    replaceIteratorVariables(node);

                    selectedRow = treeTable.getRowForPath(new TreePath(treeTableModel.getPathToRoot(node)));
                    treeTable.setRowSelectionInterval(selectedRow, selectedRow);
                    loadData(selectedRow);
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

        int columnIndex = 0;
        List<String> columnNames = new ArrayList<String>();
        columnNames.add("#");
        numColumn = columnIndex++;
        columnNames.add("Name");
        nameColumn = columnIndex++;
        columnNames.add("Type");
        typeColumn = columnIndex++;

        final TableCellRenderer numCellRenderer = new LeftCellRenderer();
        final TableCellEditor nameCellEditor = new NameEditor();

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

        final NameRenderer nameRenderer = new NameRenderer();
        treeTable.setTreeCellRenderer(nameRenderer);

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

        if (useOperatorColumn()) {
            treeTable.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent evt) {
                    // Get the path to the node at the mouse event point
                    TreePath path = treeTable.getPathForLocation(evt.getX(), evt.getY());
                    if (path != null) {
                        // Make sure the operator at the current node is not null
                        if (((OperatorNamePair) treeTableModel.getValueAt(path.getLastPathComponent(), nameColumn)).getOperator() != null) {
                            Point point = evt.getPoint();

                            // Get the rectangle the cell renderer is drawn within, with respect to the path bounds
                            Rectangle cellRect = treeTable.getCellRect(treeTable.getRowForPath(path), nameColumn, true);
                            if (cellRect != null) {
                                point.translate(-cellRect.x, -cellRect.y);
                            }

                            // Get the rectangle the cell will be drawn into, with respect to the tree table
                            Rectangle pathBounds = ((JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn())).getPathBounds(path);
                            if (pathBounds != null) {
                                point.translate(-pathBounds.x, 0);
                            }

                            // Get the location of the operator button with respect to the cell renderer
                            Point loc = nameRenderer.getOperatorButton().getLocation();

                            // If the mouse event point lies within the bounds of the operator button, change the cursor
                            if (point.x >= loc.x && point.x < loc.x + UIConstants.ICON_AND.getIconWidth() && point.y >= loc.y && point.y < loc.y + UIConstants.ICON_AND.getIconHeight()) {
                                treeTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                return;
                            }
                        }
                    }

                    // Set the cursor back to the default if necessary
                    if (treeTable.getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                        treeTable.setCursor(Cursor.getDefaultCursor());
                    }
                }
            });
        }

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

        templatePanel = new TabbedTemplatePanel(this);
        templatePanel.setBorder(BorderFactory.createEmptyBorder());

        ActionListener nameActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!updating.get()) {
                    int selectedRow = treeTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        TreeTableNode node = getNodeAtRow(selectedRow);
                        OperatorNamePair pair = (OperatorNamePair) treeTableModel.getValueAt(node, nameColumn);
                        treeTableModel.setValueAt(new OperatorNamePair(pair.getOperator(), evt.getActionCommand()), node, nameColumn);
                    }
                }
            }
        };
        for (FilterTransformerTypePlugin<T, C> plugin : getPlugins().values()) {
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

        editorTasks.add(initActionCallback("doAssignToIterator", "Add the selected " + elementName + " to a new or existing Iterator.", ActionFactory.createBoundAction("doAssignToIterator", "Assign To Iterator", null), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem assignToIteratorItem = new JMenuItem("Assign To Iterator");
        assignToIteratorItem.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        assignToIteratorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doAssignToIterator();
            }
        });
        editorPopupMenu.add(assignToIteratorItem);

        editorTasks.add(initActionCallback("doRemoveFromIterator", "Remove the selected " + elementName + " from its current Iterator.", ActionFactory.createBoundAction("doRemoveFromIterator", "Remove From Iterator", null), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem removeFromIteratorItem = new JMenuItem("Remove From Iterator");
        removeFromIteratorItem.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        removeFromIteratorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                doRemoveFromIterator();
            }
        });
        editorPopupMenu.add(removeFromIteratorItem);

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
                FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(type);
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
                    treeTableModel.setValueAt(new OperatorNamePair((Operator) getOperator(element), element.getName()), node, nameColumn);
                } else {
                    treeTableModel.setValueAt(new OperatorNamePair(element.getName()), node, nameColumn);
                }
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
        /* @formatter:off
         * 
         * TASK_ADD = 0
         * TASK_DELETE = 1
         * TASK_ASSIGN_TO_ITERATOR = 2
         * TASK_REMOVE_FROM_ITERATOR = 3
         * TASK_IMPORT = 4
         * TASK_EXPORT = 5
         * TASK_VALIDATE = 6
         * TASK_VALIDATE_ELEMENT = 7
         * TASK_MOVE_UP = 8
         * TASK_MOVE_DOWN = 9
         * 
         * @formatter:on
         */
        int rowCount = treeTable.getRowCount();
        int selectedRow = treeTable.getSelectedRow();

        if (rowCount <= 0) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_DELETE, -1, false);
        } else if (rowCount == 1) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_REMOVE_FROM_ITERATOR, TASK_REMOVE_FROM_ITERATOR, false);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
        } else {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_REMOVE_FROM_ITERATOR, TASK_REMOVE_FROM_ITERATOR, false);

            TreePath path = treeTable.getPathForRow(selectedRow);
            if (path != null) {
                TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
                TreeTableNode parent = node.getParent();

                if (parent.getParent() == null) {
                    int childIndex = parent.getIndex(node);

                    if (childIndex == 0) {
                        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, TASK_MOVE_UP, false);
                    }
                    if (childIndex == parent.getChildCount() - 1) {
                        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_DOWN, TASK_MOVE_DOWN, false);
                    }
                } else {
                    PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_REMOVE_FROM_ITERATOR, TASK_REMOVE_FROM_ITERATOR, true);
                }
            } else {
                PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
            }
        }

        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_IMPORT, TASK_EXPORT, true);
        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_VALIDATE, TASK_VALIDATE, true);
        PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_VALIDATE_ELEMENT, TASK_VALIDATE_ELEMENT, selectedRow >= 0);
        if (selectedRow < 0) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_DELETE, TASK_REMOVE_FROM_ITERATOR, false);
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
                    if (StringUtils.equalsIgnoreCase(previousType, IteratorProperties.PLUGIN_POINT) && node.getChildCount() > 0) {
                        PlatformUI.MIRTH_FRAME.alertWarning(PlatformUI.MIRTH_FRAME, "Please remove all children before changing an Iterator to a different type.");
                        return;
                    }

                    FilterTransformerTypePlugin<T, C> plugin = getPlugins().get(previousType);
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
                    treeTableModel.setValueAt(new OperatorNamePair(newElement.getName()), node, nameColumn);
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

    private class NameRenderer extends JPanel implements TreeCellRenderer {

        private JLabel bulletLabel;
        private OperatorButton operatorButton;
        private JLabel nameLabel;

        public NameRenderer() {
            setOpaque(false);
            setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0"));

            bulletLabel = new JLabel();
            add(bulletLabel);

            if (useOperatorColumn()) {
                operatorButton = new OperatorButton();
                add(operatorButton, "h 17!, w 21!, aligny top, gaptop 1, gapafter 4");
            }

            nameLabel = new JLabel();
            add(nameLabel, "growx, pushx");
        }

        public OperatorButton getOperatorButton() {
            return operatorButton;
        }

        @Override
        public Dimension getPreferredSize() {
            // Return more width than needed to allow renderer to scroll off regardless of column width
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width + Toolkit.getDefaultToolkit().getScreenSize().width, size.height);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (row >= 0 && value instanceof FilterTransformerTreeTableNode) {
                FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) value;

                if (node.getElement() instanceof IteratorElement) {
                    bulletLabel.setIcon(UIConstants.ICON_BULLET_YELLOW);
                } else {
                    bulletLabel.setIcon(UIConstants.ICON_BULLET_GREEN);
                }

                nameLabel.setText(node.getElement().getName());

                if (selected) {
                    setBackground(treeTable.getSelectionBackground());
                } else if (row % 2 == 0) {
                    setBackground(UIConstants.HIGHLIGHTER_COLOR);
                } else {
                    setBackground(treeTable.getBackground());
                }
                nameLabel.setBackground(getBackground());

                if (useOperatorColumn()) {
                    Operator operator = (Operator) getOperator(node.getElement());
                    operatorButton.setIcon(operator != null ? operator == Operator.AND ? UIConstants.ICON_AND : UIConstants.ICON_OR : null);
                    operatorButton.setBackground(getBackground());
                }
            }

            return this;
        }
    }

    private class NameEditor extends AbstractCellEditor implements TableCellEditor {

        private JPanel panel;
        private JLabel bulletLabel;
        private OperatorButton operatorButton;
        private JLabel nameLabel;
        private JTextField nameField;
        private boolean textFieldClicked = false;
        private int offset = 0;

        public NameEditor() {
            panel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0")) {
                @Override
                public void setBounds(int x, int y, int width, int height) {
                    int newOffset = offset - getInsets().left + 1;
                    super.setBounds(x + newOffset, y, width - newOffset, height);
                }
            };
            panel.setBorder(BorderFactory.createEmptyBorder());

            bulletLabel = new JLabel();
            panel.add(bulletLabel, "aligny top, gaptop 2");

            if (useOperatorColumn()) {
                operatorButton = new OperatorButton();
                operatorButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        Icon icon = operatorButton.getIcon() == UIConstants.ICON_AND ? UIConstants.ICON_OR : UIConstants.ICON_AND;
                        operatorButton.setIcon(icon);
                    }
                });
                panel.add(operatorButton, "h 17!, w 21!, aligny top, gaptop 1, gapafter 4");
            }

            nameLabel = new JLabel();
            nameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (treeTable.isEditing() && treeTable.getSelectedRow() >= 0 && evt.getClickCount() >= 2 && allowNameEdit(treeTable.getSelectedRow(), nameColumn)) {
                        textFieldClicked = true;
                        nameLabel.setVisible(false);
                        nameField.setVisible(true);
                    }
                }
            });
            panel.add(nameLabel, "growx, pushx");

            nameField = new JTextField();
            panel.add(nameField, "growx, pushx");
        }

        @Override
        public Object getCellEditorValue() {
            if (useOperatorColumn()) {
                Operator operator = operatorButton.getIcon() != null ? operatorButton.getIcon() == UIConstants.ICON_AND ? Operator.AND : Operator.OR : null;
                return new OperatorNamePair(operator, nameField.getText());
            }
            return new OperatorNamePair(nameField.getText());
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            textFieldClicked = false;

            if (evt instanceof MouseEvent) {
                MouseEvent mouseEvt = (MouseEvent) evt;
                Point point = mouseEvt.getPoint();

                // Get the path to the node at the mouse event point
                TreePath path = treeTable.getPathForLocation(mouseEvt.getX(), mouseEvt.getY());

                if (path != null) {
                    // Make sure the operator at the current node is not null
                    if (useOperatorColumn() && ((OperatorNamePair) treeTableModel.getValueAt(path.getLastPathComponent(), nameColumn)).getOperator() != null) {
                        // Get the rectangle the cell editor is drawn within, with respect to the path bounds
                        Rectangle cellRect = treeTable.getCellRect(treeTable.getRowForPath(path), nameColumn, true);
                        if (cellRect != null) {
                            point.translate(-cellRect.x, -cellRect.y);
                        }

                        // Get the rectangle the cell will be drawn into, with respect to the tree table
                        Rectangle pathBounds = ((JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn())).getPathBounds(path);
                        if (pathBounds != null) {
                            point.translate(-pathBounds.x, 0);
                        }

                        Point loc = operatorButton.getLocation();
                        if (loc.x == 0) {
                            loc.translate(UIConstants.ICON_BULLET_GREEN.getIconWidth(), 0);
                        }

                        if (point.x >= loc.x && point.x < loc.x + UIConstants.ICON_AND.getIconWidth() && point.y >= loc.y && point.y < loc.y + UIConstants.ICON_AND.getIconHeight()) {
                            return true;
                        }
                    }

                    if (mouseEvt.getClickCount() >= 2) {
                        // Get the position of the name label with respect to the editor
                        Point namePoint = nameLabel.getLocation();
                        if (namePoint.x == 0) {
                            namePoint.translate(UIConstants.ICON_BULLET_GREEN.getIconWidth(), 0);
                        }

                        // Enable the text field if the point is within the name label bounds and the event is a double-click
                        if (point.x >= namePoint.x) {
                            textFieldClicked = true;
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (row >= 0) {
                TreePath path = treeTable.getPathForRow(row);
                FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) path.getLastPathComponent();

                if (node.getElement() instanceof IteratorElement) {
                    bulletLabel.setIcon(UIConstants.ICON_BULLET_YELLOW);
                } else {
                    bulletLabel.setIcon(UIConstants.ICON_BULLET_GREEN);
                }

                nameLabel.setText(node.getElement().getName());
                nameField.setText(node.getElement().getName());

                panel.setBackground(treeTable.getSelectionBackground());
                nameLabel.setBackground(panel.getBackground());

                boolean allowNameEdit = textFieldClicked && allowNameEdit(row, column);
                nameLabel.setVisible(!allowNameEdit);
                nameField.setVisible(allowNameEdit);

                if (useOperatorColumn()) {
                    if (allowOperatorEdit(row, column)) {
                        Operator operator = (Operator) getOperator(node.getElement());
                        operatorButton.setIcon(operator != null ? operator == Operator.AND ? UIConstants.ICON_AND : UIConstants.ICON_OR : null);
                        operatorButton.setEnabled(true);
                    } else {
                        operatorButton.setIcon(null);
                        operatorButton.setEnabled(false);
                    }
                    operatorButton.setBackground(panel.getBackground());
                    operatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                // Calculate the offset to use for resizing the editor once it gets made visible
                Rectangle cellRect = treeTable.getCellRect(row, column, true);
                Rectangle pathBounds = ((JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn())).getPathBounds(path);
                offset = cellRect.x + pathBounds.x - bulletLabel.getIcon().getIconWidth() - UIConstants.ICON_AND.getIconWidth();
            }

            return panel;
        }
    }

    private class OperatorButton extends JButton {

        public OperatorButton() {
            setBorderPainted(false);
            setContentAreaFilled(false);
            setMargin(new Insets(0, 0, 0, 0));
            setMaximumSize(new Dimension(21, 17));
            setMinimumSize(new Dimension(21, 17));
            setPreferredSize(new Dimension(21, 17));
        }
    }

    protected static class OperatorNamePair extends MutablePair<Operator, String> {

        public OperatorNamePair(String name) {
            this(null, name);
        }

        public OperatorNamePair(Operator operator, String name) {
            super(operator, name);
        }

        public Operator getOperator() {
            return getLeft();
        }

        public String getName() {
            return getRight();
        }
    }

    private JSplitPane horizontalSplitPane;

    private JSplitPane verticalSplitPane;
    protected MirthTreeTable treeTable;
    private JScrollPane treeTableScrollPane;
    protected DefaultTreeTableModel treeTableModel;
    private JTabbedPane tabPane;
    private JPanel propertiesContainer;
    private JScrollPane propertiesScrollPane;
    private MirthRTextScrollPane generatedScriptTextArea;

    public TabbedTemplatePanel templatePanel;
}