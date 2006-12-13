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

package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.connectors.ConnectorClass;
import com.webreach.mirth.client.ui.connectors.DatabaseReader;
import com.webreach.mirth.client.ui.connectors.DatabaseWriter;
import com.webreach.mirth.client.ui.connectors.FTPReader;
import com.webreach.mirth.client.ui.connectors.FTPWriter;
import com.webreach.mirth.client.ui.connectors.FileReader;
import com.webreach.mirth.client.ui.connectors.FileWriter;
import com.webreach.mirth.client.ui.connectors.JMSReader;
import com.webreach.mirth.client.ui.connectors.JMSWriter;
import com.webreach.mirth.client.ui.connectors.LLPListener;
import com.webreach.mirth.client.ui.connectors.LLPSender;
import com.webreach.mirth.client.ui.connectors.PDFWriter;
import com.webreach.mirth.client.ui.connectors.SFTPReader;
import com.webreach.mirth.client.ui.connectors.SFTPWriter;
import com.webreach.mirth.client.ui.connectors.SOAPListener;
import com.webreach.mirth.client.ui.connectors.SOAPSender;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Transport;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

/** The channel editor panel. Majority of the client application */
public class ChannelSetup extends javax.swing.JPanel
{
    private static final String DESTINATION_DEFAULT_INBOUND = "File Writer";
    
    private static final String DESTINATION_DEFAULT_OUTBOUND = "File Writer";
    
    private static final String SOURCE_DEFAULT_OUTBOUND = "Database Reader";
    
    private static final String SOURCE_DEFAULT_INBOUND = "LLP Listener";
    
    private final String DESTINATION_COLUMN_NAME = "Destination";
    
    private final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    
    private final int SOURCE_TAB_INDEX = 1;
    
    private final int DESTINATIONS_TAB_INDEX = 2;
    
    private final String DATA_TYPE_KEY = "DataType";
    
    public Channel currentChannel;
    
    public String lastIndex = "";
    
    public TransformerPane transformerPane;
    
    public FilterPane filterPane;
    
    private Frame parent;
    
    private boolean isDeleting = false;
    
    private boolean loadingChannel = false;
    
    private JXTable destinationTable;
    
    private JScrollPane destinationPane;
    
    private Map<String, Transport> transports;
    
    private ArrayList<String> sourceConnectorsInbound;
    
    private ArrayList<String> destinationConnectorsInbound;
    
    private ArrayList<String> sourceConnectorsOutbound;
    
    private ArrayList<String> destinationConnectorsOutbound;
    
