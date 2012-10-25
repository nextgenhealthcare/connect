/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.JButton;

public class IconButton extends JButton {
    public IconButton() {
        super();
        
        setBorderPainted(false);
        setContentAreaFilled(false);
        setMargin(new java.awt.Insets(4, 4, 4, 4));
        setMaximumSize(new java.awt.Dimension(24, 24));
        setMinimumSize(new java.awt.Dimension(24, 24));
        setPreferredSize(new java.awt.Dimension(24, 24));
        
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBorderPainted(true);
                setContentAreaFilled(true);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBorderPainted(false);
                setContentAreaFilled(false);
            }
        });
    }
}
