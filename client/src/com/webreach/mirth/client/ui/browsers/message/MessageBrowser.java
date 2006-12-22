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

import java.awt.Cursor;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.HL7TokenMarker;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.core.ListHandlerException;
import com.webreach.mirth.client.core.MessageListHandler;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.MirthFileFilter;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.components.MirthSyntaxTextArea;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;

/**
 * The message browser panel.
 */
public class MessageBrowser extends javax.swing.JPanel
{
    private final int FIRST_PAGE = 0;
    private final int PREVIOUS_PAGE = -1;
    private final int NEXT_PAGE = 1;
    private final String MESSAGE_ID_COLUMN_NAME = "Message ID";
    private final String DATE_COLUMN_NAME = "Date";
    private final String CONNECTOR_COLUMN_NAME = "Connector";
    private final String STATUS_COLUMN_NAME = "Status";
    private final String KEY_COLUMN_NAME = "Key";
    private final String VALUE_COLUMN_NAME = "Value";
    private final String TYPE_COLUMN_NAME = "Type";
    private final String SOURCE_COLUMN_NAME = "Source";
    private JScrollPane eventPane;
    private JScrollPane mappingsPane;
    private JXTable eventTable;
    private Frame parent;
    private MessageListHandler messageListHandler;
    private List<MessageObject> messageObjectList;
    private MessageObjectFilter messageObjectFilter;
    private DefaultTableModel messageTableModel;
    
