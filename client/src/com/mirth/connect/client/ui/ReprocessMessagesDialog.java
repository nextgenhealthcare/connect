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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.filters.MessageFilter;

public class ReprocessMessagesDialog extends MirthDialog {

    private Frame parent;
    private MessageFilter filter = null;
    private String channelId;
    private boolean showWarning;

    public ReprocessMessagesDialog(String channelId, MessageFilter filter, Map<Integer, String> destinationsConnectors, Integer selectedMetaDataId, boolean showWarning) {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        this.showWarning = showWarning;
        initComponents();
        initLayout();
        this.channelId = channelId;
        this.filter = filter;
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
        overwriteCheckBox = new JCheckBox();
        overwriteCheckBox.setText("Overwrite existing messages and update statistics");

        reprocessLabel = new JLabel("Reprocess through the following destinations:");
        warningLabel = new JLabel("<html>Warning: This will reprocess all results for the current search, including those not listed on the current page.</html>");

        includedDestinationsTable = new MirthTable();
        includedDestinationsPane = new JScrollPane(includedDestinationsTable);

        okButton = new JButton();
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ItemSelectionTableModel<Integer, String> model = (ItemSelectionTableModel<Integer, String>) includedDestinationsTable.getModel();
                List<Integer> metaDataIds = model.getKeys(true);

                if (metaDataIds.size() == model.getRowCount()) {
                    metaDataIds = null;
                }

                parent.reprocessMessage(channelId, filter, overwriteCheckBox.isSelected(), metaDataIds);
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

        contentPane.add(overwriteCheckBox, "wrap");
        contentPane.add(reprocessLabel, "wrap");
        contentPane.add(includedDestinationsPane, "growx, wrap, gapbottom 6");
        contentPane.add(new JSeparator(), "growx, gapbottom 6, span");
        if (showWarning) {
            contentPane.add(warningLabel, "wrap");
        }
        contentPane.add(okButton, "alignx right, width 48, split, span");
        contentPane.add(cancelButton, "alignx right, width 48");

        pack();
    }

    private JLabel warningLabel;
    private JCheckBox overwriteCheckBox;
    private JLabel reprocessLabel;
    private JScrollPane includedDestinationsPane;
    private MirthTable includedDestinationsTable;
    private JButton okButton;
    private JButton cancelButton;
}
