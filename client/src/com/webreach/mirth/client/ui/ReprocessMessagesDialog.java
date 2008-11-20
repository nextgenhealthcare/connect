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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.filters.MessageObjectFilter;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class ReprocessMessagesDialog extends javax.swing.JDialog {

    private Frame parent;
    
    private MessageObjectFilter filter = null;
    
    private final String INCLUDED_DESTINATION_NAME_COLUMN_NAME = "Destination";
    private final String INCLUDED_STATUS_COLUMN_NAME = "Include";
    private final String INCLUDED_ID_COLUMN_NAME = "Id";
    
    /**
     * Creates new form ViewContentDialog
     */
    public ReprocessMessagesDialog(MessageObjectFilter filter) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        this.filter = filter;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        pack();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        makeIncludedDestinationsTable();
        okButton.requestFocus();
        okButton.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					okButtonActionPerformed(null);
				}
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}
        });
        setVisible(true);
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeIncludedDestinationsTable() {
        updateIncludedDestinationsTable(parent.channels.get(parent.getSelectedChannelIdFromDashboard()));

        includedDestinationsTable.setDragEnabled(false);
        includedDestinationsTable.setRowSelectionAllowed(false);
        includedDestinationsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        includedDestinationsTable.setFocusable(false);
        includedDestinationsTable.setOpaque(true);
        includedDestinationsTable.getTableHeader().setReorderingAllowed(false);
        includedDestinationsTable.setSortable(false);

        includedDestinationsTable.getColumnExt(INCLUDED_STATUS_COLUMN_NAME).setMaxWidth(50);
        includedDestinationsTable.getColumnExt(INCLUDED_STATUS_COLUMN_NAME).setMinWidth(50);
        includedDestinationsTable.getColumnExt(INCLUDED_ID_COLUMN_NAME).setVisible(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            includedDestinationsTable.setHighlighters(highlighter);
        }

        includedDestinationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {

            }
        });

        includedDestinationsPane.setViewportView(includedDestinationsTable);

        // Key Listener trigger for CTRL-S
        includedDestinationsTable.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                    PlatformUI.MIRTH_FRAME.doSaveAlerts();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    public void updateIncludedDestinationsTable(Channel channel) {
        Object[][] tableData = null;
        int tableSize = 0;
        
        List<Connector> enabledDestinations = new LinkedList<Connector>(); 
        
        if (channel != null) {        
            tableSize = channel.getEnabledDestinationConnectors().size();
            tableData = new Object[tableSize][3];

            int i = 0, j = 0;
            for (Connector connector : channel.getDestinationConnectors()) {
                if(connector.isEnabled()) { 
	            	tableData[i][0] = connector.getName();
	                tableData[i][1] = Boolean.TRUE;
	                tableData[i][2] = channel.getId() + "_destination_" + (j+1) + "_connector";
	                i++;
                }
                j++;
            }
        }

        if (channel != null && includedDestinationsTable != null) {
            RefreshTableModel model = (RefreshTableModel) includedDestinationsTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            includedDestinationsTable = new MirthTable();
            includedDestinationsTable.setModel(new RefreshTableModel(tableData, new String[]{INCLUDED_DESTINATION_NAME_COLUMN_NAME, INCLUDED_STATUS_COLUMN_NAME, INCLUDED_ID_COLUMN_NAME}) {

                boolean[] canEdit = new boolean[]{false, true, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }
    
    public boolean isReprocessOriginal() { 
        return reprocessOriginal.isSelected();
    }
    
    public List<String> getConnectors()
    {
        LinkedList<String> connectors = new LinkedList<String>();

        for (int i = 0; i < includedDestinationsTable.getModel().getRowCount(); i++)
        {
            if (((Boolean) includedDestinationsTable.getModel().getValueAt(i, 1)).booleanValue())
            {
                connectors.add((String) includedDestinationsTable.getModel().getValueAt(i, 2));
            }
        }
        return connectors;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        reprocessOriginal = new javax.swing.JCheckBox();
        includedDestinationsPane = new javax.swing.JScrollPane();
        includedDestinationsTable = null;
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reprocessing Options");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        reprocessOriginal.setText("Overwrite existing messages and update statistics");
        reprocessOriginal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reprocessOriginal.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includedDestinationsPane.setViewportView(includedDestinationsTable);

        jLabel1.setText("Reprocess to the following destinations:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, includedDestinationsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(okButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, reprocessOriginal)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(reprocessOriginal)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(includedDestinationsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
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

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    this.dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    parent.reprocessMessage(filter, isReprocessOriginal(), getConnectors());
    this.dispose();
}//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane includedDestinationsPane;
    private com.webreach.mirth.client.ui.components.MirthTable includedDestinationsTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox reprocessOriginal;
    // End of variables declaration//GEN-END:variables
}
