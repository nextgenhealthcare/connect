/* HTMLLexer.java is a generated file.  You probably want to
 * edit HTMLLexer.lex to make changes.  Use JFlex to generate it.
 * To generate HTMLLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main HTMLLexer.lex</code><br>
 * You will then have a file called HTMLLexer.java
 */

/*
 * This file is part of a <a href="http://ostermiller.org/syntax/">syntax
 * highlighting</a> package.
 * Copyright (C) 1999-2002 Stephen Ostermiller
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
 * HTMLLexer is a html 2.0 lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  HTMLLexer shredder = new HTMLLexer(System.in);
 *  HTMLToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 *  
 * <P>
 * There are two HTML Lexers that come with this package.  HTMLLexer is a basic HTML lexer
 * that knows the difference between tags, text, and comments.  HTMLLexer1 knows something
 * about the structure of tags and can return names and values from name value pairs.  It 
 * also knows about text elements such as words and character references.  The two are 
 * similar but which you should use depends on your purpose.  In my opinion the HTMLLexer1
 * is much better for syntax highlighting.
 * 
 * @see HTMLLexer1
 * @see HTMLToken
 */ 

%%

%public
%class HTMLLexer
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
            HTMLLexer shredder = new HTMLLexer(in);
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

%state SCRIPT
%state PRE
%state TEXTAREA

Digit=([0-9])
Letter=([a-zA-Z])
BLANK=([ ])
TAB=([\t])
FF=([\f])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF}|{EOL}|"&nbsp;")

