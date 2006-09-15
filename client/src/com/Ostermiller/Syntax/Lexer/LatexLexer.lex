/* LatexLexer.java is a generated file.  You probably want to
 * edit LatexLexer.lex to make changes.  Use JFlex to generate it.
 * To generate LatexLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main LatexLexer.lex</code><br>
 * You will then have a file called LatexLexer.java
 */

/*
 * This file is part of a <a href="http://ostermiller.org/syntax/">syntax
 * highlighting</a> package.
 * Copyright (C) 2002 Stephen Ostermiller
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

package com.Ostermiller.Syntax.Lexer;

import java.io.*;

/** 
 * 
 * LatexLexer is a LaTeX lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  LatexLexer shredder = new LatexLexer(System.in);
 *  LatexToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 *  
 * @see LatexToken
 */ 

%%

%public
%class LatexLexer
%implements Lexer
%function getNextToken
%type Token 

%{

	private int lastToken;

    private int nextState=YYINITIAL;
	
    /** 
     * next Token method that allows you to control if whitespace and comments are
     * returned as tokens.
     */
    public Token getNextToken(boolean returnComments, boolean returnWhiteSpace)throws IOException{
        Token t = getNextToken();
        while (t != null && ((!returnWhiteSpace && t.isWhiteSpace()) || (!returnComments && t.isComment()))){
            t = getNextToken();
        }
        return (t); 
    }

    /**
     * Prints out tokens from a file or System.in.
     * If no arguments are given, System.in will be used for input.
     * If more arguments are given, the first argument will be used as
     * the name of the file to use as input
     *
     * @param args program arguments, of which the first is a filename
     */
    public static void main(String[] args) {
        InputStream in;
        try {
            if (args.length > 0){
                File f = new File(args[0]);
                if (f.exists()){
                    if (f.canRead()){
                        in = new FileInputStream(f);
                    } else {
                        throw new IOException("Could not open " + args[0]);
                    }
                } else {
                    throw new IOException("Could not find " + args[0]);
                }                   
            } else {
                in = System.in;
            }       
            LatexLexer shredder = new LatexLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != CToken.WHITE_SPACE){
                    System.out.println(t);
                }
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Closes the current input stream, and resets the scanner to read from a new input stream.
	 * All internal variables are reset, the old input stream  cannot be reused
	 * (content of the internal buffer is discarded and lost).
	 * The lexical state is set to the initial state.
     * Subsequent tokens read from the lexer will start with the line, char, and column
     * values given here.
     *
     * @param reader The new input.
     * @param yyline The line number of the first token.
     * @param yychar The position (relative to the start of the stream) of the first token.
     * @param yycolumn The position (relative to the line) of the first token.
     * @throws IOException if an IOExecption occurs while switching readers.
     */
    public void reset(java.io.Reader reader, int yyline, int yychar, int yycolumn) throws IOException{
        yyreset(reader);
        this.yyline = yyline;
		this.yychar = yychar;
		this.yycolumn = yycolumn;
	}
%}

%line
%char
%full


BLANK=([ ])
TAB=([\t])
FF=([\f])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF}|{EOL})

EscChar=([\\])
CommentChar=([\%])
CommandChar=([\\])
SpecialChar=([\(\)\$\&\#\{\}\_\~\^\%\\])
TextChar=([^\(\)\$\&\#\{\}\_\~\^\\\% \t\f\r\n]|({EscChar}{SpecialChar}))

Comment=({CommentChar}[^\r\n]*{EOL}?)
Command=({CommandChar}{TextChar}+)
Text=(({TextChar}|{WhiteSpace})+)

%%

<YYINITIAL> {Comment} {
    lastToken = LatexToken.COMMENT;
    String text = yytext();    
    LatexToken t = (new LatexToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return(t);
}
<YYINITIAL> {Command} {
    lastToken = LatexToken.COMMAND;
    String text = yytext();    
    LatexToken t = (new LatexToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return(t);
}
<YYINITIAL> {SpecialChar} {
    lastToken = LatexToken.COMMAND_CHAR;
    String text = yytext();    
    LatexToken t = (new LatexToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return(t);
}
<YYINITIAL> {Text} {
    lastToken = LatexToken.TEXT;
    String text = yytext();    
    LatexToken t = (new LatexToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return(t);
}

/* JFlex reports this state can never be reached.
<YYINITIAL> [^] {
    System.err.println("Unmatched input.");
    String state = "";    
	String text = yytext();
    switch (nextState){
        case YYINITIAL: state = "YYINITIAL"; break;
    }
    System.err.println("State: " + state);
    System.err.println("Text: " + text);
    System.err.println("Line: " + (yyline+1));
    System.err.println("Column: " + (yycolumn+1));
	yy_ScanError(YY_NO_MATCH);
}*/

