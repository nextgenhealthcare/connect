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

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.XMLFileFilter;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
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

import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.BlankPanel;
import com.webreach.mirth.client.ui.editors.CardPanel;
import com.webreach.mirth.client.ui.editors.EditorConstants;
import com.webreach.mirth.client.ui.editors.HL7MessageBuilder;
import com.webreach.mirth.client.ui.editors.JavaScriptPanel;
import com.webreach.mirth.client.ui.editors.MapperPanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.MyComboBoxEditor;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import java.io.IOException;

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
    }
    
    /**
     * load( Transformer t ) now that the components have been initialized...
     */
    public void load(Connector c, Transformer t)
    {
        prevSelRow = -1;
        connector = c;
        transformer = t;
        
        if (parent.channelEditTasks.getContentPane().getComponent(0).isVisible())
            modified = true;
        
        tabPanel.BuildVarPanel();
        tabPanel.setDefaultComponent();
        tabPanel.setHL7Message(transformer.getTemplate());
        channel = PlatformUI.MIRTH_FRAME.channelEditPage.currentChannel;
        
        mapperPanel.setAddAsGlobal(channel);       
        if (hl7builderPanel != null)
        {
            hl7builderPanel.setAddAsGlobal(channel);
        }

        if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
        {
            hl7builderPanel = new HL7MessageBuilder(this);
            stepPanel.addCard(hl7builderPanel, HL7MESSAGE_TYPE);
            // we need to clear all the old data before we load the new
            makeTransformerTable(outboundComboBoxValues);
        }
        else
        {
            makeTransformerTable(inboundComboBoxValues);
        }
        
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
            if (hl7builderPanel != null)
                hl7builderPanel.setData(null);
        }
        
        transformerTaskPaneContainer.add(parent.getOtherPane());
        parent.setCurrentContentPage(this);
        parent.setCurrentTaskPaneContainer(transformerTaskPaneContainer);
        
        mapperPanel.update();
        jsPanel.update();
        if (hl7builderPanel != null)
        {
            hl7builderPanel.update();
        }
        updateStepNumbers();
        updateTaskPane();
       
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    public void initComponents()
    {
        
        // the available panels (cards)
        stepPanel = new CardPanel();
        blankPanel = new BlankPanel();
        mapperPanel = new MapperPanel(this);
        jsPanel = new JavaScriptPanel(this);
        
        // establish the cards to use in the Transformer
        stepPanel.addCard(blankPanel, BLANK_TYPE);
        stepPanel.addCard(mapperPanel, MAPPER_TYPE);
        
        stepPanel.addCard(jsPanel, JAVASCRIPT_TYPE);
        
        transformerTablePane = new JScrollPane();
        
        // make and place the task pane in the parent Frame
        transformerTaskPaneContainer = new JXTaskPaneContainer();
        
        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);
        viewTasks.add(initActionCallback("accept", ActionFactory
                .createBoundAction("accept", "Back to Channels", "B"),
                new ImageIcon(Frame.class
                .getResource("images/resultset_previous.png"))));
        parent.setNonFocusable(viewTasks);
        transformerTaskPaneContainer.add(viewTasks);
        
        transformerTasks = new JXTaskPane();
        transformerTasks.setTitle("Transformer Tasks");
        transformerTasks.setFocusable(false);
        
        transformerPopupMenu = new JPopupMenu();
        
        // add new step task
        transformerTasks.add(initActionCallback("addNewStep", ActionFactory
                .createBoundAction("addNewStep", "Add New Step", "N"),
                new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewStep = new JMenuItem("Add New Step");
        addNewStep.setIcon(new ImageIcon(Frame.class
                .getResource("images/add.png")));
        addNewStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addNewStep();
            }
        });
        transformerPopupMenu.add(addNewStep);
        
        // delete step task
        transformerTasks.add(initActionCallback("deleteStep", ActionFactory
                .createBoundAction("deleteStep", "Delete Step", "X"),
                new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteStep = new JMenuItem("Delete Step");
        deleteStep.setIcon(new ImageIcon(Frame.class
                .getResource("images/delete.png")));
        deleteStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteStep();
            }
        });
        transformerPopupMenu.add(deleteStep);
        
        transformerTasks.add(initActionCallback("doImport", ActionFactory
                .createBoundAction("doImport", "Import Transformer",
                "I"), new ImageIcon(Frame.class
                .getResource("images/import.png"))));
        JMenuItem importTransformer = new JMenuItem("Import Transformer");
        importTransformer.setIcon(new ImageIcon(Frame.class
                .getResource("images/import.png")));
        importTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImport();
            }
        });
        transformerPopupMenu.add(importTransformer);
        
        transformerTasks.add(initActionCallback("doExport", ActionFactory
                .createBoundAction("doExport", "Export Transformer",
                "I"), new ImageIcon(Frame.class
                .getResource("images/export.png"))));
        JMenuItem exportTransformer = new JMenuItem("Export Transformer");
        exportTransformer.setIcon(new ImageIcon(Frame.class
                .getResource("images/export.png")));
        exportTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        transformerPopupMenu.add(exportTransformer);
        
        // move step up task
        transformerTasks.add(initActionCallback("moveStepUp", ActionFactory
                .createBoundAction("moveStepUp", "Move Step Up", "U"),
                new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveStepUp = new JMenuItem("Move Step Up");
        moveStepUp.setIcon(new ImageIcon(Frame.class
                .getResource("images/arrow_up.png")));
        moveStepUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveStepUp();
            }
        });
        transformerPopupMenu.add(moveStepUp);
        
        // move step down task
        transformerTasks.add(initActionCallback("moveStepDown", ActionFactory
                .createBoundAction("moveStepDown", "Move Step Down",
                "D"), new ImageIcon(Frame.class
                .getResource("images/arrow_down.png"))));
        JMenuItem moveStepDown = new JMenuItem("Move Step Down");
        moveStepDown.setIcon(new ImageIcon(Frame.class
                .getResource("images/arrow_down.png")));
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
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        stepPanel.setBorder(BorderFactory.createEmptyBorder());
        
        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stepPanel,
                refPanel);
        hSplitPane.setContinuousLayout(true);
        hSplitPane
                .setDividerLocation(EditorConstants.TAB_PANEL_DIVIDER_LOCATION);
        
        vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                transformerTablePane, hSplitPane);
        vSplitPane.setContinuousLayout(true);
        vSplitPane.setDividerLocation(EditorConstants.TABLE_DIVIDER_LOCATION);
        
        this.setLayout(new BorderLayout());
        this.add(vSplitPane, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder());
        // END LAYOUT
        
    } // END initComponents()
    
    public void makeTransformerTable()
    {
        makeTransformerTable(defaultComboBoxValues);
    }
    
    public void makeTransformerTable(String[] comboBoxValues)
    {
        transformerTable = new JXTable();
        
        transformerTable.setModel(new DefaultTableModel(new String[] { "#",
        "Name", "Type", "Data" }, 0)
        { // Data column is hidden            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                boolean[] canEdit;
                
                if(!((String)transformerTableModel.getValueAt(rowIndex, STEP_TYPE_COL)).equals(JAVASCRIPT_TYPE))
                    canEdit = new boolean[] { false, false, true, true };
                else
                    canEdit = new boolean[] { false, true, true, true };
                     
                return canEdit[columnIndex];
            }
        });
        
        transformerTableModel = (DefaultTableModel) transformerTable.getModel();
        
        // Set the combobox editor on the type column, and add action listener
        MyComboBoxEditor comboBox = new MyComboBoxEditor(comboBoxValues);
        
        ((JXComboBox) comboBox.getComponent())
        .addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent evt)
            {
                if(evt.getStateChange() == evt.SELECTED)
                {
                    String type = evt.getItem().toString();
                    if(type.equalsIgnoreCase(JAVASCRIPT_TYPE))
                    {
                        mapperPanel.setData(null);
                        jsPanel.setData(null);
                    }
                    else if(type.equalsIgnoreCase(MAPPER_TYPE))
                    {
                        Map<Object, Object> data = mapperPanel.getData();
                        data.put("Variable", getUniqueName(true));
                        data.put("Mapping", "");
                        mapperPanel.setData(data);
                    }                
                    else if(type.equalsIgnoreCase(HL7MESSAGE_TYPE))
                    {
                        Map<Object, Object> data = mapperPanel.getData();
                        data.put("Variable", getUniqueName(true));
                        data.put("Mapping", "");
                        hl7builderPanel.setData(data);
                    }
                    stepPanel.showCard(type);
                }
            }
        });
        
        transformerTable.setSelectionMode(0); // only select one row at a time
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setMaxWidth(
                UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMaxWidth(
                UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMinWidth(120);
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setPreferredWidth(30);
        transformerTable.getColumnExt(STEP_TYPE_COL).setPreferredWidth(120);
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setCellRenderer(
                new CenterCellRenderer());
        transformerTable.getColumnExt(STEP_TYPE_COL).setCellEditor(comboBox);
        
        transformerTable.getColumnExt(STEP_DATA_COL).setVisible(false);
        transformerTable.getColumnExt(STEP_NUMBER_COL).setHeaderRenderer(
                PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        transformerTable.getColumnExt(STEP_TYPE_COL).setHeaderRenderer(
                PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        
        transformerTable.setRowHeight(UIConstants.ROW_HEIGHT);
        transformerTable.packTable(UIConstants.COL_MARGIN);
        transformerTable.setSortable(false);
        transformerTable.setOpaque(true);
        transformerTable.setRowSelectionAllowed(true);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean(
                "highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter
                    .addHighlighter(new AlternateRowHighlighter(
                    UIConstants.HIGHLIGHTER_COLOR,
                    UIConstants.BACKGROUND_COLOR,
                    UIConstants.TITLE_TEXT_COLOR));
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
        
        transformerTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!updating && !evt.getValueIsAdjusting())
                {
                    TransformerListSelected(evt);
                }
            }
        });
    }
    
    private void showTransformerPopupMenu(MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = transformerTable.rowAtPoint(new Point(evt.getX(), evt
                        .getY()));
                transformerTable.setRowSelectionInterval(row, row);
            }
            
            transformerPopupMenu.show(evt.getComponent(), evt.getX(), evt
                    .getY());
        }
    }
    
    // for the task pane
    public BoundAction initActionCallback(String callbackMethod,
            BoundAction boundAction, ImageIcon icon)
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
        
        if(row != prevSelRow)
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
            if(!dontCheckCurrentRow || dontCheckCurrentRow && i != getSelectedRow())
            {
                String temp = "";
                Map<Object, Object> data = (Map<Object, Object>) transformerTableModel
                    .getValueAt(i, STEP_DATA_COL);
            
                if (data != null)
                    temp = (String) data.get("Variable");

                if (var != null && curRow != i)
                    if (var.equalsIgnoreCase(temp))
                        unique = false;
            }
        }
        
        return unique;
    }
    
    // returns a unique default var name
    private String getUniqueName(boolean dontCheckCurrentRow)
    {
        String base = "newVar";
        int i = 0;
        
        while (true)
        {
            String var = base + i;
            if (isUnique(var, dontCheckCurrentRow))
                return var;
            i++;
        }
    }
    
    // sets the data from the previously used panel into the
    // previously selected Step object
    private void saveData(int row)
    {
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(),
                    transformerTable.getEditingColumn()).stopCellEditing();
        
        updating = true;
        
        if (isValid(row))
        {
            Map<Object, Object> data = new HashMap<Object, Object>();
            String type = (String) transformerTable.getValueAt(row,
                    STEP_TYPE_COL);
            
            if (type == MAPPER_TYPE)
            {
                data = mapperPanel.getData();
                String var = data.get("Variable").toString();
                
                // check for unique variable names if it is an INBOUND channel
                if(channel.getDirection() == Channel.Direction.INBOUND)
                {
                    if (var == null || var.equals("") || !isUnique(var, row, false))
                    {
                        invalidVar = true;
                        String msg = "";

                        transformerTable.setRowSelectionInterval(row, row);

                        if (var == null || var.equals(""))
                            msg = "The variable name cannot be blank.";
                        else
                            // var is not unique
                            msg = "'" + data.get("Variable") + "'"
                                    + " is not unique.";
                        msg += "\nPlease enter a new variable name.\n";

                        parent.alertWarning(msg);
                    }
                    else invalidVar = false;
                }
                else
                {
                    // check for empty variable names
                    if (var == null || var.equals(""))
                    {
                        invalidVar = true;
                        String msg = "";

                        transformerTable.setRowSelectionInterval(row, row);

                        if (var == null || var.equals(""))
                            msg = "The variable name cannot be blank.";

                        msg += "\nPlease enter a new variable name.\n";

                        parent.alertWarning(msg);
                    }
                    else invalidVar = false;                    
                }
                
                data = mapperPanel.getData();
                
                if(mapperPanel.addToGlobal.isSelected())
                    data.put("isGlobal", UIConstants.YES_OPTION);
                else
                    data.put("isGlobal", UIConstants.NO_OPTION);
            }
            else if (type == JAVASCRIPT_TYPE)
            {
                data = jsPanel.getData();
            }
            else if ((hl7builderPanel != null) && (type == HL7MESSAGE_TYPE))
            {
                data = hl7builderPanel.getData();
                String var = data.get("Variable").toString();
                
                // check for empty variable names
                if (var == null || var.equals(""))
                {
                    invalidVar = true;
                    String msg = "";

                    transformerTable.setRowSelectionInterval(row, row);

                    if (var == null || var.equals(""))
                        msg = "The variable name cannot be blank.";
                    
                    msg += "\nPlease enter a new variable name.\n";

                    parent.alertWarning(msg);
                }
                else invalidVar = false;
                
                data = hl7builderPanel.getData();
            }
            transformerTableModel.setValueAt(data, row, STEP_DATA_COL);
        }
        
        updating = false;
    }
    
    /**
     * loadData() loads the data object into the correct panel
     */
    private void loadData(int row)
    {
        if (isValid(row))
        {
            String type = (String) transformerTableModel.getValueAt(row,
                    STEP_TYPE_COL);
            Map<Object, Object> data = (Map<Object, Object>) transformerTableModel
                    .getValueAt(row, STEP_DATA_COL);

            setPanelData(type, data);

        }
    }
    
    private void setPanelData(String type, Map<Object, Object> data)
    {
        if (type.equalsIgnoreCase(MAPPER_TYPE))
        {
            if(data.get("isGlobal") == null || ((String)data.get("isGlobal")).equals(UIConstants.NO_OPTION))
                mapperPanel.addToGlobal.setSelected(false);
            else
                mapperPanel.addToGlobal.setSelected(true);

            mapperPanel.setData(data);
        }
        else if (type.equalsIgnoreCase(JAVASCRIPT_TYPE))
            jsPanel.setData(data);
        else if ((hl7builderPanel != null) && (type.equalsIgnoreCase(HL7MESSAGE_TYPE)))
            hl7builderPanel.setData(data);
    }
    
    /**
     * prepData( int row ) works to move the data in a panel for moves or
     * deletes
     */
    private void prepData(int row)
    {
        Map<Object, Object> d = (Map<Object, Object>) transformerTableModel
                .getValueAt(row, STEP_DATA_COL);
        String type = (String) transformerTableModel.getValueAt(row,
                STEP_TYPE_COL);
        setPanelData(type, d);
    }
    
    private void setRowData(Step step, int row)
    {
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];
        
        tableData[STEP_NUMBER_COL] = step.getSequenceNumber();
        if(step.getType().equalsIgnoreCase(MAPPER_TYPE) || step.getType().equalsIgnoreCase(HL7MESSAGE_TYPE))
            tableData[STEP_NAME_COL] = (String)((Map<Object, Object>) step.getData()).get("Variable");
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
        
        if (!invalidVar)
        {
            int rowCount = transformerTable.getRowCount();
            modified = true;
            Step step = new Step();
            step.setSequenceNumber(rowCount);
            step.setScript("");


            Map<Object, Object> data = new HashMap<Object, Object>();
            data.put( "Mapping", "" );
            if (channel.getDirection().equals(Channel.Direction.INBOUND))
            {
                step.setName(getUniqueName(false));
                data.put("Variable", step.getName());
                step.setType(MAPPER_TYPE); // mapper type by default, inbound
                mapperPanel.setData(data);
            }
            else if (channel.getDirection().equals(Channel.Direction.OUTBOUND))
            {
                step.setName(getUniqueName(false));
                data.put("Variable", step.getName());
                step.setType(HL7MESSAGE_TYPE); // hl7 message type by default, outbound
                hl7builderPanel.setData(data);
            }
            step.setData(data);
            jsPanel.setData( null );  // for completeness

            setRowData(step, rowCount);
            prevSelRow = rowCount;
            updateStepNumbers();
        }
    }
    
    /**
     * void deleteStep(MouseEvent evt) delete all selected rows
     */
    public void deleteStep()
    {
        modified = true;
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(),
                    transformerTable.getEditingColumn()).stopCellEditing();
        
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
    
    public void doImport()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new XMLFileFilter());
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            importFile = importFileChooser.getSelectedFile();
            String transformerXML = "";
            
            try
            {
                transformerXML = FileUtil.read(importFile);
            }
            catch (IOException e)
            {
                parent.alertError("File could not be read.");
                return;
            }
            
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            try
            {
                Transformer importTransformer = (Transformer)serializer.fromXML(transformerXML);
                prevSelRow = -1;
                modified = true;
                connector.setTransformer(importTransformer);
                load(connector, importTransformer);
            }
            catch (Exception e)
            {
                parent.alertError("Invalid transformer file.");
            }
        }
    }
    
    public void doExport()
    {
        accept(false);
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileFilter(new XMLFileFilter());
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            String transformerXML = serializer.toXML(transformer);
            exportFile = exportFileChooser.getSelectedFile();
            
            int length = exportFile.getName().length();
            
            if (length < 4 || !exportFile.getName().substring(length-4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");
            
            if(exportFile.exists())
                if(!parent.alertOption("This file already exists.  Would you like to overwrite it?"))
                    return;
            
            try
            {
                FileUtil.write(exportFile, transformerXML);
                parent.alertInformation("Transformer was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                parent.alertError("File could not be written.");
            }
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
        
        if (!invalidVar)
        {
            List<Step> list = new ArrayList<Step>();
            for (int i = 0; i < transformerTable.getRowCount(); i++)
            {
                Step step = new Step();
                step.setSequenceNumber(Integer.parseInt(transformerTable
                        .getValueAt(i, STEP_NUMBER_COL).toString()));
                step.setName((String) transformerTableModel.getValueAt(i,
                        STEP_NAME_COL));
                step.setType((String) transformerTableModel.getValueAt(i,
                        STEP_TYPE_COL));
                step.setData((Map) transformerTableModel.getValueAt(i,
                        STEP_DATA_COL));
                
                HashMap map = (HashMap) step.getData();
                if (step.getType().equals(TransformerPane.MAPPER_TYPE))
                {
                    
                    StringBuilder script = new StringBuilder();
                    
                    if(map.get("isGlobal") != null && ((String)map.get("isGlobal")).equalsIgnoreCase(UIConstants.YES_OPTION))
                        script.append("globalMap.put(");
                    else
                        script.append("localMap.put(");

                    script.append("'" + map.get("Variable") + "', ");
                    script.append( map.get("Mapping") + ");");
                    step.setScript(script.toString());
                }
                else if (step.getType().equals(
                        TransformerPane.JAVASCRIPT_TYPE))
                {
                    step.setScript(map.get("Script").toString());
                }
                else if (step.getType().equals(
                        TransformerPane.HL7MESSAGE_TYPE))
                {
                    StringBuilder script = new StringBuilder();
                    
                    script.append(map.get("Variable") + ".text()[0]");
                    script.append(" = ");
                    script.append(map.get("Mapping") + ".text()[0];");
                    step.setScript(script.toString());
                }
                
                list.add(step);
            }
            
            transformer.setSteps(list);
            
            transformer.setTemplate(tabPanel.getHL7Message());
            
            // reset the task pane and content to channel edit page
            if(returning)
            {
                parent.channelEditPage.setDestinationVariableList();
                parent.taskPaneContainer.add(parent.getOtherPane());
                parent.setCurrentContentPage(parent.channelEditPage);
                parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
                parent.setPanelName("Edit Channel :: "
                        + parent.channelEditPage.currentChannel.getName());
                if (modified)
                    parent.enableSave();
                modified = false;
            }
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
            stepPanel.showCard(transformerTableModel.getValueAt(selRow,
                    STEP_TYPE_COL).toString());
        }
        else if (rowCount > 0)
        {
            transformerTable.setRowSelectionInterval(0, 0);
            loadData(0);
            stepPanel.showCard(transformerTableModel.getValueAt(0,
                    STEP_TYPE_COL).toString());
        }
        
        updateTaskPane();
        updating = false;
    }
    
    /**
     * updateTaskPane() configure the task pane so that it shows only relevant
     * tasks
     */
    private void updateTaskPane()
    {
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount <= 0)
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 1,
                    -1, false);
        else if (rowCount == 1)
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0,
                    -1, true);
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4,
                    -1, false);
        }
        else
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0,
                    -1, true);
            
            int selRow = transformerTable.getSelectedRow();
            if (selRow == 0) // hide move up
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu,
                        4, 4, false);
            else if (selRow == rowCount - 1) // hide move down
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu,
                        5, 5, false);
        }
        parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 2,3, true);
    }
    
    public int getSelectedRow()
    {
        return transformerTable.getSelectedRow();
    }
    
    public DefaultTableModel getTableModel()
    {
        return transformerTableModel;
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
    
    // some helper guys
    private int prevSelRow; // track the previously selected row
    
    private Connector connector;
    
    public boolean updating; // flow control
    
    private boolean invalidVar; // selection control
    
    // panels using CardLayout
    protected CardPanel stepPanel; // the card holder
    
    protected BlankPanel blankPanel; // the cards
    
    protected MapperPanel mapperPanel; // \/
    
    protected HL7MessageBuilder hl7builderPanel; // \/
    
    protected JavaScriptPanel jsPanel; // \/
    
    // transformer constants
    public static final int STEP_NUMBER_COL = 0;
    
    public static final int NUMBER_OF_COLUMNS = 4;
    
    private String[] defaultComboBoxValues = { MAPPER_TYPE, HL7MESSAGE_TYPE,
    JAVASCRIPT_TYPE };
    
    private String[] outboundComboBoxValues = { HL7MESSAGE_TYPE, MAPPER_TYPE,
    JAVASCRIPT_TYPE };
    
    private String[] inboundComboBoxValues = { MAPPER_TYPE, JAVASCRIPT_TYPE };
    
}
