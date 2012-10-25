/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class LineNumber extends JComponent {

    // LineNumber height (abends when I use MAX_VALUE)
    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
    // LineNumber width
    private final static int WIDTH = 999;
    // Set right/left margin
    private final static int MARGIN = 5;
    // Line height of this LineNumber component
    private int lineHeight;
    // Line height of this LineNumber component
    private int fontLineHeight;
    //	
    private int currentRowWidth;
    // Metrics of this LineNumber component
    private FontMetrics fontMetrics;

    /**
     * Convenience constructor for Text Components
     */
    public LineNumber(JComponent component) {
        setBackground(EditorConstants.PANEL_BACKGROUND);
        setForeground(EditorConstants.LINENUMBER_FOREGROUND);
        setFont(EditorConstants.DEFAULT_FONT);
        setPreferredSize(WIDTH);
    }

    public void setPreferredSize(int row) {
        int width = fontMetrics.stringWidth(String.valueOf(row));

        if (currentRowWidth < width) {
            currentRowWidth = width;
            setPreferredSize(new Dimension(2 * MARGIN + width, HEIGHT));
        }
    }

    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = getFontMetrics(getFont());
        fontLineHeight = fontMetrics.getHeight();
    }

    /**
     * The line height defaults to the line height of the font for this
     * component. The line height can be overridden by setting it to a positive
     * non-zero value.
     */
    public int getLineHeight() {
        if (lineHeight == 0) {
            return fontLineHeight;
        } else {
            return lineHeight;
        }
    }

    public void setLineHeight(int lineHeight) {
        if (lineHeight > 0) {
            this.lineHeight = lineHeight;
        }
    }

    public int getStartOffset() {
        return 4;
    }

    public void paintComponent(Graphics g) {
        int lineHeight = getLineHeight();
        int startOffset = getStartOffset();
        Rectangle drawHere = g.getClipBounds();
        // System.out.println( drawHere );

        // Paint the background

        g.setColor(getBackground());
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Determine the number of lines to draw in the foreground.

        g.setColor(getForeground());
        int startLineNumber = (drawHere.y / lineHeight) + 1;
        // subtract 10 or so pixels to account for scroll bars
        int endLineNumber = startLineNumber + ((drawHere.height - 17) / lineHeight);

        int start = (drawHere.y / lineHeight) * lineHeight + lineHeight - startOffset + 1;

        // System.out.println( startLineNumber + " : " + endLineNumber + " : " +
        // start );

        for (int i = startLineNumber; i <= endLineNumber; i++) {
            String lineNumber = String.valueOf(i);
            int width = fontMetrics.stringWidth(lineNumber);
            g.drawString(lineNumber, MARGIN + currentRowWidth - width, start);
            start += lineHeight;
        }

        setPreferredSize(endLineNumber);
    }

    /*
     * public static void main(String[] args) { JFrame frame = new
     * JFrame("LineNumberDemo"); frame.setDefaultCloseOperation(
     * JFrame.EXIT_ON_CLOSE );
     * 
     * JPanel panel = new JPanel(); frame.setContentPane( panel );
     * panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
     * panel.setLayout(new BorderLayout());
     * 
     * JTextArea textPane = new JTextArea();
     * 
     * JScrollPane scrollPane = new JScrollPane(textPane);
     * panel.add(scrollPane); scrollPane.setPreferredSize(new Dimension(300,
     * 250));
     * 
     * LineNumber lineNumber = new LineNumber( textPane );
     * lineNumber.setPreferredSize(WIDTH); scrollPane.setRowHeaderView(
     * lineNumber );
     * 
     * frame.pack(); frame.setVisible(true); }
     */
}
