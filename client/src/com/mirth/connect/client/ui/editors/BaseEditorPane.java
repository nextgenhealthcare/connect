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
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.CenterCellRenderer;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.rsta.MirthRTextScrollPane;
import com.mirth.connect.client.ui.util.VariableListUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
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

    private int numColumn;
    private int operatorColumn = -1;
    private int nameColumn;
    private int typeColumn;
    private int dataColumn;
    private int columnCount = 4;

    private JXTaskPane viewTasks;
    private JXTaskPane editorTasks;
    private JPopupMenu editorPopupMenu;

    private Connector connector;
    private boolean response;
    private T originalProperties;
    private AtomicBoolean updating = new AtomicBoolean(false);

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
            if (!Objects.equals(originalProperties, properties)) {
                saveEnabled = true;
            }
            PlatformUI.MIRTH_FRAME.channelEditPanel.updateComponentShown();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(saveEnabled);
        }
    }

    protected abstract void doAccept(Connector connector, T properties, boolean response);

    public boolean isModified() {
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

            if (connector.getMode() == Connector.Mode.SOURCE) {
                templatePanel.setSourceView();
            } else if (connector.getMode() == Connector.Mode.DESTINATION) {
                templatePanel.setDestinationView(response);
            }
            templatePanel.setDefaultComponent();

            doSetProperties(connector, properties, response);

            PlatformUI.MIRTH_FRAME.setFocus(new JXTaskPane[] { viewTasks,
                    editorTasks }, false, true);
            PlatformUI.MIRTH_FRAME.setSaveEnabled(saveEnabled);

            updateTemplateVariables();
            updateTaskPane();
        } finally {
            updating.set(false);
        }

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    protected abstract void doSetProperties(Connector connector, T properties, boolean response);

    public List<C> getElements() {
        List<C> elements = new ArrayList<C>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            elements.add(getElementAt(row));
        }
        return elements;
    }

    @SuppressWarnings("unchecked")
    public C getElementAt(int modelRow) {
        C element = (C) tableModel.getValueAt(modelRow, dataColumn);
        element.setSequenceNumber(Integer.valueOf((String) tableModel.getValueAt(modelRow, numColumn)));
        element.setName((String) tableModel.getValueAt(modelRow, nameColumn));
        if (useOperatorColumn()) {
            setOperator(element, tableModel.getValueAt(modelRow, operatorColumn));
        }
        return element;
    }

    public void setElements(List<C> elements) {
        Object[][] data = new Object[elements.size()][columnCount];
        int i = 0;
        for (C element : elements) {
            data[i][numColumn] = String.valueOf(i);
            if (useOperatorColumn()) {
                data[i][operatorColumn] = getOperator(element);
            }
            data[i][nameColumn] = element.getName();
            data[i][typeColumn] = element.getType();
            data[i][dataColumn] = element.clone();
            i++;
        }
        tableModel.refreshDataVector(data);
    }

    public void setElementAt(int modelRow, C element) {
        if (isValidModelRow(modelRow)) {
            tableModel.setValueAt(String.valueOf(element.getSequenceNumber()), modelRow, numColumn);
            if (useOperatorColumn()) {
                tableModel.setValueAt(getOperator(element), modelRow, operatorColumn);
            }
            tableModel.setValueAt(element.getName(), modelRow, nameColumn);
            tableModel.setValueAt(element.getType(), modelRow, typeColumn);
            tableModel.setValueAt(element.clone(), modelRow, dataColumn);
        }
    }

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
            int rowCount = table.getRowCount();
            int selectedRow = table.getSelectedRow();
            saveData(selectedRow);

            FilterTransformerTypePlugin<C> plugin = getPlugins().get(type);
            C element = plugin.newObject(variable, mapping);
            element.setSequenceNumber(rowCount);
            element.setName(name);
            addRow(element);

            table.setRowSelectionInterval(rowCount, rowCount);
            table.scrollRowToVisible(rowCount);
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
        }
    }

    private void addRow(C element) {
        List<Object> list = new ArrayList<Object>();
        list.add(String.valueOf(element.getSequenceNumber()));
        if (useOperatorColumn()) {
            list.add(getOperator(element));
        }
        list.add(element.getName());
        list.add(element.getType());
        list.add(element.clone());
        tableModel.addRow(list.toArray(new Object[list.size()]));
    }

    public void doDeleteElement() {
        stopTableEditing();
        int selectedRow = table.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            updating.set(true);
            try {
                tableModel.removeRow(modelRow);
            } finally {
                updating.set(false);
            }

            if (isValidViewRow(selectedRow)) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
            } else if (isValidViewRow(selectedRow - 1)) {
                table.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            } else {
                propertiesScrollPane.setViewportView(null);
            }

            updateTaskPane();
            updateGeneratedCode();
            updateTemplateVariables();
            updateStepNumbers();
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
            updateStepNumbers();
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

    private String validateAll() {
        int selectedRow = table.getSelectedRow();
        int modelRow = -1;
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);
            modelRow = table.convertRowIndexToModel(selectedRow);
        }

        String containerName = getContainerName().toLowerCase();
        String elementName = getElementName().toLowerCase();

        String errors = "";
        T properties = getProperties();
        int row = 0;
        for (C element : properties.getElements()) {
            String validationMessage = validateElement(element, row == modelRow, false);
            if (StringUtils.isNotBlank(validationMessage)) {
                errors += "Error in connector \"" + connector.getName() + "\" at " + (response ? "response " : "") + containerName + " " + elementName + " " + element.getSequenceNumber() + " (\"" + element.getName() + "\"):\n" + validationMessage + "\n\n";
            }
            row++;
        }

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

    public void doValidateElement() {
        int selectedRow = table.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            String type = (String) table.getValueAt(selectedRow, typeColumn);
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
        doMoveElement(-1);
    }

    public void doMoveElementDown() {
        doMoveElement(1);
    }

    private void doMoveElement(int delta) {
        int selectedRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(selectedRow);

        if (isValidModelRow(modelRow) && isValidModelRow(modelRow + delta)) {
            saveData(selectedRow);
            updating.set(true);
            try {
                tableModel.moveRow(modelRow, modelRow, modelRow + delta);
                selectedRow = table.convertRowIndexToView(modelRow + delta);
                table.setRowSelectionInterval(selectedRow, selectedRow);
                updateStepNumbers();
                updateTaskPane();
                updateTemplateVariables(selectedRow);
            } finally {
                updating.set(false);
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
        columnNames.add("Data");
        dataColumn = columnIndex++;

        table = new MirthTable();
        table.setBorder(BorderFactory.createEmptyBorder());
        tableModel = new RefreshTableModel(columnNames.toArray(new String[columnNames.size()]), 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == nameColumn) {
                    try {
                        return getPlugins().get(getValueAt(rowIndex, typeColumn)).isNameEditable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return columnIndex == operatorColumn || columnIndex == typeColumn;
            }
        };
        table.setModel(tableModel);

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setCustomEditorControls(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(UIConstants.ROW_HEIGHT);
        table.packTable(UIConstants.COL_MARGIN);
        table.setSortable(false);
        table.setOpaque(true);
        table.setRowSelectionAllowed(true);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            table.setHighlighters(highlighter);
        }

        List<String> types = new ArrayList<String>();
        for (String pluginPointName : getPlugins().keySet()) {
            types.add(pluginPointName);
        }
        String[] typeArray = types.toArray(new String[types.size()]);

        MirthComboBoxTableCellEditor typeEditor = new MirthComboBoxTableCellEditor(table, typeArray, 2, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        table.getColumnExt(numColumn).setMaxWidth(UIConstants.MAX_WIDTH);
        table.getColumnExt(numColumn).setPreferredWidth(30);
        table.getColumnExt(numColumn).setCellRenderer(new CenterCellRenderer());

        if (operatorColumn >= 0) {
            // TODO
        }

        table.getColumnExt(typeColumn).setMaxWidth(UIConstants.MAX_WIDTH);
        table.getColumnExt(typeColumn).setMinWidth(155);
        table.getColumnExt(typeColumn).setPreferredWidth(155);
        table.getColumnExt(typeColumn).setCellRenderer(new MirthComboBoxTableCellRenderer(typeArray));
        table.getColumnExt(typeColumn).setCellEditor(typeEditor);

        table.getColumnExt(dataColumn).setVisible(false);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                tableListSelectionChanged(evt);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                checkSelectionAndPopupMenu(evt);
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                    doDeleteElement();
                }
            }
        });

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

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

        propertiesScrollPane = new JScrollPane();
        propertiesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tabPane.addTab(getElementName(), propertiesScrollPane);

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
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        tableModel.setValueAt(evt.getActionCommand(), table.convertRowIndexToModel(selectedRow), nameColumn);
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

        new DropTarget(this, this);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));

        verticalSplitPane.setTopComponent(tableScrollPane);
        verticalSplitPane.setBottomComponent(tabPane);

        horizontalSplitPane.setLeftComponent(verticalSplitPane);
        horizontalSplitPane.setRightComponent(templatePanel);

        add(horizontalSplitPane, "grow");
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed.
     */
    private void checkSelectionAndPopupMenu(MouseEvent evt) {
        int row = table.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                table.setRowSelectionInterval(row, row);
            }
            editorPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void tableListSelectionChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting() && !updating.getAndSet(true)) {
            try {
                int selectedRow = table.getSelectedRow();
                int previousRow = selectedRow == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();

                if (previousRow != selectedRow) {
                    saveData(previousRow);
                }
                loadData(selectedRow);
                updateTaskPane();
                updateGeneratedCode();
            } finally {
                updating.set(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData(int viewRow) {
        if (isValidViewRow(viewRow)) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String type = (String) tableModel.getValueAt(modelRow, typeColumn);
            C element = (C) tableModel.getValueAt(modelRow, dataColumn);

            try {
                FilterTransformerTypePlugin<C> plugin = getPlugins().get(type);
                plugin.setProperties(connector.getMode(), response, element);
                propertiesScrollPane.setViewportView(plugin.getPanel());
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            }
        } else {
            propertiesScrollPane.setViewportView(null);
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
        saveData(table.getSelectedRow());
    }

    private void saveData(int viewRow) {
        stopTableEditing();
        if (isValidViewRow(viewRow)) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String type = (String) tableModel.getValueAt(modelRow, typeColumn);
            try {
                tableModel.setValueAt(getPlugins().get(type).getProperties(), modelRow, dataColumn);
                getElementAt(modelRow);
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(this, e);
            }
        }
    }

    private void stopTableEditing() {
        if (table.isEditing()) {
            table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).stopCellEditing();
        }
    }

    private boolean isValidViewRow(int viewRow) {
        return viewRow >= 0 && viewRow < table.getRowCount();
    }

    private boolean isValidModelRow(int modelRow) {
        return modelRow >= 0 && modelRow < tableModel.getRowCount();
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
        int rowCount = table.getRowCount();
        int selectedRow = table.getSelectedRow();

        if (rowCount <= 0) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_DELETE, -1, false);
        } else if (rowCount == 1) {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, -1, false);
        } else {
            PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_ADD, -1, true);

            if (selectedRow == 0) {
                PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_UP, TASK_MOVE_UP, false);
            } else if (selectedRow == rowCount - 1) {
                PlatformUI.MIRTH_FRAME.setVisibleTasks(editorTasks, editorPopupMenu, TASK_MOVE_DOWN, TASK_MOVE_DOWN, false);
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

    private void updateGeneratedCode() {
        int selectedRow = table.getSelectedRow();
        if (isValidViewRow(selectedRow)) {
            saveData(selectedRow);

            C element = getElementAt(table.convertRowIndexToModel(selectedRow));
            try {
                generatedScriptTextArea.setText(element.getScript(false));
            } catch (ScriptBuilderException e) {
                e.printStackTrace();
                generatedScriptTextArea.setText("");
            }
        } else {
            generatedScriptTextArea.setText("");
        }
    }

    private void typeComboBoxActionPerformed(ActionEvent evt) {
        int selectedRow = table.getSelectedRow();

        if (selectedRow >= 0 && !updating.getAndSet(true)) {
            try {
                int modelRow = table.convertRowIndexToModel(selectedRow);

                String selectedType = ((JComboBox<?>) evt.getSource()).getSelectedItem().toString();
                String previousType = (String) tableModel.getValueAt(modelRow, typeColumn);

                if (!StringUtils.equalsIgnoreCase(selectedType, previousType)) {
                    FilterTransformerTypePlugin<C> plugin = getPlugins().get(previousType);
                    C selectedElement = plugin.getProperties();

                    String containerName = getContainerName().toLowerCase();
                    String elementName = getElementName().toLowerCase();
                    if (!Objects.equals(selectedElement, plugin.getDefaults()) && !PlatformUI.MIRTH_FRAME.alertOption(PlatformUI.MIRTH_FRAME, "Are you sure you would like to change this " + containerName + " " + elementName + " and lose all of the current data?")) {
                        ((JComboBox<?>) evt.getSource()).getModel().setSelectedItem(previousType);
                        return;
                    }

                    plugin = getPlugins().get(selectedType);
                    C newElement = plugin.getDefaults();
                    tableModel.setValueAt("", modelRow, nameColumn);
                    tableModel.setValueAt(newElement, modelRow, dataColumn);
                    plugin.setProperties(connector.getMode(), response, newElement);
                    propertiesScrollPane.setViewportView(plugin.getPanel());

                    updateTaskPane();
                    updateGeneratedCode();
                }
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
            } finally {
                updating.set(false);
            }
        }
    }

    private void updateStepNumbers() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            tableModel.setValueAt(String.valueOf(row), row, numColumn);
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
            } else if (tr.isDataFlavorSupported(TreeTransferable.MAPPER_DATA_FLAVOR) || tr.isDataFlavorSupported(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

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

    private JSplitPane horizontalSplitPane;

    private JSplitPane verticalSplitPane;
    private MirthTable table;
    private JScrollPane tableScrollPane;
    private RefreshTableModel tableModel;
    private JTabbedPane tabPane;
    private JScrollPane propertiesScrollPane;
    private MirthRTextScrollPane generatedScriptTextArea;

    public TabbedTemplatePanel templatePanel;
}