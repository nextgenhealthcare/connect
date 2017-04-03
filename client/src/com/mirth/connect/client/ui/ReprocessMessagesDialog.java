/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.filters.MessageFilter;

public class ReprocessMessagesDialog extends MirthDialog {

    private Frame parent;
    private MessageFilter filter = null;
    private Long messageId = null;
    private String channelId;
    private boolean showWarning;

    public ReprocessMessagesDialog(String channelId, MessageFilter filter, Long messageId, Map<Integer, String> destinationsConnectors, Integer selectedMetaDataId, boolean showWarning) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.showWarning = showWarning;
        initComponents();
        initLayout();
        this.channelId = channelId;
        this.filter = filter;
        this.messageId = messageId;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(false);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        setTitle("Reprocessing Options");

        makeIncludedDestinationsTable(destinationsConnectors, selectedMetaDataId);
        okButton.requestFocus();
        setVisible(true);
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should be added as well.
     */
    public void makeIncludedDestinationsTable(Map<Integer, String> destinationsConnectors, Integer selectedMetaDataId) {
        List<Integer> selectedMetaDataIds = null;

        if (selectedMetaDataId != null && selectedMetaDataId > 0) {
            selectedMetaDataIds = new ArrayList<Integer>();
            selectedMetaDataIds.add(selectedMetaDataId);
        }

        includedDestinationsTable = new ItemSelectionTable();
        includedDestinationsTable.setModel(new ItemSelectionTableModel<Integer, String>(destinationsConnectors, selectedMetaDataIds, "Destination", "Included"));
        includedDestinationsPane.setViewportView(includedDestinationsTable);
    }

    private void initComponents() {
        warningPane = new JEditorPane("text/html", "");
        warningPane.setBorder(BorderFactory.createEmptyBorder());
        warningPane.setBackground(getBackground());
        warningPane.setEditable(false);
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(".reprocess-dialog {font-family:\"Tahoma\";font-size:11;text-align:center}");
        warningPane.setEditorKit(editorKit);
        warningPane.setText("<html><span class=\"reprocess-dialog\" style=\"color:#FF0000\"><b>Warning:</b></span> <span class=\"reprocess-dialog\">This will reprocess <b>all</b> results for the current search criteria, including those not listed on the current page. To see how many messages will be reprocessed, close this dialog and click the Count button in the upper-right.</span></html>");

        overwriteCheckBox = new JCheckBox();
        overwriteCheckBox.setText("Overwrite existing messages and update statistics");

        reprocessLabel = new JLabel("Reprocess through the following destinations:");

        includedDestinationsTable = new MirthTable();
        includedDestinationsPane = new JScrollPane(includedDestinationsTable);

        okButton = new JButton();
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (showWarning && Preferences.userNodeForPackage(Mirth.class).getBoolean("showReprocessRemoveMessagesWarning", true)) {
                    String result = JOptionPane.showInputDialog(ReprocessMessagesDialog.this, "<html>This will reprocess all messages that match the current search criteria.<br/>To see how many messages will be reprocessed, close this dialog and<br/>click the Count button in the upper-right.<br><font size='1'><br></font>Type REPROCESSALL and click the OK button to continue.</html>", "Reprocess Results", JOptionPane.WARNING_MESSAGE);
                    if (!StringUtils.equals(result, "REPROCESSALL")) {
                        parent.alertWarning(ReprocessMessagesDialog.this, "You must type REPROCESSALL to reprocess results.");
                        return;
                    }
                }

                ItemSelectionTableModel<Integer, String> model = (ItemSelectionTableModel<Integer, String>) includedDestinationsTable.getModel();
                List<Integer> metaDataIds = model.getKeys(true);

                if (metaDataIds.size() == model.getRowCount()) {
                    metaDataIds = null;
                }

                parent.reprocessMessage(channelId, filter, messageId, overwriteCheckBox.isSelected(), metaDataIds);
                ReprocessMessagesDialog.this.dispose();
            }
        });

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ReprocessMessagesDialog.this.dispose();
            }
        });
    }

    private void initLayout() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout("insets 12, fill", "[grow]"));
        contentPane.setPreferredSize(new Dimension(425, 330));

        if (showWarning) {
            contentPane.add(warningPane, "growx, sx, wrap");
            contentPane.add(new JSeparator(), "growx, gapbottom 6, sx, wrap");
        }
        contentPane.add(overwriteCheckBox, "wrap");
        contentPane.add(reprocessLabel, "wrap");
        contentPane.add(includedDestinationsPane, "growx, wrap, gapbottom 6");
        contentPane.add(new JSeparator(), "growx, gapbottom 6, span");
        contentPane.add(okButton, "alignx right, width 48, split, span");
        contentPane.add(cancelButton, "alignx right, width 48");

        pack();
    }

    private JEditorPane warningPane;
    private JCheckBox overwriteCheckBox;
    private JLabel reprocessLabel;
    private JScrollPane includedDestinationsPane;
    private MirthTable includedDestinationsTable;
    private JButton okButton;
    private JButton cancelButton;
}
