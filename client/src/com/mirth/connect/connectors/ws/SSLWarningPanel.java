/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.awt.Desktop;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.BareBonesBrowserLaunch;
import com.mirth.connect.client.ui.UIConstants;

public class SSLWarningPanel extends JPanel implements HyperlinkListener {

    public SSLWarningPanel() {
        super(new MigLayout("insets 8, novisualpadding, hidemode 3"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setPreferredSize(new Dimension(255, 215));
        setMaximumSize(new Dimension(255, 215));

        JEditorPane editorPane = new JEditorPane("text/html", "");
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styleSheet = editorKit.getStyleSheet();
        styleSheet.addRule(".ssl-warning-panel {font-family:\"Tahoma\";font-size:11;text-align:center}");
        editorPane.setEditorKit(editorKit);
        editorPane.setDocument(editorKit.createDefaultDocument());
        editorPane.setBackground(getBackground());
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(this);
        editorPane.setText("<html><body class=\"ssl-warning-panel\"><b><span style=\"color:#404040\">Important Notice:</span></b> The default system certificate store will be used for this connection. As a result, certain security options are not available and mutual authentication (two-way authentication) is not supported.<br/><br/>The <a href=\"http://www.mirthcorp.com/products/mirth-connect#ssl-manager\">SSL Manager extension</a> for Mirth Connect provides advanced security and certificate management enhancements, including the ability to import certificates for use by source or destination connectors, as well as the ability to configure hostname verification and client authentication settings. For more information please contact <a href=\"http://www.mirthcorp.com/company/contact\">Mirth sales</a>.</body></html>");
        add(editorPane, "grow");
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == EventType.ACTIVATED && Desktop.isDesktopSupported()) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(evt.getURL().toURI());
                } else {
                    BareBonesBrowserLaunch.openURL(evt.getURL().toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
