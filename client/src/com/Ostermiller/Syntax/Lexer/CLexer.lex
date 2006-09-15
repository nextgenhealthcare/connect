/* CLexer.java is a generated file.  You probably want to
 * edit CLexer.lex to make changes.  Use JFlex to generate it.
 * To generate CLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main CLexer.lex</code><br>
 * You will then have a file called CLexer.java
 */

/*
 * This file is part of a <a href="http://ostermiller.org/syntax/">syntax
 * highlighting</a> package.
 * Copyright (C) 1999-2003 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Syntax+Highlighting
 * Copyright (C) 2003 Elliott Hughes <ehughes@bluearc.com>
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
 * CLexer is a c language lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  CLexer shredder = new CLexer(System.in);
 *  CToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 * 
 * @see CToken
 */ 

%%

%public
%class CLexer
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
            CLexer shredder = new CLexer(in);
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

%state MIDDLE_OF_LINE
%state PREPROCESSOR

HASH=("#"|"??=")
LBRACKET=("["|"??(")
RBRACKET=("]"|"??)")
BACKSLASH=([\\]|"??/")
CARET=("^"|"??'")
LBRACE=("{"|"??<")
RBRACE=("}"|"??>")
VERTICAL=("|"|"??!")
TILDE=("~"|"??-")

