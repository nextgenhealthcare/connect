/* PropertiesLexer.java is a generated file.  You probably want to
 * edit PropertiesLexer.lex to make changes.  Use JFlex to generate it.
 * To generate PropertiesLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main PropertiesLexer.lex</code><br>
 * You will then have a file called PropertiesLexer.java
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
 * PropertiesLexer is a Java Properties file lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  PropertiesLexer shredder = new PropertiesLexer(System.in);
 *  PropertiesToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 * @see PropertiesToken
 */ 

%%

%public
%class PropertiesLexer
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
            PropertiesLexer shredder = new PropertiesLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != PropertiesToken.WHITE_SPACE){
                    System.out.println(t);
                }
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
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
%column
%unicode

%state LINE_END
%state WHITE_SPACE
%state NAME
%state MID_NAME
%state MID_NAME_NEW_LINE
%state SEPARATOR
%state VALUE
%state MID_VALUE

HexDigit=([0-9a-fA-F])
BLANK=([ ])
TAB=([\t])
FF=([\f])
EscChar=([\\])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF})
LineEndingWhiteSpace=({WhiteSpace}*{EOL})
fourHex=({HexDigit}{4})
UnicodeEscape=({EscChar}[u]({fourHex}*))
Escape=(({EscChar}[^\r\n])|{UnicodeEscape})
LineEscape=({EscChar}{EOL})

NameText=([^\=\:\t\f\r\n\\ ]|{Escape})
NameTextWSeparators=([^\t\f\r\n\\ ]|{Escape})
ValueText=([^\t\f\r\n\\ ]|([\t\f ]+([^\t\f\r\n\\ ]|{Escape}))|{Escape})
Comment=([\!\#][^\r\n]*)
Name=({NameText}*)
Value=((({NameText}+){ValueText}*)?)
FullValue=((({NameTextWSeparators}+){ValueText}*)?)

%%

<YYINITIAL> {Comment} { 
    nextState = LINE_END;
    lastToken = PropertiesToken.COMMENT;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL> {WhiteSpace}+ {
    nextState = WHITE_SPACE;    
    lastToken = PropertiesToken.WHITE_SPACE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<NAME, SEPARATOR, VALUE, MID_VALUE> {WhiteSpace} {
    lastToken = PropertiesToken.WHITE_SPACE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL, LINE_END, NAME, SEPARATOR, VALUE, NAME, MID_NAME, MID_NAME_NEW_LINE, MID_VALUE> {LineEndingWhiteSpace} {
    nextState = YYINITIAL;
    lastToken = PropertiesToken.WHITE_SPACE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, WHITE_SPACE, MID_NAME, MID_NAME_NEW_LINE> {Name} {
    nextState = NAME;
    lastToken = PropertiesToken.NAME;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, WHITE_SPACE, NAME, MID_NAME, MID_NAME_NEW_LINE> ":" {
    nextState = SEPARATOR;
    lastToken = PropertiesToken.COLON;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, WHITE_SPACE, NAME, MID_NAME, MID_NAME_NEW_LINE> "=" {
    nextState = SEPARATOR;
    lastToken = PropertiesToken.EQUAL;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<NAME> {Value} {
    nextState = VALUE;
    lastToken = PropertiesToken.VALUE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<NAME, MID_NAME, MID_NAME_NEW_LINE> ({LineEscape}) {
    nextState = MID_NAME_NEW_LINE;
    lastToken = PropertiesToken.LINE_CONTINUE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<MID_NAME_NEW_LINE> ({WhiteSpace}*) {
    nextState = MID_NAME;
    lastToken = PropertiesToken.WHITE_SPACE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<SEPARATOR, MID_VALUE> {FullValue} {
    nextState = VALUE;
    lastToken = PropertiesToken.VALUE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<SEPARATOR, VALUE, MID_VALUE> ({LineEscape}) {
    nextState = MID_VALUE;
    lastToken = PropertiesToken.LINE_CONTINUE;
    String text = yytext();
    PropertiesToken t = (new PropertiesToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL,LINE_END,WHITE_SPACE,NAME,SEPARATOR,VALUE,MID_NAME,MID_NAME_NEW_LINE,MID_VALUE> [^] {
    System.err.println("Unmatched input.");
    String state = "";    
	String text = yytext();
    switch (nextState){
        case YYINITIAL: state = "YYINITIAL"; break;
        case LINE_END: state = "LINE_END"; break;
        case WHITE_SPACE: state = "WHITE_SPACE"; break;
        case NAME: state = "NAME"; break;
        case SEPARATOR: state = "SEPARATOR"; break;
        case VALUE: state = "VALUE"; break;
        case MID_NAME: state = "MID_NAME"; break;
    }
    System.err.println("State: " + state);
    System.err.println("Text: " + text);
    System.err.println("Line: " + (yyline+1));
    System.err.println("Column: " + (yycolumn+1));
	zzScanError(ZZ_NO_MATCH);
}
