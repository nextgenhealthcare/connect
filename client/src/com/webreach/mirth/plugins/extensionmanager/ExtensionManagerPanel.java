/*
 * PluginManagerPanel.java
 *
 * Created on July 17, 2007, 4:32 PM
 */

package com.webreach.mirth.plugins.extensionmanager;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.*;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.ArchiveMetaData;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

/**
 *
 * @author  brendanh
 */
public class ExtensionManagerPanel extends javax.swing.JPanel
{
    private static final String PLUGINS = "plugins";

	private static final String CONNECTORS = "connectors";

	private ExtensionManagerClient parent;
    
    private final String PLUGIN_STATUS_COLUMN_NAME = "Status";
    private final String PLUGIN_NAME_COLUMN_NAME = "Name";
    private final String PLUGIN_AUTHOR_COLUMN_NAME = "Author";
    private final String PLUGIN_URL_COLUMN_NAME = "URL";
    private final String PLUGIN_VERSION_COLUMN_NAME = "Version";
    private final String PLUGIN_MIRTH_VERSION_COLUMN_NAME = "Mirth Version";
    
    private final int PLUGIN_NAME_COLUMN_NUMBER = 1;
    
    private int lastConnectorRow = -1;
    private int lastPluginRow = -1;
    
    private final String ENABLED_STATUS = "Enabled";
    
    private Map<String, PluginMetaData> pluginData = null;
    private Map<String, ConnectorMetaData> connectorData = null;
    
    /** Creates new form PluginManagerPanel */
    public ExtensionManagerPanel(ExtensionManagerClient parent)
    {
        this.parent = parent;
        initComponents();
        makeLoadedConnectorsTable();
        makeLoadedPluginsTable();
    }
    
    /**
     * Gets the selected extension index that corresponds to the saved extensions
     * list
     */
    public MetaData getSelectedExtension()
    {
        if(loadedConnectorsTable.getSelectedRowCount() > 0)
        {
            int selectedRow = loadedConnectorsTable.getSelectedRow();
            
            if (selectedRow != -1)
            {
                String extensionName = (String) loadedConnectorsTable.getModel().getValueAt(loadedConnectorsTable.convertRowIndexToModel(selectedRow), PLUGIN_NAME_COLUMN_NUMBER);
                return connectorData.get(extensionName);
            }
        }
        else if (loadedPluginsTable.getSelectedRowCount() > 0)
        {
            int selectedRow = loadedPluginsTable.getSelectedRow();
            
            if (selectedRow != -1)
            {
                String extensionName = (String) loadedPluginsTable.getModel().getValueAt(loadedPluginsTable.convertRowIndexToModel(selectedRow), PLUGIN_NAME_COLUMN_NUMBER);
                return pluginData.get(extensionName);
            }
        }
        
        return null;
    }
    public void showExtensionProperties()
    {
        MetaData metaData = getSelectedExtension();
        if (metaData != null)
        {
            String type = "";
            if (metaData instanceof ConnectorMetaData)
            {
                type = "Connector";
            }
            else if (metaData instanceof PluginMetaData)
            {
                type = "Plugin";
            }
            
            String name =  metaData.getName();
            String version =  metaData.getPluginVersion();
            String mirthVersion =  metaData.getMirthVersion();
            String author =  metaData.getAuthor();
            String url =  metaData.getUrl();
            String description  = metaData.getDescription();
            
            new ExtensionInfoDialog(name, type, author,mirthVersion, version, url, description);
        }
    }
    
    public void enableExtension()
    {
        getSelectedExtension().setEnabled(true);
        updateLoadedConnectorsTable();
        updateLoadedPluginsTable();
    }
    
    public void disableExtension()
    {
        getSelectedExtension().setEnabled(false);
        updateLoadedConnectorsTable();
        updateLoadedPluginsTable();
    }
    
    /**
     * Makes the loaded connectors table
     */
    public void makeLoadedConnectorsTable()
    {
        updateLoadedConnectorsTable();
        
        loadedConnectorsTable.setSelectionMode(0);
        loadedConnectorsTable.setDragEnabled(false);
        loadedConnectorsTable.setRowSelectionAllowed(true);
        loadedConnectorsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedConnectorsTable.setFocusable(false);
        loadedConnectorsTable.setOpaque(true);
        loadedConnectorsTable.getTableHeader().setReorderingAllowed(true);
        loadedConnectorsTable.setSortable(true);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedConnectorsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedConnectorsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        loadedConnectorsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedConnectorsTable.setHighlighters(highlighter);
        }
        
        loadedConnectorsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                ConnectorListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        loadedConnectorsTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showConnectorPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showConnectorPopupMenu(evt, true);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() == 2)
                {
                    showExtensionProperties();
                }
            }
        });
        loadedConnectorsTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                loadedConnectorsScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);
    }
    
    public void setConnectorData(Map<String, ConnectorMetaData> connectorData)
    {
        this.connectorData = connectorData;
        updateLoadedConnectorsTable();
    }
    
    public Map<String, ConnectorMetaData> getConnectorData()
    {
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
                if (metaData.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                
                tableData[i][1] = metaData.getName();
                tableData[i][2] = metaData.getAuthor();
                tableData[i][3] = metaData.getUrl();
                tableData[i][4] = metaData.getPluginVersion();
                tableData[i][5] = metaData.getMirthVersion();
                
                i++;
            }
        }
        
        if (connectorData != null && loadedConnectorsTable != null)
        {
            lastConnectorRow = loadedConnectorsTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) loadedConnectorsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            loadedConnectorsTable = new MirthTable();
            loadedConnectorsTable.setModel(new RefreshTableModel(tableData, new String[] { PLUGIN_STATUS_COLUMN_NAME, PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME, PLUGIN_MIRTH_VERSION_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
        
        if (lastConnectorRow >= 0 && lastConnectorRow < loadedConnectorsTable.getRowCount())
            loadedConnectorsTable.setRowSelectionInterval(lastConnectorRow, lastConnectorRow);
        else
            lastConnectorRow = UIConstants.ERROR_CONSTANT;
        
        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        loadedConnectorsTable.setHighlighters(highlighter);
    }
    
    /** The action called when a connector is selected. Sets tasks as well. */
    private void ConnectorListSelected(ListSelectionEvent evt)
    {
        int row = loadedConnectorsTable.getSelectedRow();
        
        if (row >= 0 && row < loadedConnectorsTable.getRowCount())
        {
            loadedPluginsTable.deselectRows();
            
            parent.setVisibleTasks(4, -1, true);
            
            int columnNumber = loadedConnectorsTable.getColumnNumber(PLUGIN_STATUS_COLUMN_NAME);
            if (((CellData) loadedConnectorsTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(5, 5, false);
            else
                parent.setVisibleTasks(6, 6, false);
        }
    }
    
    /**
     * Show the popup menu on trigger button press (right-click). If it's on the
     * table then the row should be selected, if not any selected rows should be
     * deselected first.
     */
    private void showConnectorPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = loadedConnectorsTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                loadedConnectorsTable.setRowSelectionInterval(row, row);
            }
            else
                deselectConnectorRows();
            parent.getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    public void deselectConnectorRows()
    {
        loadedConnectorsTable.deselectRows();
        parent.setVisibleTasks(5, -1, false);
    }
    
    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeLoadedPluginsTable()
    {
        updateLoadedPluginsTable();
        
        loadedPluginsTable.setSelectionMode(0);
        loadedPluginsTable.setDragEnabled(false);
        loadedPluginsTable.setRowSelectionAllowed(true);
        loadedPluginsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        loadedPluginsTable.setFocusable(false);
        loadedPluginsTable.setOpaque(true);
        loadedPluginsTable.getTableHeader().setReorderingAllowed(true);
        loadedPluginsTable.setSortable(true);
        
        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedPluginsTable.getColumnExt(PLUGIN_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedPluginsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMaxWidth(75);
        loadedPluginsTable.getColumnExt(PLUGIN_MIRTH_VERSION_COLUMN_NAME).setMinWidth(75);
        
        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        
        loadedPluginsTable.getColumnExt(PLUGIN_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            loadedPluginsTable.setHighlighters(highlighter);
        }
        
        loadedPluginsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                PluginListSelected(evt);
            }
        });
        
        // listen for trigger button and double click to edit channel.
        loadedPluginsTable.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                showPluginPopupMenu(evt, true);
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                showPluginPopupMenu(evt, true);
            }
            
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                if (evt.getClickCount() == 2)
                {
                    showExtensionProperties();
                }
            }
        });
        
        loadedPluginsTable.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                loadedPluginsScrollPane.getMouseWheelListeners()[0].mouseWheelMoved(e);
            }
            
        });
        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);
    }
    
    public void setPluginData(Map<String, PluginMetaData> pluginData)
    {
        this.pluginData = pluginData;
        updateLoadedPluginsTable();
    }
    
    public Map<String, PluginMetaData> getPluginData()
    {
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
                if (metaData.isEnabled())
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_blue.png")), "Enabled");
                else
                    tableData[i][0] = new CellData(new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_black.png")), "Disabled");
                
                tableData[i][1] = metaData.getName();
                tableData[i][2] = metaData.getAuthor();
                tableData[i][3] = metaData.getUrl();
                tableData[i][4] = metaData.getPluginVersion();
                tableData[i][5] = metaData.getMirthVersion();
                
                i++;
            }
        }
        
        if (pluginData != null && loadedPluginsTable != null)
        {
            lastPluginRow = loadedPluginsTable.getSelectedRow();
            RefreshTableModel model = (RefreshTableModel) loadedPluginsTable.getModel();
            model.refreshDataVector(tableData);
        }
        else
        {
            loadedPluginsTable = new MirthTable();
            loadedPluginsTable.setModel(new RefreshTableModel(tableData, new String[] { PLUGIN_STATUS_COLUMN_NAME, PLUGIN_NAME_COLUMN_NAME, PLUGIN_AUTHOR_COLUMN_NAME, PLUGIN_URL_COLUMN_NAME, PLUGIN_VERSION_COLUMN_NAME, PLUGIN_MIRTH_VERSION_COLUMN_NAME })
            {
                boolean[] canEdit = new boolean[] { false, false, false, false, false, false };
                
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return canEdit[columnIndex];
                }
            });
        }
        
        if (lastPluginRow >= 0 && lastPluginRow < loadedPluginsTable.getRowCount())
            loadedPluginsTable.setRowSelectionInterval(lastPluginRow, lastPluginRow);
        else
            lastPluginRow = UIConstants.ERROR_CONSTANT;
        
        // Set highlighter.
        HighlighterPipeline highlighter = new HighlighterPipeline();
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
        }
        loadedPluginsTable.setHighlighters(highlighter);
    }
    
    /** The action called when a plugin is selected. Sets tasks as well. */
    private void PluginListSelected(ListSelectionEvent evt)
    {
        int row = loadedPluginsTable.getSelectedRow();
        
        if (row >= 0 && row < loadedPluginsTable.getRowCount())
        {
            loadedConnectorsTable.deselectRows();
            
            parent.setVisibleTasks(4, -1, true);
            
            int columnNumber = loadedPluginsTable.getColumnNumber(PLUGIN_STATUS_COLUMN_NAME);
            if (((CellData) loadedPluginsTable.getValueAt(row, columnNumber)).getText().equals(ENABLED_STATUS))
                parent.setVisibleTasks(5, 5, false);
            else
                parent.setVisibleTasks(6, 6, false);
        }
    }
    
    /**
     * Show the popup menu on trigger button press (right-click). If it's on the
     * table then the row should be selected, if not any selected rows should be
     * deselected first.
     */
    private void showPluginPopupMenu(java.awt.event.MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = loadedPluginsTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                loadedPluginsTable.setRowSelectionInterval(row, row);
            }
            else
                deselectPluginRows();
            parent.getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    public void deselectPluginRows()
    {
        loadedPluginsTable.deselectRows();
        parent.setVisibleTasks(5, -1, false);
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
        loadedConnectorsPanel = new javax.swing.JPanel();
        loadedConnectorsScrollPane = new javax.swing.JScrollPane();
        loadedConnectorsTable = null;

        setBackground(new java.awt.Color(255, 255, 255));
        loadedPluginsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedPluginsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Installed Plugins", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        loadedPluginsScrollPane.setViewportView(loadedPluginsTable);

        org.jdesktop.layout.GroupLayout loadedPluginsPanelLayout = new org.jdesktop.layout.GroupLayout(loadedPluginsPanel);
        loadedPluginsPanel.setLayout(loadedPluginsPanelLayout);
        loadedPluginsPanelLayout.setHorizontalGroup(
            loadedPluginsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loadedPluginsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        loadedPluginsPanelLayout.setVerticalGroup(
            loadedPluginsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, loadedPluginsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Install Extension from File System", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("File:");

        browseButton.setText("Browse...");
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

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel1)
                .add(installButton)
                .add(browseButton)
                .add(fileText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        loadedConnectorsPanel.setBackground(new java.awt.Color(255, 255, 255));
        loadedConnectorsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Installed Connectors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(0, 0, 0)));
        loadedConnectorsScrollPane.setViewportView(loadedConnectorsTable);

        org.jdesktop.layout.GroupLayout loadedConnectorsPanelLayout = new org.jdesktop.layout.GroupLayout(loadedConnectorsPanel);
        loadedConnectorsPanel.setLayout(loadedConnectorsPanelLayout);
        loadedConnectorsPanelLayout.setHorizontalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loadedConnectorsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        );
        loadedConnectorsPanelLayout.setVerticalGroup(
            loadedConnectorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, loadedConnectorsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loadedConnectorsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loadedPluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        parent.setWorking("Installing Extension...", true);
        installButton.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>()
        {
            private boolean retVal = false;
            public Void doInBackground()
            {
                
                try
                {
                	String location = null;
                	ZipFile zipFile = new ZipFile(fileText.getText());
            		Enumeration entries = zipFile.entries();
        			//Check if we have archive metadata
        			ZipEntry archiveMetadata = zipFile.getEntry("archive-metadata.xml");
        			if (archiveMetadata != null){
        				ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { ArchiveMetaData.class });
        				//read the file from our inputstream
        				String xml = slurp(zipFile.getInputStream(archiveMetadata));
        				//serialize out to the proper type
        				ArchiveMetaData archiveMetadataSerialized = (ArchiveMetaData) serializer.fromXML(xml);
        				if (archiveMetadataSerialized.getType() == ArchiveMetaData.Type.CONNECTOR){
        					location = CONNECTORS;
        				}else if (archiveMetadataSerialized.getType() == ArchiveMetaData.Type.PLUGIN){
        					location = PLUGINS;
        				}else{
        					throw new ClientException("Unrecognized extension type in archive-metdata.xml");
        				}
        			}else if (location == null){
        				//prompt the user
        				String answer = "";
        		        
        		        JOptionPane pane = new JOptionPane("Is the extension a Plugin or Connector?");
        		        Object[] options = new String[] { "Plugin", "Connector", "Cancel" };
        		        pane.setOptions(options);
        		        JDialog dialog = pane.createDialog(new JFrame(), "Select an Option");
        		        dialog.setVisible(true);
        		        Object obj = pane.getValue();
        		        for (int k = 0; k < options.length; k++)
        		            if (options[k].equals(obj))
        		                answer = obj.toString();
        		        
        		        if(answer.length() == 0 || answer.equals(options[2])){
        		        	retVal = false;
        		            return null;
        		        }else if(answer.equals(options[0])){
        		        	//plugin
        		        	location = PLUGINS;
        		        }else if(answer.equals(options[1])){
        		        	//connector
        		        	location = CONNECTORS;
        		        }
        			}
                    retVal = parent.install(location, new File(fileText.getText()));
                }
                catch (Exception e)
                {
                    retVal = false;
                }
                return null;
            }
            
            public void done()
            {
                parent.setWorking("", false);
                installButton.setEnabled(true);
                if(retVal)
                {
                    parent.finishInstall();
                    fileText.setText("");
                }
                else
                {
                    parent.alertError("Unable to install extension.");
                }
            }
        };
        
        worker.execute();
        
        
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
    private javax.swing.JTextField fileText;
    private javax.swing.JButton installButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel loadedConnectorsPanel;
    private javax.swing.JScrollPane loadedConnectorsScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedConnectorsTable;
    private javax.swing.JPanel loadedPluginsPanel;
    private javax.swing.JScrollPane loadedPluginsScrollPane;
    private com.webreach.mirth.client.ui.components.MirthTable loadedPluginsTable;
    // End of variables declaration//GEN-END:variables
	public static String slurp (InputStream in) throws IOException {
	    StringBuffer out = new StringBuffer();
	    byte[] b = new byte[1024];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}
}
