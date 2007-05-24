/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.editors.transformer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.MirthFileFilter;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthComboBoxCellEditor;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.EditorTableCellEditor;
import com.webreach.mirth.client.ui.editors.JavaScriptPanel;
import com.webreach.mirth.client.ui.editors.MapperPanel;
import com.webreach.mirth.client.ui.editors.MessageBuilder;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.client.ui.util.VariableListUtil;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.util.ImportConverter;

public class TransformerPane extends MirthEditorPane
{

    /**
     * CONSTRUCTOR
     */
    public TransformerPane()
    {
        prevSelRow = -1;
        modified = false;
        initComponents();
        setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * load( Transformer t ) now that the components have been initialized...
     */
    public void load(Connector c, Transformer t, boolean channelHasBeenChanged)
    {
        prevSelRow = -1;
        connector = c;
        transformer = t;
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        
        makeTransformerTable();
        
        parent.setCurrentContentPage((JPanel) this);
        
        tabTemplatePanel.setDefaultComponent();
        tabTemplatePanel.tabPanel.add("Outgoing Data", tabTemplatePanel.outgoingTab);

        // add any existing steps to the model
        List<Step> list = transformer.getSteps();
        ListIterator<Step> li = list.listIterator();
        while (li.hasNext())
        {
            Step s = li.next();
            int row = s.getSequenceNumber();
            setRowData(s, row);
        }
        // select the first row if there is one
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount > 0)
        {
            transformerTable.setRowSelectionInterval(0, 0);
            prevSelRow = 0;
        }
        else
        {
            stepPanel.showCard(BLANK_TYPE);
            mapperPanel.setData(null);
            jsPanel.setData(null);
            builderPanel.setData(null);
            loadData(-1);
        }
        
        if (connector.getMode() == Connector.Mode.SOURCE)
        {
            tabTemplatePanel.setIncomingDataType(PlatformUI.MIRTH_FRAME.channelEditPanel.getSourceDatatype());
        }
        else if (connector.getMode() == Connector.Mode.DESTINATION)
        {
            if (channel.getSourceConnector().getTransformer().getOutboundProtocol() != null)
                tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.protocols.get(channel.getSourceConnector().getTransformer().getOutboundProtocol()));
            else
                tabTemplatePanel.setIncomingDataType(PlatformUI.MIRTH_FRAME.channelEditPanel.getSourceDatatype());
        }

        if (transformer.getOutboundProtocol() != null)
        {
            tabTemplatePanel.setOutgoingDataType(((String) PlatformUI.MIRTH_FRAME.protocols.get(transformer.getOutboundProtocol())));
        }
        else
        {
            tabTemplatePanel.setOutgoingDataType(tabTemplatePanel.getIncomingDataType());
        }
        
        tabTemplatePanel.setIncomingDataProperties(transformer.getInboundProperties());
        tabTemplatePanel.setOutgoingDataProperties(transformer.getOutboundProperties());
        
        tabTemplatePanel.setIncomingMessage(transformer.getInboundTemplate());
        tabTemplatePanel.setOutgoingMessage(transformer.getOutboundTemplate());

        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTaskPaneContainer.add(parent.getOtherPane());
        
        parent.setCurrentTaskPaneContainer(transformerTaskPaneContainer);

        updateStepNumbers();
        updateTaskPane();
        
        if (channelHasBeenChanged)
            modified = true;
        else
            modified = false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    public void initComponents()
    {

        // the available panels (cards)
        stepPanel = new BasePanel();
        blankPanel = new BasePanel();
        builderPanel = new MessageBuilder(this);
        mapperPanel = new MapperPanel(this);
        jsPanel = new JavaScriptPanel(this);

        // establish the cards to use in the Transformer
        stepPanel.addCard(blankPanel, BLANK_TYPE);
        stepPanel.addCard(mapperPanel, MAPPER_TYPE);

        stepPanel.addCard(jsPanel, JAVASCRIPT_TYPE);
        stepPanel.addCard(builderPanel, MESSAGE_TYPE);

        transformerTablePane = new JScrollPane();
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        // make and place the task pane in the parent Frame
        transformerTaskPaneContainer = new JXTaskPaneContainer();

        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);
        viewTasks.add(initActionCallback("accept", ActionFactory.createBoundAction("accept", "Back to Channels", "B"), new ImageIcon(Frame.class.getResource("images/resultset_previous.png"))));
        parent.setNonFocusable(viewTasks);
        transformerTaskPaneContainer.add(viewTasks);
        transformerTasks = new JXTaskPane();
        transformerTasks.setTitle("Transformer Tasks");
        transformerTasks.setFocusable(false);

