/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package org.fife.ui.rsyntaxtextarea;

import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.ArrayUtils;

public class SyntaxViewUtil {

    /**
     * Draws custom end-of-line markers based on whether a line ends with a CR, LF, or CRLF.
     */
    public static void drawEOL(RSyntaxTextArea textArea, Graphics2D g, float x, float y) {
        if (textArea.getEOLMarkersVisible()) {
            g.setColor(textArea.getForegroundForTokenType(Token.WHITESPACE));
            g.setFont(textArea.getFontForTokenType(Token.WHITESPACE));

            if (textArea.getDocument() instanceof EOLPreservingRSyntaxDocument) {
                try {
                    int line = textArea.getLineOfOffset(textArea.viewToModel(new Point((int) x, (int) y)));
                    char[] eol = ((EOLPreservingRSyntaxDocument) textArea.getDocument()).getEOL(line);

                    if (ArrayUtils.isNotEmpty(eol)) {
                        String display = "";
                        for (char c : eol) {
                            switch (c) {
                                case '\r':
                                    display += "\\r";
                                    break;
                                case '\n':
                                    display += "\\n";
                                    break;
                            }
                        }

                        g.drawString(display, x, y);
                    }
                } catch (BadLocationException e) {
                }
            } else {
                g.drawString("\\n", x, y);
            }
        }
    }
}