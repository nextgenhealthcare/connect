/*
 * PluginManagerPanel.java
 *
 * Created on July 17, 2007, 4:32 PM
 */

package com.webreach.mirth.plugins.pluginmanager;

import com.webreach.mirth.client.ui.*;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.PluginMetaData;
import java.io.File;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

/**
 *
 * @author  brendanh
 */
public class PluginManagerPanel extends javax.swing.JPanel
{
    private PluginManagerClient parent;
    
    private final String PLUGIN_NAME_COLUMN_NAME = "Plugin Name";
    private final String PLUGIN_AUTHOR_COLUMN_NAME = "Author";
    private final String PLUGIN_URL_COLUMN_NAME = "URL";
    private final String PLUGIN_VERSION_COLUMN_NAME = "Plugin Version";
    private final String PLUGIN_MIRTH_VERSION_COLUMN_NAME = "Mirth Version";
    private final String PLUGIN_ENABLED_COLUMN_NAME = "Enabled";
    
    private Map<String, PluginMetaData> pluginData = null;
    private Map<String, ConnectorMetaData> connectorData = null;    
    
    /** Creates new form PluginManagerPanel */
    public PluginManagerPanel(PluginManagerClient parent)
    {        
        this.parent = parent;
        initComponents();
        makeLoadedConnectorsTable();
        makeLoadedPluginsTable();
    }
    
    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedConnectorsTable()
    {
        updateLoadedConnectorsTable();
        
        loadedConnectorsTable.setDragEnabled(false);
        loadedConnectorsTable.setRowSelectionAllowed(false);
        loadedConnectorsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedConnectorsTable.setFocusable(false);
        loadedConnectorsTable.setOpaque(true);
        loadedConnectorsTable.getTableHeader().setReorderingAllowed(true);
        loadedConnectorsTable.setSortable(true);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedConnectorsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_ENABLED_COLUMN_NAME).setMaxWidth(50);
        loadedConnectorsTable.getColumnExt(PLUGIN_ENABLED_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedConnectorsTable.setHighlighters(highlighter);
        }
        
        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);
    }
    
    public void setConnectorData(Map<String, ConnectorMetaData> connectorData)
    {
        this.connectorData = connectorData;
        updateLoadedConnectorsTable();
    }
    
    public Map<String, ConnectorMetaData> getConnectorData()
    {
        for(int i = 0; i < loadedConnectorsTable.getRowCount(); i++)
        {
            connectorData.get(loadedConnectorsTable.getModel().getValueAt(i,0)).setEnabled(((Boolean)loadedConnectorsTable.getModel().getValueAt(i,5)).booleanValue());
        }
        return this.connectorData;
    }
    
    public void updateLoadedConnectorsTable()
    {
        Object[][] tableData = null;
        int tableSize = 0;
        
        if (connectorData != null)
        {
            tableSize = connectorData.size();
            tableData = new Object[tableSize][6];
            
            int i = 0;
            for (ConnectorMetaData metaData : connectorData.values())
            {
                tableData[i][0] = metaData.getName();
                tableData[i][1] = metaData.getAuthor();
                tableData[i][2] = metaData.getUrl();
                tableData[i][3] = metaData.getPluginVersion();
                tableData[i][4] = metaData.getMirthVersion();
                if (metaData.isEnabled())
                    tableData[i][5] = Boolean.TRUE;
                else
                    tableData[i][5] = Boolean.FALSE;
                i++;
            }
        }
        
        if (connectorData != null && loadedConnectorsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedConnectorsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            loadedConnectorsTable = new MirthTable();
            loadedConnectorsTable.setModel(new RefreshTableModel(tableData, new String[] { PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME, PLUGIN_MIRTH_VERSION_COLUMN_NAME, PLUGIN_ENABLED_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, true };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
    }
    
    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeLoadedPluginsTable()
    {
        updateLoadedPluginsTable();
        
        loadedPluginsTable.setDragEnabled(false);
        loadedPluginsTable.setRowSelectionAllowed(false);
        loadedPluginsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedPluginsTable.setFocusable(false);
        loadedPluginsTable.setOpaque(true);
        loadedPluginsTable.getTableHeader().setReorderingAllowed(true);
        loadedPluginsTable.setSortable(true);
        
        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedPluginsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedPluginsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedPluginsTable.getColumnExt(PLUGIN_ENABLED_COLUMN_NAME).setMaxWidth(50);
        loadedPluginsTable.getColumnExt(PLUGIN_ENABLED_COLUMN_NAME).setMinWidth(50);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedPluginsTable.setHighlighters(highlighter);
        }
        
        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);
    }
    
    public void setPluginData(Map<String, PluginMetaData> pluginData)
    {
        this.pluginData = pluginData;
        updateLoadedPluginsTable();
    }
    
    public Map<String, PluginMetaData> getPluginData()
    {
        for(int i = 0; i < loadedPluginsTable.getRowCount(); i++)
        {
            pluginData.get(loadedPluginsTable.getModel().getValueAt(i,0)).setEnabled(((Boolean)loadedPluginsTable.getModel().getValueAt(i,5)).booleanValue());
        }
        return this.pluginData;
    }
    
    public void updateLoadedPluginsTable()
    {
        Object[][] tableData = null;
        int tableSize = 0;
        
        if (pluginData != null)
        {
            tableSize = pluginData.size();
            tableData = new Object[tableSize][6];
            
            int i = 0;
            for (PluginMetaData metaData : pluginData.values())
            {
                tableData[i][0] = metaData.getName();
                tableData[i][1] = metaData.getAuthor();
                tableData[i][2] = metaData.getUrl();
                tableData[i][3] = metaData.getPluginVersion();
                tableData[i][4] = metaData.getMirthVersion();
                if (metaData.isEnabled())
                    tableData[i][5] = Boolean.TRUE;
                else
                    tableData[i][5] = Boolean.FALSE;
                i++;
            }
        }
        
        if (pluginData != null && loadedPluginsTable != null)
        {
            RefreshTableModel model = (RefreshTableModel) loadedPluginsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            loadedPluginsTable = new MirthTable();
            loadedPluginsTable.setModel(new RefreshTableModel(tableData, new String[] { PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME, PLUGIN_MIRTH_VERSION_COLUMN_NAME, PLUGIN_ENABLED_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, true };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        loadedPluginsPanel = new javax.swing.JPanel();
        loadedPluginsScrollPane = new javax.swing.JScrollPane();
        loadedPluginsTable = null;
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fileText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        installButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        connectorRadioButton = new javax.swing.JRadioButton();
        pluginRadioButton = new javax.swing.JRadioButton();
        loadedConnectorsPanel = new javax.swing.JPanel();
        loadedConnectorsScrollPane = new javax.swing.JScrollPane();
        loadedConnectorsTable = null;

        setBackground(new java.awt.Color(255, 255, 255));
        loadedPluginsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedPluginsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Loaded Plugins", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);

        org.jdesktop.layout.GroupLayout loadedPluginsPanelLayout = new org.jdesktop.layout.GroupLayout(loadedPluginsPanel);
        loadedPluginsPanel.setLayout(loadedPluginsPanelLayout);
        loadedPluginsPanelLayout.setHorizontalGroup(
            loadedPluginsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loadedPluginsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );
        loadedPluginsPanelLayout.setVerticalGroup(
            loadedPluginsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, loadedPluginsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Install Extension", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("File:");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                browseButtonActionPerformed(evt);
            }
        });

        installButton.setText("Install");
        installButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                installButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Type:");

        connectorRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(connectorRadioButton);
        connectorRadioButton.setSelected(true);
        connectorRadioButton.setText("Connector");
        connectorRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        connectorRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        pluginRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(pluginRadioButton);
        pluginRadioButton.setText("Plugin");
        pluginRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pluginRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(fileText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installButton))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(connectorRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pluginRadioButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(connectorRadioButton)
                    .add(pluginRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(installButton)
                    .add(browseButton)
                    .add(fileText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loadedConnectorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedConnectorsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Loaded Connectors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);

        org.jdesktop.layout.GroupLayout loadedConnectorsPanelLayout = new org.jdesktop.layout.GroupLayout(loadedConnectorsPanel);
        loadedConnectorsPanel.setLayout(loadedConnectorsPanelLayout);
        loadedConnectorsPanelLayout.setHorizontalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loadedConnectorsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );
        loadedConnectorsPanelLayout.setVerticalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, loadedConnectorsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(loadedConnectorsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(loadedPluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loadedConnectorsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadedPluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_installButtonActionPerformed
    {//GEN-HEADEREND:event_installButtonActionPerformed
        String location;
        
        if(connectorRadioButton.isSelected())
            location = "connectors";
        else
            location = "plugins";
        
        if(parent.install(location, new File(fileText.getText())))
        {
            parent.alertInformation("Mirth must be restarted in order to load the extension.");
            fileText.setText("");
        }
            
    }//GEN-LAST:event_installButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        JFileChooser pluginFileChooser = new JFileChooser();
        pluginFileChooser.setFileFilter(new MirthFileFilter("ZIP"));
        
        Preferences userPreferences = Preferences.systemNodeForPackage(Mirth.class);
        
        File currentDir = new File(userPreferences.get("currentDirectory", ""));
        if (currentDir.exists())
            pluginFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = pluginFileChooser.showOpenDialog(this);
        File pluginFile = null;
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            userPreferences.put("currentDirectory", pluginFileChooser.getCurrentDirectory().getPath());
            pluginFile = pluginFileChooser.getSelectedFile();
            fileText.setText(pluginFile.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton connectorRadioButton;
    private javax.swing.JTextField fileText;
    private javax.swing.JButton installButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel loadedConnectorsPanel;
    private javax.swing.JScrollPane loadedConnectorsScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedConnectorsTable;
    private javax.swing.JPanel loadedPluginsPanel;
    private javax.swing.JScrollPane loadedPluginsScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedPluginsTable;
    private javax.swing.JRadioButton pluginRadioButton;
    // End of variables declaration//GEN-END:variables
    
}
