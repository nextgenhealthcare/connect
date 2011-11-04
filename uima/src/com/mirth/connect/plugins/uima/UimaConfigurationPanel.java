package com.mirth.connect.plugins.uima;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.plugins.SettingsPanelPlugin;
import com.mirth.connect.plugins.uima.model.UimaPipeline;

// XXX: Remember: Always check to make sure the table variables are set to null in the initComponents field

public class UimaConfigurationPanel extends AbstractSettingsPanel {
    private static final long serialVersionUID = 1L;

    private static final String PIPELINE_COLUMN_NAME = "Pipeline";
    private static final String CONSUMER_COUNT_COLUMN_NAME = "Consumers";
    private static final String MESSAGE_COUNT_COLUMN_NAME = "Total Messages";
    private static final String PENDING_MESSAGE_COUNT_COLUMN_NAME = "Pending Messages";
    private static final String AVG_ENQUEUE_TIME_COLUMN_NAME = "Avg Wait Time (ms)";
    private static final String DEQUEUE_COUNT_COLUMN_NAME = "Processed Messages";
    private static final String MEMORY_USAGE_COLUMN_NAME = "Memory Usage";

    private SettingsPanelPlugin plugin = null;
    
    private final List<UimaPipeline> pipelines = new ArrayList<UimaPipeline>();
    
    public UimaConfigurationPanel(String tabName, SettingsPanelPlugin plugin) {
        super(tabName);
        this.plugin = plugin;
        
        initComponents();
        makeQueueListTable();
    }
    
    
    public void doRefresh() {
        getFrame().startWorking("Loading UIMA properties...");

        final Properties serverProperties = new Properties();
        pipelines.clear();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                try {
                    if (!getFrame().confirmLeave())
                        return null;

                    if (plugin.getPropertiesFromServer() != null) {
                        serverProperties.putAll(plugin.getPropertiesFromServer());
                    }

                    pipelines.addAll((List<UimaPipeline>) plugin.invoke(UimaService.METHOD_GET_PIPELINES, null));
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setProperties(serverProperties, pipelines);
                getFrame().stopWorking("");
            }
        };

