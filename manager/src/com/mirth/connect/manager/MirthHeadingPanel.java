/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Creates the heading panel that is used for wizards, login, etc.
 */
public class MirthHeadingPanel extends JPanel {

    /** Creates new form MirthHeadingPanel */
    public MirthHeadingPanel() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill"));
        setupPanel();
        setOpaque(false);
        setPreferredSize(new Dimension(138, 22));
    }

    protected void paintComponent(Graphics g) {
        g.drawImage(new ImageIcon(getClass().getResource("images/header_nologo.png")).getImage(), 0, 0, getWidth(), getHeight(), null);
        super.paintComponent(g);
    }

    private void setupPanel() {
        JLabel headingLabel = new JLabel();
        headingLabel.setFont(new Font("Tahoma", 1, 18)); // NOI18N
        headingLabel.setForeground(new Color(255, 255, 255));
        headingLabel.setText("Mirth Connect Server Manager");

        add(headingLabel);
    }
}