StringLiteral=(([\"][^\"]*[\"])|([\'][^\']*[\']))
NameToken=(({Letter}|{Digit}|[\.\-])+)
Value=({NameToken}|{StringLiteral})
Attribute=({NameToken}(({WhiteSpace}*)"="({WhiteSpace}*){Value})?)

TagStart=("<")
EndTagStart=("</")
DocTagStart=("<!")
TagEnd=(">")
Doctype=([Dd][Oo][Cc][Tt][Yy][Pp][Ee])
DoctypeText=(([^\>\"\']|{StringLiteral})*)

NameScript=([Ss][Cc][Rr][Ii][Pp][Tt])
FalseEndScript=([\<]|[\<][\/]{WhiteSpace}*|[\<][\/]{WhiteSpace}*[Ss]|[\<][\/]{WhiteSpace}*[Ss][Cc]|[\<][\/]{WhiteSpace}*[Ss][Cc][Rr]|[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii]|[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii][Pp]|[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii][Pp][Tt]{WhiteSpace}*)
ScriptText=(([^\<]|{FalseEndScript}*[\<][^\/\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[^Ss\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][^Cc\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][Cc][^Rr\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][^Ii\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii][^Pp\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii][Pp][^Tt\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Ss][Cc][Rr][Ii][Pp][Tt]{WhiteSpace}*[^\<\>])*)

NamePre=([Pp][Rr][Ee])
FalseEndPre=([\<]|[\<][\/]{WhiteSpace}*|[\<][\/]{WhiteSpace}*[Pp]|[\<][\/]{WhiteSpace}*[Pp][Rr]|[\<][\/]{WhiteSpace}*[Pp][Rr][Ee]{WhiteSpace}*)
PreText=(([^\<]|{FalseEndPre}*[\<][^\/\<]|{FalseEndPre}*[\<][\/]{WhiteSpace}*[^Pp\<]|{FalseEndPre}*[\<][\/]{WhiteSpace}*[Pp][^Rr\<]|{FalseEndPre}*[\<][\/]{WhiteSpace}*[Pp][Rr][^Ee\<]|{FalseEndPre}*[\<][\/]{WhiteSpace}*[Pp][Rr][Ee]{WhiteSpace}*[^\<\>])*)

NameTextArea=([Tt][Ee][Xx][Tt][Aa][Rr][Ee][Aa])
FalseEndTextArea=([\<]|[\<][\/]{WhiteSpace}*|[\<][\/]{WhiteSpace}*[Tt]|[\<][\/]{WhiteSpace}*[Tt][Ee]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr][Ee]|[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr][Ee][Aa]{WhiteSpace}*)
TextAreaText=(([^\<]|{FalseEndScript}*[\<][^\/\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[^Tt\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][^Ee\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][^Xx\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][^Tt\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][^Aa\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][^Rr\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr][^Ee\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr][Ee][^Aa\<]|{FalseEndScript}*[\<][\/]{WhiteSpace}*[Tt][Ee][Xx][Tt][Aa][Rr][Ee][Aa]{WhiteSpace}*[^\<\>])*)

SGMLProcessingStart=("<?")
SGMLProcessingEnd=([\?]+">")
ASPProcessingStart=("<%")
ASPProcessingEnd=([\%]+">")
SGMLProcessingText=(([^\?]|[\?]+[^\>\?])*)
SGMLProcessing=({SGMLProcessingStart}{SGMLProcessingText}{SGMLProcessingEnd})
ASPProcessingText=(([^\%]|[\%]+[^\>\%])*)
ASPProcessing=({ASPProcessingStart}{ASPProcessingText}{ASPProcessingEnd})

Name=({Letter}({Letter}|{Digit}|[\.\-])*)
StartTag=({TagStart}{Name}(({WhiteSpace}*){Attribute})*({WhiteSpace}*){TagEnd})
ScriptStartTag=({TagStart}{NameScript}(({WhiteSpace}*){Attribute})*({WhiteSpace}*){TagEnd})
PreStartTag=({TagStart}{NamePre}(({WhiteSpace}*){Attribute})*({WhiteSpace}*){TagEnd})
TextAreaStartTag=({TagStart}{NameTextArea}(({WhiteSpace}*){Attribute})*({WhiteSpace}*){TagEnd})
EndTag=({EndTagStart}{WhiteSpace}*{Name}{WhiteSpace}*{TagEnd})
DoctypeTag=({DocTagStart}{Doctype}{DoctypeText}{TagEnd})

Comment=("--"([^\-]|([\-][^\-]))*"--")
CommentDeclaration=({DocTagStart}((({WhiteSpace}*){Comment}({WhiteSpace}*))*){TagEnd})

Word=(([^\ \r\n\f\t\<])*)

TagError=("<"[^\>]*">")
UnfinishedTag=("<"[^\>]*)

Text=(({Word}|{WhiteSpace})+)

%%

<YYINITIAL> {CommentDeclaration} {
	lastToken = HTMLToken.COMMENT;
    String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {DoctypeTag} {
	lastToken = HTMLToken.DOCTYPE_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {SGMLProcessing}|{ASPProcessing} {
	lastToken = HTMLToken.SCRIPT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {ScriptStartTag} {
    nextState = SCRIPT;
	lastToken = HTMLToken.START_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
<YYINITIAL> {PreStartTag} {
    nextState = PRE;
	lastToken = HTMLToken.START_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
<YYINITIAL> {TextAreaStartTag} {
    nextState = TEXTAREA;
	lastToken = HTMLToken.START_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
<YYINITIAL> {StartTag} {
	lastToken = HTMLToken.START_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {EndTag} {
	lastToken = HTMLToken.END_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> ({Text}) {
	lastToken = HTMLToken.TEXT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {TagError} {
	lastToken = HTMLToken.ERROR_MALFORMED_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {UnfinishedTag} {
	lastToken = HTMLToken.ERROR_MALFORMED_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<SCRIPT, PRE, TEXTAREA> {EndTag} {
    nextState = YYINITIAL;
	lastToken = HTMLToken.END_TAG;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
<SCRIPT> {ScriptText} {
	lastToken = HTMLToken.SCRIPT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<SCRIPT> {FalseEndScript} {
	lastToken = HTMLToken.SCRIPT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<PRE> {PreText} {
	lastToken = HTMLToken.TEXT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<PRE> {FalseEndPre} {
	lastToken = HTMLToken.SCRIPT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<TEXTAREA> {TextAreaText} {
	lastToken = HTMLToken.TEXT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<TEXTAREA> {FalseEndTextArea} {
	lastToken = HTMLToken.SCRIPT;
	String text = yytext();    
	HTMLToken t = (new HTMLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
