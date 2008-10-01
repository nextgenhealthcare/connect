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

package com.webreach.mirth.client.ui.browsers.event;

import java.awt.Point;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.SystemEventListHandler;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;

/**
 * The message browser panel.
 */
public class EventBrowser extends javax.swing.JPanel
{
    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
    private final String EVENT_ID_COLUMN_NAME = "Event ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String LEVEL_COLUMN_NAME = "Level";
    private final String EVENT_COLUMN_NAME = "Event";
    private Frame parent;
    private SystemEventListHandler systemEventListHandler;
    private List<SystemEvent> systemEventList;
    private SystemEventFilter systemEventFilter;
    private int eventCount = -1;
    private int currentPage = 0;
    private int pageSize;
    
    /**
     * Constructs the new message browser and sets up its default
     * information/layout.
     */
    public EventBrowser()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        makeEventTable();

        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        });
        
        pageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
        
        String[] values = new String[SystemEvent.Level.values().length + 1];
        values[0] = "ALL";
        for (int i = 1; i < values.length; i++)
            values[i] = SystemEvent.Level.values()[i - 1].toString();

        levelComboBox.setModel(new javax.swing.DefaultComboBoxModel(values));
        
        eventPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, false);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                deselectRows();
            }
        });
    }
    
    /**
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew()
    {
        // Set the default page size
        pageSize = Preferences.systemNodeForPackage(Mirth.class).getInt("messageBrowserPageSize", 20);
    	pageSizeField.setText(pageSize + "");
        
        // use the start filters and make the table.
    	parent.setVisibleTasks(parent.eventTasks, parent.eventPopupMenu, 3, 3, false);
        systemEventListHandler = null;
        
        eventField.setText("");
        levelComboBox.setSelectedIndex(0);
        long currentTime = System.currentTimeMillis();
        mirthDatePicker1.setDateInMillis(currentTime);
        mirthDatePicker2.setDateInMillis(currentTime);     
        filterButtonActionPerformed(null);
        descriptionTabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh()
    {
        filterButtonActionPerformed(null);
    }
    
    public void updateEventTable(List<SystemEvent> systemEventList)
    {
        Object[][] tableData = null;

        if (systemEventList != null)
        {
            tableData = new Object[systemEventList.size()][4];
            
            for (int i = 0; i < systemEventList.size(); i++)
            {
                SystemEvent systemEvent = systemEventList.get(i);
                
                tableData[i][0] = systemEvent.getId();
                
                Calendar calendar = systemEvent.getDate();
                
                tableData[i][1] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL", calendar);
                
                tableData[i][2] = systemEvent.getLevel().toString();
                tableData[i][3] = systemEvent.getEvent();
            }
        }
        else
        {
            tableData = new Object[0][4];
        }
        
        int systemEventListSize = 0;
        if (systemEventList != null)
            systemEventListSize = systemEventList.size();
        
        if (currentPage == 0)
            previousPageButton.setEnabled(false);
        else
            previousPageButton.setEnabled(true);
        
        int numberOfPages = getNumberOfPages(pageSize, eventCount);
        if (systemEventListSize < pageSize || pageSize == 0)
            nextPageButton.setEnabled(false);
        else if (currentPage == numberOfPages)
            nextPageButton.setEnabled(false);
        else
            nextPageButton.setEnabled(true);
        
        int startResult;
        if (systemEventListSize == 0)
            startResult = 0;
        else
            startResult = (currentPage * pageSize) + 1;
        
        int endResult;
        if (pageSize == 0)
            endResult = systemEventListSize;
        else
            endResult = (currentPage + 1) * pageSize;
        
        if (systemEventListSize < pageSize)
            endResult = endResult - (pageSize - systemEventListSize);
        
        if (eventCount == -1)
            resultsLabel.setText("Results " + startResult + " - " + endResult);
        else
            resultsLabel.setText("Results " + startResult + " - " + endResult + " of " + eventCount);

        if (eventTable != null)
        {
            //lastRow = messageTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) eventTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            eventTable = new MirthTable();
            eventTable.setModel(new RefreshTableModel(tableData, new String[] { EVENT_ID_COLUMN_NAME, DATE_COLUMN_NAME, LEVEL_COLUMN_NAME, EVENT_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }

        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        eventTable.setHighlighters(highlighter);
        deselectRows();
    }
    
    /**
     * Creates the table with all of the information given after being filtered
     * by the specified 'filter'
     */
    private void makeEventTable()
    {
        updateEventTable(null);     
        
        eventTable.setSelectionMode(0);
        
        eventTable.getColumnExt(EVENT_ID_COLUMN_NAME).setVisible(false);
        eventTable.getColumnExt(DATE_COLUMN_NAME).setMinWidth(100);
        
        eventTable.setRowHeight(UIConstants.ROW_HEIGHT);
        eventTable.setOpaque(true);
        eventTable.setRowSelectionAllowed(true);
        deselectRows();
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            eventTable.setHighlighters(highlighter);
        }
        
        eventPane.setViewportView(eventTable);
        jSplitPane1.setLeftComponent(eventPane);

        eventTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                EventListSelected(evt);
            }
        });
        
        eventTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showEventPopupMenu(evt, true);
            }
        });
    }
    
    private void getEventTableData(SystemEventListHandler handler, int page)
    {    	
        if (handler != null)
        {
            // Do all paging information below.
            try
            {
                eventCount = handler.getSize();
                currentPage = handler.getCurrentPage();
                pageSize = handler.getPageSize();
                
                if (page == FIRST_PAGE)
                {
                    systemEventList = handler.getFirstPage();
                    currentPage = handler.getCurrentPage();
                }
                else if (page == PREVIOUS_PAGE)
                {
                    if (currentPage == 0)
                        return;
                    systemEventList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                }
                else if (page == NEXT_PAGE)
                {
                    int numberOfPages = getNumberOfPages(pageSize, eventCount);
                    if (currentPage == numberOfPages)
                        return;
                    
                    systemEventList = handler.getNextPage();
                    if (systemEventList.size() == 0)
                        systemEventList = handler.getPreviousPage();
                    currentPage = handler.getCurrentPage();
                }
                
            }
            catch (ListHandlerException e)
            {
                systemEventList = null;
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }
    
    private int getNumberOfPages(int pageSize, int eventCount)
    {
        int numberOfPages;
        if (eventCount == -1)
        	return -1;
        if (pageSize == 0)
            numberOfPages = 0;
        else
        {
            numberOfPages = eventCount / pageSize;
            if ((eventCount != 0) && ((eventCount % pageSize) == 0))
                numberOfPages--;
        }
        
        return numberOfPages;
    }

    /**
     * Shows the trigger button (right-click) popup menu.
     */
    private void showEventPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = eventTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                if (row > -1)
                {
                    eventTable.setRowSelectionInterval(row, row);
                }
            }
            else
                deselectRows();
            parent.eventPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows()
    {
    	parent.setVisibleTasks(parent.eventTasks, parent.eventPopupMenu, 3, 3, false);
        if (eventTable != null)
        {
            eventTable.clearSelection();
            clearDescription();
        }
    }
    
    /**
     * Clears all description information.
     */
    public void clearDescription()
    {
        descriptionTextPane.setText("Select an event to see its description.");
    }
    
    private int getSelectedEventIndex()
    {
        int row = -1;
        if (eventTable.getSelectedRow() > -1)
        {
            row = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
        }
        return row;
    }
    
    /**
     * An action for when a row is selected in the table.
     */
    private void EventListSelected(ListSelectionEvent evt)
    {
        if (!evt.getValueIsAdjusting())
        {
            int row = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());

            if (row >= 0)
            {
            	parent.setVisibleTasks(parent.eventTasks, parent.eventPopupMenu, 3, 3, true);
            	
                descriptionTextPane.setText(systemEventList.get(row).getDescription() + "\n" + systemEventList.get(row).getAttributes());
                descriptionTextPane.setCaretPosition(0);
            }
        }
    }
    
    /**
     * Returns the ID of the selected event in the table.
     */
    public Integer getSelectedEventID()
    {
        int column = -1;
        for (int i = 0; i < eventTable.getModel().getColumnCount(); i++)
        {
            if (eventTable.getModel().getColumnName(i).equals(EVENT_ID_COLUMN_NAME))
                column = i;
        }
        return ((Integer) (eventTable.getModel().getValueAt(eventTable.convertRowIndexToModel(eventTable.getSelectedRow()), column)));
    }
    
    /**
     * Returns the current SystemEventFilter that is set.
     */
    public SystemEventFilter getCurrentFilter()
    {
        return systemEventFilter;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        filterPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        pageSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        nextPageButton = new javax.swing.JButton();
        previousPageButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        eventLabel = new javax.swing.JLabel();
        eventField = new com.webreach.mirth.client.ui.components.MirthTextField();
        mirthDatePicker1 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        jLabel3 = new javax.swing.JLabel();
        mirthDatePicker2 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        jLabel2 = new javax.swing.JLabel();
        levelComboBox = new javax.swing.JComboBox();
        levelLabel = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        descriptionPanel = new javax.swing.JPanel();
        descriptionTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        eventPane = new javax.swing.JScrollPane();
        eventTable = null;

        setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        resultsLabel.setText("Results");

        nextPageButton.setText(">");
        nextPageButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextPageButtonActionPerformed(evt);
            }
        });

        previousPageButton.setText("<");
        previousPageButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                previousPageButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Page Size:");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                            .add(jLabel6)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, resultsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .add(previousPageButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextPageButton))))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {nextPageButton, previousPageButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(resultsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nextPageButton)
                    .add(previousPageButton)))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Search", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        eventLabel.setText("Event:");

        jLabel3.setText("Start Date:");

        jLabel2.setText("End Date:");

        levelLabel.setText("Level:");

        filterButton.setText("Search");
        filterButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                filterButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(filterButton)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, eventLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(eventField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(22, 22, 22)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(levelLabel)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(levelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(eventLabel)
                    .add(eventField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(levelLabel)
                    .add(levelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel3)
                    .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterButton))
        );

        org.jdesktop.layout.GroupLayout filterPanelLayout = new org.jdesktop.layout.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        descriptionTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descriptionTabbedPane.setFocusable(false);
        descriptionPanel.setBackground(new java.awt.Color(255, 255, 255));
        descriptionPanel.setFocusable(false);
        descriptionTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        descriptionTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(descriptionTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(descriptionTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Description", descriptionPanel);

        jSplitPane1.setRightComponent(descriptionTabbedPane);

        eventPane.setViewportView(eventTable);

        jSplitPane1.setLeftComponent(eventPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_nextPageButtonActionPerformed
        parent.setWorking("Loading next page...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	getEventTableData(systemEventListHandler, NEXT_PAGE);
                return null;
            }
            
            public void done()
            {
                if (systemEventListHandler != null)
                    updateEventTable(systemEventList);
                else
                    updateEventTable(null);
                parent.setWorking("", false);
            }
        };
        worker.execute();
        
    }// GEN-LAST:event_nextPageButtonActionPerformed
    
    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_previousPageButtonActionPerformed
        parent.setWorking("Loading previous page...", true);
        
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
            	getEventTableData(systemEventListHandler, PREVIOUS_PAGE);
                return null;
            }
            
            public void done()
            {
                if (systemEventListHandler != null)
                    updateEventTable(systemEventList);
                else
                    updateEventTable(null);
                parent.setWorking("", false);
            }
        };
        worker.execute();
    }// GEN-LAST:event_previousPageButtonActionPerformed
    
    /**
     * An action when the filter button is pressed. Creates the actual filter
     * and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_filterButtonActionPerformed      
        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null)
        {
            if (mirthDatePicker1.getDateInMillis() > mirthDatePicker2.getDateInMillis())
            {
                JOptionPane.showMessageDialog(parent, "Start date cannot be after the end date.");
                return;
            }
        }

        systemEventFilter = new SystemEventFilter();
        
        if (!eventField.getText().equals(""))
            systemEventFilter.setEvent(eventField.getText());

        if (!((String) levelComboBox.getSelectedItem()).equalsIgnoreCase("ALL"))
        {
            for (int i = 0; i < SystemEvent.Level.values().length; i++)
            {
                if (((String) levelComboBox.getSelectedItem()).equalsIgnoreCase(SystemEvent.Level.values()[i].toString()))
                    systemEventFilter.setLevel(SystemEvent.Level.values()[i]);
            }
        }

        if (mirthDatePicker1.getDate() != null)
        {
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTimeInMillis(mirthDatePicker1.getDateInMillis());
            systemEventFilter.setStartDate(calendarStart);
        }
        if (mirthDatePicker2.getDate() != null)
        {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTimeInMillis(mirthDatePicker2.getDateInMillis());
            systemEventFilter.setEndDate(calendarEnd);
        }

        if (!pageSizeField.getText().equals(""))
            pageSize = Integer.parseInt(pageSizeField.getText());

        parent.setWorking("Loading events...", true);
        
        if (systemEventListHandler == null)
            updateEventTable(null);

        class EventWorker extends SwingWorker<Void, Void>
        {
            public Void doInBackground()
            {
                try
                {
                    systemEventListHandler = parent.mirthClient.getSystemEventListHandler(systemEventFilter, pageSize, false);
                }
                catch (ClientException e)
                {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                }
                getEventTableData(systemEventListHandler, FIRST_PAGE);
                return null;
            }

            public void done()
            {
                if (systemEventListHandler != null)
                    updateEventTable(systemEventList);
                else
                    updateEventTable(null);

                parent.setWorking("", false);
            }
        };
        EventWorker worker = new EventWorker();
        worker.execute();
    }// GEN-LAST:event_filterButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea descriptionTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField eventField;
    private javax.swing.JLabel eventLabel;
    private javax.swing.JScrollPane eventPane;
    private com.webreach.mirth.client.ui.components.MirthTable eventTable;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox levelComboBox;
    private javax.swing.JLabel levelLabel;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker2;
    private javax.swing.JButton nextPageButton;
    private com.webreach.mirth.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel resultsLabel;
    // End of variables declaration//GEN-END:variables
    
}
