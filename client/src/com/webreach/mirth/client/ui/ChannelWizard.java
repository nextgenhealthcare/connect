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

import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.SwingWorker;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.plugins.ChannelWizardPlugin;

/**
 * A dialog for creating a new channel
 */
public class ChannelWizard extends javax.swing.JDialog
{
    private Frame parent;
    private Map<String, ChannelWizardPlugin> loadedWizardPlugins = new HashMap<String, ChannelWizardPlugin>();
    private final String DEFAULT_COMBOBOX_VALUE = "Select Channel Wizard";

    /** Creates new form ChannelWizard */
    public ChannelWizard()
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        loadPlugins();
        
        if (loadedWizardPlugins.size() == 0)
        {
        	skipWizardButtonActionPerformed(null);
        	return;
        }
        
        String[] loadedWizardPluginNames = new String[loadedWizardPlugins.size() + 1];
        
        loadedWizardPluginNames[0] = DEFAULT_COMBOBOX_VALUE;
        
        TreeSet<String> sortedKeys = new TreeSet<String>();
        sortedKeys.addAll(loadedWizardPlugins.keySet());
        
        int index = 1;
        for (String key : sortedKeys) {
        	loadedWizardPluginNames[index] = key;
        	index++;
        }
        
        wizardComboBox.setModel(new javax.swing.DefaultComboBoxModel(loadedWizardPluginNames));
        
        wizardComboBoxActionPerformed(null);
        
