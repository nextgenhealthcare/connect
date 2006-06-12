/*
 * ChannelSetup.java
 *
 * Created on April 28, 2006, 11:05 AM
 */

package com.webreach.mirth.client;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.Validator;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import java.util.Map.Entry;
/**
 *
 * @author  brendanh
 */
public class ChannelSetup extends javax.swing.JPanel
{
    private Channel currentChannel;
    private int index;
    /**
     * Creates new form ChannelSetup
     */
    private Frame parent;
    private int lastIndex = -1;
    private boolean isDeleting = false;

    public ChannelSetup(JFrame parent)
    {
        this.parent = (Frame)parent;
        initComponents();
        String[] sourceConnectors;
        String[] destinationConnectors;
        Map<String,Transport> transports;
        try
        {
            transports = this.parent.mirthClient.getTransports();
            sourceConnectors = new String[transports.size()];
            destinationConnectors = new String[transports.size()];
            int j = 0;
            Iterator i=transports.entrySet().iterator();
            while(i.hasNext())
            {
               Entry entry = (Entry)i.next();
               sourceConnectors[j] = transports.get(entry.getKey()).getDisplayName();
               destinationConnectors[j] = transports.get(entry.getKey()).getDisplayName();
               j++;
            }
            sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sourceConnectors));
            destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinationConnectors));
        }
        catch(ClientException e)
        {
            e.printStackTrace();
        }

        index = -1;
        if (filterStandardRadio.isSelected())
        {
            filterCustomText.setBackground(new java.awt.Color(236,233,216));
            filterCustomText.setEnabled(false);
        }
        if (validationStandardRadio.isSelected())
        {
            validationCustomText.setBackground(new java.awt.Color(236,233,216));
            validationCustomText.setEnabled(false);
        }
        channelView.setMaximumSize(new Dimension(450, 3000));
    }

    public void makeDestinationTable(boolean addNew)
    {
        List<Connector> dc;
        Object[][] tableData;
        int tableSize;

        dc = currentChannel.getDestinationConnectors();
        tableSize = dc.size();
        if(addNew)
            tableSize++;
        tableData = new Object[tableSize][2];
        for (int i=0; i < tableSize; i++)
        {
            if(tableSize-1 == i && addNew)
            {
                Transformer dt = new Transformer();
                dt.setType(Transformer.Type.SCRIPT);

                Connector c = new Connector();

                c.setName(getNewDestinationName(tableSize));
                c.setTransportName((String)destinationSourceDropdown.getSelectedItem());
                c.setTransformer(dt);

                tableData[i][0] = c.getName();
                tableData[i][1] = c.getTransportName();

                dc.add(c);
            }
            else
            {
                tableData[i][0] = dc.get(i).getName();
                tableData[i][1] = dc.get(i).getTransportName();
            }
        }

        jTable1 = new JXTable();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            tableData,
            new String []
                {
                    "Destination", "Connector Type"
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

        /*class MyCellEditorListener implements CellEditorListener
        {
            public void editingCanceled(ChangeEvent e){
            System.out.println("canceled");
            }

            public void editingStopped(ChangeEvent e) {
            System.out.println("stopped");
            }
        }*/

        jTable1.setFocusable(false);
        jTable1.setSelectionMode(0);
        jTable1.setRowSelectionAllowed(true);
        jTable1.setRowHeight(20);
        ((JXTable)jTable1).setColumnMargin(2);
        jTable1.setOpaque(true);
        HighlighterPipeline highlighter = new HighlighterPipeline();
        highlighter.addHighlighter(AlternateRowHighlighter.beige);
        ((JXTable)jTable1).setHighlighters(highlighter);
        //jTable1.getCellEditor().addCellEditorListener(new MyCellEditorListener());

        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!evt.getValueIsAdjusting())
                {
                    if (lastIndex != -1 && lastIndex != jTable1.getRowCount() && !isDeleting)
                    {
                        int connectorIndex = getDestinationConnector((String)jTable1.getValueAt(lastIndex,getColumnNumber("Destination")));
                        Connector destinationConnector = currentChannel.getDestinationConnectors().get(connectorIndex);
                        destinationConnector.setProperties(connectorClass2.getProperties());
                    }

                    if(!loadConnector())
                    {
                        if(lastIndex == jTable1.getRowCount())
                            jTable1.setRowSelectionInterval(lastIndex-1,lastIndex-1);
                        else
                            jTable1.setRowSelectionInterval(lastIndex,lastIndex);
                    }
                    else
                    {
                        lastIndex = getSelectedDestination();
                    }
                }
            }
        });

        if (addNew)
            jTable1.setRowSelectionInterval(jTable1.getRowCount()-1, jTable1.getRowCount()-1);
        else if (lastIndex == -1)
            jTable1.setRowSelectionInterval(0,0);       // Makes sure the event is called when the table is created.
        else if(lastIndex == jTable1.getRowCount())
            jTable1.setRowSelectionInterval(lastIndex-1,lastIndex-1);
        else
            jTable1.setRowSelectionInterval(lastIndex,lastIndex);
        jScrollPane4.setViewportView(jTable1);
    }

    private String getNewDestinationName(int size)
    {
        String temp = "Destination ";

        for(int i = 1; i<=size; i++)
        {
            boolean exists = false;
            for(int j = 0; j < size-1; j++)
            {
                if(((String)jTable1.getValueAt(j,getColumnNumber("Destination"))).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if(!exists)
                return temp + i;
        }
        return "";
    }

    private int getColumnNumber(String name)
    {
        for (int i = 0; i < jTable1.getColumnCount(); i++)
        {
            if (jTable1.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return -1;
    }

    public int getSelectedDestination()
    {
        if(jTable1.isEditing())
            return jTable1.getEditingRow();
        else
            return jTable1.getSelectedRow();
    }

    private int getDestinationConnector(String destinationName)
    {
        List<Connector> dc = currentChannel.getDestinationConnectors();
        for(int i = 0; i<dc.size(); i++)
        {
            if(dc.get(i).getName().equalsIgnoreCase(destinationName))
                return i;
        }
        return -1;
    }

    public boolean loadConnector()
    {
        List<Connector> dc;
        String destinationName;
        if(getSelectedDestination() != -1)
            destinationName = (String)jTable1.getValueAt(getSelectedDestination(),getColumnNumber("Destination"));
        else
            return false;
        if(currentChannel != null && currentChannel.getDestinationConnectors() != null)
        {
            dc = currentChannel.getDestinationConnectors();
            for(int i = 0; i<dc.size(); i++)
            {
                if(dc.get(i).getName().equalsIgnoreCase(destinationName))
                {
                    destinationSourceDropdown.setSelectedItem(dc.get(i).getTransportName());
                    return true;
                }
            }
        }
        return false;
    }

    public void editChannel(int index)
    {
        this.index = index;
        lastIndex = -1;
        currentChannel = parent.channels.get(index);
        loadChannelInfo();
        makeDestinationTable(false);
    }

    public void addChannel(Channel channel)
    {
        index = -1;
        currentChannel = channel;
        loadChannelInfo();
        makeDestinationTable(true);
        saveChanges();
    }

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

        if (currentChannel.getSourceConnector() != null)
            sourceSourceDropdown.setSelectedItem(currentChannel.getSourceConnector().getTransportName());
        else
            sourceSourceDropdown.setSelectedIndex(0);

        String[] filters = new String[1];
        //filters[0] = currentChannel.getFilter().getName();
        filterStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(filters));

        String[] validators = new String[1];
        // validators[0] = currentChannel.getValidator().getName();
        validationStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(validators));

    }

    public boolean saveChanges()
    {
        if (summaryNameField.getText().equals(""))
        {
            JOptionPane.showMessageDialog(parent, "Channel name cannot be empty.");
                return false;
        }
        if (!currentChannel.getName().equals(summaryNameField.getText()))
        {
            for (int i = 0; i < parent.channels.size(); i++)
            {
                if (parent.channels.get(i).getName().equals(summaryNameField.getText()))
                {
                    JOptionPane.showMessageDialog(parent, "Channel name already exists.");
                    return false;
                }
            }
        }

        Transformer sourceTransformer = new Transformer();
	sourceTransformer.setType(Transformer.Type.MAP);
	sourceTransformer.getVariables().put("firstName", "TestFirstName");

        Transformer destinationTransformer = new Transformer();
	destinationTransformer.setType(Transformer.Type.SCRIPT);
	destinationTransformer.getVariables().put("lastName", "TestLastName");

        Connector sourceConnector = new Connector();
	sourceConnector.setName("sourceConnector");
	sourceConnector.setTransformer(sourceTransformer);
        sourceConnector.setTransportName((String) sourceSourceDropdown.getSelectedItem());
        sourceConnector.setProperties(connectorClass1.getProperties());



        Filter filter = new Filter();
        filter.setScript("return true;");

        Validator validator = new Validator();
	validator.getProfiles().put("profile1", "<XLST>");

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());
        currentChannel.setEnabled(summaryEnabledCheckbox.isSelected());
        currentChannel.setModified(false);
	currentChannel.setSourceConnector(sourceConnector);

        try
        {
            if(index == -1)
            {
                index = parent.channels.size();
                currentChannel.setId(parent.mirthClient.getNextId());
            }

            parent.updateChannel(currentChannel);
            parent.channelListPage.makeChannelTable();
        }
        catch (ClientException ex)
        {
            ex.printStackTrace();
        }

        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    public void addNewDestination()
    {
         makeDestinationTable(true);
    }

    public void deleteDestination()
    {
        isDeleting = true;
        List<Connector> dc = currentChannel.getDestinationConnectors();
        if(dc.size() == 1)
        {
            JOptionPane.showMessageDialog(this, "You must have at least one destination.");
            return;
        }
        dc.remove(getDestinationConnector((String)jTable1.getValueAt(getSelectedDestination(),getColumnNumber("Destination"))));
        makeDestinationTable(false);
        isDeleting = false;
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        filterButtonGroup = new javax.swing.ButtonGroup();
        validationButtonGroup = new javax.swing.ButtonGroup();
        channelView = new javax.swing.JTabbedPane();
        summary = new javax.swing.JPanel();
        summaryNameLabel = new javax.swing.JLabel();
        summaryDescriptionLabel = new javax.swing.JLabel();
        summaryNameField = new JTextField(parent);
        jScrollPane2 = new javax.swing.JScrollPane();
        summaryDescriptionText = new javax.swing.JTextArea();
        summaryDirectionLabel1 = new javax.swing.JLabel();
        summaryDirectionLabel2 = new javax.swing.JLabel();
        summaryPatternLabel1 = new javax.swing.JLabel();
        summaryPatternLabel2 = new javax.swing.JLabel();
        summaryEnabledCheckbox = new javax.swing.JCheckBox();
        source = new javax.swing.JPanel();
        sourceSourceDropdown = new javax.swing.JComboBox();
        sourceSourceLabel = new javax.swing.JLabel();
        connectorClass1 = new com.webreach.mirth.client.ConnectorClass();
        filter = new javax.swing.JPanel();
        filterStandardDropdown = new javax.swing.JComboBox();
        filterStandardRadio = new javax.swing.JRadioButton();
        filterCustomRadio = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        filterCustomText = new javax.swing.JTextPane();
        validation = new javax.swing.JPanel();
        validationStandardRadio = new javax.swing.JRadioButton();
        validationStandardDropdown = new javax.swing.JComboBox();
        validationCustomRadio = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        validationCustomText = new javax.swing.JTextPane();
        destination = new javax.swing.JPanel();
        destinationSourceDropdown = new javax.swing.JComboBox();
        destinationSourceLabel = new javax.swing.JLabel();
        connectorClass2 = new com.webreach.mirth.client.ConnectorClass();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        summary.setBackground(new java.awt.Color(255, 255, 255));
        summary.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                summaryComponentShown(evt);
            }
        });

        summaryNameLabel.setText("Channel Name:");

        summaryDescriptionLabel.setText("Channel Description:");

        summaryNameField.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                summaryNameFieldKeyTyped(evt);
            }
        });

        summaryDescriptionText.setColumns(20);
        summaryDescriptionText.setRows(5);
        summaryDescriptionText.setText("Channel Description");
        jScrollPane2.setViewportView(summaryDescriptionText);

        summaryDirectionLabel1.setText("Direction:");

        summaryDirectionLabel2.setText("Outbound");

        summaryPatternLabel1.setText("Pattern:");

        summaryPatternLabel2.setText("Application Integration");

        summaryEnabledCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        summaryEnabledCheckbox.setText("Enabled");
        summaryEnabledCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        summaryEnabledCheckbox.setFocusable(false);
        summaryEnabledCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        summaryEnabledCheckbox.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                summaryEnabledCheckboxStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout summaryLayout = new org.jdesktop.layout.GroupLayout(summary);
        summary.setLayout(summaryLayout);
        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(summaryDescriptionLabel)
                    .add(summaryDirectionLabel1)
                    .add(summaryNameLabel)
                    .add(summaryPatternLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryDirectionLabel2)
                    .add(summaryPatternLabel2)
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(45, 45, 45)
                        .add(summaryEnabledCheckbox))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(228, Short.MAX_VALUE))
        );
        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryEnabledCheckbox)
                    .add(summaryNameLabel))
                .add(18, 18, 18)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryDirectionLabel2)
                    .add(summaryDirectionLabel1))
                .add(20, 20, 20)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryPatternLabel2)
                    .add(summaryPatternLabel1))
                .add(24, 24, 24)
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryDescriptionLabel))
                .addContainerGap(328, Short.MAX_VALUE))
        );
        channelView.addTab("Summary", summary);

        source.setBackground(new java.awt.Color(255, 255, 255));
        source.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                sourceComponentShown(evt);
            }
        });

        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        sourceSourceDropdown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sourceSourceDropdownActionPerformed(evt);
            }
        });

        sourceSourceLabel.setText("Connector Type:");

        org.jdesktop.layout.GroupLayout connectorClass1Layout = new org.jdesktop.layout.GroupLayout(connectorClass1);
        connectorClass1.setLayout(connectorClass1Layout);
        connectorClass1Layout.setHorizontalGroup(
            connectorClass1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 610, Short.MAX_VALUE)
        );
        connectorClass1Layout.setVerticalGroup(
            connectorClass1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 490, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout sourceLayout = new org.jdesktop.layout.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(connectorClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .add(connectorClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Source", source);

        filter.setBackground(new java.awt.Color(255, 255, 255));
        filter.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                filterComponentShown(evt);
            }
        });

        filterStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Filter 1", "Filter 2", "Filter 3" }));

        filterStandardRadio.setBackground(new java.awt.Color(255, 255, 255));
        filterButtonGroup.add(filterStandardRadio);
        filterStandardRadio.setSelected(true);
        filterStandardRadio.setText("Standard Filter:");
        filterStandardRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        filterStandardRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        filterStandardRadio.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                filterStandardRadioActionPerformed(evt);
            }
        });

        filterCustomRadio.setBackground(new java.awt.Color(255, 255, 255));
        filterButtonGroup.add(filterCustomRadio);
        filterCustomRadio.setText("Custom Filter:");
        filterCustomRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        filterCustomRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        filterCustomRadio.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                filterCustomRadioActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(filterCustomText);

        org.jdesktop.layout.GroupLayout filterLayout = new org.jdesktop.layout.GroupLayout(filter);
        filter.setLayout(filterLayout);
        filterLayout.setHorizontalGroup(
            filterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterLayout.createSequentialGroup()
                .add(filterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filterLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(filterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(filterLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filterStandardRadio)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(filterStandardDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(filterCustomRadio)))
                    .add(filterLayout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 411, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(192, Short.MAX_VALUE))
        );
        filterLayout.setVerticalGroup(
            filterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(filterLayout.createSequentialGroup()
                .add(37, 37, 37)
                .add(filterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(filterStandardRadio)
                    .add(filterStandardDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(28, 28, 28)
                .add(filterCustomRadio)
                .add(15, 15, 15)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
        );
        channelView.addTab("Filter", filter);

        validation.setBackground(new java.awt.Color(255, 255, 255));
        validation.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                validationComponentShown(evt);
            }
        });

        validationStandardRadio.setBackground(new java.awt.Color(255, 255, 255));
        validationButtonGroup.add(validationStandardRadio);
        validationStandardRadio.setSelected(true);
        validationStandardRadio.setText("Standard Validator:");
        validationStandardRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validationStandardRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        validationStandardRadio.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                validationStandardRadioActionPerformed(evt);
            }
        });

        validationStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Validator 1", "Validator 2", "Validator 3" }));

        validationCustomRadio.setBackground(new java.awt.Color(255, 255, 255));
        validationButtonGroup.add(validationCustomRadio);
        validationCustomRadio.setText("Custom Validator:");
        validationCustomRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validationCustomRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        validationCustomRadio.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                validationCustomRadioActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(validationCustomText);

        org.jdesktop.layout.GroupLayout validationLayout = new org.jdesktop.layout.GroupLayout(validation);
        validation.setLayout(validationLayout);
        validationLayout.setHorizontalGroup(
            validationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationLayout.createSequentialGroup()
                .add(validationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(validationLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(validationStandardRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(validationStandardDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(validationLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(validationCustomRadio))
                    .add(validationLayout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 417, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(186, Short.MAX_VALUE))
        );
        validationLayout.setVerticalGroup(
            validationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationLayout.createSequentialGroup()
                .add(37, 37, 37)
                .add(validationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(validationStandardDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validationStandardRadio))
                .add(28, 28, 28)
                .add(validationCustomRadio)
                .add(15, 15, 15)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 280, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
        );
        channelView.addTab("Validation", validation);

        destination.setBackground(new java.awt.Color(255, 255, 255));
        destination.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(java.awt.event.ComponentEvent evt)
            {
                destinationComponentShown(evt);
            }
        });

        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));
        destinationSourceDropdown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                destinationSourceDropdownActionPerformed(evt);
            }
        });

        destinationSourceLabel.setText("Connector Type:");

        org.jdesktop.layout.GroupLayout connectorClass2Layout = new org.jdesktop.layout.GroupLayout(connectorClass2);
        connectorClass2.setLayout(connectorClass2Layout);
        connectorClass2Layout.setHorizontalGroup(
            connectorClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 610, Short.MAX_VALUE)
        );
        connectorClass2Layout.setVerticalGroup(
            connectorClass2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 358, Short.MAX_VALUE)
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout destinationLayout = new org.jdesktop.layout.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, connectorClass2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationLayout.createSequentialGroup()
                        .add(destinationSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectorClass2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Destinations", destination);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void summaryComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_summaryComponentShown
    {//GEN-HEADEREND:event_summaryComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, 1, false);
    }//GEN-LAST:event_summaryComponentShown

    private void sourceComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_sourceComponentShown
    {//GEN-HEADEREND:event_sourceComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, 1, false);
        parent.setVisibleTasks(parent.channelEditTasks, 3, true);
    }//GEN-LAST:event_sourceComponentShown

    private void filterComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_filterComponentShown
    {//GEN-HEADEREND:event_filterComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, 1, false);
    }//GEN-LAST:event_filterComponentShown

    private void validationComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_validationComponentShown
    {//GEN-HEADEREND:event_validationComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, 1, false);
    }//GEN-LAST:event_validationComponentShown

    private void destinationComponentShown(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_destinationComponentShown
    {//GEN-HEADEREND:event_destinationComponentShown
        parent.setVisibleTasks(parent.channelEditTasks, 1, true);
    }//GEN-LAST:event_destinationComponentShown

    private void sourceSourceDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceSourceDropdownActionPerformed

        for(int i=0; i<parent.sourceConnectors.size(); i++)
        {
            if(parent.sourceConnectors.get(i).getName().equalsIgnoreCase((String)sourceSourceDropdown.getSelectedItem()))
            {
                connectorClass1 = parent.sourceConnectors.get(i);
            }
        }
        connectorClass1.setDefaults();
        if (currentChannel.getSourceConnector() != null && currentChannel.getSourceConnector().getTransportName().equals(connectorClass1.getName()))
            connectorClass1.setProperties(currentChannel.getSourceConnector().getProperties());
        source.removeAll();

        org.jdesktop.layout.GroupLayout sourceLayout = (org.jdesktop.layout.GroupLayout)source.getLayout();
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(connectorClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .add(connectorClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        source.updateUI();
    }//GEN-LAST:event_sourceSourceDropdownActionPerformed

    private void destinationSourceDropdownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_destinationSourceDropdownActionPerformed
    {//GEN-HEADEREND:event_destinationSourceDropdownActionPerformed
        for(int i=0; i<parent.destinationConnectors.size(); i++)
        {
            if(parent.destinationConnectors.get(i).getName().equalsIgnoreCase((String)destinationSourceDropdown.getSelectedItem()))
                connectorClass2 = parent.destinationConnectors.get(i);
        }
        List<Connector> dc = currentChannel.getDestinationConnectors();
        Connector temp = dc.get(getDestinationConnector((String)jTable1.getValueAt(getSelectedDestination(),getColumnNumber("Destination"))));
        temp.setTransportName((String)destinationSourceDropdown.getSelectedItem());
        currentChannel.setDestinationConnectors(dc);
        if (!((String)jTable1.getValueAt(getSelectedDestination(),getColumnNumber("Connector Type"))).equals(temp.getTransportName()) && getSelectedDestination() != -1)
            jTable1.setValueAt((String)destinationSourceDropdown.getSelectedItem(),getSelectedDestination(),getColumnNumber("Connector Type"));

        Connector destinationConnector = currentChannel.getDestinationConnectors().get(getDestinationConnector((String)jTable1.getValueAt(getSelectedDestination(),getColumnNumber("Destination"))));

        // on first load of connector
        if (destinationConnector.getProperties().size() == 0)
        {
            connectorClass2.setDefaults();
            destinationConnector.setProperties(connectorClass2.getProperties());
        }

        connectorClass2.setProperties(destinationConnector.getProperties());
        
        destination.removeAll();

        org.jdesktop.layout.GroupLayout destinationLayout = (org.jdesktop.layout.GroupLayout)destination.getLayout();
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, connectorClass2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, destinationLayout.createSequentialGroup()
                        .add(destinationSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 344, Short.MAX_VALUE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectorClass2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        destination.updateUI();
    }//GEN-LAST:event_destinationSourceDropdownActionPerformed

    private void summaryEnabledCheckboxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_summaryEnabledCheckboxStateChanged
// TODO add your handling code here:
    }//GEN-LAST:event_summaryEnabledCheckboxStateChanged

    private void summaryNameFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_summaryNameFieldKeyTyped
// TODO add your handling code here:
        //parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }//GEN-LAST:event_summaryNameFieldKeyTyped

    private void filterCustomRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterCustomRadioActionPerformed
// TODO add your handling code here:
        filterCustomText.setBackground(new java.awt.Color(255,255,255));
        filterCustomText.setEnabled(true);
    }//GEN-LAST:event_filterCustomRadioActionPerformed

    private void filterStandardRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterStandardRadioActionPerformed
// TODO add your handling code here:
        filterCustomText.setBackground(new java.awt.Color(236,233,216));
        filterCustomText.setEnabled(false);
    }//GEN-LAST:event_filterStandardRadioActionPerformed

    private void validationStandardRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationStandardRadioActionPerformed
// TODO add your handling code here:
        validationCustomText.setBackground(new java.awt.Color(236,233,216));
        validationCustomText.setEnabled(false);
    }//GEN-LAST:event_validationStandardRadioActionPerformed

    private void validationCustomRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationCustomRadioActionPerformed
// TODO add your handling code here:
        validationCustomText.setBackground(new java.awt.Color(255,255,255));
        validationCustomText.setEnabled(true);
    }//GEN-LAST:event_validationCustomRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane channelView;
    private com.webreach.mirth.client.ConnectorClass connectorClass1;
    private com.webreach.mirth.client.ConnectorClass connectorClass2;
    private javax.swing.JPanel destination;
    private javax.swing.JComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private javax.swing.JPanel filter;
    private javax.swing.ButtonGroup filterButtonGroup;
    private javax.swing.JRadioButton filterCustomRadio;
    private javax.swing.JTextPane filterCustomText;
    private javax.swing.JComboBox filterStandardDropdown;
    private javax.swing.JRadioButton filterStandardRadio;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel source;
    private javax.swing.JComboBox sourceSourceDropdown;
    private javax.swing.JLabel sourceSourceLabel;
    private javax.swing.JPanel summary;
    private javax.swing.JLabel summaryDescriptionLabel;
    private javax.swing.JTextArea summaryDescriptionText;
    private javax.swing.JLabel summaryDirectionLabel1;
    private javax.swing.JLabel summaryDirectionLabel2;
    private javax.swing.JCheckBox summaryEnabledCheckbox;
    private javax.swing.JTextField summaryNameField;
    private javax.swing.JLabel summaryNameLabel;
    private javax.swing.JLabel summaryPatternLabel1;
    private javax.swing.JLabel summaryPatternLabel2;
    private javax.swing.JPanel validation;
    private javax.swing.ButtonGroup validationButtonGroup;
    private javax.swing.JRadioButton validationCustomRadio;
    private javax.swing.JTextPane validationCustomText;
    private javax.swing.JComboBox validationStandardDropdown;
    private javax.swing.JRadioButton validationStandardRadio;
    // End of variables declaration//GEN-END:variables

}
