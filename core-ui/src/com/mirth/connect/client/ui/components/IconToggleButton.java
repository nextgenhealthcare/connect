/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JToggleButton;

public class IconToggleButton extends JToggleButton {

    public IconToggleButton() {
        super();
        init();
    }

    public IconToggleButton(Icon icon) {
        super(icon);
        init();
    }

    public void setContentFilled(boolean filled) {
        setBorderPainted(filled);
        setContentAreaFilled(filled);
    }

    private void init() {
        setBorderPainted(false);
        setContentAreaFilled(false);
        setMargin(new Insets(4, 4, 4, 4));
        setMaximumSize(new Dimension(24, 24));
        setMinimumSize(new Dimension(24, 24));
        setPreferredSize(new Dimension(24, 24));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setContentFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (!isSelected()) {
                    setContentFilled(false);
                }
            }
        });
    }
}