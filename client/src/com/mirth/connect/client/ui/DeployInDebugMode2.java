/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import com.mirth.connect.client.ui.codetemplate.CodeTemplatePanel;
import com.mirth.connect.client.ui.codetemplate.CodeTemplateRootTreeTableNode;
import com.mirth.connect.client.ui.codetemplate.CodeTemplateTreeTableModel;
import com.mirth.connect.client.ui.components.ChannelInfo;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.util.DisplayUtil;

import net.miginfocom.swing.MigLayout;

/** Creates the About Mirth dialog. The content is loaded from about.txt. */
public class DeployInDebugMode2 extends MirthDialog {

    private static final int LIBRARY_CHANNELS_NAME_COLUMN = 1;

    private Frame parent;
    private JPanel deployInDebugPanel;
    private MirthTreeTable deployInDebugChannelsTable;
    private JPanel deployInDebugRightPanel;
    private JPanel deployInDebugChannelsSelectPanel;
    private JLabel deployInDebugChannelsLabel;
    private JLabel deployInDebugChannelsSelectAllLabel;
    private JLabel deployInDebugChannelsDeselectAllLabel;
    private JLabel deployInDebugChannelsFilterLabel;
    private JTextField deployInDebugChannelsFilterField;
    private JScrollPane deployInDebugChannelsScrollPane;
    private Color borderColor = new Color(110, 110, 110);
    /** Creates new form AboutMirth */
    public DeployInDebugMode2() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
       
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        DisplayUtil.setResizable(this, false);
        setPreferredSize(new Dimension(400, 300));
        setModal(true);
        
        initComponents();
        initLayout();

        pack();

      
        setVisible(true);
    }

      private void initLayout() {
        


//        notificationPanel.add(headerListPanel, "grow");
//        notificationPanel.add(headerContentPanel, "wrap, grow");
//        notificationPanel.add(listScrollPane, "grow");
//        notificationPanel.add(contentScrollPane, "grow");
//          notificationPanel.add(aboutContent);
          notificationPanel.add(debugLabel,"alignx left");
          notificationPanel.add(libraryChannelsSelectAllLabel,"alignx left");
          notificationPanel.add(libraryChannelsDeselectAllLabel,"alignx left");
          notificationPanel.add(new JSeparator(), "grow, gaptop 4, span");
          notificationPanel.add(deployUndeployCheckBox1);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(deployUndeployCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(attachmentBatchCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(sourceConnectorBatchCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(sourceFilterTransformerCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(destinationFilterTransformerCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");
          notificationPanel.add(destinationRespCheckBox);
          notificationPanel.add(new JSeparator(), "grow, span");

        add(notificationPanel, "grow, push, span");
        add(new JSeparator(), "grow, gaptop 4, span");
          
          
//        add(debugLabel,"alignx left");
//        add(libraryChannelsSelectAllLabel,"alignx left");
//        add(libraryChannelsDeselectAllLabel,"alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");

//        
//        add(deployUndeployCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
        

        
//        add(attachmentBatchCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//
//        add(sourceConnectorBatchCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//
//        add(sourceFilterTransformerCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//
//        add(destinationFilterTransformerCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//
//        add(destinationScriptsCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//
//        add(destinationRespCheckBox, "alignx left");
//        add(new JSeparator(), "grow, gaptop 4, span");
//        
        


        add(okButton, "alignx center, width 60, spany 2");

        add(cancelButton, "alignx center, width 60, spany 2");

    }
    
    private void initComponents() {
        setLayout(new MigLayout("insets 12", "[]", "[fill][]"));
        setTitle("Debug Channel");
        
        notificationPanel = new JPanel();
        notificationPanel.setLayout(new MigLayout("insets 0 0 0 0, fill", "[200!][]", "[25!]0[]"));
        notificationPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        
        debugLabel=new JLabel("Select channel script to debug");
        
        libraryChannelsSelectAllLabel = new JLabel("<html><u>Select All</u></html>");
        libraryChannelsSelectAllLabel.setForeground(Color.BLUE);
        libraryChannelsSelectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        libraryChannelsSelectAllLabel.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (evt.getComponent().isEnabled()) {
//                    for (int row = 0; row < libraryChannelsTable.getRowCount(); row++) {
//                        ChannelInfo channelInfo = (ChannelInfo) libraryChannelsTable.getValueAt(row, LIBRARY_CHANNELS_NAME_COLUMN);
//                        channelInfo.setEnabled(true);
//                        libraryChannelsTable.setValueAt(channelInfo, row, LIBRARY_CHANNELS_NAME_COLUMN);
//                    }
                    setEnabled(true);
                }
            }
        });
        
        libraryChannelsDeselectAllLabel = new JLabel("<html><u>Deselect All</u></html>");
        libraryChannelsDeselectAllLabel.setForeground(Color.BLUE);
        libraryChannelsDeselectAllLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        deployUndeployCheckBox1 = new JCheckBox("Deploy/Undeploy/Preprocessor/Postprocessor scripts");
        deployUndeployCheckBox1.setBackground(UIConstants.BACKGROUND_COLOR);

        deployUndeployCheckBox = new JCheckBox("Deploy/Undeploy/Preprocessor/Postprocessor scripts");
        deployUndeployCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
//        notificationPanel.add(deployUndeploy1);


        attachmentBatchCheckBox = new JCheckBox("Attachment/Batch scripts");
        attachmentBatchCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        sourceConnectorBatchCheckBox = new JCheckBox("Source connector scripts");
        sourceConnectorBatchCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        sourceFilterTransformerCheckBox = new JCheckBox("Source filter/transformer");
        sourceFilterTransformerCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        destinationFilterTransformerCheckBox = new JCheckBox("Destination filter/transformer");
        destinationFilterTransformerCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);

        destinationScriptsCheckBox = new JCheckBox("Destination connector scripts");
        destinationScriptsCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
        
        destinationRespCheckBox = new JCheckBox("Destination response transformer");
        destinationRespCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);


        cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        
        okButton = new JButton("OK");



              
//        headerListPanel.add(deployUndeploy1);
//        headerListPanel.add(deployUndeploy2);
//        headerListPanel.add(deployUndeploy3);
//        headerListPanel.add(deployUndeploy4);
//        headerListPanel.add(deployUndeploy5);
//        headerListPanel.add(deployUndeploy6);
////        add(deployUndeploy6, "alignx left");

        pack();
    }
    
    private void close() {
        this.dispose();
    }
    

    @Override
    public void onCloseAction() {
        close();
    }
    
    public void switchPanel() {
        
           }

    public static final String NEW_CHANNELS = "Debug Channel";
    private javax.swing.JTextPane aboutContent;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.mirth.connect.client.ui.MirthHeadingPanel mirthHeadingPanel1;
    JLabel debugLabel;
    JLabel libraryChannelsSelectAllLabel; 
    JLabel libraryChannelsDeselectAllLabel;
    
    private JCheckBox deployUndeployCheckBox1;
    private JCheckBox deployUndeployCheckBox;
    private JCheckBox attachmentBatchCheckBox;
    private JCheckBox sourceConnectorBatchCheckBox;
    private JCheckBox sourceFilterTransformerCheckBox;
    private JCheckBox destinationFilterTransformerCheckBox;
    private JCheckBox destinationScriptsCheckBox;
    private JCheckBox destinationRespCheckBox;


    
    private JPanel headerListPanel;
    private JPanel notificationPanel;
 
    private JFrame deployFrame;
    private JButton cancelButton;
    private JButton okButton;


    
    // End of variables declaration//GEN-END:variables
}
