/*
 * TextAreaDefaults.java - Encapsulates default values for various settings
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPopupMenu;

import com.mirth.connect.client.ui.UIConstants;

/**
 * Encapsulates default settings for a text area. This can be passed to the
 * constructor once the necessary fields have been filled out. The advantage of
 * doing this over calling lots of set() methods after creating the text area is
 * that this method is faster.
 */
public class TextAreaDefaults
{

    // public String STYLE = "RUBY BLUE";//based on
    // http://wiseheartdesign.com/articles/2006/03/11/ruby-blue-textmate-theme
    public String STYLE = "Default";

    public InputHandler inputHandler;

    public SyntaxDocument document;

    public boolean editable;

    public boolean caretVisible;

    public boolean caretBlinks;

    public boolean blockCaret;

    public int electricScroll;

    public int cols;

    public int rows;

    public SyntaxStyle[] styles;

    public Color caretColor;

    public Color selectionColor;

    public Color lineHighlightColor;

    public Color foreground;

    public Color background;

    public boolean lineHighlight;

    public Color bracketHighlightColor;

    public boolean bracketHighlight;

    public Color eolMarkerColor;

    public boolean eolMarkers;

    public boolean paintInvalid;

    public Font font;

    public JPopupMenu popup;

    /**
     * Returns a new TextAreaDefaults object with the default values filled in.
     */
    public TextAreaDefaults()
    {

        this.inputHandler = new DefaultInputHandler();
        this.inputHandler.addDefaultKeyBindings();
        this.document = new SyntaxDocument();
        this.editable = true;

        this.blockCaret = false;
        this.caretVisible = true;
        this.caretBlinks = true;
        this.electricScroll = 1;

        this.cols = 1;
        this.rows = 1;
        // TODO: Make this dynamic via properties file
        if (STYLE.equals("RUBY BLUE"))
        {
            this.styles = SyntaxUtilities.getSyntaxStyles(STYLE);

            this.caretColor = Color.white; // Color.red;
            this.background = new Color(0x121E31);
            this.foreground = Color.white;
            this.selectionColor = new Color(0x38566F);
            this.lineHighlightColor = new Color(0x253E5A);
            this.lineHighlight = true;
            this.bracketHighlightColor = new Color(0x38566F);
            this.bracketHighlight = true;
            this.eolMarkerColor = new Color(0x009999);
            this.eolMarkers = false; // true;
            this.paintInvalid = false; // true;
            this.font = new Font(UIConstants.MONOSPACED_FONT_NAME, Font.PLAIN, 14);
        }
        else
        {
            this.styles = SyntaxUtilities.getSyntaxStyles(STYLE);
            this.caretColor = Color.black; // Color.red;
            this.background = Color.white;
            this.foreground = Color.black;
            this.selectionColor = new Color(0xc6d3de);
            this.lineHighlightColor = new Color(0x253E5A);
            this.lineHighlight = false;
            this.bracketHighlightColor = Color.darkGray;
            this.bracketHighlight = true;
            this.eolMarkerColor = Color.lightGray;// Color(0x009999);
            this.eolMarkers = true; // true;
            this.paintInvalid = false; // true;
            this.font = new Font(UIConstants.MONOSPACED_FONT_NAME, Font.PLAIN, 12);
        }

    }
}
