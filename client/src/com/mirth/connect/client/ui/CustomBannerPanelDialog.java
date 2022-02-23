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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import com.mirth.connect.client.ui.util.DisplayUtil;

import net.miginfocom.swing.MigLayout;

public class CustomBannerPanelDialog extends JDialog {

    public CustomBannerPanelDialog(JFrame parent, String title, String text) {
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
              System.exit(0);
            }
        });
        
        this.notificationText = text;
        this.title = title;
        
        DisplayUtil.setResizable(this, false);
        setPreferredSize(new Dimension(800,600));
        setModal(true);
        
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }
        
        initComponents();
        setVisible(true);
        

    }
    
    private void initComponents() {
        setLayout(new MigLayout("insets 12", "[]", "[fill][]"));
        setTitle(title);

        JPanel outerPane = new JPanel();
        Box box = Box.createVerticalBox();
        outerPane.setLayout(new BoxLayout(outerPane, BoxLayout.PAGE_AXIS));
        textArea = new JTextArea(35, 85);
        textArea.setBackground(UIConstants.BACKGROUND_COLOR);
        textArea.setEditable(false);
        textArea.setText(notificationText);

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
    

    private String notificationText; 
    private String title; 
    protected JTextArea textArea;
    protected JButton btnAccept, btnCancel;
}