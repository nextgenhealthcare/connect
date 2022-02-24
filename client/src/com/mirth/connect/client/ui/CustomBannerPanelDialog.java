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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
        
        DisplayUtil.setResizable(this, true);
        setPreferredSize(new Dimension(800,600));
        setModal(true);
        this.setIconImage(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/mirth_32_ico.png")).getImage());
        
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
    	// layout sets 12 pixel border
        setLayout(new MigLayout("insets 12", "[]", "[fill][]"));
        setTitle(title);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);		// set dialog box to background color
        setBackground(UIConstants.BACKGROUND_COLOR);						// get all other backgrounds for each piece

        textArea = new JTextArea();
        textArea.setBackground(getBackground());
        textArea.setEditable(false);
        textArea.setText(notificationText);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));	// add 8 pixels padding to text
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBackground(getBackground());
        
        btnAccept = new JButton("Accept");
        btnAccept.setVerticalTextPosition(AbstractButton.BOTTOM);
        btnAccept.setHorizontalTextPosition(AbstractButton.LEFT); 			// LEFT, for left-to-right locale
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

        add(scrollPane, "grow, sx, push, h 100%");				// fill the screen with the scrollPane
        add(new JSeparator(), "grow, span, gap 0 0 4 4");		// set gap on top and bottom 4 pixels
        add(btnAccept, "newline, right, split 2");				// put buttons on bottom right of dialog
        add(btnCancel);

        pack();
    }
    
    private String notificationText; 
    private String title; 
    protected JTextArea textArea;
    protected JButton btnAccept, btnCancel;
}