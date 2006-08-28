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


package com.webreach.mirth.client.ui.editors.filter;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import com.webreach.mirth.client.ui.XMLFileFilter;
import com.webreach.mirth.client.ui.editors.BlankPanel;
import com.webreach.mirth.client.ui.editors.CardPanel;
import com.webreach.mirth.client.ui.editors.EditorConstants;
import com.webreach.mirth.client.ui.editors.JavaScriptPanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.MyComboBoxEditor;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;



public class FilterPane extends MirthEditorPane
{
    
    /** CONSTRUCTOR
     */
    public FilterPane()
    {
        prevSelRow = -1;
        modified = false;
        initComponents();
    }
    
    /** load( Filter f )
     */
    public void load(Connector c, Filter f)
    {
        prevSelRow = -1;
        filter = f;
        connector = c;
        
        if (parent.channelEditTasks.getContentPane().getComponent(0).isVisible())
            modified = true;
        else
            modified = false;
        
        // we need to clear all the old data before we load the new
        makeFilterTable();
        
        // add any existing steps to the model
        List<Rule> list = filter.getRules();
        ListIterator<Rule> li = list.listIterator();
        while ( li.hasNext() )
        {
            Rule s = li.next();
            int row = s.getSequenceNumber();
            setRowData( s, row );
        }
        
        tabPanel.setHL7Message( filter.getTemplate() );
        tabPanel.setDefaultComponent();
        
        int rowCount = filterTableModel.getRowCount();
        // select the first row if there is one
        if ( rowCount > 0 )
        {
            filterTable.setRowSelectionInterval( 0, 0 );
            prevSelRow = 0;
        }
        else
        {
            rulePanel.showCard( BLANK_TYPE );
            jsPanel.setData( null );
        }
        
        filterTaskPaneContainer.add(parent.getOtherPane());
        parent.setCurrentContentPage( this );
        parent.setCurrentTaskPaneContainer( filterTaskPaneContainer );
        
        jsPanel.update();
        updateRuleNumbers();
        updateTaskPane();
    }
    
