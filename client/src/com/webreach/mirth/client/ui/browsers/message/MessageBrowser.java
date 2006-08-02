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


package com.webreach.mirth.client.ui.browsers.message;

import com.Ostermiller.Syntax.HighlightedDocument;
import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.HL7TreePanel;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.filters.MessageEventFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel
{
    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String CHANNEL_ID_COLUMN_NAME = "Channel ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String SENDING_FACILITY_COLUMN_NAME = "Sending Facility";
    private final String EVENT_COLUMN_NAME = "Event";
    private final String CONTROL_ID_COLUMN_NAME = "Control ID";
    private final String STATUS_COLUMN_NAME = "Status";
    
    private JScrollPane eventPane;
    private JXTable eventTable;
    private Frame parent;
    private List<MessageEvent> messageEventList;
    private HL7TreePanel HL7Panel;
    private JScrollPane HL7ScrollPane;
    private static HighlightedDocument xmlDoc;
    private static HighlightedDocument er7Doc;
    private Document normalDoc;
    
    /**
     * Constructs the new message browser and sets up its default information/layout.
     */
    public MessageBrowser()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
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
        
        xmlDoc = new HighlightedDocument();
        xmlDoc.setHighlightStyle(HighlightedDocument.HTML_STYLE);
        er7Doc = new HighlightedDocument();
        er7Doc.setHighlightStyle(HighlightedDocument.C_STYLE);
        normalDoc = XMLTextPane.getDocument();
        
        HL7Panel = new HL7TreePanel();
        HL7ScrollPane = new JScrollPane();
        HL7ScrollPane.setViewportView(HL7Panel);
        HL7ScrollPane.setFocusable(false);
        descriptionTabbedPane.addTab("HL7", HL7ScrollPane);
        
        String[] values = new String[MessageEvent.Status.values().length + 1];
        values[0] = "ALL";
        for (int i = 1; i < values.length; i++)
            values[i] = MessageEvent.Status.values()[i-1].toString();
        
        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(values));
        
        eventPane = new JScrollPane();
        
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
        
        eventPane.setViewportView(eventTable);
        
        jPanel2.removeAll();  
        
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eventPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eventPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
        );
        
        jPanel2.updateUI();
    }
    
    /**
     * Loads up a clean message browser as if a new one was constructed.
     */
    public void loadNew()
    {
        // use the start filters and make the table.
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, false);
        sendingFacilityField.setText("");
        controlIDField.setText("");
        eventField.setText("");
        statusComboBox.setSelectedIndex(0);
        long currentTime = System.currentTimeMillis();
        mirthDatePicker1.setDateInMillis(currentTime);
        mirthDatePicker2.setDateInMillis(currentTime);
        
        filterButtonActionPerformed(null);
        clearDescription();
        descriptionTabbedPane.setSelectedIndex(0);
    }
    
    /**
     * Refreshes the panel with the curent filter information.
     */
    public void refresh()
    {
        deselectRows();
        filterButtonActionPerformed(null);
    }
    
    /**
     * Creates the table with all of the information given after
     * being filtered by the specified 'filter'
     */
    public void makeEventTable(MessageEventFilter filter) {
        eventTable = new JXTable();
        try 
        {
            messageEventList = parent.mirthClient.getMessageEvents(filter);
        } 
        catch (ClientException ex)
        {
            messageEventList = null;
            ex.printStackTrace();
        }
        
        if (messageEventList == null)
            return;
                
        Object[][] tableData = new Object[messageEventList.size()][7];
        
        for (int i=0; i < messageEventList.size(); i++)
        {
            MessageEvent messageEvent = messageEventList.get(i);
            
            tableData[i][0] = messageEvent.getId();
            tableData[i][1] = messageEvent.getChannelId();
            
            Calendar calendar = messageEvent.getDate();
            
            tableData[i][2] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar);
            tableData[i][3] = messageEvent.getSendingFacility();
            tableData[i][4] = messageEvent.getEvent();
            tableData[i][5] = messageEvent.getControlId();
            tableData[i][6] = messageEvent.getStatus();
            
        }
                
        
        eventTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData,
                new String []
        {
            MESSAGE_ID_COLUMN_NAME, CHANNEL_ID_COLUMN_NAME, DATE_COLUMN_NAME, SENDING_FACILITY_COLUMN_NAME, EVENT_COLUMN_NAME, CONTROL_ID_COLUMN_NAME, STATUS_COLUMN_NAME
        }
        ) {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false
            };
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        
        eventTable.setSelectionMode(0);        
        
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(DATE_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(SENDING_FACILITY_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        eventTable.getColumnExt(CONTROL_ID_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        eventTable.getColumnExt(STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setCellRenderer(new CenterCellRenderer());
        eventTable.getColumnExt(CHANNEL_ID_COLUMN_NAME).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);  
        
        eventTable.packTable(UIConstants.COL_MARGIN);    
        
        eventTable.setRowHeight(UIConstants.ROW_HEIGHT);
        eventTable.setOpaque(true);
        eventTable.setRowSelectionAllowed(true);
        clearDescription();
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            eventTable.setHighlighters(highlighter);
        }
        
        eventPane.setViewportView(eventTable);
        
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
                eventTable.setRowSelectionInterval(row, row);
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
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, false);
        eventTable.clearSelection();
        clearDescription();
    }
    
    /**
     * Clears all description information.
     */
    public void clearDescription()
    {
        ER7TextPane.setDocument(normalDoc);
        ER7TextPane.setText("Select a message to view ER7-encoded HL7 message.");
        XMLTextPane.setDocument(normalDoc);
        XMLTextPane.setText("Select a message to view XML-encoded HL7 message.");
        HL7Panel.clearMessage();
    }
    
    /**
     * An action for when a row is selected in the table.
     */
    private void EventListSelected(ListSelectionEvent evt)
    {
        if (!evt.getValueIsAdjusting())
        {
            int row = eventTable.getSelectedRow();

            if(row >= 0)
            {
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 2, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                String message = messageEventList.get(row).getMessage();
                ER7TextPane.setDocument(er7Doc);
                ER7TextPane.setText(message.replaceAll("\r", "\n"));
                ER7TextPane.setCaretPosition(0);
                
                ER7Serializer serializer = new ER7Serializer();
                XMLTextPane.setDocument(xmlDoc);
                XMLTextPane.setText(serializer.serialize(message));
                XMLTextPane.setCaretPosition(0);
                
                HL7Panel.setMessage(message);
                
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    /**
     * Returns the ID of the selected message in the table.
     */
    public int getSelectedMessageID()
    {
        int column = -1;
        for (int i = 0; i < eventTable.getColumnCount(); i++)
        {
            if (eventTable.getColumnName(i).equals(MESSAGE_ID_COLUMN_NAME))
                column = i;
        }
        return ((Integer)eventTable.getValueAt(eventTable.getSelectedRow(), column)).intValue();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        filterPanel = new javax.swing.JPanel();
        sendingFacilityLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mirthDatePicker1 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        mirthDatePicker2 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        jLabel1 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sendingFacilityField = new com.webreach.mirth.client.ui.components.MirthTextField();
        eventField = new com.webreach.mirth.client.ui.components.MirthTextField();
        controlIDField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        descriptionPanel = new javax.swing.JPanel();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        ER7Panel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ER7TextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();
        XMLPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        XMLTextPane = new com.webreach.mirth.client.ui.components.MirthTextPane();

        setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter By", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        sendingFacilityLabel.setText("Sending Facility:");

        jLabel3.setText("Start Date:");

        filterButton.setText("Filter");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("End Date:");

        jLabel1.setText("Control ID:");

        jLabel5.setText("Status:");

        jLabel4.setText("Event:");

        org.jdesktop.layout.GroupLayout filterPanelLayout = new org.jdesktop.layout.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(sendingFacilityLabel)
                    .add(jLabel4)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterButton)
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(16, 16, 16)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(eventField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(sendingFacilityField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                        .add(14, 14, 14)
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(filterPanelLayout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(controlIDField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(filterPanelLayout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sendingFacilityLabel)
                    .add(jLabel1)
                    .add(sendingFacilityField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(controlIDField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(eventField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterButton))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
        );

        descriptionPanel.setBackground(new java.awt.Color(255, 255, 255));
        descriptionTabbedPane.setFocusable(false);
        ER7Panel.setBackground(new java.awt.Color(255, 255, 255));
        ER7Panel.setFocusable(false);
        ER7TextPane.setEditable(false);
        jScrollPane3.setViewportView(ER7TextPane);

        org.jdesktop.layout.GroupLayout ER7PanelLayout = new org.jdesktop.layout.GroupLayout(ER7Panel);
        ER7Panel.setLayout(ER7PanelLayout);
        ER7PanelLayout.setHorizontalGroup(
            ER7PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ER7PanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addContainerGap())
        );
        ER7PanelLayout.setVerticalGroup(
            ER7PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ER7PanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("ER7", ER7Panel);

        XMLPanel.setBackground(new java.awt.Color(255, 255, 255));
        XMLPanel.setFocusable(false);
        XMLTextPane.setEditable(false);
        jScrollPane2.setViewportView(XMLTextPane);

        org.jdesktop.layout.GroupLayout XMLPanelLayout = new org.jdesktop.layout.GroupLayout(XMLPanel);
        XMLPanel.setLayout(XMLPanelLayout);
        XMLPanelLayout.setHorizontalGroup(
            XMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(XMLPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addContainerGap())
        );
        XMLPanelLayout.setVerticalGroup(
            XMLPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(XMLPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("XML", XMLPanel);

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * An action when the filter button is pressed.  Creates
     * the actual filter and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null)
        {
            if (mirthDatePicker1.getDateInMillis() > mirthDatePicker2.getDateInMillis())
            {
                JOptionPane.showMessageDialog(parent, "Start date cannot be after the end date.");
                return;
            }
        }
        
        MessageEventFilter filter = new MessageEventFilter();
        
        filter.setChannelId(parent.status.get(parent.statusListPage.getSelectedStatus()).getChannelId());
        
        if (!sendingFacilityField.getText().equals(""))
            filter.setSendingFacility(sendingFacilityField.getText());
        
        if (!controlIDField.getText().equals(""))
            filter.setControlId(controlIDField.getText());
        
        if (!eventField.getText().equals(""))
            filter.setEvent(eventField.getText());
        
        if (!((String)statusComboBox.getSelectedItem()).equalsIgnoreCase("ALL"))
        {
            for (int i = 0; i < MessageEvent.Status.values().length; i++)
            {
                if (((String)statusComboBox.getSelectedItem()).equalsIgnoreCase(MessageEvent.Status.values()[i].toString()))
                    filter.setStatus(MessageEvent.Status.values()[i]);
            }
        }
        
        if (mirthDatePicker1.getDate() != null)
        {
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTimeInMillis(mirthDatePicker1.getDateInMillis());
            filter.setStartDate(calendarStart);
        }
        if (mirthDatePicker2.getDate() != null)
        {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTimeInMillis(mirthDatePicker2.getDateInMillis());
            filter.setEndDate(calendarEnd);
        }
        makeEventTable(filter);
    }//GEN-LAST:event_filterButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ER7Panel;
    private com.webreach.mirth.client.ui.components.MirthTextPane ER7TextPane;
    private javax.swing.JPanel XMLPanel;
    private com.webreach.mirth.client.ui.components.MirthTextPane XMLTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField controlIDField;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private com.webreach.mirth.client.ui.components.MirthTextField eventField;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker2;
    private com.webreach.mirth.client.ui.components.MirthTextField sendingFacilityField;
    private javax.swing.JLabel sendingFacilityLabel;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
    
}
