/*
 * This file is part of the programmer editor demo
 * Copyright (C) 2001-2005 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Syntax+Highlighting
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * See COPYING.TXT for details.
 */
package com.Ostermiller.Syntax;

import javax.swing.text.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.Ostermiller.Syntax.Lexer.*;

/**
 * A <a href="http://ostermiller.org/syntax/editor.html">demonstration text
 * editor</a> that uses syntax highlighting.
 */
public class HighlightedDocument extends DefaultStyledDocument {
	public static final Object C_STYLE = CLexer.class;
	public static final Object HTML_STYLE = HTMLLexer.class;
	public static final Object HTML_KEY_STYLE = HTMLLexer1.class;
	public static final Object JAVA_STYLE = JavaLexer.class;
	public static final Object JAVASCRIPT_STYLE = JavaScriptLexer.class;
	public static final Object LATEX_STYLE = LatexLexer.class;
	public static final Object PLAIN_STYLE = PlainLexer.class;
	public static final Object PROPERTIES_STYLE = PropertiesLexer.class;
	public static final Object SQL_STYLE = SQLLexer.class;
	public static final Object GRAYED_OUT_STYLE = new Object();

	/**
	 * A reader wrapped around the document so that the document can be fed into
	 * the lexer.
	 */
	private DocumentReader documentReader;
	
	/** If non-null, all is drawn with this style (no lexing). */
	private AttributeSet globalStyle = null;

	/**
	 * The lexer that tells us what colors different words should be.
	 */
	private Lexer syntaxLexer;

	/**
	 * A thread that handles the actual coloring.
	 */
	private Colorer colorer;

	/**
	 * A lock for modifying the document, or for actions that depend on the
	 * document not being modified.
	 */
	private Object docLock = new Object();

	/**
	 * Create a new Demo
	 */
	public HighlightedDocument() {

		// Start the thread that does the coloring
		colorer = new Colorer(this);
		colorer.start();

		// create the new document.
		documentReader = new DocumentReader(this);
		syntaxLexer = new JavaLexer(documentReader);
	}

	/**
	 * Color or recolor the entire document
	 */
	public void colorAll() {
		color(0, getLength());
	}

	/**
	 * Color a section of the document. The actual coloring will start somewhere
	 * before the requested position and continue as long as needed.
	 * 
	 * @param position
	 *            the starting point for the coloring.
	 * @param adjustment
	 *            amount of text inserted or removed at the starting point.
	 */
	public void color(int position, int adjustment) {
		colorer.color(position, adjustment);
	}
	
	public void setGlobalStyle(AttributeSet value) {
		globalStyle = value;
		colorAll();
	}

	public void setHighlightStyle(Object value) {
		if (value == HighlightedDocument.GRAYED_OUT_STYLE) {
			setGlobalStyle(TokenStyles.getStyle("grayedOut"));
			return;
		}

		if (!(value instanceof Class))
			value = HighlightedDocument.PLAIN_STYLE;
		Class source = (Class) value;
		Class[] parms = { Reader.class };
		Object[] args = { documentReader };
		try {
			Constructor cons = source.getConstructor(parms);
			syntaxLexer = (Lexer) cons.newInstance(args);
			globalStyle = null;
			colorAll();
		} catch (SecurityException e) {
			System.err.println("HighlightEditor.SecurityException");
		} catch (NoSuchMethodException e) {
			System.err.println("HighlightEditor.NoSuchMethod");
		} catch (InstantiationException e) {
			System.err.println("HighlightEditor.InstantiationException");
		} catch (InvocationTargetException e) {
			System.err.println("HighlightEditor.InvocationTargetException");
		} catch (IllegalAccessException e) {
			System.err.println("HighlightEditor.IllegalAccessException");
		}
	}
	
	//
	// Intercept inserts and removes to color them.
	//
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		synchronized (docLock) {
			super.insertString(offs, str, a);
			color(offs, str.length());
			documentReader.update(offs, str.length());
		}
	}

	public void remove(int offs, int len) throws BadLocationException {
		synchronized (docLock) {
			super.remove(offs, len);
			color(offs, -len);
			documentReader.update(offs, -len);
		}
	}

	// methods for Colorer to retrieve information
	DocumentReader getDocumentReader() { return documentReader; }
	Object getDocumentLock() { return docLock; }
	Lexer getSyntaxLexer() { return syntaxLexer; }
	AttributeSet getGlobalStyle() { return globalStyle; }
}
