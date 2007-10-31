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

import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXTable;
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

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class RepositoryDialog extends javax.swing.JDialog
{
    private static final int INSTALL_COLUMN = 4;
	private ExtensionManagerClient parent;
    private final String EXTENSION_TYPE_COLUMN_NAME = "Type";
    private final String EXTENSION_NAME_COLUMN_NAME = "Name";
    private final String EXTENSION_VERSION_COLUMN_NAME = "Version";
    //private final String EXTENSION_MIRTHVERSION_COLUMN_NAME = "Mirth Version";
    private final String EXTENSION_AUTHOR_COLUMN_NAME = "Author";
    private final String EXTENSION_DESCRIPTION_COLUMN_NAME = "Description";
    //private final String EXTENSION_URL_COLUMN_NAME = "Url";
    private final String EXTENSION_INSTALL_COLUMN_NAME = "Install";
    private ExtensionUtil pluginUtil = new ExtensionUtil();
    private boolean cancel = false;
    private ExtensionInfo[] displayedExtensionInfo = null;
    private Map<String, MetaData> extensions = new HashMap<String, MetaData>();
    
    /**
     * Creates new form ViewContentDialog
     */
    public RepositoryDialog(ExtensionManagerClient parent) throws ClientException
    {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = parent;
        
        initComponents();
        extensions.putAll(PlatformUI.MIRTH_FRAME.getPluginMetaData());
        extensions.putAll(PlatformUI.MIRTH_FRAME.getConnectorMetaData());
        
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = PlatformUI.MIRTH_FRAME.getSize();
        Point loc = PlatformUI.MIRTH_FRAME.getLocation();
        setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        progressBar.setVisible(false);
        installUpdatesButton.setEnabled(false);
        setVisible(true);
        makeLoadedExtensionsTable();
        
        
        checkForUpdatesButtonActionPerformed(null);
    }
    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedExtensionsTable()
    {
        //updateLoadedExtensionsTable();
        
        loadedExtensionTable = new MirthTable();
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] {EXTENSION_TYPE_COLUMN_NAME,
        EXTENSION_NAME_COLUMN_NAME, EXTENSION_VERSION_COLUMN_NAME,
        EXTENSION_AUTHOR_COLUMN_NAME,
        EXTENSION_INSTALL_COLUMN_NAME})
        {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, true };
            
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
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMinWidth(60);
        loadedExtensionTable.getColumnExt(EXTENSION_TYPE_COLUMN_NAME).setMaxWidth(60);
        
        loadedExtensionTable.getColumnExt(EXTENSION_NAME_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_VERSION_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_VERSION_COLUMN_NAME).setMinWidth(90);
        
        
        loadedExtensionTable.getColumnExt(EXTENSION_AUTHOR_COLUMN_NAME).setMaxWidth(120);
        loadedExtensionTable.getColumnExt(EXTENSION_AUTHOR_COLUMN_NAME).setMinWidth(90);
        
        // loadedExtensionTable.getColumnExt(EXTENSION_DESCRIPTION_COLUMN_NAME).setMaxWidth(50);
        // loadedExtensionTable.getColumnExt(EXTENSION_DESCRIPTION_COLUMN_NAME).setMinWidth(100);
        
        //  loadedExtensionTable.getColumnExt(EXTENSION_URL_COLUMN_NAME).setMaxWidth(50);
        //  loadedExtensionTable.getColumnExt(EXTENSION_URL_COLUMN_NAME).setMinWidth(75);
        
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMaxWidth(50);
        loadedExtensionTable.getColumnExt(EXTENSION_INSTALL_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedExtensionTable.setHighlighters(highlighter);
        }
        //  loadedExtensionTable.packTable(UIConstants.COL_MARGIN);
        loadedExtensionScrollPane.setViewportView(loadedExtensionTable);
        loadedExtensionTable.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                    if (row > -1 && displayedExtensionInfo != null)
                    {
                        
                        String type = displayedExtensionInfo[row].getType();
                        String name =  displayedExtensionInfo[row].getName();
                        String version =  displayedExtensionInfo[row].getVersion();
                        String mirthVersion =  displayedExtensionInfo[row].getMirthVersion();
                        String author =  displayedExtensionInfo[row].getAuthor();
                        String url =  displayedExtensionInfo[row].getUrl();
                        String description  = displayedExtensionInfo[row].getDescription();
                        
                        new ExtensionInfoDialog(name, type, author,mirthVersion, version, url, description);
                    }
                }
                else
                {
                    int col = loadedExtensionTable.convertColumnIndexToModel(loadedExtensionTable.getSelectedColumn());
                    if (col == INSTALL_COLUMN)
                    {
                        int row = loadedExtensionTable.convertRowIndexToModel(loadedExtensionTable.getSelectedRow());
                        boolean value = ((Boolean)loadedExtensionTable.getModel().getValueAt(row,INSTALL_COLUMN)).booleanValue();
                        loadedExtensionTable.getModel().setValueAt(!value, row, col);
                    }
                    
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
    }
    
    public void installUpdates()
    {        
    	installUpdatesButton.setEnabled(false);
    	checkForUpdatesButton.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
        	private boolean installedExtensions = false;
            public Void doInBackground()
            {
                for (int i = 0; i < loadedExtensionTable.getModel().getRowCount(); i++)
                {
                    if (cancel)
                    {
                        break;
                    }
                    boolean update = ((Boolean)loadedExtensionTable.getModel().getValueAt(i,INSTALL_COLUMN)).booleanValue();
                    if (update)
                    {
                        String name = displayedExtensionInfo[i].getName();                
                        statusLabel.setText("Downloading extension: " + name);
                        if (cancel)
                        {
                            break;
                        }
                        progressBar.setVisible(true);
                        File file = pluginUtil.downloadFileToDisk(pluginUtil.getDynamicURL(displayedExtensionInfo[i].getDownloadUrl(), displayedExtensionInfo[i].getVersion(), displayedExtensionInfo[i].getName(), displayedExtensionInfo[i].getId()), statusLabel, progressBar);
                        progressBar.setVisible(false);
                        statusLabel.setText("Installing extension: " + name);
                        if (cancel)
                        {
                            break;
                        }
                        statusLabel.setText("Updating extension: " + displayedExtensionInfo[i].getName());
                        parent.install(displayedExtensionInfo[i].getType().toLowerCase() + "s", file);
                        installedExtensions = true;
                    }
                }
                
                return null;
            }
            
            public void done()
            {
            	checkForUpdatesButton.setEnabled(true);
            	if (installedExtensions){
	                statusLabel.setText("Extensions Installed!");
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
        ArrayList<String> updateVersion = new ArrayList<String>();
        statusLabel.setText("Retrieving extension list...");
        progressBar.setIndeterminate(true);
        String extensionInfoXML = pluginUtil.getStringFromURL(pluginUtil.getDynamicURL("http://extensions.mirthproject.org/repository/?mirthversion=${mirthVersion}&serverid=${serverid}", "",""));
        ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[]{ExtensionInfo.class});
        ExtensionInfo[] extensionInfo = null;
        
        try
        {
            extensionInfo = (ExtensionInfo[]) serializer.fromXML(extensionInfoXML);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        // Only display extensions for download that are not already isntalled
        if (extensionInfo != null)
        {
        	ArrayList<Object[]> tempTableData = new ArrayList<Object[]>();
        	ArrayList<ExtensionInfo> tempExtensionInfoList = new ArrayList<ExtensionInfo>();
        	
	        for (int i = 0; i < extensionInfo.length; i++)
	        {
	        	if (!extensions.containsKey(extensionInfo[i].getName()))
	        	{
	        		Object[] tempRow = new Object[7];
	        		tempRow[0] = extensionInfo[i].getType();
	        		tempRow[1] = extensionInfo[i].getName();
	        		tempRow[2] = extensionInfo[i].getVersion();
	        		tempRow[3] = extensionInfo[i].getAuthor();
		            //tableData[i][4] = extensionInfo[i].getDescription();
		            // tableData[i][5] = extensionInfo[i].getDescription();
		            // tableData[i][5] = extensionInfo[i].getUrl();
	        		tempRow[INSTALL_COLUMN] = Boolean.FALSE;
	        		
	        		tempTableData.add(tempRow);
	        		tempExtensionInfoList.add(extensionInfo[i]);
	        	}
	        }

	        tableData = new Object[tempTableData.size()][7];
	        displayedExtensionInfo = new ExtensionInfo[tempTableData.size()];
	        
	        for (int i = 0; i < tempTableData.size(); i++)
	        {
	        	tableData[i] = tempTableData.get(i);
	        	// Create a list of the installed extensions for reference by selected row.
	        	displayedExtensionInfo[i] = tempExtensionInfoList.get(i);
	        }
        }
        
        if (extensionInfo == null || extensionInfo.length == 0)
        {
        	statusLabel.setText("No Extensions Found.");
        	return;
        }
        else if (tableData.length == 0)
        {
        	statusLabel.setText("No New Extensions Found.");
        	return;
        }
        else
        {
        	installUpdatesButton.setEnabled(true);
        	statusLabel.setText("Ready to Install Extensions!");
        }
        progressBar.setIndeterminate(false);
        
        if (loadedExtensionTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedExtensionTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
        }
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
        setTitle("Available Extensions");
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

        checkForUpdatesButton.setText("Refresh Extensions");
        checkForUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesButtonActionPerformed(evt);
            }
        });

        installUpdatesButton.setText("Install Selected");
        installUpdatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installUpdatesButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Available Extensions");

        loadedExtensionScrollPane.setMaximumSize(null);
        loadedExtensionScrollPane.setMinimumSize(null);
        loadedExtensionScrollPane.setPreferredSize(new java.awt.Dimension(350, 200));
        loadedExtensionTable.setModel(new RefreshTableModel(new Object[][]{}, new String[] {EXTENSION_TYPE_COLUMN_NAME,
            EXTENSION_NAME_COLUMN_NAME, EXTENSION_VERSION_COLUMN_NAME,
            EXTENSION_AUTHOR_COLUMN_NAME,
            EXTENSION_INSTALL_COLUMN_NAME}));
loadedExtensionScrollPane.setViewportView(loadedExtensionTable);

org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
jPanel1.setLayout(jPanel1Layout);
jPanel1Layout.setHorizontalGroup(
    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
    .add(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loadedExtensionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
            .add(jLabel2)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installUpdatesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkForUpdatesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeButton))
            .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 497, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void checkForUpdatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkForUpdatesButtonActionPerformed

        PlatformUI.MIRTH_FRAME.setWorking("Checking for updates...", true);
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
                PlatformUI.MIRTH_FRAME.setWorking("", false);
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