BooleanLiteral=("true"|"false")
HexDigit=([0-9a-fA-F])
Digit=([0-9])
OctalDigit=([0-7])
TetraDigit=([0-3])
NonZeroDigit=([1-9])
Letter=([a-zA-Z_])
BLANK=([ ])
TAB=([\t])
FF=([\f])
EscChar=({BACKSLASH})
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF}|{EOL})
NonBreakingWhiteSpace=({BLANK}|{TAB}|{FF})
AnyNonSeparator=([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\']|{HASH}|{BACKSLASH})

OctEscape1=({EscChar}{OctalDigit})
OctEscape2=({EscChar}{OctalDigit}{OctalDigit})
OctEscape3=({EscChar}{TetraDigit}{OctalDigit}{OctalDigit})
OctEscape=({OctEscape1}|{OctEscape2}|{OctEscape3})

HexEscape=({EscChar}[x|X]{HexDigit}{HexDigit})

Escape=({EscChar}([a]|[b]|[f]|[n]|[r]|[t]|[v]|[\']|[\"]|[\?]|{BACKSLASH}|[0]))
Identifier=({Letter}({Letter}|{Digit}|"$")*)
ErrorIdentifier=({AnyNonSeparator}+)

Comment=("//"[^\r\n]*)
TradCommentBegin=("/*")
DocCommentBegin =("/**")
NonTermStars=([^\*\/]*[\*]+[^\*\/])
TermStars=([\*]+[\/])
CommentText=((([^\*]*[\/])|{NonTermStars})*)
CommentEnd=([^\*]*{TermStars})
TradComment=({TradCommentBegin}{CommentText}{CommentEnd})
DocCommentEnd1=([^\/\*]{CommentText}{CommentEnd})
DocCommentEnd2=({NonTermStars}{CommentText}{CommentEnd})
DocComment=({DocCommentBegin}({DocCommentEnd1}|{DocCommentEnd2}|{TermStars}|[\/]))
OpenComment=({TradCommentBegin}{CommentText}([^\*]*)([\*]*))

LongSuffix=(([lL][uU]?)|([uU][lL]?))
DecimalNum=(([0]|{NonZeroDigit}{Digit}*){LongSuffix}?)
OctalNum=([0]{OctalDigit}*{LongSuffix}?)
HexNum=([0]([x]|[X]){HexDigit}{HexDigit}*{LongSuffix}?)

Sign=([\+\-])
SignedInt=({Sign}?{Digit}+)
Expo=([eE])
ExponentPart=({Expo}{SignedInt})
FloatSuffix=([fFlL])
FloatWDecimal=(({Digit}*[\.]{Digit}+)|({Digit}+[\.]{Digit}*))
Float1=({FloatWDecimal}{ExponentPart}?)
Float2=({Digit}+{ExponentPart})
Float=(({Float1}|{Float2}){FloatSuffix}?)
ErrorFloat=({Digit}({AnyNonSeparator}|[\.])*)

AnyChrChr=([^\'\n\r\\])
TrigraphChar = ({HASH}|{LBRACKET}|{RBRACKET}|{CARET}|{LBRACE}|{RBRACE}|{VERTICAL}|{TILDE})
UnclosedCharacter=([\']({Escape}|{OctEscape}|{HexEscape}|{TrigraphChar}|{AnyChrChr}))
Character=({UnclosedCharacter}[\'])
MalformedUnclosedCharacter=([\']({AnyChrChr}|({EscChar}[^\n\r]))*)
MalformedCharacter=([\'][\']|{MalformedUnclosedCharacter}[\'])

AnyStrChr=([^\"\n\r\\\?])
SlashNewLine=({BACKSLASH}{EOL})
FalseTrigraph= (("?"(("?")*)[^\=\(\)\/\'\<\>\!\-\\\?\"\n\r])|("?"[\=\(\)\/\'\<\>\!\-]))
UnclosedString=([\"]((((("?")*)({Escape}|{OctEscape}|{HexEscape}|{TrigraphChar}))|{FalseTrigraph}|{AnyStrChr}|{SlashNewLine})*)(("?")*))
String=({UnclosedString}[\"])
MalformedUnclosedString=([\"]([^\"\n\r])*)
MalformedString=({MalformedUnclosedString}[\"])

PreProcessorKeyWord=("include"|"include_next"|"define"|"undef"|"if"|"ifdef"|"ifndef"|"else"|"elif"|"endif"|"line"|"pragma"|"error")
PreProcessorEscapes=({EscChar}{EOL}|{EscChar})
PreProcessorText=(([^\n\r\/]|{PreProcessorEscapes}|[\/][^\/\*\n\r]|[\/]{PreProcessorEscapes})*)
PreProcessorDirective=({HASH}({NonBreakingWhiteSpace}*){PreProcessorKeyWord}{PreProcessorText})
MalformedPreProcessorDirective=({HASH}({NonBreakingWhiteSpace}*)([^\/\n\r\ \t\f\\]*))

%% 

<YYINITIAL> {PreProcessorDirective} {
    nextState = PREPROCESSOR;
	lastToken = CToken.PREPROCESSOR_DIRECTIVE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
<PREPROCESSOR> {PreProcessorText} {
	lastToken = CToken.PREPROCESSOR_DIRECTIVE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return(t);
}
<YYINITIAL> {MalformedPreProcessorDirective} {
    nextState = PREPROCESSOR;
	lastToken = CToken.ERROR_MALFORMED_PREPROCESSOR_DIRECTIVE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL, MIDDLE_OF_LINE> "(" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_LPAREN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
    }
<YYINITIAL, MIDDLE_OF_LINE> ")" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_RPAREN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {LBRACE} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_LBRACE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {RBRACE} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_RBRACE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {LBRACKET} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_LBRACKET;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {RBRACKET} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_RBRACKET;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ";" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_SEMICOLON;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "," {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_COMMA;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "." {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_PERIOD;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "->" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.SEPARATOR_ARROW;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> "=" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ">" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_GREATER_THAN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "<" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LESS_THAN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("!"|"not") {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LOGICAL_NOT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ({TILDE}|"compl") {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_COMPLIMENT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "?" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_QUESTION;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ":" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_COLON;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "+" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_ADD;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "-" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SUBTRACT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "*" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_MULTIPLY;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "/" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_DIVIDE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("&"|"bitand") {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_AND;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ({VERTICAL}|"bitor") {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_OR;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ({CARET}|"xor") {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_XOR;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "%" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_MOD;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> "==" {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_EQUAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "<=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LESS_THAN_OR_EQUAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ">=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_GREATER_THAN_OR_EQUAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("!="|"not_eq") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_NOT_EQUAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {VERTICAL}{VERTICAL} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LOGICAL_OR;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("&&"|"and") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LOGICAL_AND;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("||"|"or") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_LOGICAL_OR;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "++" { 
    lastToken = CToken.OPERATOR_INCREMENT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "--" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_DECREMENT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ">>" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SHIFT_RIGHT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "<<" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SHIFT_LEFT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> "+=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_ADD_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "-=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SUBTRACT_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "*=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_MULTIPLY_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "/=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_DIVIDE_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ("&="|"and_eq") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_AND_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ({VERTICAL}"="|"or_eq") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_OR_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ({CARET}"="|"xor_eq") { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_BITWISE_XOR_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "%=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_MOD_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> "<<=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SHIFT_LEFT_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> ">>=" { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.OPERATOR_SHIFT_RIGHT_ASSIGN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> "asm" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_ASM;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "auto" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_AUTO;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "break" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_BREAK;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "case" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CASE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "const" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CONST;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "continue" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CONTINUE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "default" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_DEFAULT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "do" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_DO;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "else" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_ELSE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "enum" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_ENUM;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "explicit" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_EXPLICIT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "export" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_EXPLICIT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "extern" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_EXTERN;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "for" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_FOR;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "goto" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_GOTO;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "if" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_IF;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "register" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_REGISTER;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "return" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_RETURN;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "sizeof" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_SIZEOF;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "static" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_STATIC;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "struct" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_STRUCT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "switch" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_SWITCH;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "typedef" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_TYPEDEF;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "union" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_UNION;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "volatile" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_VOLATILE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "while" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_WHILE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "catch" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CATCH;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "class" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CLASS;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "const_cast" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CONST_CAST;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "delete" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_DELETE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "dynamic_cast" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_DYNAMIC_CAST;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "friend" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_FRIEND;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "inline" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_INLINE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "mutable" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_MUTABLE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "namespace" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_NAMESPACE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "new" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_NEW;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "operator" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_OPERATOR;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "overload" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_OVERLOAD;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "private" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_PRIVATE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "protected" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_PROTECTED;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "public" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_PUBLIC;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "reinterpret_cast" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_REINTERPRET_CAST;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "static_cast" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_STATIC_CAST;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "template" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_TEMPLATE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "this" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_THIS;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "throw" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_THROW;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "try" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_TRY;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "typeid" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_TYPEID;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "typename" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_TYPENAME;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "using" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_USING;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "virtual" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_VIRTUAL;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "bool" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_BOOL;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "char" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_CHAR;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "double" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_DOUBLE;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "float" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_FLOAT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "int" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_INT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "long" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_LONG;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "short" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_SHORT;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "signed" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_SIGNED;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "unsigned" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_UNSIGNED;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "void" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_VOID;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}
<YYINITIAL, MIDDLE_OF_LINE> "wchar_t" {
    nextState = MIDDLE_OF_LINE;
	lastToken = CToken.RESERVED_WORD_WCHAR_T;
	String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return(t);
}

<YYINITIAL, MIDDLE_OF_LINE> {BooleanLiteral} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_BOOLEAN;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> {Identifier} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.IDENTIFIER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> {DecimalNum} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_INTEGER_DECIMAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {OctalNum} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_INTEGER_OCTAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {HexNum} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_INTEGER_HEXIDECIMAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {Float} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_FLOATING_POINT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {Character} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_CHARACTER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {String} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.LITERAL_STRING;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> ({NonBreakingWhiteSpace}+) { 
    lastToken = CToken.WHITE_SPACE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	return (t);
}

<YYINITIAL, MIDDLE_OF_LINE, PREPROCESSOR> ({WhiteSpace}+) { 
    nextState = YYINITIAL;
    lastToken = CToken.WHITE_SPACE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE, PREPROCESSOR> {Comment} { 
    nextState = YYINITIAL;
    lastToken = CToken.COMMENT_END_OF_LINE;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {DocComment} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.COMMENT_DOCUMENTATION;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {TradComment} {
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.COMMENT_TRADITIONAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<PREPROCESSOR> {TradComment} {
    lastToken = CToken.COMMENT_TRADITIONAL;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL, MIDDLE_OF_LINE> {UnclosedString} { 
    /* most of these errors have to be caught down near the end of the file.
     * This way, previous expressions of the same length have precedence.
     * This is really useful for catching anything bad by just allowing it 
     * to slip through the cracks. 
     */ 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_UNCLOSED_STRING;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {MalformedUnclosedString} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_MALFORMED_UNCLOSED_STRING;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {MalformedString} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_MALFORMED_STRING;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {UnclosedCharacter} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_UNCLOSED_CHARACTER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {MalformedUnclosedCharacter} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_MALFORMED_UNCLOSED_CHARACTER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {MalformedCharacter} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_MALFORMED_CHARACTER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {ErrorFloat} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_FLOAT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE> {ErrorIdentifier} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_IDENTIFIER;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
<YYINITIAL, MIDDLE_OF_LINE, PREPROCESSOR> {OpenComment} { 
    nextState = MIDDLE_OF_LINE;
    lastToken = CToken.ERROR_UNCLOSED_COMMENT;
    String text = yytext();
	CToken t = (new CToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    yybegin(nextState);
    return (t);
}
