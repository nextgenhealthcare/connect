/*
 * SyntaxUtilities.java - Utility functions used by syntax colorizing
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashSet;

import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

import org.syntax.jedit.tokenmarker.Token;

/**
 * Class with several utility functions used by jEdit's syntax colorizing
 * subsystem.
 * 
 * @author Slava Pestov
 * @version $Id: SyntaxUtilities.java,v 1.9 1999/12/13 03:40:30 sp Exp $
 */
public class SyntaxUtilities
{
    // MIRTH-2000: HashSet of invalid control characters
    public static final HashSet<Character> invalidCharSet;

    static {
        invalidCharSet = new HashSet<Character>();
        // x00-x08
        invalidCharSet.add('\u0000');
        invalidCharSet.add('\u0001');
        invalidCharSet.add('\u0002');
        invalidCharSet.add('\u0003');
        invalidCharSet.add('\u0004');
        invalidCharSet.add('\u0005');
        invalidCharSet.add('\u0006');
        invalidCharSet.add('\u0007');
        invalidCharSet.add('\u0008');
        // x0B-x0C
        invalidCharSet.add('\u000B');
        invalidCharSet.add('\u000C');
        // x0E-x1F
        invalidCharSet.add('\u000E');
        invalidCharSet.add('\u000F');
        invalidCharSet.add('\u0010');
        invalidCharSet.add('\u0011');
        invalidCharSet.add('\u0012');
        invalidCharSet.add('\u0013');
        invalidCharSet.add('\u0014');
        invalidCharSet.add('\u0015');
        invalidCharSet.add('\u0016');
        invalidCharSet.add('\u0017');
        invalidCharSet.add('\u0018');
        invalidCharSet.add('\u0019');
        invalidCharSet.add('\u001A');
        invalidCharSet.add('\u001B');
        invalidCharSet.add('\u001C');
        invalidCharSet.add('\u001D');
        invalidCharSet.add('\u001E');
        invalidCharSet.add('\u001F');
        // x7F
        invalidCharSet.add('\u007F');
    }
    