    /** This method is called from within the constructor to
     *  initialize the form.
     */
    public void initComponents()
    {
        
        // the available panels (cards)
        rulePanel = new CardPanel();
        blankPanel = new BlankPanel();
        jsPanel = new JavaScriptPanel( this );
        // 		establish the cards to use in the Filter
        rulePanel.addCard( blankPanel, BLANK_TYPE );
        rulePanel.addCard( jsPanel, JAVASCRIPT_TYPE );
        
        filterTablePane = new JScrollPane();
        
        // make and place the task pane in the parent Frame
        filterTaskPaneContainer = new JXTaskPaneContainer();
        
        viewTasks = new JXTaskPane();
        viewTasks.setTitle( "Mirth Views" );
        viewTasks.setFocusable( false );
        
        filterPopupMenu = new JPopupMenu();
        
        viewTasks.add(initActionCallback( "accept",
                ActionFactory.createBoundAction( "accept", "Back to Channels", "B" ),
                new ImageIcon( Frame.class.getResource( "images/resultset_previous.png" )) ));
        parent.setNonFocusable( viewTasks );
        filterTaskPaneContainer.add( viewTasks );
        
        filterTasks = new JXTaskPane();
        filterTasks.setTitle( "Filter Tasks" );
        filterTasks.setFocusable( false );
        
        // add new rule task
        filterTasks.add( initActionCallback( "addNewRule",
                ActionFactory.createBoundAction( "addNewRule", "Add New Rule", "N" ),
                new ImageIcon( Frame.class.getResource( "images/add.png" )) ));
        JMenuItem addNewRule = new JMenuItem("Add New Rule");
        addNewRule.setIcon( new ImageIcon( Frame.class.getResource( "images/add.png" )) );
        addNewRule.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addNewRule();
            }
        });
        filterPopupMenu.add(addNewRule);
        
        // delete rule task
        filterTasks.add( initActionCallback( "deleteRule",
                ActionFactory.createBoundAction( "deleteRule", "Delete Rule", "X" ),
                new ImageIcon( Frame.class.getResource( "images/delete.png" )) ));
        JMenuItem deleteRule = new JMenuItem("Delete Rule");
        deleteRule.setIcon( new ImageIcon( Frame.class.getResource( "images/delete.png" )) );
        deleteRule.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteRule();
            }
        });
        filterPopupMenu.add(deleteRule);
        
        filterTasks.add(initActionCallback("doImport", ActionFactory
                .createBoundAction("doImport", "Import Filter",
                "I"), new ImageIcon(Frame.class
                .getResource("images/import.png"))));
        JMenuItem importFilter = new JMenuItem("Import Filter");
        importFilter.setIcon(new ImageIcon(Frame.class
                .getResource("images/import.png")));
        importFilter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImport();
            }
        });
        filterPopupMenu.add(importFilter);
        
        filterTasks.add(initActionCallback("doExport", ActionFactory
                .createBoundAction("doExport", "Export Filter",
                "I"), new ImageIcon(Frame.class
                .getResource("images/export.png"))));
        JMenuItem exportFilter = new JMenuItem("Export Filter");
        exportFilter.setIcon(new ImageIcon(Frame.class
                .getResource("images/export.png")));
        exportFilter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        filterPopupMenu.add(exportFilter);
        
        // move rule up task
        filterTasks.add( initActionCallback( "moveRuleUp",
                ActionFactory.createBoundAction( "moveRuleUp", "Move Rule Up", "U" ),
                new ImageIcon( Frame.class.getResource( "images/arrow_up.png" )) ));
        JMenuItem moveRuleUp = new JMenuItem("Move Rule Up");
        moveRuleUp.setIcon( new ImageIcon( Frame.class.getResource( "images/arrow_up.png" )) );
        moveRuleUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveRuleUp();
            }
        });
        filterPopupMenu.add(moveRuleUp);
        
        // move rule down task
        filterTasks.add( initActionCallback( "moveRuleDown",
                ActionFactory.createBoundAction( "moveRuleDown", "Move Rule Down", "D" ),
                new ImageIcon( Frame.class.getResource( "images/arrow_down.png" )) ));
        JMenuItem moveRuleDown = new JMenuItem("Move Rule Down");
        moveRuleDown.setIcon( new ImageIcon( Frame.class.getResource( "images/arrow_down.png" )) );
        moveRuleDown.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveRuleDown();
            }
        });
        filterPopupMenu.add(moveRuleDown);
        
        // add the tasks to the taskpane, and the taskpane to the mirth client
        parent.setNonFocusable( filterTasks );
        filterTaskPaneContainer.add( filterTasks );
        
                /*
                otherTasks = new JXTaskPane();
        otherTasks.setTitle("Other");
        otherTasks.setFocusable(false);
        otherTasks.add(initActionCallback("doHelp", ActionFactory.createBoundAction("doHelp","Help on this topic","H"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/help.png"))));
        otherTasks.add(initActionCallback("goToAbout",  ActionFactory.createBoundAction("goToAbout","About Mirth","U"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/about.png"))));
        otherTasks.add(initActionCallback("goToMirth", ActionFactory.createBoundAction("goToMirth","Visit MirthProject.org","V"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/home.png"))));
        otherTasks.add(initActionCallback("doLogout", ActionFactory.createBoundAction("doLogout","Logout","G"), new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/disconnect.png"))));
        filterTaskPaneContainer.add(otherTasks);
                 */
        
        makeFilterTable();
        
        // BGN LAYOUT
        filterTablePane.setBorder( BorderFactory.createEmptyBorder() );
        rulePanel.setBorder( BorderFactory.createEmptyBorder() );
        
        hSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                rulePanel, refPanel );
        hSplitPane.setContinuousLayout( true );
        hSplitPane.setDividerLocation( EditorConstants.TAB_PANEL_DIVIDER_LOCATION );
        
        vSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
                filterTablePane, hSplitPane );
        vSplitPane.setContinuousLayout( true );
        vSplitPane.setDividerLocation( EditorConstants.TABLE_DIVIDER_LOCATION );
        
        this.setLayout( new BorderLayout() );
        this.add( vSplitPane, BorderLayout.CENTER );
        this.setBorder( BorderFactory.createEmptyBorder() );
        // END LAYOUT
        
    }  // END initComponents()
    
    public void makeFilterTable()
    {
        filterTable = new JXTable();
        
        filterTable.setModel( new DefaultTableModel(
                new String [] { "#", "Operator", "Script" }, 0 )
        {
            boolean[] canEdit = new boolean [] { false, true, false };
            
            public boolean isCellEditable( int row, int col )
            {
                if ( row == 0 && col == RULE_OP_COL )
                    return false;
                return canEdit[col];
            }
        });
        
        filterTableModel = (DefaultTableModel)filterTable.getModel();
        
        // Set the combobox editor on the operator column, and add action listener
        MyComboBoxEditor comboBox = new MyComboBoxEditor( comboBoxValues );
        ((JXComboBox)comboBox.getComponent()).addItemListener(
                new ItemListener()
        {
            public void itemStateChanged( ItemEvent evt )
            {
                updateOperations();
            }
        });
        
        filterTable.setSelectionMode( 0 );		// only select one row at a time
        
        filterTable.getColumnExt( RULE_NUMBER_COL ).setMaxWidth( UIConstants.MAX_WIDTH );
        filterTable.getColumnExt( RULE_OP_COL ).setMaxWidth( UIConstants.MAX_WIDTH );
        
        filterTable.getColumnExt( RULE_NUMBER_COL ).setPreferredWidth( 30 );
        filterTable.getColumnExt( RULE_OP_COL ).setPreferredWidth( 60 );
        
        filterTable.getColumnExt( RULE_NUMBER_COL ).setCellRenderer( new CenterCellRenderer() );
        filterTable.getColumnExt( RULE_OP_COL ).setCellEditor( comboBox );
        
        filterTable.getColumnExt( RULE_NUMBER_COL ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
        filterTable.getColumnExt( RULE_OP_COL ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
        
        filterTable.setRowHeight( UIConstants.ROW_HEIGHT );
        filterTable.packTable( UIConstants.COL_MARGIN );
        filterTable.setSortable( false );
        filterTable.setOpaque( true );
        filterTable.setRowSelectionAllowed( true );
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            filterTable.setHighlighters( highlighter );
        }
        
        filterTable.setBorder( BorderFactory.createEmptyBorder() );
        filterTablePane.setBorder( BorderFactory.createEmptyBorder() );
        
        filterTablePane.setViewportView( filterTable );
        
        filterTable.addMouseListener(
                new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showFilterPopupMenu(evt, true);
            }
            
            public void mouseReleased(MouseEvent evt)
            {
                showFilterPopupMenu(evt, true);
            }
        });
        
        filterTablePane.addMouseListener(
                new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showFilterPopupMenu(evt, false);
            }
            
            public void mouseReleased(MouseEvent evt)
            {
                showFilterPopupMenu(evt, false);
            }
        });
        
        filterTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent evt )
            {
                if ( !updating && !evt.getValueIsAdjusting() )
                    FilterListSelected(evt);
            }
        });
        
    }
    
    private void showFilterPopupMenu(MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = filterTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                filterTable.setRowSelectionInterval(row, row);
            }
            
            filterPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    // for the task pane
    public BoundAction initActionCallback(
            String callbackMethod, BoundAction boundAction, ImageIcon icon )
    {
        
        if(icon != null) boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.registerCallback( this, callbackMethod );
        return boundAction;
    }
    
    // called whenever a table row is (re)selected
    private void FilterListSelected( ListSelectionEvent evt )
    {
        updating = true;
        
        int row = filterTable.getSelectedRow();
        int last = evt.getLastIndex();
        
        saveData( prevSelRow );
        
        if( isValid( row ) )
            loadData( row );
        else if ( isValid( last ) )
        {
            loadData( last );
            row = last;
        }
        
        rulePanel.showCard( JAVASCRIPT_TYPE );
        filterTable.setRowSelectionInterval( row, row );
        prevSelRow = row;
        updateTaskPane();
        
        updating = false;
    }
    
    // returns true if the row is a valid index in the existing model
    private boolean isValid( int row )
    {
        return ( row >= 0 && row < filterTableModel.getRowCount() );
    }
    
    // sets the data from the previously used panel into the
    // previously selected Rule object
    private void saveData( int row )
    {
        if ( filterTable.isEditing() )
            filterTable.getCellEditor( filterTable.getEditingRow(),
                    filterTable.getEditingColumn() ).stopCellEditing();
        
        updating = true;
        
        if ( isValid( row ))
        {
            Map<Object,Object> m = jsPanel.getData();
            
            filterTableModel.setValueAt(
                    m.get("Script"), row, RULE_SCRIPT_COL );
        }
        
        updating = false;
    }
    
    // loads the data object from the currently selected row
    // into the correct panel
    private void loadData( int row )
    {
        if ( isValid( row ) )
        {
            Map<Object,Object> m = new HashMap<Object,Object>();
            m.put("Script", filterTableModel.getValueAt( row, RULE_SCRIPT_COL ));
            jsPanel.setData( m );
        }
    }
    
    // display a rule in the table
    private void setRowData( Rule rule, int row )
    {
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];
        
        tableData[RULE_NUMBER_COL] = rule.getSequenceNumber();
        tableData[RULE_OP_COL] = rule.getOperator();
        tableData[RULE_SCRIPT_COL] = rule.getScript();
        
        updating = true;
        filterTableModel.addRow( tableData );
        filterTable.setRowSelectionInterval( row, row );
        updating = false;
    }
    
    /** void addNewRule()
     *  add a new rule to the end of the list
     */
    public void addNewRule()
    {
        modified = true;
        int rowCount = filterTable.getRowCount();
        Rule rule = new Rule();
        
        saveData( filterTable.getSelectedRow() );
        
        rule.setSequenceNumber( rowCount );
        rule.setScript( "return true;" );
        if ( rowCount == 0 )
            rule.setOperator( Rule.Operator.NONE );	// NONE operator by default on row 0
        else
            rule.setOperator( Rule.Operator.AND );	// AND operator by default elsewhere
        
        setRowData( rule, rowCount );
        prevSelRow = rowCount;
        updateRuleNumbers();
    }
    
    /** void deleteRule(MouseEvent evt)
     *  delete all selected rows
     */
    public void deleteRule()
    {
        modified = true;
        if ( filterTable.isEditing() )
            filterTable.getCellEditor( filterTable.getEditingRow(),
                    filterTable.getEditingColumn() ).stopCellEditing();
        
        updating = true;
        
        int row = filterTable.getSelectedRow();
        if ( isValid( row ) )
            filterTableModel.removeRow( row );
        
        updating = false;
        
        if ( isValid( row ) )
            filterTable.setRowSelectionInterval( row, row );
        else if ( isValid( row - 1 ) )
            filterTable.setRowSelectionInterval( row - 1, row - 1 );
        else
        {
            rulePanel.showCard( BLANK_TYPE );
            jsPanel.setData( null );
        }
        
        updateRuleNumbers();
    }
    
    /** void moveRule( int i )
     *  move the selected row i places
     */
    public void moveRuleUp()
    { moveRule( -1 ); }
    public void moveRuleDown()
    { moveRule( 1 ); }
    public void moveRule( int i )
    {
        modified = true;
        int selRow = filterTable.getSelectedRow();
        int moveTo = selRow + i;
        
        // we can't move past the first or last row
        if ( moveTo >= 0 && moveTo < filterTable.getRowCount() )
        {
            saveData( selRow );
            loadData( moveTo );
            filterTableModel.moveRow( selRow, selRow, moveTo );
            filterTable.setRowSelectionInterval( moveTo, moveTo );
        }
        
        updateRuleNumbers();
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
            String filterXML = "";
            
            try
            {
                filterXML = FileUtil.read(importFile);
            }
            catch (IOException e)
            {
                parent.alertError("File could not be read.");
                return;
            }
            
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            try
            {
                Filter importFilter = (Filter)serializer.fromXML(filterXML);
                prevSelRow = -1;
                modified = true;
                connector.setFilter(importFilter);
                load(connector, importFilter);
            }
            catch (Exception e)
            {
                parent.alertError("Invalid filter file.");
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
            String filterXML = serializer.toXML(filter);
            exportFile = exportFileChooser.getSelectedFile();
            
            int length = exportFile.getName().length();
            
            if (length < 4 || !exportFile.getName().substring(length-4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");
            
            if(exportFile.exists())
                if(!parent.alertOption("This file already exists.  Would you like to overwrite it?"))
                    return;
            
            try
            {
                FileUtil.write(exportFile, filterXML);
                parent.alertInformation("Filter was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                parent.alertError("File could not be written.");
            }
        }
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
        saveData( filterTable.getSelectedRow() );
        
        List<Rule> list = new ArrayList<Rule>();
        for ( int i = 0;  i < filterTable.getRowCount();  i++ )
        {
            Rule rule = new Rule();
            rule.setSequenceNumber( Integer.parseInt(
                    filterTable.getValueAt( i, RULE_NUMBER_COL ).toString() ));
            
            if ( i == 0 )
                rule.setOperator( Rule.Operator.NONE );
            else
                rule.setOperator( Rule.Operator.valueOf(
                        filterTableModel.getValueAt( i, RULE_OP_COL ).toString() ));
            
            rule.setScript( (String)filterTableModel.getValueAt( i, RULE_SCRIPT_COL ));
            
            list.add( rule );
        }
        
        filter.setRules( list );
        filter.setTemplate( tabPanel.getHL7Message() );
        
        // reset the task pane and content to channel edit page
        if(returning)
        {
            parent.channelEditPage.setDestinationVariableList();
            parent.taskPaneContainer.add(parent.getOtherPane());
            parent.setCurrentContentPage( parent.channelEditPage );
            parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
            parent.setPanelName("Edit Channel :: " +  parent.channelEditPage.currentChannel.getName());
            if ( modified ) 
                parent.enableSave();
            modified = false;
        }
    }
    
    /** void updateRuleNumbers()
     *  traverses the table and updates all data numbers, both in the model
     *  and the view, after any change to the table
     */
    private void updateRuleNumbers()
    {
        updating = true;
        
        int rowCount = filterTableModel.getRowCount();
        int selRow = filterTable.getSelectedRow();
        
        for ( int i = 0;  i < rowCount;  i++ )
            filterTableModel.setValueAt( i, i, RULE_NUMBER_COL );
        
        updateOperations();
        if ( isValid( selRow ) )
        {
            filterTable.setRowSelectionInterval( selRow, selRow );
            loadData( selRow );
            rulePanel.showCard( JAVASCRIPT_TYPE );
        }
        else if ( rowCount > 0 )
        {
            filterTable.setRowSelectionInterval( 0, 0 );
            loadData( 0 );
            rulePanel.showCard( JAVASCRIPT_TYPE );
        }
        
        updateTaskPane();
        updating = false;
    }
    
    /** updateTaskPane()
     *  configure the task pane so that it shows only relevant tasks
     */
    private void updateTaskPane()
    {
        int rowCount = filterTableModel.getRowCount();
        if ( rowCount <= 0 )
            parent.setVisibleTasks( filterTasks, filterPopupMenu, 1, -1, false );
        else if ( rowCount == 1 )
        {
            parent.setVisibleTasks( filterTasks, filterPopupMenu, 0, -1, true );
            parent.setVisibleTasks( filterTasks, filterPopupMenu, 2, -1, false );
        }
        else
        {
            parent.setVisibleTasks( filterTasks, filterPopupMenu, 0, -1, true );
            
            int selRow = filterTable.getSelectedRow();
            if ( selRow == 0 ) // hide move up
                parent.setVisibleTasks( filterTasks, filterPopupMenu, 4, 4, false );
            else if ( selRow == rowCount - 1 ) // hide move down
                parent.setVisibleTasks( filterTasks, filterPopupMenu, 5, 5, false );
        }
        parent.setVisibleTasks( filterTasks, filterPopupMenu, 2, 3, true );
    }
    
    /** updateOperations()
     *  goes through all existing rules, enforcing rule 0 to be
     *  a Rule.Operator.NONE, and any other NONEs to ANDs.
     */
    private void updateOperations()
    {
        for ( int i = 0;  i < filterTableModel.getRowCount(); i++ )
        {
            if ( i == 0 )
                filterTableModel.setValueAt( "", i, RULE_OP_COL );
            else if ( filterTableModel.getValueAt( i, RULE_OP_COL ).toString().equals( "" ) )
                filterTableModel.setValueAt( Rule.Operator.AND.toString(), i, RULE_OP_COL );
        }
    }
    
//	............................................................................\\
    
    // used to load this pane
    private Filter filter;
    
    // fields
    private JXTable filterTable;
    private DefaultTableModel filterTableModel;
    private JScrollPane filterTablePane;
    private JSplitPane hSplitPane;
    private JSplitPane vSplitPane;
    private boolean updating;				// allow the selection listener to breathe
    JXTaskPaneContainer filterTaskPaneContainer;
    JXTaskPane viewTasks;
    JXTaskPane filterTasks;
    JXTaskPane otherTasks;
    JPopupMenu filterPopupMenu;
    
    
    // this little sucker is used to track the last row that had
    // focus after a new row is selected
    private int prevSelRow;	// no row by default
    private Connector connector;
    
    // panels using CardLayout
    protected CardPanel rulePanel;		// the card holder
    protected BlankPanel blankPanel;	// the cards
    protected JavaScriptPanel jsPanel;  //    \/
    
    // filter constants
    public static final int RULE_NUMBER_COL  = 0;
    public static final int RULE_OP_COL  = 1;
    public static final int RULE_SCRIPT_COL  = 2;
    public static final int NUMBER_OF_COLUMNS = 3;
    public static final String BLANK_TYPE = "";
    public static final String JAVASCRIPT_TYPE = "JavaScript";
    private String[] comboBoxValues = new String[] {
        Rule.Operator.AND.toString(), Rule.Operator.OR.toString() };
    
}