    private static SyntaxDocument preprocessorDoc;
    /**
     * Creates the Channel Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public ChannelSetup()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        if(parent.sourceConnectors.size() == 0)
        {
            parent.sourceConnectors.add(new DatabaseReader());
            //parent.sourceConnectors.add(new HTTPListener());
            //parent.sourceConnectors.add(new HTTPSListener());
            parent.sourceConnectors.add(new LLPListener());
            parent.sourceConnectors.add(new FileReader());
            parent.sourceConnectors.add(new FTPReader());
            parent.sourceConnectors.add(new SFTPReader());
            parent.sourceConnectors.add(new JMSReader());
            parent.sourceConnectors.add(new SOAPListener());
        }
        if(parent.destinationConnectors.size() == 0)
        {
            parent.destinationConnectors.add(new DatabaseWriter());
            //parent.destinationConnectors.add(new EmailSender());
            parent.destinationConnectors.add(new FileWriter());
            parent.destinationConnectors.add(new LLPSender());
            parent.destinationConnectors.add(new JMSWriter());
            parent.destinationConnectors.add(new FTPWriter());
            parent.destinationConnectors.add(new SFTPWriter());
            parent.destinationConnectors.add(new PDFWriter());
            parent.destinationConnectors.add(new JMSWriter());
            parent.destinationConnectors.add(new SOAPSender());
        }
        
        initComponents();
        preprocessorDoc = new SyntaxDocument();
        preprocessorDoc.setTokenMarker(new JavaScriptTokenMarker());
        preprocessor.setDocument(preprocessorDoc);
        numDays.setDocument(new MirthFieldConstraints(3, false, true));
        
        channelView.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
        });
        destinationPane = new JScrollPane();
        
        try
        {
            transports = this.parent.mirthClient.getTransports();
            sourceConnectorsInbound = new ArrayList<String>();
            sourceConnectorsOutbound = new ArrayList<String>();
            destinationConnectorsInbound = new ArrayList<String>();
            destinationConnectorsOutbound = new ArrayList<String>();
            Iterator i = transports.entrySet().iterator();
            while (i.hasNext())
            {
                Entry entry = (Entry) i.next();
                if (transports.get(entry.getKey()).getType() == Transport.Type.LISTENER
                        && transports.get(entry.getKey()).isInbound())
                {
                    if (entry.getKey().equals(SOURCE_DEFAULT_INBOUND))
                        sourceConnectorsInbound.add(0,transports.get(entry.getKey()).getName());
                    else
                        sourceConnectorsInbound.add(transports.get(entry.getKey()).getName());
                }
                if (transports.get(entry.getKey()).getType() == Transport.Type.LISTENER
                        && transports.get(entry.getKey()).isOutbound())
                {
                    if (entry.getKey().equals(SOURCE_DEFAULT_OUTBOUND))
                        sourceConnectorsOutbound.add(0,transports.get(entry.getKey()).getName());
                    else
                        sourceConnectorsOutbound.add(transports.get(entry.getKey())
                        .getName());
                }
                if (transports.get(entry.getKey()).getType() == Transport.Type.SENDER
                        && transports.get(entry.getKey()).isInbound())
                {
                    if (entry.getKey().equals(DESTINATION_DEFAULT_INBOUND))
                        destinationConnectorsInbound.add(0, transports.get(entry.getKey()).getName());
                    else
                        destinationConnectorsInbound.add(transports.get(entry.getKey()).getName());
                }
                if (transports.get(entry.getKey()).getType() == Transport.Type.SENDER
                        && transports.get(entry.getKey()).isOutbound())
                {
                    if (entry.getKey().equals(DESTINATION_DEFAULT_OUTBOUND))
                        destinationConnectorsOutbound.add(0, transports.get(entry.getKey()).getName());
                    else
                        destinationConnectorsOutbound.add(transports.get(entry.getKey()).getName());
                }
            }
        }
        catch (ClientException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
        channelView.setMaximumSize(new Dimension(450, 3000));
    }
    
    /**
     * Shows the trigger-button popup menu. If the trigger was pressed on a row
     * of the destination table, that row should be selected as well.
     */
    private void showChannelEditPopupMenu(java.awt.event.MouseEvent evt,
            boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = destinationTable.rowAtPoint(new Point(evt.getX(), evt
                        .getY()));
                destinationTable.setRowSelectionInterval(row, row);
            }
            parent.channelEditPopupMenu.show(evt.getComponent(), evt.getX(),
                    evt.getY());
        }
    }
    
    /**
     * Is called to load the transformer pane on either the source or
     * destination
     */
    public void editTransformer()
    {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
            transformerPane.load(currentChannel.getSourceConnector(),
                    currentChannel.getSourceConnector().getTransformer());
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            if (currentChannel.getMode() == Channel.Mode.APPLICATION)
                transformerPane.load(currentChannel.getDestinationConnectors()
                .get(0), currentChannel.getDestinationConnectors().get(
                        0).getTransformer());
            else
            {
                int destination = getDestinationConnectorIndex((String) destinationTable
                        .getValueAt(getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME)));
                transformerPane.load(currentChannel.getDestinationConnectors()
                .get(destination), currentChannel
                        .getDestinationConnectors().get(destination)
                        .getTransformer());
            }
        }
    }
    
    /** Is called to load the filter pane on either the source or destination */
    public void editFilter()
    {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
            filterPane.load(currentChannel.getSourceConnector(), currentChannel
                    .getSourceConnector().getFilter());
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            if (currentChannel.getMode() == Channel.Mode.APPLICATION)
                filterPane.load(currentChannel.getDestinationConnectors()
                .get(0), currentChannel.getDestinationConnectors().get(
                        0).getFilter());
            else
            {
                int destination = getDestinationConnectorIndex((String) destinationTable
                        .getValueAt(getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME)));
                filterPane.load(currentChannel.getDestinationConnectors().get(
                        destination), currentChannel.getDestinationConnectors()
                        .get(destination).getFilter());
            }
        }
    }
    
    /**
     * Makes the destination table with a parameter that is true if a new
     * destination should be added as well.
     */
    public void makeDestinationTable(boolean addNew)
    {
        List<Connector> destinationConnectors;
        Object[][] tableData;
        int tableSize;
        
        destinationConnectors = currentChannel.getDestinationConnectors();
        tableSize = destinationConnectors.size();
        if (addNew)
            tableSize++;
        tableData = new Object[tableSize][2];
        for (int i = 0; i < tableSize; i++)
        {
            if (tableSize - 1 == i && addNew)
            {
                Connector connector = makeNewConnector();
                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName((String) destinationSourceDropdown
                        .getItemAt(0));
                
                tableData[i][0] = connector.getName();
                tableData[i][1] = connector.getTransportName();
                
                destinationConnectors.add(connector);
            }
            else
            {
                tableData[i][0] = destinationConnectors.get(i).getName();
                tableData[i][1] = destinationConnectors.get(i)
                .getTransportName();
            }
        }
        
        destinationTable = new JXTable();
        
        destinationTable.setModel(new javax.swing.table.DefaultTableModel(
                tableData, new String[] { DESTINATION_COLUMN_NAME,
                CONNECTOR_TYPE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, false };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        
        // Set the custom cell editor for the Destination Name column.
        destinationTable.getColumnModel().getColumn(
                destinationTable.getColumnModel().getColumnIndex(
                DESTINATION_COLUMN_NAME)).setCellEditor(
                new DestinationTableCellEditor());
        
        destinationTable.setSelectionMode(0);
        destinationTable.setRowSelectionAllowed(true);
        destinationTable.setRowHeight(UIConstants.ROW_HEIGHT);
        destinationTable.setFocusable(false); // Need to figure a way to make
        // the arrow keys work here
        // because the pane that shows
        // up steals the focus
        destinationTable.setSortable(false);
        
        destinationTable.setOpaque(true);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean(
                "highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter
                    .addHighlighter(new AlternateRowHighlighter(
                    UIConstants.HIGHLIGHTER_COLOR,
                    UIConstants.BACKGROUND_COLOR,
                    UIConstants.TITLE_TEXT_COLOR));
            ((JXTable) destinationTable).setHighlighters(highlighter);
        }
        
        // This action is called when a new selection is made on the destination
        // table.
        destinationTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!evt.getValueIsAdjusting())
                {
                    int last = getDestinationIndex(lastIndex);
                    if (last != -1
                            && last != destinationTable.getRowCount()
                            && !isDeleting)
                    {
                        int connectorIndex = getDestinationConnectorIndex((String) destinationTable
                                .getValueAt(
                                last,
                                getColumnNumber(DESTINATION_COLUMN_NAME)));
                        Connector destinationConnector = currentChannel
                                .getDestinationConnectors().get(
                                connectorIndex);
                        destinationConnector
                                .setProperties(destinationConnectorClass
                                .getProperties());
                    }
                    
                    if (!loadConnector())
                    {
                        if (getDestinationIndex(lastIndex) == destinationTable
                                .getRowCount())
                            destinationTable.setRowSelectionInterval(
                                    last - 1, last - 1);
                        else
                            destinationTable.setRowSelectionInterval(
                                    last, last);
                    }
                    else
                    {
                        lastIndex = ((String) destinationTable
                                .getValueAt(
                                getSelectedDestinationIndex(),
                                getColumnNumber(DESTINATION_COLUMN_NAME)));
                    }
                    
                    checkVisibleDestinationTasks();
                }
            }
        });
        
        // Mouse listener for trigger-button popup on the table.
        destinationTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, true);
            }
        });
        
        // Checks to see what to set the new row selection to based on
        // last index and if a new destination was added.
        int last = getDestinationIndex(lastIndex);
        if (addNew)
            destinationTable.setRowSelectionInterval(destinationTable
                    .getRowCount() - 1, destinationTable.getRowCount() - 1);
        else if (last == -1)
            destinationTable.setRowSelectionInterval(0, 0); // Makes sure the
        // event is called
        // when the table is
        // created.
        else if (last == destinationTable.getRowCount())
            destinationTable.setRowSelectionInterval(last - 1, last - 1);
        else
            destinationTable.setRowSelectionInterval(last, last);
        destinationPane.setViewportView(destinationTable);
        
        // Mouse listener for trigger-button popup on the table pane (not actual
        // table).
        destinationPane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showChannelEditPopupMenu(evt, false);
            }
        });
        //Key Listener trigger for CTRL-S
        destinationTable.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doSaveChanges();
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
    
    /** Get the index of a destination by being passed its name. */
    private int getDestinationIndex(String destinationName)
    {
        for (int i = 0; i < destinationTable.getRowCount(); i++)
        {
            if (((String) destinationTable.getValueAt(i,
                    getColumnNumber(DESTINATION_COLUMN_NAME)))
                    .equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }
    
    /**
     * Get the name that should be used for a new destination so that it is
     * unique.
     */
    private String getNewDestinationName(int size)
    {
        String temp = "Destination ";
        
        for (int i = 1; i <= size; i++)
        {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++)
            {
                if (((String) destinationTable.getValueAt(j,
                        getColumnNumber(DESTINATION_COLUMN_NAME)))
                        .equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if (!exists)
                return temp + i;
        }
        return "";
    }
    
    /** Get the column index number based on its name. */
    private int getColumnNumber(String name)
    {
        for (int i = 0; i < destinationTable.getColumnCount(); i++)
        {
            if (destinationTable.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }
    
    /** Get the currently selected destination index */
    public int getSelectedDestinationIndex()
    {
        if (destinationTable.isEditing())
            return destinationTable.getEditingRow();
        else
            return destinationTable.getSelectedRow();
    }
    
    /** Get a destination connector index by passing in its name */
    private int getDestinationConnectorIndex(String destinationName)
    {
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        for (int i = 0; i < destinationConnectors.size(); i++)
        {
            if (destinationConnectors.get(i).getName().equalsIgnoreCase(
                    destinationName))
                return i;
        }
        return -1;
    }
    
    /** Loads a selected connector and returns true on success. */
    public boolean loadConnector()
    {
        List<Connector> destinationConnectors;
        String destinationName;
        
        if (getSelectedDestinationIndex() != -1)
            destinationName = (String) destinationTable.getValueAt(
                    getSelectedDestinationIndex(),
                    getColumnNumber(DESTINATION_COLUMN_NAME));
        else
            return false;
        
        if (currentChannel != null
                && currentChannel.getDestinationConnectors() != null)
        {
            destinationConnectors = currentChannel.getDestinationConnectors();
            for (int i = 0; i < destinationConnectors.size(); i++)
            {
                if (destinationConnectors.get(i).getName().equalsIgnoreCase(
                        destinationName))
                {
                    boolean visible = parent.channelEditTasks.getContentPane()
                    .getComponent(0).isVisible();
                    destinationSourceDropdown
                            .setSelectedItem(destinationConnectors.get(i)
                            .getTransportName());
                    parent.channelEditTasks.getContentPane().getComponent(0)
                    .setVisible(visible);
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Sets the overall panel to edit the channel with the given channel index. */
    public void editChannel(Channel channel)
    {
        loadingChannel = true;
        lastIndex = "";
        currentChannel = channel;
        
        checkPropertyValidity(currentChannel.getSourceConnector(),
                parent.sourceConnectors);
        
        /*
         * Once application integration is added clean up this code
         */
        if (currentChannel.getMode() == Channel.Mode.ROUTER
                || currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            List<Connector> destinations = currentChannel
                    .getDestinationConnectors();
            for (int i = 0; i < destinations.size(); i++)
            {
                checkPropertyValidity(destinations.get(i),
                        parent.destinationConnectors);
            }
        }
        else
        {
            checkPropertyValidity(currentChannel.getDestinationConnectors()
            .get(0), parent.destinationConnectors);
        }
        
        if (currentChannel.getDirection() == Channel.Direction.INBOUND)
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(
                    sourceConnectorsInbound.toArray()));
            destinationSourceDropdown
                    .setModel(new javax.swing.DefaultComboBoxModel(
                    destinationConnectorsInbound.toArray()));
        }
        else
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(
                    sourceConnectorsOutbound.toArray()));
            destinationSourceDropdown
                    .setModel(new javax.swing.DefaultComboBoxModel(
                    destinationConnectorsOutbound.toArray()));
        }
        
        loadChannelInfo();
        
        if (currentChannel.getMode() == Channel.Mode.ROUTER
                || currentChannel.getMode() == Channel.Mode.BROADCAST)
            makeDestinationTable(false);
        else
            generateSingleDestinationPage();
        
        setDestinationVariableList();
        loadingChannel = false;
        
        channelView.setSelectedIndex(0);
    }
    
    /**
     * Adds a new channel that is passed in and then sets the overall panel to
     * edit that channel.
     */
    public void addChannel(Channel channel)
    {
        loadingChannel = true;
        lastIndex = "";
        currentChannel = channel;
        
        if (currentChannel.getDirection() == Channel.Direction.INBOUND)
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(
                    sourceConnectorsInbound.toArray()));
            destinationSourceDropdown
                    .setModel(new javax.swing.DefaultComboBoxModel(
                    destinationConnectorsInbound.toArray()));
        }
        else
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(
                    sourceConnectorsOutbound.toArray()));
            destinationSourceDropdown
                    .setModel(new javax.swing.DefaultComboBoxModel(
                    destinationConnectorsOutbound.toArray()));
        }
        
        Connector sourceConnector = makeNewConnector();
        sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName((String) sourceSourceDropdown
                .getItemAt(0));
        currentChannel.setSourceConnector(sourceConnector);
        
        if (currentChannel.getMode() == Channel.Mode.APPLICATION)
        {
            List<Connector> dc;
            dc = currentChannel.getDestinationConnectors();
            
            Connector c = makeNewConnector();
            c.setName("Destination");
            c.setTransportName((String) destinationSourceDropdown.getItemAt(0));
            dc.add(c);
        }
        
        loadChannelInfo();
        
        if (currentChannel.getMode() == Channel.Mode.ROUTER
                || currentChannel.getMode() == Channel.Mode.BROADCAST)
            makeDestinationTable(true);
        else
            generateSingleDestinationPage();
        
        setDestinationVariableList();
        loadingChannel = false;
        channelView.setSelectedIndex(0);
        parent.enableSave();
    }
    
    /** Load all of the saved channel information into the channel editor */
    private void loadChannelInfo()
    {
        parent.setPanelName("Edit Channel :: " +  currentChannel.getName());
        summaryNameField.setText(currentChannel.getName());
        summaryDescriptionText.setText(currentChannel.getDescription());
        if (currentChannel.getDirection().equals(Channel.Direction.INBOUND))
            summaryDirectionLabel2.setText("Inbound");
        else if (currentChannel.getDirection().equals(
                Channel.Direction.OUTBOUND))
        {
            summaryDirectionLabel2.setText("Outbound");
            currentChannel.setMode(Channel.Mode.ROUTER);
        }
        
        if (currentChannel.getMode().equals(Channel.Mode.APPLICATION))
            summaryPatternLabel2.setText("Application");
        else if (currentChannel.getMode().equals(Channel.Mode.BROADCAST))
            summaryPatternLabel2.setText("Broadcast");
        else if (currentChannel.getMode().equals(Channel.Mode.ROUTER))
            summaryPatternLabel2.setText("Router");
        
        if (currentChannel.isEnabled())
            summaryEnabledCheckbox.setSelected(true);
        else
            summaryEnabledCheckbox.setSelected(false);
        
        if(currentChannel.getPreprocessingScript() != null)
            preprocessor.setText(currentChannel.getPreprocessingScript());
        else
            preprocessor.setText("// Modify the message variable below to pre process data\r\nreturn message;");
        
        if (((String) currentChannel.getProperties().get("recv_xml_encoded")) != null
                && ((String) currentChannel.getProperties().get(
                "recv_xml_encoded")).equalsIgnoreCase("true"))
            xmlPreEncoded.setSelected(true);
        else
            xmlPreEncoded.setSelected(false);
        
        if(((String)currentChannel.getProperties().get("transactional")) != null && ((String)currentChannel.getProperties().get("transactional")).equalsIgnoreCase("true"))
            transactionalCheckBox.setSelected(true);
        else
            transactionalCheckBox.setSelected(false);
        
        if (((String) currentChannel.getProperties().get("encryptData")) != null
                && ((String) currentChannel.getProperties().get("encryptData"))
                .equalsIgnoreCase("true"))
            encryptMessagesCheckBox.setSelected(true);
        else
            encryptMessagesCheckBox.setSelected(false);
        
        if (currentChannel.getProtocol() == null)
            currentChannel.setProtocol(Channel.Protocol.HL7);
        
        if (((String) currentChannel.getProperties().get("store_messages")) != null
                && ((String) currentChannel.getProperties().get(
                "store_messages")).equalsIgnoreCase("false"))
        {
            storeMessages.setSelected(false);
            storeMessagesAll.setEnabled(false);
            storeMessagesAll.setSelected(true);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
            days.setEnabled(false);
        }
        else
        {
            storeMessages.setSelected(true);
            
            if (((String) currentChannel.getProperties().get(
                    "error_messages_only")) != null
                    && ((String) currentChannel.getProperties().get(
                    "error_messages_only")).equalsIgnoreCase("true"))
                storeMessagesErrors.setSelected(true);
            else
                storeMessagesErrors.setSelected(false);
            
            if (((String) currentChannel.getProperties().get("max_message_age")) != null
                    && !((String) currentChannel.getProperties().get(
                    "max_message_age")).equalsIgnoreCase("-1"))
            {
                numDays.setText((String) currentChannel.getProperties().get(
                        "max_message_age"));
                storeMessagesDays.setSelected(true);
                numDays.setEnabled(true);
            }
            else
            {
                storeMessagesAll.setSelected(true);
                numDays.setText("");
            }
        }
        
        boolean visible = parent.channelEditTasks.getContentPane()
        .getComponent(0).isVisible();
        
        sourceSourceDropdown.setSelectedItem(currentChannel
                .getSourceConnector().getTransportName());
        
        if (currentChannel.getMode().equals(Channel.Mode.APPLICATION))
        {
            destinationSourceDropdown.setSelectedItem(currentChannel
                    .getDestinationConnectors().get(0).getTransportName());
        }
        
        if (((String) currentChannel.getProperties().get("initialState")) != null
                && ((String) currentChannel.getProperties().get("initialState"))
                .equalsIgnoreCase("started"))
            initialState.setSelectedItem("Started");
        else
            initialState.setSelectedItem("Stopped");
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(
                visible);
    }
    
    /**
     * Save all of the current channel information in the editor to the actual
     * channel
     */
    public boolean saveChanges()
    {
        if (currentChannel.getDirection() == Channel.Direction.OUTBOUND){
        	Iterator<Connector> it = currentChannel.getDestinationConnectors().iterator();
        	boolean templateExists = true;
        	while (it.hasNext() && templateExists){
        		Connector destinationConnector = it.next();
        		if (destinationConnector.getTransformer().getTemplate() == null || destinationConnector.getTransformer().getTemplate().length() == 0){
        			parent.alertWarning("This channel has a blank transformer template.");
        			templateExists = false;
        		}
        	}
    	}
        if (summaryNameField.getText().equals(""))
        {
            JOptionPane.showMessageDialog(parent,
                    "Channel name cannot be empty.");
            return false;
        }
        
        if (!currentChannel.getName().equals(summaryNameField.getText()))
        {
            if (!parent.checkChannelName(summaryNameField.getText()))
                return false;
        }
        
        boolean enabled = summaryEnabledCheckbox.isSelected();
        
        currentChannel.getSourceConnector().setProperties(
                sourceConnectorClass.getProperties());
        
        if (parent.currentContentPage == transformerPane)
        {
            transformerPane.accept(false);
            transformerPane.modified = false; //TODO: Check this. Fix to prevent double save on confirmLeave
        }
        if (parent.currentContentPage == filterPane)
        {
            filterPane.accept(false);
            filterPane.modified = false; //TODO: Check this. Fix to prevent double save on confirmLeave
        }
        
        Connector temp;
        if (currentChannel.getMode() == Channel.Mode.APPLICATION)
            temp = currentChannel.getDestinationConnectors().get(0);
        else
            temp = currentChannel.getDestinationConnectors().get(
                    getDestinationConnectorIndex((String) destinationTable
                    .getValueAt(getSelectedDestinationIndex(),
                    getColumnNumber(DESTINATION_COLUMN_NAME))));
        temp.setProperties(destinationConnectorClass.getProperties());
        
        if (checkAllForms(currentChannel))
        {
            enabled = false;

            if (!parent
                    .alertOption("There was a problem with one or more of your connectors.  Please validate all of\nyour connectors to find the problem. Would you still like to save this channel even\nthough you will not be able to enable this channel until you fix the problem(s)?"))
                return false;
            else
                summaryEnabledCheckbox.setSelected(false);
        }
        
        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());
        currentChannel.setEnabled(enabled);
        
        currentChannel.setPreprocessingScript(preprocessor.getText());
        
        if (xmlPreEncoded.isSelected())
            currentChannel.getProperties().put("recv_xml_encoded", "true");
        else
            currentChannel.getProperties().put("recv_xml_encoded", "false");
        
        if(transactionalCheckBox.isSelected())
            currentChannel.getProperties().put("transactional", "true");
        else
            currentChannel.getProperties().put("transactional", "false");
        
        
        if (encryptMessagesCheckBox.isSelected())
            currentChannel.getProperties().put("encryptData", "true");
        else
            currentChannel.getProperties().put("encryptData", "false");
        
        if (storeMessages.isSelected())
        {
            currentChannel.getProperties().put("store_messages", "true");
            if (storeMessagesAll.isSelected())
                currentChannel.getProperties().put("max_message_age", "-1");
            else
                currentChannel.getProperties().put("max_message_age",
                        numDays.getText());
            
            if (storeMessagesErrors.isSelected())
                currentChannel.getProperties().put("error_messages_only",
                        "true");
            else
                currentChannel.getProperties().put("error_messages_only",
                        "false");
        }
        else
        {
            currentChannel.getProperties().put("store_messages", "false");
            currentChannel.getProperties().put("max_message_age", "-1");
        }
        
        if (((String) initialState.getSelectedItem())
        .equalsIgnoreCase("Stopped"))
            currentChannel.getProperties().put("initialState", "stopped");
        else
            currentChannel.getProperties().put("initialState", "started");
        
        boolean updated = true;
        
        try
        {
            if(!parent.channels.containsKey(currentChannel.getId()))
                currentChannel.setId(parent.mirthClient.getGuid());

            updated = parent.updateChannel(currentChannel);
            currentChannel = parent.channels.get(currentChannel.getId());
            parent.channelPanel.makeChannelTable();
        }
        catch (ClientException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
        return updated;
    }
    
    /** Adds a new destination. */
    public void addNewDestination()
    {
        makeDestinationTable(true);
        destinationPane.getViewport().setViewPosition(new Point(0, destinationTable.getRowHeight()*destinationTable.getRowCount()));
        parent.enableSave();
    }
    
    /** Deletes the selected destination. */
    public void deleteDestination()
    {
        isDeleting = true;
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        if (destinationConnectors.size() == 1)
        {
            JOptionPane.showMessageDialog(parent,
                    "You must have at least one destination.");
            return;
        }
        
        destinationConnectors
                .remove(getDestinationConnectorIndex((String) destinationTable
                .getValueAt(getSelectedDestinationIndex(),
                getColumnNumber(DESTINATION_COLUMN_NAME))));
        makeDestinationTable(false);
        parent.enableSave();
        isDeleting = false;
    }
    
    /**
     * Checks to see which tasks, move up and move down, should be available for
     * destinations and enables or disables them.
     */
    public void checkVisibleDestinationTasks()
    {
        if (channelView.getSelectedComponent() == destination)
        {
            if (getSelectedDestinationIndex() == 0)
                parent.setVisibleTasks(parent.channelEditTasks,
                        parent.channelEditPopupMenu, 4, 4, false);
            else
                parent.setVisibleTasks(parent.channelEditTasks,
                        parent.channelEditPopupMenu, 4, 4, true);
            
            if (getSelectedDestinationIndex() == destinationTable.getRowCount() - 1)
                parent.setVisibleTasks(parent.channelEditTasks,
                        parent.channelEditPopupMenu, 5, 5, false);
            else
                parent.setVisibleTasks(parent.channelEditTasks,
                        parent.channelEditPopupMenu, 5, 5, true);
        }
    }
    
    /**
     * Moves the selected destination to the previous spot in the array list.
     */
    public void moveDestinationUp()
    {
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        int destinationIndex = getSelectedDestinationIndex();
        
        destinationConnectors.add(destinationIndex - 1, destinationConnectors
                .get(destinationIndex));
        destinationConnectors.remove(destinationIndex + 1);
        
        makeDestinationTable(false);
        setDestinationVariableList();
        parent.enableSave();
    }
    
    /**
     * Moves the selected destination to the next spot in the array list.
     */
    public void moveDestinationDown()
    {
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        int destinationIndex = getSelectedDestinationIndex();
        
        destinationConnectors.add(destinationIndex + 2, destinationConnectors
                .get(destinationIndex));
        destinationConnectors.remove(destinationIndex);
        
        makeDestinationTable(false);
        setDestinationVariableList();
        parent.enableSave();
    }
    
    public boolean checkAllForms(Channel channel)
    {
        boolean problemFound = false;
        ConnectorClass tempConnector = null;
        Properties tempProps = null;
        
        for (int i = 0; i < channel.getDestinationConnectors().size(); i++)
        {
            for (int j = 0; j < parent.destinationConnectors.size(); j++)
            {
                if (parent.destinationConnectors.get(j).getName()
                .equalsIgnoreCase(
                        channel.getDestinationConnectors().get(i)
                        .getTransportName()))
                {
                    tempConnector = parent.destinationConnectors.get(j);
                    tempProps = channel.getDestinationConnectors().get(i)
                    .getProperties();
                }
            }
            if (tempConnector != null
                    && !tempConnector.checkProperties(tempProps))
                problemFound = true;
            
            tempConnector = null;
            tempProps = null;
        }
        
        for (int i = 0; i < parent.sourceConnectors.size(); i++)
        {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase(
                    channel.getSourceConnector().getTransportName()))
            {
                tempConnector = parent.sourceConnectors.get(i);
                tempProps = channel.getSourceConnector().getProperties();
            }
        }
        if (tempConnector != null && !tempConnector.checkProperties(tempProps))
            problemFound = true;
        
        return problemFound;
    }
    
    public void validateForm()
    {
        if (source.isVisible())
        {
            if (!sourceConnectorClass.checkProperties(sourceConnectorClass
                    .getProperties()))
                parent.alertWarning("This form is missing required data.");
            else
                parent.alertInformation("The form was successfully validated.");
        }
        else
        {
            if (!destinationConnectorClass
                    .checkProperties(destinationConnectorClass.getProperties()))
                parent.alertWarning("This form is missing required data.");
            else
                parent.alertInformation("The form was successfully validated.");
        }
    }
    
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        filterButtonGroup = new javax.swing.ButtonGroup();
        validationButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        channelView = new javax.swing.JTabbedPane();
        summary = new javax.swing.JPanel();
        summaryNameLabel = new javax.swing.JLabel();
        summaryDescriptionLabel = new javax.swing.JLabel();
        summaryNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        summaryDirectionLabel1 = new javax.swing.JLabel();
        summaryDirectionLabel2 = new javax.swing.JLabel();
        summaryPatternLabel1 = new javax.swing.JLabel();
        summaryPatternLabel2 = new javax.swing.JLabel();
        summaryEnabledCheckbox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        xmlPreEncoded = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        summaryDescriptionText = new com.webreach.mirth.client.ui.components.MirthTextPane();
        jLabel1 = new javax.swing.JLabel();
        initialState = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encryptMessagesCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        storeMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        storeMessagesAll = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        storeMessagesDays = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        days = new javax.swing.JLabel();
        numDays = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel3 = new javax.swing.JLabel();
        storeMessagesErrors = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        transactionalCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        preprocessor = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        jLabel2 = new javax.swing.JLabel();
        source = new javax.swing.JPanel();
        sourceSourceDropdown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        sourceSourceLabel = new javax.swing.JLabel();
        sourceConnectorClass = new com.webreach.mirth.client.ui.connectors.ConnectorClass();
        destination = new javax.swing.JPanel();
        destinationSourceDropdown = new com.webreach.mirth.client.ui.components.MirthComboBox();
        destinationSourceLabel = new javax.swing.JLabel();
        destinationConnectorClass = new com.webreach.mirth.client.ui.connectors.ConnectorClass();
        destinationVariableList = new com.webreach.mirth.client.ui.VariableList();

        channelView.setFocusable(false);
        summary.setBackground(new java.awt.Color(255, 255, 255));
        summary.setFocusable(false);
        summary.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                summaryComponentShown(evt);
            }
        });

        summaryNameLabel.setText("Channel Name:");

        summaryDescriptionLabel.setText("Description:");

        summaryDirectionLabel1.setText("Direction:");

        summaryDirectionLabel2.setText("Outbound");

        summaryPatternLabel1.setText("Pattern:");

        summaryPatternLabel2.setText("Application Integration");

        summaryEnabledCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryEnabledCheckbox.setSelected(true);
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        xmlPreEncoded.setBackground(new java.awt.Color(255, 255, 255));
        xmlPreEncoded.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        xmlPreEncoded.setText("Channel will receive XML pre-encoded HL7 messages");
        xmlPreEncoded.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jScrollPane1.setViewportView(summaryDescriptionText);

        jLabel1.setText("Initial State:");

        initialState.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Started", "Stopped" }));

        encryptMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        encryptMessagesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptMessagesCheckBox.setText("Encrypt messages in database");
        encryptMessagesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        storeMessages.setBackground(new java.awt.Color(255, 255, 255));
        storeMessages.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessages.setText("Store messages");
        storeMessages.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesActionPerformed(evt);
            }
        });

        storeMessagesAll.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesAll);
        storeMessagesAll.setText("Store indefinitely");
        storeMessagesAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesAllActionPerformed(evt);
            }
        });

        storeMessagesDays.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesDays.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(storeMessagesDays);
        storeMessagesDays.setText("Store for ");
        storeMessagesDays.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesDaysActionPerformed(evt);
            }
        });

        days.setText("day(s)");

        jLabel3.setText("Messages:");

        storeMessagesErrors.setBackground(new java.awt.Color(255, 255, 255));
        storeMessagesErrors.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        storeMessagesErrors.setText("With errors only");
        storeMessagesErrors.setMargin(new java.awt.Insets(0, 0, 0, 0));
        storeMessagesErrors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeMessagesErrorsActionPerformed(evt);
            }
        });

        transactionalCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        transactionalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transactionalCheckBox.setText("Transactional Endpoints");
        transactionalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        preprocessor.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setText("Preprocessing Script:");

        org.jdesktop.layout.GroupLayout summaryLayout = new org.jdesktop.layout.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(summaryPatternLabel1)
                    .add(summaryDirectionLabel1)
                    .add(summaryNameLabel)
                    .add(jLabel3)
                    .add(summaryDescriptionLabel)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(storeMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(encryptMessagesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryLayout.createSequentialGroup()
                        .add(35, 35, 35)
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(storeMessagesAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryLayout.createSequentialGroup()
                                .add(storeMessagesDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(4, 4, 4)
                                .add(numDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(days))
                            .add(storeMessagesErrors, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryPatternLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryDirectionLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(initialState, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(46, 46, 46)
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(transactionalCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(xmlPreEncoded, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(preprocessor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                    .add(summaryLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(summaryNameLabel))
                    .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryDirectionLabel1)
                    .add(summaryDirectionLabel2)
                    .add(xmlPreEncoded, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryPatternLabel1)
                    .add(summaryPatternLabel2)
                    .add(transactionalCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(initialState, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(encryptMessagesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(storeMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(storeMessagesErrors, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(storeMessagesAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(days)
                    .add(storeMessagesDays, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryDescriptionLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(preprocessor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                .addContainerGap())
        );
        channelView.addTab("Summary", summary);

        source.setBackground(new java.awt.Color(255, 255, 255));
        source.setFocusable(false);
        source.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                sourceComponentShown(evt);
            }
        });

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        sourceSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceSourceLabel.setText("Connector Type:");

        org.jdesktop.layout.GroupLayout sourceConnectorClassLayout = new org.jdesktop.layout.GroupLayout(sourceConnectorClass);
        sourceConnectorClass.setLayout(sourceConnectorClassLayout);
        sourceConnectorClassLayout.setHorizontalGroup(
            sourceConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 634, Short.MAX_VALUE)
        );
        sourceConnectorClassLayout.setVerticalGroup(
            sourceConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 441, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout sourceLayout = new org.jdesktop.layout.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sourceConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(sourceLayout.createSequentialGroup()
                        .add(sourceSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        sourceLayout.setVerticalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceSourceLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Source", source);

        destination.setBackground(new java.awt.Color(255, 255, 255));
        destination.setFocusable(false);
        destination.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                destinationComponentShown(evt);
            }
        });

        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        destinationSourceDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        destinationSourceLabel.setText("Connector Type:");

        org.jdesktop.layout.GroupLayout destinationConnectorClassLayout = new org.jdesktop.layout.GroupLayout(destinationConnectorClass);
        destinationConnectorClass.setLayout(destinationConnectorClassLayout);
        destinationConnectorClassLayout.setHorizontalGroup(
            destinationConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 442, Short.MAX_VALUE)
        );
        destinationConnectorClassLayout.setVerticalGroup(
            destinationConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 441, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout destinationLayout = new org.jdesktop.layout.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                        .add(destinationConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationVariableList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(destinationLayout.createSequentialGroup()
                        .add(destinationSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(destinationVariableList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
                .addContainerGap())
        );
        channelView.addTab("Destinations", destination);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void storeMessagesErrorsActionPerformed(
            java.awt.event.ActionEvent evt)
    {
        // TODO add your handling code here:
    }
    
    private void storeMessagesDaysActionPerformed(java.awt.event.ActionEvent evt)
    {
        numDays.setEnabled(true);
    }
    
    private void storeMessagesAllActionPerformed(java.awt.event.ActionEvent evt)
    {
        numDays.setEnabled(false);
        numDays.setText("");
    }
    
    private void storeMessagesActionPerformed(java.awt.event.ActionEvent evt)
    {
        if (storeMessages.isSelected())
        {
            storeMessagesAll.setEnabled(true);
            storeMessagesDays.setEnabled(true);
            storeMessagesErrors.setEnabled(true);
            days.setEnabled(true);
        }
        else
        {
            storeMessagesAll.setEnabled(false);
            storeMessagesDays.setEnabled(false);
            storeMessagesErrors.setEnabled(false);
            days.setEnabled(false);
            numDays.setText("");
            numDays.setEnabled(false);
        }
    }
    
    /** Action when the summary tab is shown. */
    private void summaryComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_summaryComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks,
                parent.channelEditPopupMenu, 1, 7, false);
    }
    
    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_sourceComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks,
                parent.channelEditPopupMenu, 1, 1, true);
        if (currentChannel.getMode() == Channel.Mode.ROUTER)
        {
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 2, 7, false);
        }
        else
        {
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 2, 5, false);
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 6, 7, true);
        }
    }
    
    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(java.awt.event.ComponentEvent evt)// GEN-FIRST:event_destinationComponentShown
    {
        parent.setVisibleTasks(parent.channelEditTasks,
                parent.channelEditPopupMenu, 1, 1, true);
        if (currentChannel.getMode() == Channel.Mode.APPLICATION)
        {
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 2, 5, false);
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 6, 7, true);
        }
        else if (currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 2, 3, true);
            checkVisibleDestinationTasks();
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 6, 7, false);
        }
        else
        {
            parent.setVisibleTasks(parent.channelEditTasks,
                    parent.channelEditPopupMenu, 2, 7, true);
            checkVisibleDestinationTasks();
        }
    }
    
    /** Action when an action is performed on the source connector type dropdown. */
    private void sourceSourceDropdownActionPerformed(
            java.awt.event.ActionEvent evt)
    {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel)
        {
            if (sourceConnectorClass.getName() != null
                    && sourceConnectorClass.getName().equals(
                    (String) sourceSourceDropdown.getSelectedItem()))
                return;
            
            if (!compareProps(sourceConnectorClass.getProperties(),
                    sourceConnectorClass.getDefaults())
                    || currentChannel.getSourceConnector().getFilter()
                    .getRules().size() > 0
                    || currentChannel.getSourceConnector().getTransformer()
                    .getSteps().size() > 0)
            {
                boolean changeType = parent
                        .alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType)
                {
                    sourceSourceDropdown.setSelectedItem(sourceConnectorClass
                            .getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }
        
        // Get the selected source connector and set it.
        for (int i = 0; i < parent.sourceConnectors.size(); i++)
        {
            if (parent.sourceConnectors.get(i).getName().equalsIgnoreCase(
                    (String) sourceSourceDropdown.getSelectedItem()))
            {
                sourceConnectorClass = parent.sourceConnectors.get(i);
            }
        }
        
        // Sets all of the properties, transformer, filter, etc. on the new
        // source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if (sourceConnector != null)
        {
            String dataType = sourceConnector.getProperties().getProperty(
                    DATA_TYPE_KEY);
            if (dataType == null)
                dataType = "";
            
            if (sourceConnector.getProperties().size() == 0
                    || !dataType.equals((String) sourceSourceDropdown
                    .getSelectedItem()))
            {
                String name = sourceConnector.getName();
                changeConnectorType(sourceConnector);
                sourceConnector.setName(name);
                sourceConnectorClass.setProperties(sourceConnectorClass
                        .getDefaults());
                sourceConnector.setProperties(sourceConnectorClass
                        .getProperties());
            }
            
            sourceConnector.setTransportName((String) sourceSourceDropdown
                    .getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);
            sourceConnectorClass.setProperties(sourceConnector.getProperties());
        }
        
        source.removeAll();
        
        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout sourceLayout = (org.jdesktop.layout.GroupLayout) source
                .getLayout();
        sourceLayout
                .setHorizontalGroup(sourceLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                sourceLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                sourceLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                sourceConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .add(
                sourceLayout
                .createSequentialGroup()
                .add(
                sourceSourceLabel)
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                sourceSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                110,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap()));
        sourceLayout
                .setVerticalGroup(sourceLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                sourceLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                sourceLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.BASELINE)
                .add(
                sourceSourceLabel,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                15,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(
                sourceSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                sourceConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .addContainerGap()));
        
        source.updateUI();
    }
    
    /**
     * Action when an action is performed on the destination connector type
     * dropdown. Fires off either generateMultipleDestinationPage() or
     * generateSingleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(
            java.awt.event.ActionEvent evt)
    {
        // If a channel is not being loaded then alert the user when necessary
        // that
        // changing the connector type will lose all current connector data.
        if (currentChannel.getMode() == Channel.Mode.ROUTER
                || currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            if (!loadingChannel)
            {
                if (destinationConnectorClass.getName() != null
                        && destinationConnectorClass.getName().equals(
                        (String) destinationSourceDropdown
                        .getSelectedItem())
                        && lastIndex
                        .equals((String) destinationTable
                        .getValueAt(
                        getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME))))
                    return;
                
                // if the selected destination is still the same AND the default
                // properties/transformer/filter have
                // not been changed from defaults then ask if the user would
                // like to really change connector type.
                if (lastIndex.equals((String) destinationTable.getValueAt(
                        getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME)))
                        && (!compareProps(destinationConnectorClass
                        .getProperties(), destinationConnectorClass
                        .getDefaults())
                        || currentChannel
                        .getDestinationConnectors()
                        .get(
                        getDestinationConnectorIndex((String) destinationTable
                        .getValueAt(
                        getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME))))
                        .getFilter().getRules().size() > 0 || currentChannel
                        .getDestinationConnectors()
                        .get(
                        getDestinationConnectorIndex((String) destinationTable
                        .getValueAt(
                        getSelectedDestinationIndex(),
                        getColumnNumber(DESTINATION_COLUMN_NAME))))
                        .getTransformer().getSteps().size() > 0))
                {
                    boolean changeType = parent
                            .alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                    if (!changeType)
                    {
                        destinationSourceDropdown
                                .setSelectedItem(destinationConnectorClass
                                .getProperties().get(DATA_TYPE_KEY));
                        return;
                    }
                }
            }
            generateMultipleDestinationPage();
        }
        else
        {
            if (!loadingChannel)
            {
                if (destinationConnectorClass.getName() != null
                        && destinationConnectorClass.getName().equals(
                        (String) destinationSourceDropdown
                        .getSelectedItem()))
                    return;
                if (!compareProps(destinationConnectorClass.getProperties(),
                        destinationConnectorClass.getDefaults())
                        || currentChannel.getDestinationConnectors().get(0)
                        .getFilter().getRules().size() > 0
                        || currentChannel.getDestinationConnectors().get(0)
                        .getTransformer().getSteps().size() > 0)
                {
                    boolean changeType = parent
                            .alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                    if (!changeType)
                    {
                        destinationSourceDropdown
                                .setSelectedItem(destinationConnectorClass
                                .getProperties().get(DATA_TYPE_KEY));
                        return;
                    }
                }
            }
            generateSingleDestinationPage();
        }
    }
    
    public void generateMultipleDestinationPage()
    {
        // Get the selected destination connector and set it.
        for (int i = 0; i < parent.destinationConnectors.size(); i++)
        {
            if (parent.destinationConnectors.get(i).getName().equalsIgnoreCase(
                    (String) destinationSourceDropdown.getSelectedItem()))
                destinationConnectorClass = parent.destinationConnectors.get(i);
        }
        
        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        int connectorIndex = getDestinationConnectorIndex((String) destinationTable
                .getValueAt(getSelectedDestinationIndex(),
                getColumnNumber(DESTINATION_COLUMN_NAME)));
        Connector destinationConnector = destinationConnectors
                .get(connectorIndex);
        
        String dataType = destinationConnector.getProperties().getProperty(
                DATA_TYPE_KEY);
        if (dataType == null)
            dataType = "";
        
        // Debug with:
        // System.out.println(destinationConnector.getTransportName() + " " +
        // (String)destinationSourceDropdown.getSelectedItem());
        
        // Set to defaults on first load of connector or if it has changed
        // types.
        if (destinationConnector.getProperties().size() == 0
                || !dataType.equals((String) destinationSourceDropdown
                .getSelectedItem()))
        {
            String name = destinationConnector.getName();
            changeConnectorType(destinationConnector);
            destinationConnector.setName(name);
            destinationConnectorClass.setProperties(destinationConnectorClass
                    .getDefaults());
            destinationConnector.setProperties(destinationConnectorClass
                    .getProperties());
        }
        
        // Set the transport name of the destination connector and set it in the
        // list.
        destinationConnector
                .setTransportName((String) destinationSourceDropdown
                .getSelectedItem());
        destinationConnectors.set(connectorIndex, destinationConnector);
        
        // If the connector type has changed then set the new value in the
        // destination table.
        if (!((String) destinationTable.getValueAt(
                getSelectedDestinationIndex(),
                getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME)))
                .equals(destinationConnector.getTransportName())
                && getSelectedDestinationIndex() != -1)
            destinationTable.setValueAt((String) destinationSourceDropdown
                    .getSelectedItem(), getSelectedDestinationIndex(),
                    getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME));
        
        // Debug with:
        // System.out.println(destinationConnector.getProperties().toString());
        destinationConnectorClass.setProperties(destinationConnector
                .getProperties());
        setDestinationVariableList();
                /*
                 * if (currentChannel.getDirection() == Channel.Direction.OUTBOUND) { //
                 * destinationVariableList.setVariableListOutbound(); List<Step>
                 * concatenatedSteps =
                 * getMultipleDestinationSteps(destinationConnector);
                 * destinationVariableList.setVariableListInbound(concatenatedSteps);
                 * destinationVariableList.setDestinationMappingsLabel(); } else { if
                 * (currentChannel.getMode() == Channel.Mode.ROUTER ||
                 * currentChannel.getMode() == Channel.Mode.APPLICATION) { List<Step>
                 * concatenatedSteps =
                 * getMultipleDestinationSteps(destinationConnector);
                 * destinationVariableList .setVariableListInbound(concatenatedSteps);
                 * destinationVariableList.setDestinationMappingsLabel(); } else if
                 * (currentChannel.getMode() == Channel.Mode.BROADCAST) {
                 * destinationVariableList.setVariableListInbound(currentChannel
                 * .getSourceConnector().getTransformer().getSteps());
                 * destinationVariableList.setSourceMappingsLabel(); } }
                 */
        destination.removeAll();
        
        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout destinationLayout = (org.jdesktop.layout.GroupLayout) destination
                .getLayout();
        destinationLayout
                .setHorizontalGroup(destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                destinationLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.TRAILING)
                .add(
                org.jdesktop.layout.GroupLayout.LEADING,
                destinationPane,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                610,
                Short.MAX_VALUE)
                .add(
                org.jdesktop.layout.GroupLayout.LEADING,
                destinationLayout
                .createSequentialGroup()
                .add(
                destinationSourceLabel)
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                110,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                destinationLayout
                .createSequentialGroup()
                .add(
                destinationConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationVariableList,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap()));
        destinationLayout
                .setVerticalGroup(destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                destinationLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                destinationPane,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                143,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.BASELINE)
                .add(
                destinationSourceLabel)
                .add(
                destinationSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                destinationConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .add(
                destinationVariableList,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                497,
                Short.MAX_VALUE))
                .addContainerGap()));
        destination.updateUI();
    }
    
    private List<Step> getMultipleDestinationSteps(Connector currentDestination)
    {
        final String VAR_PATTERN = "globalMap.put\\(['|\"]([^'|^\"]*)[\"|']";
        
        List<Step> concatenatedSteps = new ArrayList<Step>();
        List<Connector> destinationConnectors = currentChannel
                .getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (currentDestination == destination)
            {
                seenCurrent = true;
                // add all the variables
                List<Step> destinationSteps = destination.getTransformer()
                .getSteps();
                concatenatedSteps.addAll(destinationSteps);
                
            }
            else if (!seenCurrent)
            {
                // add only the global variables
                List<Step> destinationSteps = destination.getTransformer()
                .getSteps();
                Iterator stepIterator = destinationSteps.iterator();
                while (stepIterator.hasNext())
                {
                    Step step = (Step) stepIterator.next();
                    HashMap map = (HashMap) step.getData();
                    if (step.getType().equals(TransformerPane.MAPPER_TYPE))
                    {
                        // Check if the step is global
                        if (map.containsKey("isGlobal"))
                        {
                            if (((String) map.get("isGlobal"))
                            .equalsIgnoreCase(UIConstants.YES_OPTION))
                                concatenatedSteps.add(step);
                        }
                    }
                    else if (step.getType().equals(TransformerPane.JAVASCRIPT_TYPE))
                    {
                        Pattern pattern = Pattern.compile(VAR_PATTERN);
                        Matcher matcher = pattern.matcher(step.getScript());
                        while (matcher.find())
                        {
                            String key = matcher.group(1);
                            Step tempStep = new Step();
                            Map tempMap = new HashMap();
                            tempMap.put("Variable", key);
                            tempStep.setData(tempMap);
                            tempStep.setType(TransformerPane.MAPPER_TYPE);
                            concatenatedSteps.add(tempStep);
                        }
                    }
                }
            }
        }
        return concatenatedSteps;
    }
    
    public void generateSingleDestinationPage()
    {
        // Get the selected destination connector and set it.
        for (int i = 0; i < parent.destinationConnectors.size(); i++)
        {
            if (parent.destinationConnectors.get(i).getName().equalsIgnoreCase(
                    (String) destinationSourceDropdown.getSelectedItem()))
                destinationConnectorClass = parent.destinationConnectors.get(i);
        }
        
        // Sets all of the properties, transformer, filter, etc. on the new
        // destination connector.
        int connectorIndex = 0;
        Connector destinationConnector = currentChannel
                .getDestinationConnectors().get(connectorIndex);
        if (destinationConnector != null)
        {
            String dataType = destinationConnector.getProperties().getProperty(
                    DATA_TYPE_KEY);
            if (dataType == null)
                dataType = "";
            
            if (destinationConnector.getProperties().size() == 0
                    || !dataType.equals((String) destinationSourceDropdown
                    .getSelectedItem()))
            {
                String name = destinationConnector.getName();
                changeConnectorType(destinationConnector);
                destinationConnector.setName(name);
                destinationConnectorClass
                        .setProperties(destinationConnectorClass.getDefaults());
                destinationConnector.setProperties(destinationConnectorClass
                        .getProperties());
            }
            
            destinationConnector
                    .setTransportName((String) destinationSourceDropdown
                    .getSelectedItem());
            currentChannel.getDestinationConnectors().set(connectorIndex,
                    destinationConnector);
            destinationConnectorClass.setProperties(destinationConnector
                    .getProperties());
        }
        
        destination.removeAll();
        
        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout destinationLayout = (org.jdesktop.layout.GroupLayout) destination
                .getLayout();
        destinationLayout
                .setHorizontalGroup(destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                destinationLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                destinationLayout
                .createSequentialGroup()
                .add(
                destinationConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationVariableList,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(
                destinationLayout
                .createSequentialGroup()
                .add(
                destinationSourceLabel)
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                110,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap()));
        destinationLayout
                .setVerticalGroup(destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                destinationLayout
                .createSequentialGroup()
                .addContainerGap()
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.BASELINE)
                .add(
                destinationSourceLabel)
                .add(
                destinationSourceDropdown,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(
                org.jdesktop.layout.LayoutStyle.RELATED)
                .add(
                destinationLayout
                .createParallelGroup(
                org.jdesktop.layout.GroupLayout.LEADING)
                .add(
                destinationConnectorClass,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE)
                .add(
                destinationVariableList,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                497,
                Short.MAX_VALUE))
                .addContainerGap()));
        
        destination.updateUI();
    }
    
    /** Sets the destination variable list from the transformer steps */
    public void setDestinationVariableList()
    {
        
        if (currentChannel.getMode() == Channel.Mode.ROUTER
                || currentChannel.getDirection() == Channel.Direction.OUTBOUND)
        {
            int destination = getDestinationConnectorIndex((String) destinationTable
                    .getValueAt(getSelectedDestinationIndex(),
                    getColumnNumber(DESTINATION_COLUMN_NAME)));
            List<Step> concatenatedSteps = getMultipleDestinationSteps(currentChannel
                    .getDestinationConnectors().get(destination));
            destinationVariableList.setVariableListInbound(concatenatedSteps);
            // destinationVariableList.setVariableListInbound(currentChannel.getDestinationConnectors().get(destination).getTransformer().getSteps());
            destinationVariableList.setDestinationMappingsLabel();
        }
        else if (currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            destinationVariableList.setVariableListInbound(currentChannel
                    .getSourceConnector().getTransformer().getSteps());
            destinationVariableList.setSourceMappingsLabel();
        }
        else
        {
            destinationVariableList.setVariableListInbound(currentChannel
                    .getDestinationConnectors().get(0).getTransformer()
                    .getSteps());
            destinationVariableList.setDestinationMappingsLabel();
        }
        
        destinationVariableList.repaint();
        
    }
    
    /** Returns a new connector, that has a new transformer and filter */
    public Connector makeNewConnector()
    {
        Connector c = new Connector();
        Transformer dt = new Transformer();
        Filter df = new Filter();
        
        c.setTransformer(dt);
        c.setFilter(df);
        return c;
    }
    
    /** Changes the connector type without clearing filter and transformer */
    public void changeConnectorType(Connector c)
    {
        Transformer oldTransformer = c.getTransformer();
        Filter oldFilter = c.getFilter();
        c = makeNewConnector();
        c.setTransformer(oldTransformer);
        c.setFilter(oldFilter);
    }
    
    /** Returns the source connector class */
    public ConnectorClass getSourceConnector()
    {
        return sourceConnectorClass;
    }
    
    /** Returns the destination connector class */
    public ConnectorClass getDestinationConnector()
    {
        return destinationConnectorClass;
    }
    
    /**
     * Checks for properties that are new or not used and adds or removes them.
     */
    private void checkPropertyValidity(Connector connector,
            ArrayList<ConnectorClass> connectors)
    {
        Enumeration<?> propertyKeys;
        Properties properties = connector.getProperties();
        Properties propertiesDefaults = null;
        
        for (int j = 0; j < connectors.size(); j++)
        {
            if (connectors.get(j).getName().equalsIgnoreCase(
                    connector.getTransportName()))
            {
                propertiesDefaults = connectors.get(j).getDefaults();
            }
        }
        
        propertyKeys = properties.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            if (propertiesDefaults.getProperty(key) == null)
            {
                properties.remove(key);
            }
        }
        
        propertyKeys = propertiesDefaults.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            if (properties.getProperty(key) == null)
            {
                if ( propertiesDefaults.getProperty(key)!= null)
                    properties.put(key, propertiesDefaults.getProperty(key));
            }
        }
    }
    
    /** A method to compare two properties file to check if they are the same. */
    private boolean compareProps(Properties p1, Properties p2)
    {
        Enumeration<?> propertyKeys = p1.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String) propertyKeys.nextElement();
            //System.out.println(key + " " + p1.getProperty(key) + " " + p2.getProperty(key));
            if (p1.getProperty(key) == null)
            {
                if (p2.getProperty(key) != null)
                    return false;
            }
            else if (!p1.getProperty(key).equals(p2.getProperty(key)))
                return false;
        }
        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTabbedPane channelView;
    private javax.swing.JLabel days;
    private javax.swing.JPanel destination;
    private com.webreach.mirth.client.ui.connectors.ConnectorClass destinationConnectorClass;
    private com.webreach.mirth.client.ui.components.MirthComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private com.webreach.mirth.client.ui.VariableList destinationVariableList;
    private com.webreach.mirth.client.ui.components.MirthCheckBox encryptMessagesCheckBox;
    private javax.swing.ButtonGroup filterButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthComboBox initialState;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private com.webreach.mirth.client.ui.components.MirthTextField numDays;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea preprocessor;
    private javax.swing.JPanel source;
    private com.webreach.mirth.client.ui.connectors.ConnectorClass sourceConnectorClass;
    private com.webreach.mirth.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private com.webreach.mirth.client.ui.components.MirthCheckBox storeMessages;
    private com.webreach.mirth.client.ui.components.MirthRadioButton storeMessagesAll;
    private com.webreach.mirth.client.ui.components.MirthRadioButton storeMessagesDays;
    private com.webreach.mirth.client.ui.components.MirthCheckBox storeMessagesErrors;
    private javax.swing.JPanel summary;
    private javax.swing.JLabel summaryDescriptionLabel;
    private com.webreach.mirth.client.ui.components.MirthTextPane summaryDescriptionText;
    private javax.swing.JLabel summaryDirectionLabel1;
    private javax.swing.JLabel summaryDirectionLabel2;
    private com.webreach.mirth.client.ui.components.MirthCheckBox summaryEnabledCheckbox;
    private com.webreach.mirth.client.ui.components.MirthTextField summaryNameField;
    private javax.swing.JLabel summaryNameLabel;
    private javax.swing.JLabel summaryPatternLabel1;
    private javax.swing.JLabel summaryPatternLabel2;
    private com.webreach.mirth.client.ui.components.MirthCheckBox transactionalCheckBox;
    private javax.swing.ButtonGroup validationButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthCheckBox xmlPreEncoded;
    // End of variables declaration//GEN-END:variables
    
}