        transformerPopupMenu = new JPopupMenu();

        // add new step task
        transformerTasks.add(initActionCallback("addNewStep", ActionFactory.createBoundAction("addNewStep", "Add New Step", "N"), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewStep = new JMenuItem("Add New Step");
        addNewStep.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        addNewStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addNewStep();
            }
        });
        transformerPopupMenu.add(addNewStep);

        // delete step task
        transformerTasks.add(initActionCallback("deleteStep", ActionFactory.createBoundAction("deleteStep", "Delete Step", "X"), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteStep = new JMenuItem("Delete Step");
        deleteStep.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        deleteStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteStep();
            }
        });
        transformerPopupMenu.add(deleteStep);

        transformerTasks.add(initActionCallback("doImport", ActionFactory.createBoundAction("doImport", "Import Transformer", "I"), new ImageIcon(Frame.class.getResource("images/import.png"))));
        JMenuItem importTransformer = new JMenuItem("Import Transformer");
        importTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/import.png")));
        importTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImport();
            }
        });
        transformerPopupMenu.add(importTransformer);

        transformerTasks.add(initActionCallback("doExport", ActionFactory.createBoundAction("doExport", "Export Transformer", "E"), new ImageIcon(Frame.class.getResource("images/export.png"))));
        JMenuItem exportTransformer = new JMenuItem("Export Transformer");
        exportTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/export.png")));
        exportTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        transformerPopupMenu.add(exportTransformer);

        transformerTasks.add(initActionCallback("doValidate", ActionFactory.createBoundAction("doValidate", "Validate JavaScript", "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem validateStep = new JMenuItem("Validate JavaScript");
        validateStep.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        validateStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doValidate();
            }
        });
        transformerPopupMenu.add(validateStep);

        // move step up task
        transformerTasks.add(initActionCallback("moveStepUp", ActionFactory.createBoundAction("moveStepUp", "Move Step Up", "P"), new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveStepUp = new JMenuItem("Move Step Up");
        moveStepUp.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_up.png")));
        moveStepUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveStepUp();
            }
        });
        transformerPopupMenu.add(moveStepUp);

        // move step down task
        transformerTasks.add(initActionCallback("moveStepDown", ActionFactory.createBoundAction("moveStepDown", "Move Step Down", "D"), new ImageIcon(Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveStepDown = new JMenuItem("Move Step Down");
        moveStepDown.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_down.png")));
        moveStepDown.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveStepDown();
            }
        });
        transformerPopupMenu.add(moveStepDown);

        // add the tasks to the taskpane, and the taskpane to the mirth client
        parent.setNonFocusable(transformerTasks);
        transformerTaskPaneContainer.add(transformerTasks);

        makeTransformerTable();

        // BGN LAYOUT
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setMinimumSize(new Dimension(0, 40));
        stepPanel.setBorder(BorderFactory.createEmptyBorder());

        hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transformerTablePane, stepPanel);
        hSplitPane.setContinuousLayout(true);
        //hSplitPane.setDividerSize(6);
        hSplitPane.setOneTouchExpandable(true);
        vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hSplitPane, refPanel);
        //vSplitPane.setDividerSize(6);
        vSplitPane.setOneTouchExpandable(true);
        vSplitPane.setContinuousLayout(true);
        resizePanes();

        hSplitPane.setBorder(BorderFactory.createEmptyBorder());
        vSplitPane.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new BorderLayout());
        this.add(vSplitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        // END LAYOUT

    } // END initComponents()

    public void makeTransformerTable()
    {
        transformerTable = new JXTable();
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTable.setModel(new DefaultTableModel(new String[] { "#", "Name", "Type", "Data" }, 0)
        { // Data column is hidden
                    public boolean isCellEditable(int rowIndex, int columnIndex)
                    {
                        boolean[] canEdit;

                        if (!((String) transformerTableModel.getValueAt(rowIndex, STEP_TYPE_COL)).equals(JAVASCRIPT_TYPE))
                            canEdit = new boolean[] { false, false, true, true };
                        else
                            canEdit = new boolean[] { false, true, true, true };

                        return canEdit[columnIndex];
                    }
                });

        transformerTableModel = (DefaultTableModel) transformerTable.getModel();

        transformerTable.getColumnModel().getColumn(STEP_NAME_COL).setCellEditor(new EditorTableCellEditor(this));

        // Set the combobox editor on the type column, and add action listener
        MirthComboBoxCellEditor comboBox = new MirthComboBoxCellEditor(defaultComboBoxValues, this);

        ((JXComboBox) comboBox.getComponent()).addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent evt)
            {
                if (evt.getStateChange() == evt.SELECTED)
                {
                    String type = evt.getItem().toString();
                    int row = getSelectedRow();

                    if (type.equalsIgnoreCase((String) transformerTable.getValueAt(row, STEP_TYPE_COL)))
                        return;

                    modified = true;

                    if (type.equalsIgnoreCase(JAVASCRIPT_TYPE))
                    {
                        jsPanel.setData(null);
                        updateTaskPane();
                        getTableModel().setValueAt("New Step", row, STEP_NAME_COL);
                    }
                    else if (type.equalsIgnoreCase(MAPPER_TYPE))
                    {
                        Map<Object, Object> data = mapperPanel.getData();
                        data.put("Variable", "");
                        data.put("Mapping", "");
                        data.put(UIConstants.IS_GLOBAL, UIConstants.IS_GLOBAL_CONNECTOR);
                        mapperPanel.setData(data);
                        updateTaskPane();
                        getTableModel().setValueAt("", row, STEP_NAME_COL);
                    }
                    else if (type.equalsIgnoreCase(MESSAGE_TYPE))
                    {
                        Map<Object, Object> data = builderPanel.getData();
                        data.put("Variable", "");
                        data.put("Mapping", "");
                        builderPanel.setData(data);
                        updateTaskPane();
                        getTableModel().setValueAt("", row, STEP_NAME_COL);
                    }
                    stepPanel.showCard(type);
                }
            }
        });

        transformerTable.setSelectionMode(0); // only select one row at a time

        transformerTable.getColumnExt(STEP_NUMBER_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMinWidth(120);

        transformerTable.getColumnExt(STEP_NUMBER_COL).setPreferredWidth(30);
        transformerTable.getColumnExt(STEP_TYPE_COL).setPreferredWidth(120);

        transformerTable.getColumnExt(STEP_NUMBER_COL).setCellRenderer(new CenterCellRenderer());
        transformerTable.getColumnExt(STEP_TYPE_COL).setCellEditor(comboBox);
        // transformerTable.getColumnExt(STEP_TYPE_COL).setCellRenderer(new
        // MyComboBoxRenderer(comboBoxValues));

        transformerTable.getColumnExt(STEP_DATA_COL).setVisible(false);
        transformerTable.getColumnExt(STEP_NUMBER_COL).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        transformerTable.getColumnExt(STEP_TYPE_COL).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);

        transformerTable.setRowHeight(UIConstants.ROW_HEIGHT);
        transformerTable.packTable(UIConstants.COL_MARGIN);
        transformerTable.setSortable(false);
        transformerTable.setOpaque(true);
        transformerTable.setRowSelectionAllowed(true);
        transformerTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            transformerTable.setHighlighters(highlighter);
        }

        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setViewportView(transformerTable);

        // listen for mouse clicks on the actual table
        transformerTable.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, true);
            }

            public void mouseReleased(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, true);
            }
        });

        // listen for mouse clicks on the empty part of the pane
        transformerTablePane.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, false);
            }

            public void mouseReleased(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, false);
            }
        });

        transformerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!updating && !evt.getValueIsAdjusting())
                {
                    TransformerListSelected(evt);
                }
            }
        });
        transformerTable.addKeyListener(new KeyListener()
        {

            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doSaveChannel();
                }
            }

            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void keyTyped(KeyEvent e)
            {
                // TODO Auto-generated method stub

            }

        });
    }

    private void showTransformerPopupMenu(MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = transformerTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                transformerTable.setRowSelectionInterval(row, row);
            }

            transformerPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    // for the task pane
    public BoundAction initActionCallback(String callbackMethod, BoundAction boundAction, ImageIcon icon)
    {

        if (icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }

    // called when a table row is (re)selected
    private void TransformerListSelected(ListSelectionEvent evt)
    {
        updating = true;

        int row = transformerTable.getSelectedRow();
        int last = evt.getLastIndex();

        if (row != prevSelRow)
        {
            saveData(prevSelRow);

            if (invalidVar)
            {
                row = prevSelRow;
                invalidVar = false;
            }

            if (isValid(row))
                loadData(row);
            else if (isValid(last))
            {
                loadData(last);
                row = last;
            }

            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            stepPanel.showCard(type);
            transformerTable.setRowSelectionInterval(row, row);

            prevSelRow = row;
            updateTaskPane();
        }

        updating = false;
    }

    // returns true if the row is a valid index in the existing model
    private boolean isValid(int row)
    {
        return (row >= 0 && row < transformerTableModel.getRowCount());
    }

    // returns true if the variable name is unique
    // if an integer is provided, don't check against
    // the var in that row
    public boolean isUnique(String var, boolean dontCheckCurrentRow)
    {
        return isUnique(var, -1, dontCheckCurrentRow);
    }

    public boolean isUnique(String var, int curRow, boolean dontCheckCurrentRow)
    {
        boolean unique = true;

        for (int i = 0; i < transformerTableModel.getRowCount(); i++)
        {
            if (!dontCheckCurrentRow || dontCheckCurrentRow && i != getSelectedRow())
            {
                String temp = "";
                Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(i, STEP_DATA_COL);

                if (data != null)
                    temp = (String) data.get("Variable");

                if (var != null && curRow != i)
                    if (var.equalsIgnoreCase(temp))
                        unique = false;
            }
        }

        return unique;
    }

    // sets the data from the previously used panel into the
    // previously selected Step object
    private void saveData(int row)
    {
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();

        //updating = true;

        if (isValid(row))
        {
            Map<Object, Object> data = new HashMap<Object, Object>();
            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);

            if (type.equals(MAPPER_TYPE))
            {
                data = mapperPanel.getData();
                String var = data.get("Variable").toString();

                if (var == null || var.equals("") || !isUnique(var, row, false) || var.indexOf(" ") != -1 || var.indexOf(".") != -1)
                {
                    invalidVar = true;
                    String msg = "";

                    transformerTable.setRowSelectionInterval(row, row);

                    if (var == null || var.equals(""))
                        msg = "The variable name cannot be blank.";
                    else if (var.indexOf(" ") != -1 || var.indexOf(".") != -1)
                        msg = "The variable name contains invalid characters.";
                    else
                        // var is not unique
                        msg = "'" + data.get("Variable") + "'" + " is not unique.";
                    msg += "\nPlease enter a new variable name.\n";

                    parent.alertWarning(msg);
                }
                else
                {
                    invalidVar = false;
                }
                data = mapperPanel.getData();

            }
            else if (type.equals(JAVASCRIPT_TYPE))
            {
                data = jsPanel.getData();
            }
            else if (type.equals(MESSAGE_TYPE))
            {
                data = builderPanel.getData();
                String var = data.get("Variable").toString();

                // check for empty variable names
                if (var == null || var.equals(""))
                {
                    invalidVar = true;
                    String msg = "";

                    transformerTable.setRowSelectionInterval(row, row);

                    if (var == null || var.equals(""))
                        msg = "The mapping field cannot be blank.";

                    msg += "\nPlease enter a new mapping field name.\n";

                    parent.alertWarning(msg);
                }
                else
                    invalidVar = false;

                data = builderPanel.getData();
            }
            transformerTableModel.setValueAt(data, row, STEP_DATA_COL);
        }

        //updating = false;
    }

    /**
     * loadData() loads the data object into the correct panel
     */
    private void loadData(int row)
    {
        if (isValid(row))
        {
            String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
            Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);

            setPanelData(type, data);
        }

        if (connector.getMode() == Connector.Mode.SOURCE)
            tabTemplatePanel.updateVariables(connector.getFilter().getRules(), buildStepList(new ArrayList<Step>(), row));
        else
            tabTemplatePanel.updateVariables(getGlobalRuleVariables(), buildStepList(getGlobalStepVariables(), row));
    }

    private void setPanelData(String type, Map<Object, Object> data)
    {
        if (type.equalsIgnoreCase(MAPPER_TYPE))
            mapperPanel.setData(data);
        else if (type.equalsIgnoreCase(JAVASCRIPT_TYPE))
            jsPanel.setData(data);
        else if (type.equalsIgnoreCase(MESSAGE_TYPE))
            builderPanel.setData(data);
    }

    /**
     * prepData( int row ) works to move the data in a panel for moves or
     * deletes
     */
    private void prepData(int row)
    {
        Map<Object, Object> d = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);
        String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
        setPanelData(type, d);
    }

    private void setRowData(Step step, int row)
    {
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];

        tableData[STEP_NUMBER_COL] = step.getSequenceNumber();
        if (step.getType().equalsIgnoreCase(MAPPER_TYPE) || step.getType().equalsIgnoreCase(MESSAGE_TYPE))
            tableData[STEP_NAME_COL] = (String) ((Map<Object, Object>) step.getData()).get("Variable");
        else
            tableData[STEP_NAME_COL] = step.getName();
        tableData[STEP_TYPE_COL] = step.getType();
        tableData[STEP_DATA_COL] = step.getData();

        updating = true;
        transformerTableModel.addRow(tableData);
        transformerTable.setRowSelectionInterval(row, row);
        updating = false;
    }

    /**
     * void addNewStep() add a new step to the end of the list
     */
    public void addNewStep()
    {
        saveData(transformerTable.getSelectedRow());

        if (!invalidVar || transformerTable.getRowCount() == 0)
        {
            int rowCount = transformerTable.getRowCount();
            modified = true;
            Step step = new Step();
            step.setSequenceNumber(rowCount);
            step.setScript("");

            Map<Object, Object> data = new HashMap<Object, Object>();
            data.put("Mapping", "");
            step.setName("");
            data.put("Variable", step.getName());

            if (tabTemplatePanel.tabPanel.getSelectedComponent() == tabTemplatePanel.outgoingTab)
            {
                step.setType(MESSAGE_TYPE); // hl7 message type by default,
                // outbound
                builderPanel.setData(data);
                mapperPanel.setData(null);
                jsPanel.setData(null);
            }
            else
            {
                step.setType(MAPPER_TYPE); // mapper type by default, inbound
                mapperPanel.setData(data);
                builderPanel.setData(null);
                jsPanel.setData(null);
            }

            step.setData(data);
            setRowData(step, rowCount);
            prevSelRow = rowCount;
            updateStepNumbers();
            transformerTable.setRowSelectionInterval(rowCount, rowCount);
            transformerTable.setVisibleRowCount(rowCount);
            transformerTablePane.getViewport().setViewPosition(new Point(0, transformerTable.getRowHeight() * rowCount));
        }
    }

    /**
     * void deleteStep(MouseEvent evt) delete all selected rows
     */
    public void deleteStep()
    {
        modified = true;
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();

        updating = true;

        int row = transformerTable.getSelectedRow();
        if (isValid(row + 1))
            prepData(row + 1);

        if (isValid(row))
            transformerTableModel.removeRow(row);

        updating = false;

        if (isValid(row))
            transformerTable.setRowSelectionInterval(row, row);
        else if (isValid(row - 1))
            transformerTable.setRowSelectionInterval(row - 1, row - 1);
        else
        {
            stepPanel.showCard(BLANK_TYPE);
            mapperPanel.setData(null);
            jsPanel.setData(null);
        }

        updateStepNumbers();
    }

    /*
     * Import the transfomer
     */
    public void doImport()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            importFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            Preferences.systemNodeForPackage(Mirth.class).put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            importFile = importFileChooser.getSelectedFile();
            String transformerXML = "";
           
            MessageObject.Protocol incomingProtocol = null, outgoingProtocol = null;
            
            for (MessageObject.Protocol protocol : MessageObject.Protocol.values())
            {
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getIncomingDataType()))
                {
                    incomingProtocol = protocol;
                }
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getOutgoingDataType()))
                {
                    outgoingProtocol = protocol;
                }
            }

            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            try
            {
                transformerXML = ImportConverter.convertTransformer(importFile, incomingProtocol, outgoingProtocol);
                Transformer importTransformer = (Transformer) serializer.fromXML(transformerXML);
                prevSelRow = -1;
                modified = true;
                connector.setTransformer(importTransformer);
                load(connector, importTransformer, modified);
            }
            catch (Exception e)
            {
                parent.alertError("Invalid transformer file.");
            }
        }
    }

    /*
     * Export the transfomer
     */
    public void doExport()
    {
        accept(false);
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            Preferences.systemNodeForPackage(Mirth.class).put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            String transformerXML = serializer.toXML(transformer);
            exportFile = exportFileChooser.getSelectedFile();

            int length = exportFile.getName().length();

            if (length < 4 || !exportFile.getName().substring(length - 4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");

            if (exportFile.exists())
                if (!parent.alertOption("This file already exists.  Would you like to overwrite it?"))
                    return;

            try
            {
                FileUtil.write(exportFile, transformerXML, false);
                parent.alertInformation("Transformer was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                parent.alertError("File could not be written.");
            }
        }
    }

    /*
     * Validate the current step if it has JavaScript
     */
    public void doValidate()
    {
        try
        {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + jsPanel.getJavaScript() + "}", null, 1, null);
            parent.alertInformation("JavaScript was successfully validated.");
        }
        catch (EvaluatorException e)
        {
            parent.alertInformation("Error on line " + e.lineNumber() + ": " + e.getMessage() + ".");
        }
        finally
        {
            Context.exit();
        }
    }

    /**
     * void moveStep( int i ) move the selected row i places
     */
    public void moveStepUp()
    {
        moveStep(-1);
    }

    public void moveStepDown()
    {
        moveStep(1);
    }

    public void moveStep(int i)
    {
        modified = true;
        int selRow = transformerTable.getSelectedRow();
        int moveTo = selRow + i;

        // we can't move past the first or last row
        if (isValid(moveTo))
        {
            saveData(selRow);
            loadData(moveTo);
            transformerTableModel.moveRow(selRow, selRow, moveTo);
            transformerTable.setRowSelectionInterval(moveTo, moveTo);
        }

        updateStepNumbers();
        parent.enableSave();
    }

    public List<Step> buildStepList(List<Step> list, int endingRow)
    {
        for (int i = 0; i < endingRow; i++)
        {
            Step step = new Step();
            step.setSequenceNumber(Integer.parseInt(transformerTable.getValueAt(i, STEP_NUMBER_COL).toString()));
            step.setName((String) transformerTableModel.getValueAt(i, STEP_NAME_COL));
            step.setType((String) transformerTableModel.getValueAt(i, STEP_TYPE_COL));
            step.setData((Map) transformerTableModel.getValueAt(i, STEP_DATA_COL));

            HashMap map = (HashMap) step.getData();
            if (step.getType().equals(TransformerPane.MAPPER_TYPE))
            {
                String regexArray = buildRegexArray(map);

                StringBuilder script = new StringBuilder();

                if (map.get(UIConstants.IS_GLOBAL) != null)
                    script.append((String)map.get(UIConstants.IS_GLOBAL) + "Map.put(");
                else
                    script.append(UIConstants.IS_GLOBAL_CONNECTOR + "Map.put(");

                // default values need to be provided
                // so we don't cause syntax errors in the JS
                script.append("'" + map.get("Variable") + "', ");
                String defaultValue = (String) map.get("DefaultValue");
                if (defaultValue.length() == 0)
                {
                    defaultValue = "''";
                }
                String mapping = (String) map.get("Mapping");
                if (mapping.length() == 0)
                {
                    mapping = "''";
                }
                script.append("validate(" + mapping + ", " + defaultValue + ", " + regexArray + "));");
                step.setScript(script.toString());
            }
            else if (step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
            {
                step.setScript(map.get("Script").toString());
            }
            else if (step.getType().equals(TransformerPane.MESSAGE_TYPE))
            {
                String regexArray = buildRegexArray(map);
                StringBuilder script = new StringBuilder();
                String variable = (String) map.get("Variable");
                String defaultValue = (String) map.get("DefaultValue");
                if (defaultValue.length() == 0)
                {
                    defaultValue = "''";
                }
                String mapping = (String) map.get("Mapping");
                if (mapping.length() == 0)
                {
                    mapping = "''";
                }
                script.append(variable);
                script.append(" = ");
                script.append("validate(" + mapping + ", " + defaultValue + ", " + regexArray + ");");
                step.setScript(script.toString());
            }

            list.add(step);
        }
        return list;
    }
    
    private List<Rule> getGlobalRuleVariables()
    {
        ArrayList<Rule> concatenatedRules = new ArrayList<Rule>();
        VariableListUtil.getRuleGlobalVariables(concatenatedRules, channel.getSourceConnector());
        
        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (connector == destination)
            {
                VariableListUtil.getRuleGlobalVariables(concatenatedRules, destination);
                seenCurrent = true;
            }
            else if (!seenCurrent)
            {
                VariableListUtil.getRuleGlobalVariables(concatenatedRules, destination);
            }
        }
        return concatenatedRules;
    }
    
    private List<Step> getGlobalStepVariables()
    {
        ArrayList<Step> concatenatedSteps = new ArrayList<Step>();
        VariableListUtil.getStepGlobalVariables(concatenatedSteps, channel.getSourceConnector());
        
        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (connector == destination)
            {
                seenCurrent = true;
            }
            else if (!seenCurrent)
            {
                VariableListUtil.getStepGlobalVariables(concatenatedSteps, destination);
            }
        }
        return concatenatedSteps;
    }

    private String buildRegexArray(HashMap map)
    {
        ArrayList<String[]> regexes = (ArrayList<String[]>) map.get("RegularExpressions");

        StringBuilder regexArray = new StringBuilder();

        regexArray.append("new Array(");

        if(regexes.size() > 0)
        {
            for(int i = 0; i < regexes.size(); i++)
            {
                regexArray.append("new Array(" + regexes.get(i)[0] + ", " + regexes.get(i)[1] + ")");
                if (i+1 == regexes.size())
                    regexArray.append(")");
                else
                    regexArray.append(",");
            }
        }
        else
        {
            regexArray.append(")");
        }

        return regexArray.toString();
    }

    /**
     * void accept(MouseEvent evt) returns a vector of vectors to the caller of
     * this.
     */
    public void accept()
    {
        accept(true);
    }

    public void accept(boolean returning)
    {
        saveData(transformerTable.getSelectedRow());

        if (!invalidVar || transformerTable.getRowCount() == 0)
        {
            List<Step> list = buildStepList(new ArrayList<Step>(), transformerTable.getRowCount());

            transformer.setSteps(list);

            for (MessageObject.Protocol protocol : MessageObject.Protocol.values())
            {
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getIncomingDataType()))
                {
                    transformer.setInboundProtocol(protocol);
                }
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getOutgoingDataType()))
                {
                    transformer.setOutboundProtocol(protocol);
                }
            }

            transformer.setInboundTemplate(tabTemplatePanel.getIncomingMessage());
            transformer.setOutboundTemplate(tabTemplatePanel.getOutgoingMessage());

            transformer.setInboundProperties(tabTemplatePanel.getIncomingDataProperties());
            transformer.setOutboundProperties(tabTemplatePanel.getOutgoingDataProperties());

            // reset the task pane and content to channel edit page
            if (returning)
            {
                parent.channelEditPanel.setDestinationVariableList();
                parent.taskPaneContainer.add(parent.getOtherPane());
                parent.setCurrentContentPage(parent.channelEditPanel);
                parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
                parent.setPanelName("Edit Channel - " + parent.channelEditPanel.currentChannel.getName());
                if (modified)
                    parent.enableSave();
                
                parent.channelEditPanel.updateComponentShown();                
                modified = false;
            }
            
            invalidVar = false;
        }
    }

    /**
     * void updateStepNumbers() traverses the table and updates all data
     * numbers, both in the model and the view, after any change to the table
     */
    private void updateStepNumbers()
    {
        updating = true;

        int rowCount = transformerTableModel.getRowCount();
        int selRow = transformerTable.getSelectedRow();

        for (int i = 0; i < rowCount; i++)
            transformerTableModel.setValueAt(i, i, STEP_NUMBER_COL);

        if (isValid(selRow))
        {
            transformerTable.setRowSelectionInterval(selRow, selRow);
            loadData(selRow);
            stepPanel.showCard(transformerTableModel.getValueAt(selRow, STEP_TYPE_COL).toString());
        }
        else if (rowCount > 0)
        {
            transformerTable.setRowSelectionInterval(0, 0);
            loadData(0);
            stepPanel.showCard(transformerTableModel.getValueAt(0, STEP_TYPE_COL).toString());
        }

        updateTaskPane();
        updating = false;
    }

    /**
     * updateTaskPane() configure the task pane so that it shows only relevant
     * tasks
     */
    public void updateTaskPane()
    {
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount <= 0)
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 1, -1, false);
        else if (rowCount == 1)
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, -1, false);
        }
        else
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);

            int selRow = transformerTable.getSelectedRow();
            if (selRow == 0) // hide move up
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 5, 5, false);
            else if (selRow == rowCount - 1) // hide move down
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 6, 6, false);
        }
        parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 2, 3, true);
        String type = null;
        int selectedRow = getSelectedRow();
        if (selectedRow > -1)
        {
            type = (String) transformerTableModel.getValueAt(getSelectedRow(), STEP_TYPE_COL);
        }
        if (type != null && type.equals(JAVASCRIPT_TYPE))
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, true);
        else
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, false);
    }

    public int getSelectedRow()
    {
        return transformerTable.getSelectedRow();
    }

    public DefaultTableModel getTableModel()
    {
        return transformerTableModel;
    }

    public void resizePanes()
    {
        hSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 3.5));
        vSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 2 + PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 5.5));
        tabTemplatePanel.resizePanes();
    }

    public void setHighlighters()
    {
        jsPanel.setHighlighters();
        mapperPanel.setHighlighters();
        builderPanel.setHighlighters();
    }

    public void unsetHighlighters()
    {
        jsPanel.unsetHighlighters();
        mapperPanel.unsetHighlighters();
        builderPanel.unsetHighlighters();
    }

    // ............................................................................\\

    // used to load the pane
    private Transformer transformer;
    // fields
    private JXTable transformerTable;
    private DefaultTableModel transformerTableModel;
    private JScrollPane transformerTablePane;
    private JSplitPane vSplitPane;
    private JSplitPane hSplitPane;
    private JXTaskPaneContainer transformerTaskPaneContainer;
    private JXTaskPane transformerTasks;
    private JPopupMenu transformerPopupMenu;
    private JXTaskPane viewTasks;
    private JXTaskPane otherTasks;
    private Channel channel;
    private Connector connector;
    public boolean updating; // flow control
    private boolean invalidVar; // selection control
    // panels using CardLayout
    protected BasePanel stepPanel; // the card holder
    protected BasePanel blankPanel;
    protected MapperPanel mapperPanel;
    protected MessageBuilder builderPanel;
    protected JavaScriptPanel jsPanel;
    public static final int NUMBER_OF_COLUMNS = 4;
    private String[] defaultComboBoxValues = { MAPPER_TYPE, MESSAGE_TYPE, JAVASCRIPT_TYPE };
    private String[] transformerComboBoxValues = { MESSAGE_TYPE, MAPPER_TYPE, JAVASCRIPT_TYPE };

}
