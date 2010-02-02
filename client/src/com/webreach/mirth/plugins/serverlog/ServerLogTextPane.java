package com.webreach.mirth.plugins.serverlog;

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
