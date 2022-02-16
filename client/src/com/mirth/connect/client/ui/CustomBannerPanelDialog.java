/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import com.mirth.connect.client.ui.util.DisplayUtil;

import net.miginfocom.swing.MigLayout;

public class CustomBannerPanelDialog extends JDialog {

    public CustomBannerPanelDialog() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        DisplayUtil.setResizable(this, false);
        setPreferredSize(new Dimension(400, 300));
        setModal(true);

        initComponents();
        pack();
        setVisible(true);
    }
    
    private void initComponents() {
        setLayout(new MigLayout("insets 12", "[]", "[fill][]"));
        setTitle("Notification");

        JPanel outerPane = new JPanel();
        Box box = Box.createVerticalBox();
        outerPane.setLayout(new BoxLayout(outerPane, BoxLayout.PAGE_AXIS));
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);

        btnAccept = new JButton("Accept");
        btnAccept.setVerticalTextPosition(AbstractButton.BOTTOM);
        btnAccept.setHorizontalTextPosition(AbstractButton.LEFT); //aka LEFT, for left-to-right locales
        btnAccept.setActionCommand("accept");
        btnAccept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });
        
        btnCancel = new JButton("Cancel");
        btnCancel.setVerticalTextPosition(AbstractButton.BOTTOM);
        btnCancel.setHorizontalTextPosition(AbstractButton.RIGHT);
        btnCancel.setActionCommand("cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });

        box.add(scrollPane);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(btnAccept);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(btnCancel);

        box.add(new JSeparator(), "grow, gaptop 4, span");
        
        box.add(buttonPane);

        add(box);

        pack();
    }
    
    protected JTextArea textArea;
    protected JButton btnAccept, btnCancel;
}