        jLabel2.setText("New Channel");
        jLabel2.setForeground(UIConstants.HEADER_TITLE_TEXT_COLOR);
        setModal(true);
        pack();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();
        
        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
        	setLocationRelativeTo(null);
        } else {
	        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        
        setResizable(false);
        setVisible(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    //Extension point for ExtensionPoint.Type.CLIENT_CHANNEL_WIZARD
    @ExtensionPointDefinition(mode=ExtensionPoint.Mode.CLIENT, type=ExtensionPoint.Type.CLIENT_CHANNEL_WIZARD)
    public void loadPlugins()
    {
        try
        {
            Map<String, PluginMetaData> plugins = parent.getPluginMetaData();
            for (PluginMetaData metaData : plugins.values())
            {
                if (metaData.isEnabled())
                {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints())
                    {
                        try
                        {
                            if (extensionPoint.getMode().equals(ExtensionPoint.Mode.CLIENT) && extensionPoint.getType().equals(ExtensionPoint.Type.CLIENT_CHANNEL_WIZARD) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0)
                            {
                                String pluginName = extensionPoint.getName();
                                Class clazz = Class.forName(extensionPoint.getClassName());
                                Constructor[] constructors = clazz.getDeclaredConstructors();
                                for (int i=0; i < constructors.length; i++) {
                                    Class parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters is 1.
                                    if (parameters.length == 1) {
                                        ChannelWizardPlugin wizardPlugin = (ChannelWizardPlugin) constructors[i].newInstance(new Object[] { pluginName });
                                        loadedWizardPlugins.put(pluginName, wizardPlugin);
                                        i = constructors.length;
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            parent.alertException(this, e.getStackTrace(), e.getMessage());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        channelOverview = new javax.swing.JPanel();
        runWizardButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mirthHeadingPanel1 = new com.webreach.mirth.client.ui.MirthHeadingPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        wizardLabel = new javax.swing.JLabel();
        wizardComboBox = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();
        skipWizardButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Channel");

        channelOverview.setBackground(new java.awt.Color(255, 255, 255));
        channelOverview.setName(""); // NOI18N

        runWizardButton.setText("Run Wizard");
        runWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runWizardButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("New Channel");

        org.jdesktop.layout.GroupLayout mirthHeadingPanel1Layout = new org.jdesktop.layout.GroupLayout(mirthHeadingPanel1);
        mirthHeadingPanel1.setLayout(mirthHeadingPanel1Layout);
        mirthHeadingPanel1Layout.setHorizontalGroup(
            mirthHeadingPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mirthHeadingPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 255, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(114, Short.MAX_VALUE))
        );
        mirthHeadingPanel1Layout.setVerticalGroup(
            mirthHeadingPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mirthHeadingPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Select a channel wizard...");

        wizardLabel.setText("Wizard:");

        wizardComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wizardComboBoxActionPerformed(evt);
            }
        });

        descriptionLabel.setText("Description:");

        descriptionTextPane.setEditable(false);
        descriptionScrollPane.setViewportView(descriptionTextPane);

        skipWizardButton.setText("Skip Wizard");
        skipWizardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipWizardButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout channelOverviewLayout = new org.jdesktop.layout.GroupLayout(channelOverview);
        channelOverview.setLayout(channelOverviewLayout);
        channelOverviewLayout.setHorizontalGroup(
            channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .add(channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, wizardLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, descriptionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(wizardComboBox, 0, 288, Short.MAX_VALUE)
                    .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                .add(20, 20, 20))
            .add(channelOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(228, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, channelOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addContainerGap())
            .add(mirthHeadingPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
            .add(channelOverviewLayout.createSequentialGroup()
                .addContainerGap(96, Short.MAX_VALUE)
                .add(cancelButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(runWizardButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(skipWizardButton)
                .addContainerGap())
        );

        channelOverviewLayout.linkSize(new java.awt.Component[] {cancelButton, runWizardButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        channelOverviewLayout.setVerticalGroup(
            channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, channelOverviewLayout.createSequentialGroup()
                .add(mirthHeadingPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wizardLabel)
                    .add(wizardComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .add(20, 20, 20)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(channelOverviewLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(runWizardButton)
                    .add(skipWizardButton))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelOverview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(channelOverview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void skipWizardButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_skipWizardButtonActionPerformed
    {//GEN-HEADEREND:event_skipWizardButtonActionPerformed
    	parent.createNewChannel();
    	this.dispose();
    }//GEN-LAST:event_skipWizardButtonActionPerformed

    private void wizardComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wizardComboBoxActionPerformed
    {//GEN-HEADEREND:event_wizardComboBoxActionPerformed
    	String wizard = (String)wizardComboBox.getSelectedItem();
    	
    	if (wizard.equals(DEFAULT_COMBOBOX_VALUE))
    	{
    		runWizardButton.setEnabled(false);
    		descriptionTextPane.setText("Select a channel wizard or press \"Skip Wizard\" to continue...");
    	}
    	else
    	{
    		runWizardButton.setEnabled(true);
    		descriptionTextPane.setText(parent.getPluginMetaData().get((String)wizardComboBox.getSelectedItem()).getDescription());
    	}
    }//GEN-LAST:event_wizardComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
    	this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void runWizardButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_runWizardButtonActionPerformed
    {//GEN-HEADEREND:event_runWizardButtonActionPerformed
    	
    	final String wizard = (String) wizardComboBox.getSelectedItem();
    	parent.setWorking("Running " + wizard + " wizard...", true);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
        	Channel channel = null;
        	
            public Void doInBackground()
            {
            	channel = loadedWizardPlugins.get(wizard).runWizard();
            	return null;
            }

            public void done()
            { 
            	if (channel != null)
            	{
	                parent.editChannel(channel);
	                parent.enableSave();
            	}
                parent.setWorking("", false);
            }
        };

        worker.execute();
        
    	this.dispose();
    }//GEN-LAST:event_runWizardButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel channelOverview;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private com.webreach.mirth.client.ui.MirthHeadingPanel mirthHeadingPanel1;
    private javax.swing.JButton runWizardButton;
    private javax.swing.JButton skipWizardButton;
    private javax.swing.JComboBox wizardComboBox;
    private javax.swing.JLabel wizardLabel;
    // End of variables declaration//GEN-END:variables

}
