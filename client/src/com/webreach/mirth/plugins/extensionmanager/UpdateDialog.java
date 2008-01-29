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

package com.webreach.mirth.plugins.extensionmanager;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.RefreshTableModel;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class UpdateDialog extends javax.swing.JDialog
{
    private ExtensionManagerClient parent;
    
    private final String EXTENSION_NAME_COLUMN_NAME = "Name";
    private final String EXTENSION_INSTALLED_VERSION_COLUMN_NAME = "Installed Version";
    private final String EXTENSION_INSTALL_UPDATE_COLUMN_NAME = "Update";
    private final String EXTENSION_UPDATE_VERSION_COLUMN_NAME = "Update Version";
    private Map<String, MetaData> extensions = new HashMap<String, MetaData>();
    private Map<String, ExtensionInfo> updatableExtensions = new HashMap<String, ExtensionInfo>();
    private ExtensionUtil pluginUtil = new ExtensionUtil();
    private boolean cancel = false;
    /**
     * Creates new form ViewContentDialog
     */
    public UpdateDialog(ExtensionManagerClient parent) throws ClientException
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = parent;
        
        initComponents();
        extensions.putAll(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        extensions.putAll(PlatformUI.MIRTH_FRAME.getConnectorMetaData());
        //extensions.put
        //setPreferredSize(new Dimension(400,400));
        
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();
        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        progressBar.setVisible(false);
        checkForUpdatesButtonActionPerformed(null);
        makeLoadedExtensionsTable();
        
        setVisible(true);
    }
    
    public UpdateDialog(ExtensionManagerClient parent, MetaData metadata) throws ClientException
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = parent;
        
        initComponents();
        extensions.put(metadata.getName(), metadata);        
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();
        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        progressBar.setVisible(false);
        installUpdatesButton.setEnabled(false);
        checkForUpdatesButtonActionPerformed(null);
        makeLoadedExtensionsTable();
        
        setVisible(true);
    }
    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedExtensionsTable()
    {
        
        loadedExtensionTable = new MirthTable();
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] { EXTENSION_NAME_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME,  EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_INSTALL_UPDATE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { false, false, false, true };
            
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });
        loadedExtensionTable.setDragEnabled(false);
        loadedExtensionTable.setRowSelectionAllowed(true);
        loadedExtensionTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedExtensionTable.setFocusable(false);
        loadedExtensionTable.setOpaque(true);
        loadedExtensionTable.getTableHeader().setReorderingAllowed(true);
        loadedExtensionTable.setSortable(true);
        loadedExtensionTable.setSelectionMode(0);
        //loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMaxWidth(280);
        loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALLED_VERSION_COLUMN_NAME).setMinWidth(90);
        
        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_UPDATE_VERSION_COLUMN_NAME).setMinWidth(90);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_UPDATE_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_UPDATE_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedExtensionTable.setHighlighters(highlighter);
        }
        loadedExtensionTable.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                    if (row > -1 && updatableExtensions != null)
                    {
                        ExtensionInfo extensionInfo = updatableExtensions.get(loadedExtensionTable.getModel().getValueAt(row, 0) + "" + loadedExtensionTable.getModel().getValueAt(row, 2));
                        String type = extensionInfo.getType();
                        String name =  extensionInfo.getName();
                        String version =  extensionInfo.getVersion();
                        String mirthVersion =  extensionInfo.getMirthVersion();
                        String author =  extensionInfo.getAuthor();
                        String url =  extensionInfo.getUrl();
                        String description  = extensionInfo.getDescription();
                                            
                        new ExtensionInfoDialog(name, type, author,mirthVersion, version, url, description);
                    }
                }
                else
                {
                    /*
                    int col = loadedExtensionTable.convertColumnIndexToModel(loadedExtensionTable.getSelectedColumn());
                    if (col == 3)
                    {
                        int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                        boolean value = ((Boolean)loadedExtensionTable.getModel().getValueAt(row,3)).booleanValue();
                        loadedExtensionTable.getModel().setValueAt(!value, row, col);
                    }
                    */
                    
                }
            }
            public void mouseEntered(MouseEvent e)
            {
            }
            public void mouseExited(MouseEvent e)
            {
            }
            public void mousePressed(MouseEvent e)
            {
            }
            public void mouseReleased(MouseEvent e)
            {
            }
        });
        loadedExtensionTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                loadedExtensionScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);
    }
    
    
    
    public boolean isUpdateAvailable(MetaData extension)
    {
        return false;
    }
    public void installUpdates()
    {
        installUpdatesButton.setEnabled(false);
        checkForUpdatesButton.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
        	private boolean installedUpdates = false;
            public Void doInBackground()
            {
                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++)
                {
                    boolean update = ((Boolean)loadedExtensionTable.getModel().getValueAt(i,3)).booleanValue();
                    if (update)
                    {
                        String name = (String)loadedExtensionTable.getModel().getValueAt(i, 0);
                        String version = (String)loadedExtensionTable.getModel().getValueAt(i, 2);
                        ExtensionInfo plugin = updatableExtensions.get(name + version);
                        statusLabel.setText("Downloading extension: " + plugin.getName());
                        if (cancel)
                        {
                            break;
                        }
                        progressBar.setVisible(true);
                        File file = pluginUtil.downloadFileToDisk(pluginUtil.getDynamicURL(plugin.getDownloadUrl(), plugin.getVersion(), plugin.getName(), plugin.getId()), statusLabel, progressBar);
                        progressBar.setVisible(false);
                        if (cancel)
                        {
                            break;
                        }
                        statusLabel.setText("Updating extension: " + plugin.getName());
                        parent.install(plugin.getType().toLowerCase() + "s", file);
                        installedUpdates = true;
                    }
                }
                
                return null;
            }
            
            public void done()
            {
            	
            	checkForUpdatesButton.setEnabled(true);
            	if (installedUpdates){
	                statusLabel.setText("Updates Installed!");
	                parent.finishInstall();
	                dispose();
            	}
            }
        };
        
        worker.execute();
    }
    
    public void updateLoadedExtensionsTable()
    {
        Object[][] tableData = null;
        int tableSize = 0;
        ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[]{ExtensionInfo.class});
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressBar.setMaximum(extensions.size());
        for (MetaData metaData : extensions.values())
        {
        	if (cancel){
        		return;
        	}
            if (metaData.getUpdateUrl() != null)
            {
                statusLabel.setText("Checking: " + metaData.getName());
                String url = pluginUtil.getDynamicURL(metaData.getUpdateUrl(), metaData.getPluginVersion(), metaData.getName());
                String updateText = pluginUtil.getStringFromURL(url);
                //System.out.println(updateText);
                if (updateText.length() > 0)
                {
                	ExtensionInfo[] extensionInfo = (ExtensionInfo[]) serializer.fromXML(updateText);
                	for (int i = 0; i < extensionInfo.length; i++){
                		updatableExtensions.put(extensionInfo[i].getName() + extensionInfo[i].getVersion(), extensionInfo[i]);
                	}
                }
            }
            progressBar.setValue(progressBar.getValue()+1);
        }
        if (updatableExtensions.size() > 0){
        	statusLabel.setText("Ready to Install Updates!");
        	installUpdatesButton.setEnabled(true);
        }else{
        	statusLabel.setText("No Updates Found.");
        }
        tableSize = updatableExtensions.size();
        progressBar.setVisible(false);
        tableData = new Object[tableSize][4];
        
        int i = 0;
        for (ExtensionInfo metaData : updatableExtensions.values())
        {
            
            tableData[i][0] = metaData.getName();
            tableData[i][1] = extensions.get(metaData.getName()).getPluginVersion();
            tableData[i][2] = metaData.getVersion();
            tableData[i][3] = Boolean.TRUE;
            i++;
        }
        
        
        if (loadedExtensionTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedExtensionTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
        }
        progressBar.setValue(0);
    }
	
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        checkForUpdatesButton = new javax.swing.JButton();
        installUpdatesButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        loadedExtensionScrollPane = new javax.swing.JScrollPane();
        loadedExtensionTable = new com.webreach.mirth.client.ui.components.MirthTable();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Extension Updater");
        setResizable(false);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMaximumSize(null);
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 400));
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        statusLabel.setText("Idle");

        checkForUpdatesButton.setText("Check for Updates");
        checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesButtonActionPerformed(evt);
            }
        });

        installUpdatesButton.setText("Install Updates");
        installUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installUpdatesButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Extension Updates");

        loadedExtensionScrollPane.setMaximumSize(null);
        loadedExtensionScrollPane.setMinimumSize(null);
        loadedExtensionScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] { EXTENSION_NAME_COLUMN_NAME, EXTENSION_INSTALLED_VERSION_COLUMN_NAME,  EXTENSION_UPDATE_VERSION_COLUMN_NAME, EXTENSION_INSTALL_UPDATE_COLUMN_NAME }));
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installUpdatesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(checkForUpdatesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(closeButton))
                    .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 437, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel)
                .add(10, 10, 10)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(closeButton)
                        .add(checkForUpdatesButton)
                        .add(installUpdatesButton))
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void checkForUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkForUpdatesButtonActionPerformed
// TODO add your handling code here:
        //Probably should be a swing worker
        
        //PlatformUI.MIRTH_FRAME.setWorking("Checking for updates...", true);
        checkForUpdatesButton.setEnabled(false);
        installUpdatesButton.setEnabled(false);

        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            public Void doInBackground()
            {
                updateLoadedExtensionsTable();
                return null;
            }
            
            public void done()
            {
            	checkForUpdatesButton.setEnabled(true);
            	
                //PlatformUI.MIRTH_FRAME.setWorking("", false);
            	//makeLoadedExtensionsTable();
            }
        };
        
        worker.execute();
        
    }//GEN-LAST:event_checkForUpdatesButtonActionPerformed
    
    private void installUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installUpdatesButtonActionPerformed
        installUpdates();
    }//GEN-LAST:event_installUpdatesButtonActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        cancel = true;
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton checkForUpdatesButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton installUpdatesButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane loadedExtensionScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedExtensionTable;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
    
}