    /**
     * Constructs the new message browser and sets up its default information/layout.
     */
    public MessageBrowser()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
        mappingsPane = new JScrollPane();
        makeMappingsTable(new String[0][0], true);
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(MappingsPanel);
        MappingsPanel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mappingsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mappingsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                );
        
        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                if (evt.isPopupTrigger())
                    parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        });
        
        pageSizeField.setDocument(new MirthFieldConstraints(3, false, true));
        
        String[] values = new String[MessageObject.Status.values().length + 1];
        values[0] = "ALL";
        for (int i = 1; i < values.length; i++)
            values[i] = MessageObject.Status.values()[i-1].toString();
        
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
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 4, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 5, 5, true);
        statusComboBox.setSelectedIndex(0);
        long currentTime = System.currentTimeMillis();
        mirthDatePicker1.setDateInMillis(currentTime);
        mirthDatePicker2.setDateInMillis(currentTime);
        pageSizeField.setText("20");
        //Clear the table first
        eventTable.setModel(new javax.swing.table.DefaultTableModel(
                null,
                new String []
        {
            DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, TYPE_COLUMN_NAME, SOURCE_COLUMN_NAME, STATUS_COLUMN_NAME
        }
        ));
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
     * Export the current messages to XML or HTML
     */
    public void export()
    {
        String answer = "";
        
        JOptionPane pane = new JOptionPane("Would you like to export the file to XML or HTML?");
        Object[] options = new String[] { "XML", "HTML", "Cancel" };
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(new JFrame(), "Select an Option");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        for (int k = 0; k < options.length; k++)
            if (options[k].equals(obj))
                answer = obj.toString();
        
        if(answer.length() == 0 || answer.equals(options[2]))
            return;
        
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        exportFileChooser.setFileFilter(new MirthFileFilter(answer));
        int returnVal = exportFileChooser.showSaveDialog(parent);
        File exportFile = null;
        File exportDirectory = null;
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                exportFile = exportFileChooser.getSelectedFile();
                int length = exportFile.getName().length();
                String messages = "";
                List<MessageObject> messageObjects = messageListHandler.getAllPages();
                
                if(answer.equals("HTML"))
                {
                    String name = parent.status.get(parent.statusPanel.getSelectedStatus()).getName();
                    File rawFile = new File(exportFile.getParent() + "/" + name + "_raw_messages.txt");
                    File transformedFile = new File(exportFile.getParent() + "/" + name + "_transformed_messages.txt");
                    File encodedFile= new File(exportFile.getParent() + "/" + name + "_encoded_messages.txt");
                    
                    MessageObjectHtmlSerializer serializer = new MessageObjectHtmlSerializer();
                    messages = serializer.toHtml(messageObjects);
                    
                    if (length < 5 || !exportFile.getName().substring(length-5, length).equals(".html"))
                        exportFile = new File(exportFile.getAbsolutePath() + ".html");
                    
                    if(exportFile.exists() || rawFile.exists() || transformedFile.exists() || encodedFile.exists())
                        if(!parent.alertOption("Some of the files already exist.  Would you like to overwrite them?"))
                            return;
                    
                    serializer.outputMessages(messageObjects,rawFile,transformedFile,encodedFile);
                    
                    FileUtil.write(exportFile, messages);
                    parent.alertInformation("All message data was written successfully.");
                }
                else
                {
                    if(exportFile.exists())
                        if(!parent.alertOption("The file " + exportFile.getName() + " already exists.  Would you like to overwrite it?"))
                            return;
                    
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                    for(int i = 0; i < messageObjects.size(); i++)
                    {
                        messages += serializer.toXML(messageObjects.get(i));
                        messages += "\n";
                    }
                    
                    if (length < 4 || !exportFile.getName().substring(length-4, length).equals(".xml"))
                        exportFile = new File(exportFile.getAbsolutePath() + ".xml");
                    
                    FileUtil.write(exportFile, messages);
                    parent.alertInformation("All messages were written successfully to " + exportFile.getPath() + ".");
                }
            }
            catch (ListHandlerException ex)
            {
                parent.alertError("File could not be written.");
            }
            catch (IOException ex)
            {
                parent.alertError("File could not be written.");
            }
        }
    }
    
    /**
     * Creates the table with all of the information given after
     * being filtered by the specified 'filter'
     */
    public void makeMessageTable(MessageListHandler handler, int page)
    {
        eventTable = new JXTable();
        Object[][] tableData = null;
        if (handler != null)
        {
            // Do all paging information below.
            try
            {
                if (page == FIRST_PAGE)
                    messageObjectList = handler.getFirstPage();
                else if (page == PREVIOUS_PAGE)
                    messageObjectList = handler.getPreviousPage();
                else if (page == NEXT_PAGE)
                {
                    messageObjectList = handler.getNextPage();
                    if (messageObjectList.size() == 0)
                        messageObjectList = handler.getPreviousPage();
                }
                
                int messageCount = handler.getSize();
                int currentPage = handler.getCurrentPage();
                int pageSize = handler.getPageSize();
                
                if (pageSize == -1)
                    pageSize = 0;
                
                pageSizeField.setText(pageSize + "");
                
                if (handler.getCurrentPage() == 0)
                    previousPageButton.setEnabled(false);
                else
                    previousPageButton.setEnabled(true);
                
                int numberOfPages;
                
                if (pageSize == 0)
                    numberOfPages = 0;
                else
                {
                    numberOfPages = messageCount / pageSize;
                    if ((messageCount != 0) && ((messageCount % pageSize) == 0))
                        numberOfPages--;
                }
                
                if (currentPage == numberOfPages)
                    nextPageButton.setEnabled(false);
                else
                    nextPageButton.setEnabled(true);
                
                int startResult;
                if (messageCount == 0)
                    startResult = 0;
                else
                    startResult = (currentPage * pageSize) + 1;
                
                int endResult;
                if (pageSize == 0)
                    endResult = messageCount;
                else
                    endResult = (currentPage + 1) * pageSize;
                
                if (messageCount < endResult)
                    endResult = messageCount;
                resultsLabel.setText("Results " + startResult + " - " + endResult + " of " + messageCount);
                
            }
            catch (ListHandlerException e)
            {
                messageObjectList = null;
                parent.alertException(e.getStackTrace(), e.getMessage());
            }
            
            if (messageObjectList != null)
            {
                
                
                tableData = new Object[messageObjectList.size()][6];
                
                for (int i=0; i < messageObjectList.size(); i++)
                {
                    MessageObject messageObject = messageObjectList.get(i);
                    
                    tableData[i][0] = messageObject.getId();
                    
                    Calendar calendar = messageObject.getDateCreated();
                    
                    tableData[i][1] = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar);
                    tableData[i][2] = messageObject.getConnectorName();
                    
                    tableData[i][3] = messageObject.getType();
                    tableData[i][4] = messageObject.getSource();
                    tableData[i][5] = messageObject.getStatus();
                }
            }
            else
            {
                tableData = new Object[0][6];
            }
        }
        else
        {
            tableData = new Object[0][6];
        }
        
        eventTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData,
                new String []
        {
            MESSAGE_ID_COLUMN_NAME, DATE_COLUMN_NAME, CONNECTOR_COLUMN_NAME, TYPE_COLUMN_NAME, SOURCE_COLUMN_NAME, STATUS_COLUMN_NAME
        }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false
            };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        
        messageTableModel = (DefaultTableModel) eventTable.getModel();
        
        eventTable.setSelectionMode(0);
        
        eventTable.getColumnExt(MESSAGE_ID_COLUMN_NAME).setVisible(false);
        
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
    
    private void makeMappingsTable(String[][] tableData, boolean cleared)
    {
        if (tableData.length == 0)
        {
            tableData = new String[1][2];
            if (cleared)
                tableData[0][0] = "Please select a message to view mappings.";
            else
                tableData[0][0] = "There are no mappings present.";
            tableData[0][1] = "";
        }
        JXTable mappingsTable = new JXTable();
        
        mappingsTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData,
                new String []
        {
            KEY_COLUMN_NAME, VALUE_COLUMN_NAME
        }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false
            };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        
        mappingsTable.setSelectionMode(0);
        
        mappingsPane.setViewportView(mappingsTable);
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
            parent.messagePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /**
     * Deselects all rows in the table and clears the description information.
     */
    public void deselectRows()
    {
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 4, -1, false);
        parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 5, 5, true);
        if (eventTable != null){
        	eventTable.clearSelection();
        	clearDescription();
        }
    }
    
    /**
     * Clears all description information.
     */
    public void clearDescription()
    {
        RawMessageTextPane.setDocument(new SyntaxDocument());
        RawMessageTextPane.setText("Select a message to view the raw message.");
        TransformedMessageTextPane.setDocument(new SyntaxDocument());
        TransformedMessageTextPane.setText("Select a message to view the transformed message.");
        EncodedMessageTextPane.setDocument(new SyntaxDocument());
        EncodedMessageTextPane.setText("Select a message to view the encoded message.");
        ErrorsTextPane.setDocument(new SyntaxDocument());
        ErrorsTextPane.setText("Select a message to view any errors.");
        makeMappingsTable(new String[0][0], true);
    }
    
    /**
     * An action for when a row is selected in the table.
     */
    private void EventListSelected(ListSelectionEvent evt)
    {
        if (!evt.getValueIsAdjusting())
        {
        	int row = -1;
        	if (eventTable.getSelectedRow() > -1)
            {
        		row = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
            }
            
            if(row >= 0)
            {
                parent.setVisibleTasks(parent.messageTasks, parent.messagePopupMenu, 4, -1, true);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                MessageObject currentMessage = messageObjectList.get(row);
                
                setCorrectDocument(RawMessageTextPane, currentMessage.getRawData(), currentMessage.getRawDataProtocol());
                setCorrectDocument(TransformedMessageTextPane, currentMessage.getTransformedData(), currentMessage.getTransformedDataProtocol());
                setCorrectDocument(EncodedMessageTextPane, currentMessage.getEncodedData(), currentMessage.getEncodedDataProtocol());
                setCorrectDocument(ErrorsTextPane, currentMessage.getErrors(), null);
                
                Map variableMap = currentMessage.getVariableMap();
                Iterator variableMapSetIterator = variableMap.entrySet().iterator();
                String[][] tableData = new String[variableMap.size()][2];
                for (int i=0; variableMapSetIterator.hasNext(); i++)
                {
                    Entry variableMapEntry = (Entry)variableMapSetIterator.next();
                    tableData[i][0] = variableMapEntry.getKey().toString();
                    tableData[i][1] = variableMapEntry.getValue().toString();
                }
                makeMappingsTable(tableData, false);
                
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
    
    private void setCorrectDocument(MirthSyntaxTextArea textPane, String message, MessageObject.Protocol protocol)
    {
        SyntaxDocument newDoc = new SyntaxDocument();
        
        if (message != null)
        {
            if (protocol != null)
            {
                if (protocol.equals(MessageObject.Protocol.HL7))
                {
                    newDoc.setTokenMarker(new HL7TokenMarker());
                    message = message.replace('\r', '\n');
                    //HL7 (ER7) encoded messages have \r as end of line segments
                    //The syntax editor box only recognizes \n
                    //Add \n to make things look normal
                }
                else if (protocol.equals(MessageObject.Protocol.XML))
                    newDoc.setTokenMarker(new XMLTokenMarker());
                else if (protocol.equals(MessageObject.Protocol.X12))
                    newDoc.setTokenMarker(new XMLTokenMarker());
            }
            
            textPane.setDocument(newDoc);
            textPane.setText(message); //TODO: Check newlines
        }
        else
        {
            textPane.setDocument(newDoc);
            textPane.setText("");
        }
        
        textPane.setCaretPosition(0);
    }
    
    /**
     * Returns the ID of the selected message in the table.
     */
    public String getSelectedMessageID()
    {
        int column = -1;
        for (int i = 0; i < messageTableModel.getColumnCount(); i++)
        {
            if (messageTableModel.getColumnName(i).equals(MESSAGE_ID_COLUMN_NAME))
                column = i;
        }
        return ((String)messageTableModel.getValueAt(eventTable.convertRowIndexToModel(eventTable.getSelectedRow()), column));
    }
    
    /**
     * Returns the current MessageObjectFilter that is set.
     */
    public MessageObjectFilter getCurrentFilter()
    {
        return messageObjectFilter;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        filterPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        filterButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mirthDatePicker1 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        mirthDatePicker2 = new com.webreach.mirth.client.ui.components.MirthDatePicker();
        statusComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        previousPageButton = new javax.swing.JButton();
        nextPageButton = new javax.swing.JButton();
        pageSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        connectorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        messageTypeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel7 = new javax.swing.JLabel();
        messageSourceField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jSplitPane1 = new javax.swing.JSplitPane();
        descriptionPanel = new javax.swing.JPanel();
        descriptionTabbedPane = new javax.swing.JTabbedPane();
        RawMessagePanel = new javax.swing.JPanel();
        RawMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        TransformedMessagePanel = new javax.swing.JPanel();
        TransformedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        EncodedMessagePanel = new javax.swing.JPanel();
        EncodedMessageTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        MappingsPanel = new javax.swing.JPanel();
        ErrorsPanel = new javax.swing.JPanel();
        ErrorsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filter By", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel3.setText("Start Date:");

        filterButton.setText("Filter");
        filterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("End Date:");

        jLabel5.setText("Status:");

        previousPageButton.setText("Previous Page");
        previousPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousPageButtonActionPerformed(evt);
            }
        });

        nextPageButton.setText("Next Page");
        nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPageButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Page Size:");

        resultsLabel.setForeground(new java.awt.Color(204, 0, 0));
        resultsLabel.setText("Results");

        jLabel1.setText("Connector:");

        jLabel4.setText("Message Type:");

        jLabel7.setText("Source:");

        org.jdesktop.layout.GroupLayout filterPanelLayout = new org.jdesktop.layout.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(mirthDatePicker1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(messageTypeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(connectorField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(10, 10, 10)
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel2)
                            .add(jLabel7)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(messageSourceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(statusComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(filterButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 19, Short.MAX_VALUE)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(previousPageButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextPageButton))
                    .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, resultsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(filterPanelLayout.createSequentialGroup()
                            .add(jLabel6)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(2, 2, 2))
        );

        filterPanelLayout.linkSize(new java.awt.Component[] {nextPageButton, previousPageButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterPanelLayout.createSequentialGroup()
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel5)
                    .add(connectorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel4)
                        .add(messageTypeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel7)
                        .add(messageSourceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(8, 8, 8)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterPanelLayout.createSequentialGroup()
                        .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(filterPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel3)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mirthDatePicker1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(mirthDatePicker2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(filterButton))
                    .add(jLabel2))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, filterPanelLayout.createSequentialGroup()
                .addContainerGap(38, Short.MAX_VALUE)
                .add(resultsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pageSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nextPageButton)
                    .add(previousPageButton))
                .add(13, 13, 13))
        );

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        descriptionTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descriptionTabbedPane.setFocusable(false);
        RawMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        RawMessagePanel.setFocusable(false);
        RawMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        RawMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout RawMessagePanelLayout = new org.jdesktop.layout.GroupLayout(RawMessagePanel);
        RawMessagePanel.setLayout(RawMessagePanelLayout);
        RawMessagePanelLayout.setHorizontalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RawMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        RawMessagePanelLayout.setVerticalGroup(
            RawMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RawMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(RawMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Raw Message", RawMessagePanel);

        TransformedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        TransformedMessagePanel.setFocusable(false);
        TransformedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        TransformedMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout TransformedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(TransformedMessagePanel);
        TransformedMessagePanel.setLayout(TransformedMessagePanelLayout);
        TransformedMessagePanelLayout.setHorizontalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(TransformedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        TransformedMessagePanelLayout.setVerticalGroup(
            TransformedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(TransformedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(TransformedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Transformed Message", TransformedMessagePanel);

        EncodedMessagePanel.setBackground(new java.awt.Color(255, 255, 255));
        EncodedMessagePanel.setFocusable(false);
        EncodedMessageTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        EncodedMessageTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout EncodedMessagePanelLayout = new org.jdesktop.layout.GroupLayout(EncodedMessagePanel);
        EncodedMessagePanel.setLayout(EncodedMessagePanelLayout);
        EncodedMessagePanelLayout.setHorizontalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(EncodedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        EncodedMessagePanelLayout.setVerticalGroup(
            EncodedMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(EncodedMessagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(EncodedMessageTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Encoded Message", EncodedMessagePanel);

        MappingsPanel.setBackground(new java.awt.Color(255, 255, 255));
        MappingsPanel.setFocusable(false);
        org.jdesktop.layout.GroupLayout MappingsPanelLayout = new org.jdesktop.layout.GroupLayout(MappingsPanel);
        MappingsPanel.setLayout(MappingsPanelLayout);
        MappingsPanelLayout.setHorizontalGroup(
            MappingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 661, Short.MAX_VALUE)
        );
        MappingsPanelLayout.setVerticalGroup(
            MappingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 70, Short.MAX_VALUE)
        );
        descriptionTabbedPane.addTab("Mappings", MappingsPanel);

        ErrorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        ErrorsPanel.setFocusable(false);
        ErrorsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ErrorsTextPane.setEditable(false);

        org.jdesktop.layout.GroupLayout ErrorsPanelLayout = new org.jdesktop.layout.GroupLayout(ErrorsPanel);
        ErrorsPanel.setLayout(ErrorsPanelLayout);
        ErrorsPanelLayout.setHorizontalGroup(
            ErrorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ErrorsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        ErrorsPanelLayout.setVerticalGroup(
            ErrorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ErrorsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(ErrorsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );
        descriptionTabbedPane.addTab("Errors", ErrorsPanel);

        org.jdesktop.layout.GroupLayout descriptionPanelLayout = new org.jdesktop.layout.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(descriptionTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
        );
        jSplitPane1.setBottomComponent(descriptionPanel);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jTable1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        jSplitPane1.setLeftComponent(jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
            .add(filterPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(filterPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void nextPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPageButtonActionPerformed
        parent.setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground()
            {
                makeMessageTable(messageListHandler, NEXT_PAGE);
                return null;
            }
            
            public void done()
            {
                parent. setWorking(false);
            }
        };
        worker.execute();
        
    }//GEN-LAST:event_nextPageButtonActionPerformed
    
    private void previousPageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousPageButtonActionPerformed
        parent.setWorking(true);
        
        SwingWorker worker = new SwingWorker <Void, Void> ()
        {
            public Void doInBackground()
            {
                makeMessageTable(messageListHandler, PREVIOUS_PAGE);
                return null;
            }
            public void done()
            {
                parent. setWorking(false);
            }
        };
        worker.execute();
    }//GEN-LAST:event_previousPageButtonActionPerformed
    
    /**
     * An action when the filter button is pressed.  Creates
     * the actual filter and remakes the table with that filter.
     */
    private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterButtonActionPerformed
        int pageSize = 0;
        
        if (mirthDatePicker1.getDate() != null && mirthDatePicker2.getDate() != null)
        {
            if (mirthDatePicker1.getDateInMillis() > mirthDatePicker2.getDateInMillis())
            {
                JOptionPane.showMessageDialog(parent, "Start date cannot be after the end date.");
                return;
            }
        }
        
        messageObjectFilter = new MessageObjectFilter();
        
        messageObjectFilter.setChannelId(parent.status.get(parent.statusPanel.getSelectedStatus()).getChannelId());
        
        if (!connectorField.getText().equals(""))
            messageObjectFilter.setConnectorName(connectorField.getText());
        if (!messageSourceField.getText().equals(""))
            messageObjectFilter.setSource(messageSourceField.getText());
        if (!messageTypeField.getText().equals(""))
            messageObjectFilter.setType(messageTypeField.getText());
        if (!((String)statusComboBox.getSelectedItem()).equalsIgnoreCase("ALL"))
        {
            for (int i = 0; i < MessageObject.Status.values().length; i++)
            {
                if (((String)statusComboBox.getSelectedItem()).equalsIgnoreCase(MessageObject.Status.values()[i].toString()))
                    messageObjectFilter.setStatus(MessageObject.Status.values()[i]);
            }
        }
        
        if (mirthDatePicker1.getDate() != null)
        {
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTimeInMillis(mirthDatePicker1.getDateInMillis());
            messageObjectFilter.setStartDate(calendarStart);
        }
        if (mirthDatePicker2.getDate() != null)
        {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTimeInMillis(mirthDatePicker2.getDateInMillis());
            messageObjectFilter.setEndDate(calendarEnd);
        }
        
        if (!pageSizeField.getText().equals(""))
            pageSize = Integer.parseInt(pageSizeField.getText());
        
        parent.setWorking(true);
        if (messageListHandler == null)
        {
            makeMessageTable(null, FIRST_PAGE);
            messageListHandler = parent.mirthClient.getMessageListHandler(messageObjectFilter, pageSize);
            makeMessageTable(messageListHandler, FIRST_PAGE);
            parent.setWorking(false);
        }
        else
        {
            class MessageWorker extends SwingWorker<Void, Void>
            {
                protected int pageSize;
                public Void doInBackground()
                {
                    messageListHandler = parent.mirthClient.getMessageListHandler(messageObjectFilter, pageSize);
                    
                    return null;
                }
                
                public void done()
                {
                    makeMessageTable(messageListHandler, FIRST_PAGE);
                    parent.setWorking(false);
                }
            };
            MessageWorker worker = new MessageWorker();
            worker.pageSize = pageSize;
            worker.execute();
        }
        
        
        
    }//GEN-LAST:event_filterButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel EncodedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea EncodedMessageTextPane;
    private javax.swing.JPanel ErrorsPanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea ErrorsTextPane;
    private javax.swing.JPanel MappingsPanel;
    private javax.swing.JPanel RawMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea RawMessageTextPane;
    private javax.swing.JPanel TransformedMessagePanel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea TransformedMessageTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField connectorField;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTabbedPane descriptionTabbedPane;
    private javax.swing.JButton filterButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private com.webreach.mirth.client.ui.components.MirthTextField messageSourceField;
    private com.webreach.mirth.client.ui.components.MirthTextField messageTypeField;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker1;
    private com.webreach.mirth.client.ui.components.MirthDatePicker mirthDatePicker2;
    private javax.swing.JButton nextPageButton;
    private com.webreach.mirth.client.ui.components.MirthTextField pageSizeField;
    private javax.swing.JButton previousPageButton;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JComboBox statusComboBox;
    // End of variables declaration//GEN-END:variables
    
}
