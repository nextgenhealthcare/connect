/*
 * JavaScriptTokenMarker.java - JavaScript token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.*;
import javax.swing.text.Segment;

/**
 * JavaScript token marker.
 *
 * @author Slava Pestov
 * @version $Id: JavaScriptTokenMarker.java,v 1.3 1999/12/13 03:40:29 sp Exp $
 */
public class JavaScriptTokenMarker extends CTokenMarker
{
	public JavaScriptTokenMarker()
	{
		super(false,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(javaScriptKeywords == null)
		{
			javaScriptKeywords = new KeywordMap(false);
			javaScriptKeywords.add("function",Token.KEYWORD3);
			javaScriptKeywords.add("get",Token.KEYWORD3);
			javaScriptKeywords.add("var",Token.KEYWORD1);
			javaScriptKeywords.add("else",Token.KEYWORD1);
			javaScriptKeywords.add("for",Token.KEYWORD1);
			javaScriptKeywords.add("if",Token.KEYWORD1);
			javaScriptKeywords.add("in",Token.KEYWORD1);
			javaScriptKeywords.add("new",Token.KEYWORD1);
			javaScriptKeywords.add("return",Token.KEYWORD1);
			javaScriptKeywords.add("while",Token.KEYWORD1);
			javaScriptKeywords.add("with",Token.KEYWORD1);
			javaScriptKeywords.add("break",Token.KEYWORD1);
			javaScriptKeywords.add("case",Token.KEYWORD1);
			javaScriptKeywords.add("continue",Token.KEYWORD1);
			javaScriptKeywords.add("default",Token.KEYWORD1);
			javaScriptKeywords.add("false",Token.LABEL);
			javaScriptKeywords.add("this",Token.LABEL);
			javaScriptKeywords.add("true",Token.LABEL);
			//Mirth specific
			//TODO: Figure out a consistent coloring scheme
			javaScriptKeywords.add("msg",Token.KEYWORD1);
			javaScriptKeywords.add("hl7_xml",Token.KEYWORD1);
			javaScriptKeywords.add("localMap",Token.KEYWORD1);
			javaScriptKeywords.add("globalMap",Token.KEYWORD1);
			javaScriptKeywords.add("hl7_er7",Token.KEYWORD1);
			javaScriptKeywords.add("logger",Token.KEYWORD3);
			javaScriptKeywords.add("message",Token.KEYWORD3);
			javaScriptKeywords.add("SMTPConnectionFactory",Token.KEYWORD3);
			javaScriptKeywords.add("DatabaseConnectionFactory",Token.KEYWORD3);
			javaScriptKeywords.add("executeCachedQuery",Token.KEYWORD1);
			javaScriptKeywords.add("createDatabaseConnection",Token.KEYWORD1);
			javaScriptKeywords.add("createSMTPConnection",Token.KEYWORD1);
			javaScriptKeywords.add("executeUpdate",Token.KEYWORD1);
			/*
			javaScriptKeywords.add("send",Token.KEYWORD1);
			javaScriptKeywords.add("info",Token.KEYWORD1);
			javaScriptKeywords.add("error",Token.KEYWORD1);
			javaScriptKeywords.add("close",Token.KEYWORD1);
			javaScriptKeywords.add("next",Token.KEYWORD1);
			javaScriptKeywords.add("getInt",Token.KEYWORD1);
			javaScriptKeywords.add("getString",Token.KEYWORD1);
			javaScriptKeywords.add("length",Token.KEYWORD1);
			*/
			//TODO: Should functions have coloring?
			//TODO: Finish adding variables from Mirth
			
		}
		return javaScriptKeywords;
	}

	// private members
	private static KeywordMap javaScriptKeywords;
}
