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

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.connectors.ConnectorClass;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Transport;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import java.util.Map.Entry;

/** The channel editor panel.  Majority of the client application */
public class ChannelSetup extends javax.swing.JPanel
{
    private final String DESTINATION_COLUMN_NAME = "Destination";
    private final String CONNECTOR_TYPE_COLUMN_NAME = "Connector Type";
    private final int SOURCE_TAB_INDEX = 1;
    private final int DESTINATIONS_TAB_INDEX = 2;
    private final String DATA_TYPE_KEY = "DataType";
    
    public Channel currentChannel;
    public String lastIndex = "";
    public TransformerPane transformerPane;
    public FilterPane filterPane;
    
    private int index;
    private Frame parent;
    private boolean isDeleting = false;
    private boolean loadingChannel = false;
    private JXTable destinationTable;
    private JScrollPane destinationPane;
    private Map<String,Transport> transports;
    private ArrayList<String> sourceConnectorsInbound;
    private ArrayList<String> destinationConnectorsInbound;
    private ArrayList<String> sourceConnectorsOutbound;
    private ArrayList<String> destinationConnectorsOutbound;
    /** Creates the Channel Editor panel.  Calls initComponents() and 
     *  sets up the model, dropdowns, and mouse listeners.
     */
    public ChannelSetup()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
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
        transformerPane = new TransformerPane();
        filterPane = new FilterPane();
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            transports = this.parent.mirthClient.getTransports();
            sourceConnectorsInbound = new ArrayList<String>();
            sourceConnectorsOutbound = new ArrayList<String>();
            destinationConnectorsInbound = new ArrayList<String>();
            destinationConnectorsOutbound = new ArrayList<String>();
            Iterator i=transports.entrySet().iterator();
            while(i.hasNext())
            {
               Entry entry = (Entry)i.next();
               if(transports.get(entry.getKey()).getType() == Transport.Type.LISTENER && transports.get(entry.getKey()).isInbound())
                   sourceConnectorsInbound.add(transports.get(entry.getKey()).getName());
               
               if(transports.get(entry.getKey()).getType() == Transport.Type.LISTENER && transports.get(entry.getKey()).isOutbound())
                   sourceConnectorsOutbound.add(transports.get(entry.getKey()).getName());
               
               if(transports.get(entry.getKey()).getType() == Transport.Type.SENDER && transports.get(entry.getKey()).isInbound())
                   destinationConnectorsInbound.add(transports.get(entry.getKey()).getName());
               
               if(transports.get(entry.getKey()).getType() == Transport.Type.SENDER && transports.get(entry.getKey()).isOutbound())
                   destinationConnectorsOutbound.add(transports.get(entry.getKey()).getName());
            }
        }
        catch(ClientException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        index = -1;
        channelView.setMaximumSize(new Dimension(450, 3000));
    }
    
    /** Shows the trigger-button popup menu.  If the trigger was pressed
     *  on a row of the destination table, that row should be selected as well.
     */
    private void showChannelEditPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = destinationTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                destinationTable.setRowSelectionInterval(row, row);
            }
            parent.channelEditPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    /** Is called to load the transformer pane on either the source or destination */
    public void editTransformer()
    {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
            transformerPane.load(currentChannel.getSourceConnector().getTransformer());
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            if (currentChannel.getMode() == Channel.Mode.APPLICATION)
                transformerPane.load(currentChannel.getDestinationConnectors().get(0).getTransformer());
            else
            {
                int destination = getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)));
                transformerPane.load(currentChannel.getDestinationConnectors().get(destination).getTransformer());
            }
        }
    }
    
    /** Is called to load the filter pane on either the source or destination */
    public void editFilter()
    {
        if (channelView.getSelectedIndex() == SOURCE_TAB_INDEX)
            filterPane.load(currentChannel.getSourceConnector().getFilter());
        
        else if (channelView.getSelectedIndex() == DESTINATIONS_TAB_INDEX)
        {
            if (currentChannel.getMode() == Channel.Mode.APPLICATION)
                filterPane.load(currentChannel.getDestinationConnectors().get(0).getFilter());
            else
            {
                int destination = getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)));
                filterPane.load(currentChannel.getDestinationConnectors().get(destination).getFilter());
            }
        }
    }
    
    /** Makes the destination table with a parameter that is true if a new
     *  destination should be added as well.
     */
    public void makeDestinationTable(boolean addNew)
    {
        List<Connector> destinationConnectors;
        Object[][] tableData;
        int tableSize;

        destinationConnectors = currentChannel.getDestinationConnectors();
        tableSize = destinationConnectors.size();
        if(addNew)
            tableSize++;
        tableData = new Object[tableSize][2];
        for (int i=0; i < tableSize; i++)
        {
            if(tableSize-1 == i && addNew)
            {
                Connector connector = makeNewConnector();
                connector.setName(getNewDestinationName(tableSize));
                connector.setTransportName((String)destinationSourceDropdown.getItemAt(0));

                tableData[i][0] = connector.getName();
                tableData[i][1] = connector.getTransportName();

                destinationConnectors.add(connector);
            }
            else
            {
                tableData[i][0] = destinationConnectors.get(i).getName();
                tableData[i][1] = destinationConnectors.get(i).getTransportName();
            }
        }

        destinationTable = new JXTable();

        destinationTable.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
                {
                    DESTINATION_COLUMN_NAME, CONNECTOR_TYPE_COLUMN_NAME
                }
            )
            {
                boolean[] canEdit = new boolean []
                {
                    true, false
                };

                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit [columnIndex];
                }
        });
        
        // Set the custom cell editor for the Destination Name column.
        destinationTable.getColumnModel().getColumn(destinationTable.getColumnModel().getColumnIndex(DESTINATION_COLUMN_NAME)).setCellEditor(new DestinationTableCellEditor());

        destinationTable.setSelectionMode(0);
        destinationTable.setRowSelectionAllowed(true);
        destinationTable.setRowHeight(UIConstants.ROW_HEIGHT);
        destinationTable.setFocusable(false);   // Need to figure a way to make the arrow keys work here because the pane that shows up steals the focus
        
        destinationTable.setOpaque(true);
        
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            ((JXTable)destinationTable).setHighlighters(highlighter);
        }
        
        // This action is called when a new selection is made on the destination table.
        destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!evt.getValueIsAdjusting())
                {
                    int last = getDestinationIndex(lastIndex);
                    if (last != -1 && last != destinationTable.getRowCount() && !isDeleting)
                    {
                        int connectorIndex = getDestinationConnectorIndex((String)destinationTable.getValueAt(last,getColumnNumber(DESTINATION_COLUMN_NAME)));
                        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
                        destinationConnector.setProperties(destinationConnectorClass.getProperties());
                    }

                    if(!loadConnector())
                    {
                        if(getDestinationIndex(lastIndex) == destinationTable.getRowCount())
                            destinationTable.setRowSelectionInterval(last-1,last-1);
                        else
                            destinationTable.setRowSelectionInterval(last,last);
                    }
                    else
                    {
                        lastIndex = ((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)));
                    }
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
            destinationTable.setRowSelectionInterval(destinationTable.getRowCount()-1, destinationTable.getRowCount()-1);
        else if (last == -1)
            destinationTable.setRowSelectionInterval(0,0);       // Makes sure the event is called when the table is created.
        else if(last == destinationTable.getRowCount())
            destinationTable.setRowSelectionInterval(last-1,last-1);
        else
            destinationTable.setRowSelectionInterval(last,last);
        destinationPane.setViewportView(destinationTable);
        
        // Mouse listener for trigger-button popup on the table pane (not actual table).
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
        
    }
    
    /** Get the index of a destination by being passed its name. */
    private int getDestinationIndex(String destinationName)
    {
        for (int i = 0; i < destinationTable.getRowCount(); i++)
        {
            if(((String)destinationTable.getValueAt(i,getColumnNumber(DESTINATION_COLUMN_NAME))).equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }

    /** Get the name that should be used for a new destination so that
     *  it is unique.
     */
    private String getNewDestinationName(int size)
    {
        String temp = "Destination ";

        for(int i = 1; i<=size; i++)
        {
            boolean exists = false;
            for(int j = 0; j < size-1; j++)
            {
                if(((String)destinationTable.getValueAt(j,getColumnNumber(DESTINATION_COLUMN_NAME))).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if(!exists)
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
        if(destinationTable.isEditing())
            return destinationTable.getEditingRow();
        else
            return destinationTable.getSelectedRow();
    }

    /** Get a destination connector index by passing in its name */
    private int getDestinationConnectorIndex(String destinationName)
    {
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        for(int i = 0; i<destinationConnectors.size(); i++)
        {
            if(destinationConnectors.get(i).getName().equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }

    /** Loads a selected connector and returns true on success. */
    public boolean loadConnector()
    {
        List<Connector> destinationConnectors;
        String destinationName;
        
        if(getSelectedDestinationIndex() != -1)
            destinationName = (String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME));
        else
            return false;
        
        if(currentChannel != null && currentChannel.getDestinationConnectors() != null)
        {
            destinationConnectors = currentChannel.getDestinationConnectors();
            for(int i = 0; i<destinationConnectors.size(); i++)
            {
                if(destinationConnectors.get(i).getName().equalsIgnoreCase(destinationName))
                {
                    boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
                    destinationSourceDropdown.setSelectedItem(destinationConnectors.get(i).getTransportName());
                    parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
                    return true;
                }
            }
        }
        return false;
    }

    /** Sets the overall panel to edit the channel with the given channel index. */
    public void editChannel(int index)
    {
        loadingChannel = true;
        this.index = index;
        lastIndex = "";
        currentChannel = parent.channels.get(index);
        
        channelView.setSelectedComponent(summary);
        
        if(currentChannel.getDirection() == Channel.Direction.INBOUND)
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectorsInbound.toArray()));
            destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectorsInbound.toArray()));
        }
        else
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectorsOutbound.toArray()));
            destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectorsOutbound.toArray()));
        }
        
        loadChannelInfo();
        
        if(currentChannel.getMode() == Channel.Mode.ROUTER || currentChannel.getMode() == Channel.Mode.BROADCAST)
            makeDestinationTable(false);
        else
            generateSingleDestinationPage();
        
        setDestinationVariableList();
        loadingChannel = false;
    }

    /** Adds a new channel that is passed in and then sets the overall panel to
     *  edit that channel. */
    public void addChannel(Channel channel)
    {
        loadingChannel = true;
        index = -1;
        lastIndex = "";
        currentChannel = channel;
        
        if(currentChannel.getDirection() == Channel.Direction.INBOUND)
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectorsInbound.toArray()));
            destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectorsInbound.toArray()));
        }
        else
        {
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectorsOutbound.toArray()));
            destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectorsOutbound.toArray()));
        }
        
        channelView.setSelectedComponent(summary);
        
        Connector sourceConnector = makeNewConnector();
	sourceConnector.setName("sourceConnector");
        sourceConnector.setTransportName((String)sourceSourceDropdown.getItemAt(0));
        sourceConnector.setProperties(sourceConnectorClass.getProperties());
        
        currentChannel.setSourceConnector(sourceConnector);
                
        if(currentChannel.getMode() == Channel.Mode.APPLICATION)
        {
            List<Connector> dc;
            dc = currentChannel.getDestinationConnectors();

            Connector c = makeNewConnector();
            c.setName("Destination");
            c.setTransportName((String)destinationSourceDropdown.getItemAt(0));
            dc.add(c);
        }
        
        loadChannelInfo();
        
        if(currentChannel.getMode() == Channel.Mode.ROUTER || currentChannel.getMode() == Channel.Mode.BROADCAST)
            makeDestinationTable(true);
        else
            generateSingleDestinationPage();
        
        setDestinationVariableList();
        
        saveChanges();
        loadingChannel = false;
    }

    /** Load all of the saved channel information into the channel editor */
    private void loadChannelInfo()
    {
        summaryNameField.setText(currentChannel.getName());
        summaryDescriptionText.setText(currentChannel.getDescription());
        if (currentChannel.getDirection().equals(Channel.Direction.INBOUND))
            summaryDirectionLabel2.setText("Inbound");
        else if (currentChannel.getDirection().equals(Channel.Direction.OUTBOUND))
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
        
        if(((String)currentChannel.getProperties().get("recv_xml_encoded")) != null && ((String)currentChannel.getProperties().get("recv_xml_encoded")).equalsIgnoreCase("true"))
            xmlPreEncoded.setSelected(true);
        else
            xmlPreEncoded.setSelected(false);

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());
        
        if (currentChannel.getMode().equals(Channel.Mode.APPLICATION))
        {
            destinationSourceDropdown.setSelectedItem(currentChannel.getDestinationConnectors().get(0).getTransportName());
        }
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    /** Save all of the current channel information in the editor to the actual channel */
    public boolean saveChanges()
    {
        if (summaryNameField.getText().equals(""))
        {
            JOptionPane.showMessageDialog(parent, "Channel name cannot be empty.");
                return false;
        }
        if (!currentChannel.getName().equals(summaryNameField.getText()))
        {
            if(!parent.checkChannelName(summaryNameField.getText()))
                return false;
        }
        
        currentChannel.getSourceConnector().setProperties(sourceConnectorClass.getProperties());
        
        Connector temp;
        if(currentChannel.getMode() == Channel.Mode.APPLICATION)
            temp = currentChannel.getDestinationConnectors().get(0);
        else
            temp = currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME))));
        temp.setProperties(destinationConnectorClass.getProperties());

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());
        currentChannel.setEnabled(summaryEnabledCheckbox.isSelected());
        
        if(xmlPreEncoded.isSelected())
            currentChannel.getProperties().put("recv_xml_encoded", "true");
        else
            currentChannel.getProperties().put("recv_xml_encoded", "false");
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            if(index == -1)
            {
                index = parent.channels.size();
                currentChannel.setId(parent.mirthClient.getNextId());
            }

            parent.updateChannel(currentChannel);
            currentChannel = parent.channels.get(index);
            parent.channelListPage.makeChannelTable();
        }
        catch (ClientException e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        return true;
    }

    /** Adds a new destination. */
    public void addNewDestination()
    {
         makeDestinationTable(true);
         parent.enableSave();
    }

    /** Deletes the selected destination. */
    public void deleteDestination()
    {
        isDeleting = true;
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        if(destinationConnectors.size() == 1)
        {
            JOptionPane.showMessageDialog(parent, "You must have at least one destination.");
            return;
        }
        
        destinationConnectors.remove(getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME))));
        makeDestinationTable(false);
        parent.enableSave();
        isDeleting = false;
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        filterButtonGroup = new javax.swing.ButtonGroup();
        validationButtonGroup = new javax.swing.ButtonGroup();
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
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        xmlPreEncoded.setBackground(new java.awt.Color(255, 255, 255));
        xmlPreEncoded.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        xmlPreEncoded.setText("Channel will receive XML pre-encoded HL7 messages.");
        xmlPreEncoded.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jScrollPane1.setViewportView(summaryDescriptionText);

        org.jdesktop.layout.GroupLayout summaryLayout = new org.jdesktop.layout.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryDescriptionLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryPatternLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryDirectionLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryPatternLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(summaryDirectionLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(50, 50, 50)
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(xmlPreEncoded, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(36, 36, 36))
                    .add(summaryLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                        .add(85, 85, 85))))
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryNameLabel)
                    .add(summaryEnabledCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryDirectionLabel1)
                    .add(summaryDirectionLabel2)
                    .add(xmlPreEncoded, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryPatternLabel1)
                    .add(summaryPatternLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryDescriptionLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE))
                .add(85, 85, 85))
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
            .add(0, 628, Short.MAX_VALUE)
        );
        sourceConnectorClassLayout.setVerticalGroup(
            sourceConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 379, Short.MAX_VALUE)
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
            .add(0, 436, Short.MAX_VALUE)
        );
        destinationConnectorClassLayout.setVerticalGroup(
            destinationConnectorClassLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 379, Short.MAX_VALUE)
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
                    .add(destinationVariableList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                .addContainerGap())
        );
        channelView.addTab("Destinations", destination);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /** Action when the summary tab is shown. */
    private void summaryComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_summaryComponentShown
    {//GEN-HEADEREND:event_summaryComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 4, false);
    }//GEN-LAST:event_summaryComponentShown

    /** Action when the source tab is shown. */
    private void sourceComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_sourceComponentShown
    {//GEN-HEADEREND:event_sourceComponentShown
        if(currentChannel.getMode() == Channel.Mode.ROUTER)
        {
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 4, false);
        }
        else
        {
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 2, false);
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 3, 4, true);
        }
    }//GEN-LAST:event_sourceComponentShown

    /** Action when the destinations tab is shown. */
    private void destinationComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_destinationComponentShown
    {//GEN-HEADEREND:event_destinationComponentShown
        if(currentChannel.getMode() == Channel.Mode.APPLICATION)
        {
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 2, false);
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 3, 4, true);
        }
        else if(currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 2, true);
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 3, 4, false);
        }
        else
            parent.setVisibleTasks(parent.channelEditTasks, parent.channelEditPopupMenu, 1, 4, true);
    }//GEN-LAST:event_destinationComponentShown

    /** Action when an action is performed on the source connector type dropdown. */
    private void sourceSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceSourceDropdownActionPerformed
        // If a channel is not being loaded then alert the user when necessary that
        // changing the connector type will lose all current connector data.
        if (!loadingChannel)
        {
            if (sourceConnectorClass.getName() != null && sourceConnectorClass.getName().equals((String)sourceSourceDropdown.getSelectedItem()))
                return;

            if (!compareProps(sourceConnectorClass.getProperties(), sourceConnectorClass.getDefaults()) || 
                    currentChannel.getSourceConnector().getFilter().getRules().size() > 0 ||
                    currentChannel.getSourceConnector().getTransformer().getSteps().size() > 0)
            {
                boolean changeType = parent.alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                if (!changeType)
                {
                    sourceSourceDropdown.setSelectedItem(sourceConnectorClass.getProperties().get(DATA_TYPE_KEY));
                    return;
                }
            }
        }
        
        // Get the selected source connector and set it.
        for(int i=0; i<parent.sourceConnectors.size(); i++)
        {
            if(parent.sourceConnectors.get(i).getName().equalsIgnoreCase((String)sourceSourceDropdown.getSelectedItem()))
            {
                sourceConnectorClass = parent.sourceConnectors.get(i);
            }
        }

        // Sets all of the properties, transformer, filter, etc. on the new source connector.
        Connector sourceConnector = currentChannel.getSourceConnector();
        if(sourceConnector != null)
        {
            String dataType = sourceConnector.getProperties().getProperty(DATA_TYPE_KEY);
            if (dataType == null)
                dataType = "";

            if (sourceConnector.getProperties().size() == 0 || !dataType.equals((String)sourceSourceDropdown.getSelectedItem()))
            {
                String name = sourceConnector.getName();
                sourceConnector = makeNewConnector();
                sourceConnector.setName(name);
                sourceConnectorClass.setProperties(sourceConnectorClass.getDefaults());
                sourceConnector.setProperties(sourceConnectorClass.getProperties());
            }

            sourceConnector.setTransportName((String)sourceSourceDropdown.getSelectedItem());
            currentChannel.setSourceConnector(sourceConnector);
            sourceConnectorClass.setProperties(sourceConnector.getProperties());
        }
        
        source.removeAll();
        
        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout sourceLayout = (org.jdesktop.layout.GroupLayout)source.getLayout();
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
        
        source.updateUI();
    }//GEN-LAST:event_sourceSourceDropdownActionPerformed

    /** Action when an action is performed on the destination connector type dropdown.
     *  Fires off either generateMultipleDestinationPage() or generateSingleDestinationPage()
     */
    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_destinationSourceDropdownActionPerformed
    {//GEN-HEADEREND:event_destinationSourceDropdownActionPerformed
        // If a channel is not being loaded then alert the user when necessary that
        // changing the connector type will lose all current connector data.
        if(currentChannel.getMode() == Channel.Mode.ROUTER || currentChannel.getMode() == Channel.Mode.BROADCAST)
        {
            if (!loadingChannel)
            {
                if (destinationConnectorClass.getName() != null && destinationConnectorClass.getName().equals((String)destinationSourceDropdown.getSelectedItem()) && lastIndex.equals((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME))))
                    return;

                // if the selected destination is still the same AND the default properties/transformer/filter have 
                // not been changed from defaults then ask if the user would like to really change connector type.
                if (lastIndex.equals((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME))) && (!compareProps(destinationConnectorClass.getProperties(), destinationConnectorClass.getDefaults()) || 
                    currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)))).getFilter().getRules().size() > 0 ||
                    currentChannel.getDestinationConnectors().get(getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)))).getTransformer().getSteps().size() > 0))
                {
                    boolean changeType = parent.alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                    if (!changeType)
                    {
                        destinationSourceDropdown.setSelectedItem(destinationConnectorClass.getProperties().get(DATA_TYPE_KEY));
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
                if (destinationConnectorClass.getName() != null && destinationConnectorClass.getName().equals((String)destinationSourceDropdown.getSelectedItem()))
                    return;
                if (!compareProps(destinationConnectorClass.getProperties(), destinationConnectorClass.getDefaults()) || 
                    currentChannel.getDestinationConnectors().get(0).getFilter().getRules().size() > 0 ||
                    currentChannel.getDestinationConnectors().get(0).getTransformer().getSteps().size() > 0)
                {
                    boolean changeType = parent.alertOption("Are you sure you would like to change this connector type and lose all of the current connector data?");
                    if (!changeType)
                    {
                        destinationSourceDropdown.setSelectedItem(destinationConnectorClass.getProperties().get(DATA_TYPE_KEY));
                        return;
                    }
                }
            }
            generateSingleDestinationPage();
        }
    }//GEN-LAST:event_destinationSourceDropdownActionPerformed

    public void generateMultipleDestinationPage()
    {
        // Get the selected destination connector and set it.
        for(int i=0; i<parent.destinationConnectors.size(); i++)
        {
            if(parent.destinationConnectors.get(i).getName().equalsIgnoreCase((String)destinationSourceDropdown.getSelectedItem()))
                destinationConnectorClass = parent.destinationConnectors.get(i);
        }

        // Get the currently selected destination connector.
        List<Connector> destinationConnectors = currentChannel.getDestinationConnectors();
        int connectorIndex = getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)));
        Connector destinationConnector = destinationConnectors.get(connectorIndex);
                
        String dataType = destinationConnector.getProperties().getProperty(DATA_TYPE_KEY);
        if (dataType == null)
            dataType = "";

        // Debug with:
        // System.out.println(destinationConnector.getTransportName() + " " + (String)destinationSourceDropdown.getSelectedItem());
        
        // Set to defaults on first load of connector or if it has changed types.
        if (destinationConnector.getProperties().size() == 0 || !dataType.equals((String)destinationSourceDropdown.getSelectedItem()))
        {
            String name = destinationConnector.getName();
            destinationConnector = makeNewConnector();
            destinationConnector.setName(name);
            destinationConnectorClass.setProperties(destinationConnectorClass.getDefaults());
            destinationConnector.setProperties(destinationConnectorClass.getProperties());    
        }
        
        // Set the transport name of the destination connector and set it in the list.
        destinationConnector.setTransportName((String)destinationSourceDropdown.getSelectedItem());
        destinationConnectors.set(connectorIndex, destinationConnector);
        
        // If the connector type has changed then set the new value in the destination table.
        if (!((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME))).equals(destinationConnector.getTransportName()) && getSelectedDestinationIndex() != -1)
            destinationTable.setValueAt((String)destinationSourceDropdown.getSelectedItem(),getSelectedDestinationIndex(),getColumnNumber(CONNECTOR_TYPE_COLUMN_NAME));
        
        // Debug with:
        // System.out.println(destinationConnector.getProperties().toString());
        destinationConnectorClass.setProperties(destinationConnector.getProperties());
        
        if(currentChannel.getDirection() == Channel.Direction.OUTBOUND)
            destinationVariableList.setVariableListOutbound();
        else
        {
            if(currentChannel.getMode() == Channel.Mode.ROUTER || currentChannel.getMode() == Channel.Mode.APPLICATION)
            {
                destinationVariableList.setVariableListInbound(destinationConnector.getTransformer().getSteps());
                destinationVariableList.setDestinationMappingsLabel();
            }
            else if(currentChannel.getMode() == Channel.Mode.BROADCAST)
            {
                destinationVariableList.setVariableListInbound(currentChannel.getSourceConnector().getTransformer().getSteps());
                destinationVariableList.setSourceMappingsLabel();
            }
        }
        
        destination.removeAll();
        
        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout destinationLayout = (org.jdesktop.layout.GroupLayout)destination.getLayout();
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationLayout.createSequentialGroup()
                        .add(destinationSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                        .add(destinationConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationVariableList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationConnectorClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(destinationVariableList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE))
                .addContainerGap())
        );
        destination.updateUI();
    }

    public void generateSingleDestinationPage()
    {
        // Get the selected destination connector and set it.
        for(int i=0; i<parent.destinationConnectors.size(); i++)
        {
            if(parent.destinationConnectors.get(i).getName().equalsIgnoreCase((String)destinationSourceDropdown.getSelectedItem()))
                destinationConnectorClass = parent.destinationConnectors.get(i);
        }

        // Sets all of the properties, transformer, filter, etc. on the new destination connector.
        int connectorIndex = 0;
        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
        if(destinationConnector != null)
        {
            String dataType = destinationConnector.getProperties().getProperty(DATA_TYPE_KEY);
            if (dataType == null)
                dataType = "";

            if (destinationConnector.getProperties().size() == 0 || !dataType.equals((String)destinationSourceDropdown.getSelectedItem()))
            {
                String name = destinationConnector.getName();
                destinationConnector = makeNewConnector();
                destinationConnector.setName(name);
                destinationConnectorClass.setProperties(destinationConnectorClass.getDefaults());
                destinationConnector.setProperties(destinationConnectorClass.getProperties());
            }

            destinationConnector.setTransportName((String)destinationSourceDropdown.getSelectedItem());
            currentChannel.getDestinationConnectors().set(connectorIndex, destinationConnector);
            destinationConnectorClass.setProperties(destinationConnector.getProperties());
        }
                
        destination.removeAll();

        // Reset the generated layout.
        org.jdesktop.layout.GroupLayout destinationLayout = (org.jdesktop.layout.GroupLayout)destination.getLayout();
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
                    .add(destinationVariableList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE))
                .addContainerGap())
        );

        destination.updateUI();
    }
    
    /** Sets the destination variable list from the transformer steps */
    public void setDestinationVariableList()
    {
        if(currentChannel.getDirection() == Channel.Direction.OUTBOUND)
            destinationVariableList.setVariableListOutbound();
        else
        {
            if(currentChannel.getMode() == Channel.Mode.ROUTER)
            {
                int destination = getDestinationConnectorIndex((String)destinationTable.getValueAt(getSelectedDestinationIndex(),getColumnNumber(DESTINATION_COLUMN_NAME)));
                destinationVariableList.setVariableListInbound(currentChannel.getDestinationConnectors().get(destination).getTransformer().getSteps());
                destinationVariableList.setDestinationMappingsLabel();
            }
            else if(currentChannel.getMode() == Channel.Mode.BROADCAST)
            {
                destinationVariableList.setVariableListInbound(currentChannel.getSourceConnector().getTransformer().getSteps());
                destinationVariableList.setSourceMappingsLabel();
            }
            else
            {
                destinationVariableList.setVariableListInbound(currentChannel.getDestinationConnectors().get(0).getTransformer().getSteps());
                destinationVariableList.setDestinationMappingsLabel();
            }
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
    
    /** A method to compare two properties file to check if they are the same. */
    private boolean compareProps(Properties p1, Properties p2)
    {
        Enumeration<?> propertyKeys = p1.propertyNames();
        while (propertyKeys.hasMoreElements())
        {
            String key = (String)propertyKeys.nextElement();
            if (!p1.getProperty(key).equals(p2.getProperty(key)))
                return false;
        }
        return true;
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane channelView;
    private javax.swing.JPanel destination;
    private com.webreach.mirth.client.ui.connectors.ConnectorClass destinationConnectorClass;
    private com.webreach.mirth.client.ui.components.MirthComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private com.webreach.mirth.client.ui.VariableList destinationVariableList;
    private javax.swing.ButtonGroup filterButtonGroup;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel source;
    private com.webreach.mirth.client.ui.connectors.ConnectorClass sourceConnectorClass;
    private com.webreach.mirth.client.ui.components.MirthComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
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
    private javax.swing.ButtonGroup validationButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthCheckBox xmlPreEncoded;
    // End of variables declaration//GEN-END:variables

}