    /**
     * Checks if a subregion of a <code>Segment</code> is equal to a string.
     * 
     * @param ignoreCase
     *            True if case should be ignored, false otherwise
     * @param text
     *            The segment
     * @param offset
     *            The offset into the segment
     * @param match
     *            The string to match
     */
    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, String match)
    {
        int length = offset + match.length();
        char[] textArray = text.array;
        if (length > text.offset + text.count)
            return false;
        for (int i = offset, j = 0; i < length; i++, j++)
        {
            char c1 = textArray[i];
            char c2 = match.charAt(j);
            if (ignoreCase)
            {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2)
                return false;
        }
        return true;
    }

    /**
     * Checks if a subregion of a <code>Segment</code> is equal to a character
     * array.
     * 
     * @param ignoreCase
     *            True if case should be ignored, false otherwise
     * @param text
     *            The segment
     * @param offset
     *            The offset into the segment
     * @param match
     *            The character array to match
     */
    public static boolean regionMatches(boolean ignoreCase, Segment text, int offset, char[] match)
    {
        int length = offset + match.length;
        char[] textArray = text.array;
        if (length > text.offset + text.count)
            return false;
        for (int i = offset, j = 0; i < length; i++, j++)
        {
            char c1 = textArray[i];
            char c2 = match[j];
            if (ignoreCase)
            {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
            }
            if (c1 != c2)
                return false;
        }
        return true;
    }

    /**
     * Returns the default style table. This can be passed to the
     * <code>setStyles()</code> method of <code>SyntaxDocument</code> to use
     * the default syntax styles.
     */
    public static SyntaxStyle[] getDefaultSyntaxStyles()
    {
        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

        styles[Token.COMMENT1] = new SyntaxStyle(new Color(0x428BDD), true, false);
        styles[Token.COMMENT2] = new SyntaxStyle(new Color(0x428BDD), true, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(new Color(0xF8BB00), false, true);
        styles[Token.KEYWORD2] = new SyntaxStyle(new Color(0xB53B3C), false, false);
        styles[Token.KEYWORD3] = new SyntaxStyle(new Color(0xB53B3C), false, true);
        styles[Token.LITERAL1] = new SyntaxStyle(new Color(0x02C005), false, false);
        styles[Token.LITERAL2] = new SyntaxStyle(new Color(0xEDDD3D), false, false);
        styles[Token.LABEL] = new SyntaxStyle(new Color(0x8AA6C1), true, true);
        styles[Token.OPERATOR] = new SyntaxStyle(new Color(0x85A1BB), false, true);
        styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);
        styles[Token.DIGIT] = new SyntaxStyle(new Color(0xEDDD3D), false, false);
        return styles;

    }

    // eventually this will load syntax style dynamically from properties file
    public static SyntaxStyle[] getSyntaxStyles(String name)
    {
        SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];
        if (name.equals("RUBY BLUE"))
        {
            styles[Token.COMMENT1] = new SyntaxStyle(new Color(0x428BDD), true, false);
            styles[Token.COMMENT2] = new SyntaxStyle(new Color(0x428BDD), true, false);
            styles[Token.KEYWORD1] = new SyntaxStyle(new Color(0xF8BB00), false, true);
            styles[Token.KEYWORD2] = new SyntaxStyle(new Color(0xEDDD3D), false, false);
            styles[Token.KEYWORD3] = new SyntaxStyle(new Color(0x85A1BB), false, true);
            styles[Token.LITERAL1] = new SyntaxStyle(new Color(0x02C005), false, false);
            styles[Token.LITERAL2] = new SyntaxStyle(new Color(0xEDDD3D), false, false);
            styles[Token.LABEL] = new SyntaxStyle(new Color(0x8AA6C1), true, true);
            styles[Token.OPERATOR] = new SyntaxStyle(Color.lightGray, false, true);
            styles[Token.INVALID] = new SyntaxStyle(Color.red, false, true);
            styles[Token.DIGIT] = new SyntaxStyle(new Color(0xEDDD3D), false, false);
            return styles;
        }
        else
        {
            Color maroon = new Color(0xB03060);
            Color darkBlue = new Color(0x000080);
            Color darkGreen = Color.GREEN.darker().darker();
            Color darkPurple = new Color(0xA020F0).darker();
            Color darkRed = Color.red.darker();

            styles[Token.COMMENT1] = new SyntaxStyle(darkGreen, false, false);
            styles[Token.COMMENT2] = new SyntaxStyle(darkGreen, false, false);
            styles[Token.KEYWORD1] = new SyntaxStyle(Color.blue, false, false);
            styles[Token.KEYWORD2] = new SyntaxStyle(Color.orange, false, false);
            styles[Token.KEYWORD3] = new SyntaxStyle(darkPurple, false, true);
            styles[Token.LITERAL1] = new SyntaxStyle(maroon, false, false);
            styles[Token.LITERAL2] = new SyntaxStyle(darkRed, false, false);
            styles[Token.LABEL] = new SyntaxStyle(darkGreen, false, false);
            styles[Token.OPERATOR] = new SyntaxStyle(Color.gray.darker(), false, true);
            styles[Token.INVALID] = new SyntaxStyle(Color.red, false, false);
            styles[Token.DIGIT] = new SyntaxStyle(maroon, false, false);
            return styles;
        }
    }

    /**
     * Paints the specified line onto the graphics context. Note that this
     * method munges the offset and count values of the segment.
     * 
     * @param line
     *            The line segment
     * @param tokens
     *            The token list for the line
     * @param styles
     *            The syntax style list
     * @param expander
     *            The tab expander used to determine tab stops. May be null
     * @param gfx
     *            The graphics context
     * @param x
     *            The x co-ordinate
     * @param y
     *            The y co-ordinate
     * @return The x co-ordinate, plus the width of the painted string
     */
    public static int paintSyntaxLine(Segment line, Token tokens, SyntaxStyle[] styles, TabExpander expander, Graphics gfx, int x, int y)
    {
        Font defaultFont = gfx.getFont();
        Color defaultColor = gfx.getColor();

        int offset = 0;
        for (;;)
        {
            byte id = tokens.id;
            if (id == Token.END)
                break;

            int length = tokens.length;
            if (id == Token.NULL)
            {
                if (!defaultColor.equals(gfx.getColor()))
                    gfx.setColor(defaultColor);
                if (!defaultFont.equals(gfx.getFont()))
                    gfx.setFont(defaultFont);
            }
            else
                styles[id].setGraphicsFlags(gfx, defaultFont);

            line.count = length;
            
            /*
             * MIRTH-2000: We want to replace all invalid control characters
             * with a bold red question mark '?'. Instead of rendering each
             * token as a whole string, render each character individually. If
             * it is an invalid character, render it as a bold red question mark
             * '?'.
             */
            int tokenLength = line.getEndIndex();
            for (int i = line.getBeginIndex(); i < tokenLength; i++) {
                if (invalidCharSet.contains(line.array[i])) {
                    Font oldFont = gfx.getFont();
                    gfx.setFont(new Font(oldFont.getFamily(), Font.BOLD, oldFont.getSize()));
                    Color oldColor = gfx.getColor();
                    gfx.setColor(Color.RED);
                    x = Utilities.drawTabbedText(new Segment(new char[] { '?' }, 0, 1), x, y, gfx, expander, 0);
                    gfx.setColor(oldColor);
                    gfx.setFont(oldFont);
                } else {
                    x = Utilities.drawTabbedText(new Segment(line.array, line.getBeginIndex(), 1), x, y, gfx, expander, 0);
                }

                line.offset += 1;
                offset += 1;
                length--;

                line.count = length;
            }

            // MIRTH-2000: Old Drawing code
//            x = Utilities.drawTabbedText(line, x, y, gfx, expander, 0);
//            line.offset += length;
//            offset += length;

            tokens = tokens.next;
        }

        return x;
    }

    // private members
    private SyntaxUtilities()
    {
    }
}
