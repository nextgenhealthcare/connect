/*
 * ChannelSetup.java
 *
 * Created on April 28, 2006, 11:05 AM
 */

package com.webreach.mirth.client;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Validator;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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

    public ChannelSetup(JFrame parent)
    {
        this.parent = (Frame)parent;
        initComponents();

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

    public void editChannel(int index)
    {
        this.index = index;
        currentChannel = parent.channels.get(index);
        loadChannelInfo();
    }

    public void addChannel(Channel channel)
    {
        index = -1;
        currentChannel = channel;
        loadChannelInfo();
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

        String[] sources = new String[1];
        if (currentChannel.getSourceConnector() != null)
            sources[0] = currentChannel.getSourceConnector().getName();
        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(sources));

        String[] filters = new String[1];
//        filters[0] = currentChannel.getFilter().getName();
        filterStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(filters));

        String[] validators = new String[1];
//        validators[0] = currentChannel.getValidator().getName();
        validationStandardDropdown.setModel(new javax.swing.DefaultComboBoxModel(validators));

        List<Connector> destinationList = currentChannel.getDestinationConnectors();
        String[] destinations = new String[destinationList.size()];

        for (int i=0; i < destinationList.size(); i++)
            destinations[i] = destinationList.get(i).getName();
        destinationDestinationDropdown.setModel(new javax.swing.DefaultComboBoxModel(destinations));
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
	sourceConnector.getProperties().put("key", "value");
	sourceConnector.setTransformer(sourceTransformer);

        Connector destinationConnector1 = new Connector();
	destinationConnector1.setName("destinationConnector1");
	destinationConnector1.getProperties().put("key1", "value1");
	destinationConnector1.getProperties().put("key2", "value2");
	destinationConnector1.setTransformer(destinationTransformer);

	Connector destinationConnector2 = new Connector();
	destinationConnector2.setName("destinationConnector2");
	destinationConnector2.getProperties().put("key1", "value1");
	destinationConnector2.getProperties().put("key2", "value2");
	destinationConnector2.setTransformer(destinationTransformer);

        Filter filter = new Filter();
        filter.setScript("return true;");

        Validator validator = new Validator();
	validator.getProfiles().put("profile1", "<XLST>");

        currentChannel.setName(summaryNameField.getText());
        currentChannel.setDescription(summaryDescriptionText.getText());
        currentChannel.setEnabled(summaryEnabledCheckbox.isSelected());
	currentChannel.setInitialStatus(Channel.Status.STOPPED);
        currentChannel.setModified(false);
	currentChannel.setSourceConnector(sourceConnector);

	currentChannel.setFilter(filter);
	currentChannel.setValidator(validator);

        currentChannel.getProperties().put("test", "test");

	currentChannel.getDestinationConnectors().add(destinationConnector1);
	currentChannel.getDestinationConnectors().add(destinationConnector2);

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
        emailSender1 = new com.webreach.mirth.client.EmailSender();
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
        destinationDestinationDropdown = new javax.swing.JComboBox();
        destinationSourceDropdown = new javax.swing.JComboBox();
        destinationSourceNewButton = new javax.swing.JButton();
        destinationSourceLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        emailSender2 = new com.webreach.mirth.client.EmailSender();

        summary.setBackground(new java.awt.Color(255, 255, 255));
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
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryDescriptionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(summaryDirectionLabel1)
                            .add(summaryNameLabel)
                            .add(summaryPatternLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(summaryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryDirectionLabel2)
                    .add(summaryPatternLabel2)
                    .add(summaryLayout.createSequentialGroup()
                        .add(summaryNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(45, 45, 45)
                        .add(summaryEnabledCheckbox))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
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
                .addContainerGap(196, Short.MAX_VALUE))
        );
        channelView.addTab("Summary", summary);

        source.setBackground(new java.awt.Color(255, 255, 255));
        sourceSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));

        sourceSourceLabel.setText("Connector Type:");

        org.jdesktop.layout.GroupLayout sourceLayout = new org.jdesktop.layout.GroupLayout(source);
        source.setLayout(sourceLayout);
        sourceLayout.setHorizontalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(emailSender1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                    .add(sourceLayout.createSequentialGroup()
                        .add(sourceSourceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        sourceLayout.setVerticalGroup(
            sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourceSourceLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sourceSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(emailSender1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Source", source);

        filter.setBackground(new java.awt.Color(255, 255, 255));
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
                .addContainerGap(18, Short.MAX_VALUE))
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        channelView.addTab("Filter", filter);

        validation.setBackground(new java.awt.Color(255, 255, 255));
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
                .addContainerGap(12, Short.MAX_VALUE))
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        channelView.addTab("Validation", validation);

        destination.setBackground(new java.awt.Color(255, 255, 255));
        destinationDestinationDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Destination 1", "Destination 2", "Destination 3", "Destination 4" }));

        destinationSourceDropdown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TCP/IP", "Database", "Email" }));

        destinationSourceNewButton.setText("New");

        destinationSourceLabel.setText("Connector Type:");

        jLabel1.setText("Destination:");

        org.jdesktop.layout.GroupLayout destinationLayout = new org.jdesktop.layout.GroupLayout(destination);
        destination.setLayout(destinationLayout);
        destinationLayout.setHorizontalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationLayout.createSequentialGroup()
                        .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(destinationLayout.createSequentialGroup()
                                .add(33, 33, 33)
                                .add(jLabel1))
                            .add(destinationLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(destinationSourceLabel)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(destinationSourceDropdown, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(destinationDestinationDropdown, 0, 87, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destinationSourceNewButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(destinationLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(emailSender2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)))
                .addContainerGap())
        );
        destinationLayout.setVerticalGroup(
            destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(destinationLayout.createSequentialGroup()
                .addContainerGap()
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationDestinationDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(destinationSourceNewButton)
                    .add(jLabel1))
                .add(18, 18, 18)
                .add(destinationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationSourceLabel)
                    .add(destinationSourceDropdown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(emailSender2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addContainerGap())
        );
        channelView.addTab("Destinations", destination);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JPanel destination;
    private javax.swing.JComboBox destinationDestinationDropdown;
    private javax.swing.JComboBox destinationSourceDropdown;
    private javax.swing.JLabel destinationSourceLabel;
    private javax.swing.JButton destinationSourceNewButton;
    private com.webreach.mirth.client.EmailSender emailSender1;
    private com.webreach.mirth.client.EmailSender emailSender2;
    private javax.swing.JPanel filter;
    private javax.swing.ButtonGroup filterButtonGroup;
    private javax.swing.JRadioButton filterCustomRadio;
    private javax.swing.JTextPane filterCustomText;
    private javax.swing.JComboBox filterStandardDropdown;
    private javax.swing.JRadioButton filterStandardRadio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