        worker.execute();
    }

    public void doSave() {
        getFrame().startWorking("Saving UIMA properties...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                try {
                    plugin.setPropertiesToServer(getProperties());
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            public void done() {
                setSaveEnabled(false);
                getFrame().stopWorking("");
            }
        };

        worker.execute();
    }    
    
    public void setProperties(Properties properties, List<UimaPipeline> pipelines) {
        if (properties.getProperty("jmxServerUrl") != null) {
            jmxServerUrlField.setText(properties.getProperty("jmxServerUrl"));
        }

        if (properties.getProperty("jmxUsername") != null) {
            jmxUsernameField.setText(properties.getProperty("jmxUsername"));
        }

        if (properties.getProperty("jmxPassword") != null) {
            jmxPasswordField.setText(properties.getProperty("jmxPassword"));
        }

        updateQueueListTable(pipelines);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        properties.put("jmxServerUrl", jmxServerUrlField.getText());
        properties.put("jmxUsername", jmxUsernameField.getText());
        properties.put("jmxPassword", jmxPasswordField.getText());

        return properties;
    }

    public void makeQueueListTable() {
        updateQueueListTable(null);

        queueListTable.setDoubleBuffered(true);

        queueListTable.setSelectionMode(0);

        queueListTable.packTable(UIConstants.COL_MARGIN);

        queueListTable.setRowHeight(UIConstants.ROW_HEIGHT);
        queueListTable.setOpaque(true);
        queueListTable.setRowSelectionAllowed(true);

        queueListTable.setSortable(true);

        queueListPane.setViewportView(queueListTable);
    }
 
    @SuppressWarnings("serial")
    public void updateQueueListTable(List<UimaPipeline> pipelines) {
        Object[][] tableData = null;

        if (pipelines != null) {
            tableData = new Object[pipelines.size()][7];
            for (int i = 0; i < pipelines.size(); i++) {
                UimaPipeline pipeline = pipelines.get(i);
                tableData[i][0] = pipeline.getName();
                tableData[i][1] = pipeline.getCursorPercentUsage() + "% / " + pipeline.getMemoryPercentUsage() + "%";
                tableData[i][2] = pipeline.getConsumerCount();
                tableData[i][3] = pipeline.getAvgEnqueueTime();
                tableData[i][4] = pipeline.getPendingMessageCount();
                tableData[i][5] = pipeline.getDequeueCount();
                tableData[i][6] = pipeline.getMessageCount();
            }
        }

        if (queueListTable != null) {
            RefreshTableModel model = (RefreshTableModel) queueListTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            queueListTable = new MirthTable();
            queueListTable.setModel(new RefreshTableModel(tableData, new String[] { 
                PIPELINE_COLUMN_NAME,
                MEMORY_USAGE_COLUMN_NAME,
                CONSUMER_COUNT_COLUMN_NAME,
                AVG_ENQUEUE_TIME_COLUMN_NAME,
                PENDING_MESSAGE_COUNT_COLUMN_NAME,
                DEQUEUE_COUNT_COLUMN_NAME,
                MESSAGE_COUNT_COLUMN_NAME
            })
            {
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            });
        }
        
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            queueListTable.setHighlighters(highlighter);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jmxUsernameLabel = new javax.swing.JLabel();
        jmxUsernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        getPipelinesButton = new javax.swing.JButton();
        jmxPasswordField = new com.mirth.connect.client.ui.components.MirthTextField();
        jmxPasswordLabel = new javax.swing.JLabel();
        jmxServerUrlLabel = new javax.swing.JLabel();
        jmxServerUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        jPanel2 = new javax.swing.JPanel();
        queueListPane = new javax.swing.JScrollPane();
        queueListTable = null;

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "UIMA Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jmxUsernameLabel.setText("Username:");

        jmxUsernameField.setToolTipText("The username needed to authenticate to the JMX server. If no authentication is needed, leave this field blank.");

        getPipelinesButton.setText("Get Pipelines");
        getPipelinesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getPipelinesButtonActionPerformed(evt);
            }
        });

        jmxPasswordField.setToolTipText("The password needed to authenticate to the JMX server. If no authentication is needed, leave this field blank.");

        jmxPasswordLabel.setText("Password:");

        jmxServerUrlLabel.setText("JMX Server URL:");

        jmxServerUrlField.setToolTipText("This is the JMX server connection URL. It can be either 'host:port' or a full JMX URL.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jmxUsernameLabel)
                    .addComponent(jmxPasswordLabel)
                    .addComponent(jmxServerUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jmxServerUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(getPipelinesButton))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jmxPasswordField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jmxUsernameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
                .addContainerGap(164, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jmxServerUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jmxServerUrlLabel)
                    .addComponent(getPipelinesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jmxUsernameLabel)
                    .addComponent(jmxUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jmxPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jmxPasswordLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        queueListPane.setViewportView(queueListTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(queueListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(queueListPane, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(709, 709, 709))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void getPipelinesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPipelinesButtonActionPerformed
        try {
            Properties connectionInfo = new Properties();
            connectionInfo.setProperty("jmx.url", jmxServerUrlField.getText());
            connectionInfo.setProperty("jmx.username", jmxUsernameField.getText());
            connectionInfo.setProperty("jmx.password", jmxPasswordField.getText());

            List<UimaPipeline> testList = (List<UimaPipeline>) plugin.invoke(UimaService.METHOD_TEST_PIPELINE, connectionInfo);
            if (testList == null) {
                throw new ClientException("Error connecting to JMX");
            }
            getFrame().alertInformation(getFrame(), "Sucessfully refreshed pipeline list.");
        } catch (ClientException e) {
            getFrame().alertError(getFrame(), "Failed to refresh pipeline list.");
        }
    }//GEN-LAST:event_getPipelinesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton getPipelinesButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private com.mirth.connect.client.ui.components.MirthTextField jmxPasswordField;
    private javax.swing.JLabel jmxPasswordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField jmxServerUrlField;
    private javax.swing.JLabel jmxServerUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField jmxUsernameField;
    private javax.swing.JLabel jmxUsernameLabel;
    private javax.swing.JScrollPane queueListPane;
    private com.mirth.connect.client.ui.components.MirthTable queueListTable;
    // End of variables declaration//GEN-END:variables
    
}
