/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.applets.editor;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class LineNumber extends JComponent
{
    private final static Color DEFAULT_BACKGROUND = new Color(204, 204, 255);
    private final static Color DEFAULT_FOREGROUND = Color.black;
    private final static Font  DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 12);

    //  LineNumber height (abends when I use MAX_VALUE)
    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Set right/left margin
    private final static int MARGIN = 5;

    //  Variables for this LineNumber component
    private FontMetrics fontMetrics;
    private int lineHeight;
    private int currentDigits;

    //  Metrics of the component used in the constructor
    private JComponent component;
    private int componentFontHeight;
    private int componentFontAscent;

    /**
     *	Convenience constructor for Text Components
     */
    public LineNumber(JComponent component)
    {
        if (component == null)
        {
            setFont( DEFAULT_FONT );
            this.component = this;
        }
        else
        {
//            setFont( component.getFont() );
            setFont( DEFAULT_FONT );
            this.component = component;
        }

        setBackground( DEFAULT_BACKGROUND );
        setForeground( DEFAULT_FOREGROUND );
        setPreferredWidth( 99 );
    }

    /**
     *  Calculate the width needed to display the maximum line number
     */
    public void setPreferredWidth(int lines)
    {
        int digits = String.valueOf(lines).length();

        //  Update sizes when number of digits in the line number changes

        if (digits != currentDigits && digits > 1)
        {
            currentDigits = digits;
            int width = fontMetrics.charWidth( '0' ) * digits;
            Dimension d = getPreferredSize();
            d.setSize(2 * MARGIN + width, HEIGHT);
            setPreferredSize( d );
            setSize( d );
        }
    }

    /**
     *  Reset variables that are dependent on the font.
     */
    public void setFont(Font font)
    {
        super.setFont(font);
        fontMetrics = getFontMetrics( getFont() );
        componentFontHeight = fontMetrics.getHeight();
        componentFontAscent = fontMetrics.getAscent();
    }

    /**
     *  The line height defaults to the line height of the font for this
     *  component.
     */
    public int getLineHeight()
    {
        if (lineHeight == 0)
            return componentFontHeight;
        else
            return lineHeight;
    }

    /**
     *  Override the default line height with a positive value.
     *  For example, when you want line numbers for a JTable you could
     *  use the JTable row height.
     */
    public void setLineHeight(int lineHeight)
    {
        if (lineHeight > 0)
            this.lineHeight = lineHeight;
    }

    public int getStartOffset()
    {
        return component.getInsets().top + componentFontAscent;
    }

    public void paintComponent(Graphics g)
    {
        int lineHeight = getLineHeight();
        int startOffset = getStartOffset();
        Rectangle drawHere = g.getClipBounds();

        // Paint the background

        g.setColor( getBackground() );
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        //  Determine the number of lines to draw in the foreground.

        g.setColor( getForeground() );
        int startLineNumber = (drawHere.y / lineHeight) + 1;
        int endLineNumber = startLineNumber + (drawHere.height / lineHeight);

        int start = (drawHere.y / lineHeight) * lineHeight + startOffset;

        for (int i = startLineNumber; i <= endLineNumber; i++)
        {
            String lineNumber = String.valueOf(i);
            int stringWidth = fontMetrics.stringWidth( lineNumber );
            int rowWidth = getSize().width;
            g.drawString(lineNumber, rowWidth - stringWidth - MARGIN, start);
            start += lineHeight;
        }

        int rows = component.getSize().height / componentFontHeight;
        setPreferredWidth( rows );
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("LineNumberDemo");
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        JPanel panel = new JPanel();
        frame.setContentPane( panel );
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.setLayout(new BorderLayout());

        JTextPane textPane = new JTextPane();
        textPane.setFont( new Font("monospaced", Font.PLAIN, 12) );
        textPane.setText("abc");

        JScrollPane scrollPane = new JScrollPane(textPane);
        panel.add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(300, 250));

        LineNumber lineNumber = new LineNumber( textPane );
        scrollPane.setRowHeaderView( lineNumber );

        frame.pack();
        frame.setVisible(true);
    }
}

