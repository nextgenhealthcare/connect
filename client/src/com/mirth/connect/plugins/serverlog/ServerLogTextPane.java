/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 * This ServerLogTextPane extends JTextPane disabling Word Wrap by default.
 */
public class ServerLogTextPane extends JTextPane {

    public ServerLogTextPane() {
        super();
    }

    public ServerLogTextPane(StyledDocument doc) {
        super(doc);
    }

    // turn off word wrap
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
}
