package com.webreach.mirth.plugins.serverlog;

import javax.swing.text.StyledDocument;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: chrisr
 * Date: Apr 15, 2008
 * Time: 5:25:52 PM
 * To change this template use File | Settings | File Templates.
 */

/*
    This ServerLogTextPane extends JTextPane disabling Word Wrap by default.
*/
public class ServerLogTextPane extends JTextPane
{
        public ServerLogTextPane() {
            super();
        }

        public ServerLogTextPane (StyledDocument doc) {
            super(doc);
        }

        // turn off word wrap
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }
}
