/* JavaScriptLexer.java is a generated file.  You probably want to
 * edit JavaScriptLexer.lex to make changes.  Use JFlex to generate it.
 * To generate JavaScriptLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main JavaScriptLexer.lex</code><br>
 * You will then have a file called JavaScriptLexer.java
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
 * JavaScriptLexer is a java script lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  JavaScriptLexer shredder = new JavaScriptLexer(System.in);
 *  JavaScriptToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 * The tokens returned should comply with the 
 * <A Href="http://developer.netscape.com/docs/manuals/communicator/jsref/index.htm">
 * Java Script Reference</A>.
 * @see JavaScriptToken
 */ 

%%

%public
%class JavaScriptLexer
%implements Lexer
%function getNextToken
%type Token 

%{
    int lastToken;

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
            JavaScriptLexer shredder = new JavaScriptLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != JavaScriptToken.WHITE_SPACE){
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

HexDigit=([0-9a-fA-F])
Digit=([0-9])
OctalDigit=([0-7])
TetraDigit=([0-3])
NonZeroDigit=([1-9])
Letter=([a-zA-Z_$])
BLANK=([ ])
TAB=([\t])
FF=([\f])
EscChar=([\\])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF}|{EOL})
AnyNonSeparator=([^\t\f\r\n\ \(\)\{\}\[\]\;\,\.\=\>\<\!\~\?\:\+\-\*\/\&\|\^\%\"\'])

OctEscape1=({EscChar}{OctalDigit})
OctEscape2=({EscChar}{OctalDigit}{OctalDigit})
OctEscape3=({EscChar}{TetraDigit}{OctalDigit}{OctalDigit})
OctEscape=({OctEscape1}|{OctEscape2}|{OctEscape3})

UnicodeEscape=({EscChar}[u]{HexDigit}{HexDigit}{HexDigit}{HexDigit})

Escape=({EscChar}([r]|[n]|[b]|[f]|[t]|[\\]|[\']|[\"]))
JavaLetter=({Letter}|{UnicodeEscape})
Identifier=({JavaLetter}({JavaLetter}|{Digit})*)
ErrorIdentifier=({AnyNonSeparator}+)

HTMLComment=("<!--"[^\r\n]*)
Comment=("//"[^\r\n]*)
TradCommentBegin=("/*")
NonTermStars=([^\*\/]*[\*]+[^\*\/])
TermStars=([\*]+[\/])
CommentText=((([^\*]*[\/])|{NonTermStars})*)
CommentEnd=([^\*]*{TermStars})
TradComment=({TradCommentBegin}{CommentText}{CommentEnd})
OpenComment=({TradCommentBegin}{CommentText}([^\*]*)([\*]*))

Sign=([\+]|[\-])
LongSuffix=([l]|[L])
DecimalNum=(([0]|{NonZeroDigit}{Digit}*))
OctalNum=([0]{OctalDigit}*)
HexNum=([0]([x]|[X]){HexDigit}{HexDigit}*)
DecimalLong=({DecimalNum}{LongSuffix})
OctalLong=({OctalNum}{LongSuffix})
HexLong=({HexNum}{LongSuffix})

SignedInt=({Sign}?{Digit}+)
Expo=([e]|[E])
ExponentPart=({Expo}{SignedInt})
FloatSuffix=([f]|[F])
DoubleSuffix=([d]|[D])
FloatDouble1=({Digit}+[\.]{Digit}*{ExponentPart}?)
FloatDouble2=([\.]{Digit}+{ExponentPart}?)
FloatDouble3=({Digit}+{ExponentPart})
FloatDouble4=({Digit}+)
Double1=({FloatDouble1}{DoubleSuffix}?)
Double2=({FloatDouble2}{DoubleSuffix}?)
Double3=({FloatDouble3}{DoubleSuffix}?)
Double4=({FloatDouble4}{DoubleSuffix})
Float1=({FloatDouble1}{FloatSuffix})
Float2=({FloatDouble2}{FloatSuffix})
Float3=({FloatDouble3}{FloatSuffix})
Float4=({FloatDouble4}{FloatSuffix})
Float=({Float1}|{Float2}|{Float3}|{Float4})
Double=({Double1}|{Double2}|{Double3}|{Double4}) 

ZeroFloatDouble1=([0]+[\.][0]*{ExponentPart}?)
ZeroFloatDouble2=([\.][0]+{ExponentPart}?)
ZeroFloatDouble3=([0]+{ExponentPart})
ZeroFloatDouble4=([0]+)
ZeroDouble1=({ZeroFloatDouble1}{DoubleSuffix}?)
ZeroDouble2=({ZeroFloatDouble2}{DoubleSuffix}?)
ZeroDouble3=({ZeroFloatDouble3}{DoubleSuffix}?)
ZeroDouble4=({ZeroFloatDouble4}{DoubleSuffix})
ZeroFloat1=({ZeroFloatDouble1}{FloatSuffix})
ZeroFloat2=({ZeroFloatDouble2}{FloatSuffix})
ZeroFloat3=({ZeroFloatDouble3}{FloatSuffix})
ZeroFloat4=({ZeroFloatDouble4}{FloatSuffix})
ZeroFloat=({ZeroFloat1}|{ZeroFloat2}|{ZeroFloat3}|{ZeroFloat4})
ZeroDouble=({ZeroDouble1}|{ZeroDouble2}|{ZeroDouble3}|{ZeroDouble4})

ErrorFloat=({Digit}({AnyNonSeparator}|[\.])*)

AnyChrChr=([^\'\n\r\\])
UnclosedCharacter=([\']({Escape}|{OctEscape}|{UnicodeEscape}|{AnyChrChr}))
Character=({UnclosedCharacter}[\'])
MalformedUnclosedCharacter=([\']({AnyChrChr}|({EscChar}[^\n\r]))*)
MalformedCharacter=([\'][\']|{MalformedUnclosedCharacter}[\'])

AnyStrChr=([^\"\n\r\\])
UnclosedString=([\"]({Escape}|{OctEscape}|{UnicodeEscape}|{AnyStrChr})*)
String=({UnclosedString}[\"])
MalformedUnclosedString=([\"]({EscChar}|{AnyStrChr})*)
MalformedString=({MalformedUnclosedString}[\"])

%%

<YYINITIAL> "(" { 
    lastToken = JavaScriptToken.SEPARATOR_LPAREN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
    }
<YYINITIAL> ")" {
    lastToken = JavaScriptToken.SEPARATOR_RPAREN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "{" {
    lastToken = JavaScriptToken.SEPARATOR_LBRACE;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "}" {
    lastToken = JavaScriptToken.SEPARATOR_RBRACE;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "[" {
    lastToken = JavaScriptToken.SEPARATOR_LBRACKET;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "]" {
    lastToken = JavaScriptToken.SEPARATOR_RBRACKET;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> ";" {
    lastToken = JavaScriptToken.SEPARATOR_SEMICOLON;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "," {
    lastToken = JavaScriptToken.SEPARATOR_COMMA;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "." {
    lastToken = JavaScriptToken.SEPARATOR_PERIOD;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}

<YYINITIAL> "=" {
    lastToken = JavaScriptToken.OPERATOR_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> ">" {
    lastToken = JavaScriptToken.OPERATOR_GREATER_THAN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "<" {
    lastToken = JavaScriptToken.OPERATOR_LESS_THAN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "!" {
    lastToken = JavaScriptToken.OPERATOR_LOGICAL_NOT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "~" {
    lastToken = JavaScriptToken.OPERATOR_BITWISE_COMPLIMENT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "?" {
    lastToken = JavaScriptToken.OPERATOR_QUESTION;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> ":" {
    lastToken = JavaScriptToken.OPERATOR_COLON;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "+" {
    lastToken = JavaScriptToken.OPERATOR_ADD;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "-" {
    lastToken = JavaScriptToken.OPERATOR_SUBTRACT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "*" {
    lastToken = JavaScriptToken.OPERATOR_MULTIPLY;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "/" {
    lastToken = JavaScriptToken.OPERATOR_DIVIDE;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "&" {
    lastToken = JavaScriptToken.OPERATOR_BITWISE_AND;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "|" {
    lastToken = JavaScriptToken.OPERATOR_BITWISE_OR;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "^" {
    lastToken = JavaScriptToken.OPERATOR_BITWISE_XOR;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}
<YYINITIAL> "%" {
    lastToken = JavaScriptToken.OPERATOR_MOD;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+1, nextState));
    return (t);
}

<YYINITIAL> "==" {
    lastToken = JavaScriptToken.OPERATOR_EQUAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "<=" { 
    lastToken = JavaScriptToken.OPERATOR_LESS_THAN_OR_EQUAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> ">=" { 
    lastToken = JavaScriptToken.OPERATOR_GREATER_THAN_OR_EQUAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "!=" { 
    lastToken = JavaScriptToken.OPERATOR_NOT_EQUAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "||" { 
    lastToken = JavaScriptToken.OPERATOR_LOGICAL_OR;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "&&" { 
    lastToken = JavaScriptToken.OPERATOR_LOGICAL_AND;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "++" { 
    lastToken = JavaScriptToken.OPERATOR_INCREMENT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "--" { 
    lastToken = JavaScriptToken.OPERATOR_DECREMENT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> ">>" { 
    lastToken = JavaScriptToken.OPERATOR_SHIFT_RIGHT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "<<" { 
    lastToken = JavaScriptToken.OPERATOR_SHIFT_LEFT;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> ">>>" { 
    lastToken = JavaScriptToken.OPERATOR_SHIFT_RIGHT_UNSIGNED;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+3, nextState));
    return (t);
}
<YYINITIAL> "+=" { 
    lastToken = JavaScriptToken.OPERATOR_ADD_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "-=" { 
    lastToken = JavaScriptToken.OPERATOR_SUBTRACT_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "*=" { 
    lastToken = JavaScriptToken.OPERATOR_MULTIPLY_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "/=" { 
    lastToken = JavaScriptToken.OPERATOR_DIVIDE_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "&=" { 
    lastToken = JavaScriptToken.OPERATOR_BITWISE_AND_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "|=" { 
    lastToken = JavaScriptToken.OPERATOR_BITWISE_OR_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "^=" { 
    lastToken = JavaScriptToken.OPERATOR_BITWISE_XOR_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "%=" {
    lastToken = JavaScriptToken.OPERATOR_MOD_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+2, nextState));
    return (t);
}
<YYINITIAL> "<<=" {
    lastToken = JavaScriptToken.OPERATOR_SHIFT_LEFT_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+3, nextState));
    return (t);
}
<YYINITIAL> ">>=" {
    lastToken = JavaScriptToken.OPERATOR_SHIFT_RIGHT_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+3, nextState));
    return (t);
}
<YYINITIAL> ">>>=" {
    lastToken = JavaScriptToken.OPERATOR_SHIFT_RIGHT_UNSIGNED_ASSIGN;
    JavaScriptToken t = (new JavaScriptToken(lastToken,yytext(),yyline,yychar,yychar+4, nextState));
    return (t);
}

<YYINITIAL> "abstract" {
    lastToken = JavaScriptToken.RESERVED_WORD_ABSTRACT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "boolean" {
    lastToken = JavaScriptToken.RESERVED_WORD_BOOLEAN;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "break" {
    lastToken = JavaScriptToken.RESERVED_WORD_BREAK;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "byte" {
    lastToken = JavaScriptToken.RESERVED_WORD_BYTE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "case" {
    lastToken = JavaScriptToken.RESERVED_WORD_CASE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "catch" {
    lastToken = JavaScriptToken.RESERVED_WORD_CATCH;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "char" {
    lastToken = JavaScriptToken.RESERVED_WORD_CHAR;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "class" {
    lastToken = JavaScriptToken.RESERVED_WORD_CLASS;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "const" {
    lastToken = JavaScriptToken.RESERVED_WORD_CONST;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "continue" {
    lastToken = JavaScriptToken.RESERVED_WORD_CONTINUE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+8, nextState));
    return (t);
}
<YYINITIAL> "default" {
    lastToken = JavaScriptToken.RESERVED_WORD_DEFAULT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "do" {
    lastToken = JavaScriptToken.RESERVED_WORD_DO;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+2, nextState));
    return (t);
}
<YYINITIAL> "double" {
    lastToken = JavaScriptToken.RESERVED_WORD_DOUBLE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "else" {
    lastToken = JavaScriptToken.RESERVED_WORD_ELSE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "extends" {
    lastToken = JavaScriptToken.RESERVED_WORD_EXTENDS;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "final" {
    lastToken = JavaScriptToken.RESERVED_WORD_FINAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "finally" {
    lastToken = JavaScriptToken.RESERVED_WORD_FINALLY;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "float" {
    lastToken = JavaScriptToken.RESERVED_WORD_FLOAT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "for" {
    lastToken = JavaScriptToken.RESERVED_WORD_FOR;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+3, nextState));
    return (t);
}
<YYINITIAL> "function" {
    lastToken = JavaScriptToken.RESERVED_WORD_FUNCTION;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+8, nextState));
    return (t);
}
<YYINITIAL> "goto" {
    lastToken = JavaScriptToken.RESERVED_WORD_GOTO;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "if" {
    lastToken = JavaScriptToken.RESERVED_WORD_IF;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+2, nextState));
    return (t);
}
<YYINITIAL> "implements" {
    lastToken = JavaScriptToken.RESERVED_WORD_IMPLEMENTS;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+10, nextState));
    return (t);
}
<YYINITIAL> "import" {
    lastToken = JavaScriptToken.RESERVED_WORD_IMPORT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "in" {
    lastToken = JavaScriptToken.RESERVED_WORD_IN;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+2, nextState));
    return (t);
}
<YYINITIAL> "instanceof" {
    lastToken = JavaScriptToken.RESERVED_WORD_INSTANCEOF;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+10, nextState));
    return (t);
}
<YYINITIAL> "int" {
    lastToken = JavaScriptToken.RESERVED_WORD_INT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+3, nextState));
    return (t);
}
<YYINITIAL> "interface" {
    lastToken = JavaScriptToken.RESERVED_WORD_INTERFACE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+9, nextState));
    return (t);
}
<YYINITIAL> "long" {
    lastToken = JavaScriptToken.RESERVED_WORD_LONG;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "native" {
    lastToken = JavaScriptToken.RESERVED_WORD_NATIVE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "new" {
    lastToken = JavaScriptToken.RESERVED_WORD_NEW;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+3, nextState));
    return (t);
}
<YYINITIAL> "package" {
    lastToken = JavaScriptToken.RESERVED_WORD_PACKAGE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "private" {
    lastToken = JavaScriptToken.RESERVED_WORD_PRIVATE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+7, nextState));
    return (t);
}
<YYINITIAL> "protected" {
    lastToken = JavaScriptToken.RESERVED_WORD_PROTECTED;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+9, nextState));
    return (t);
}
<YYINITIAL> "public" {
    lastToken = JavaScriptToken.RESERVED_WORD_PUBLIC;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "return" {
    lastToken = JavaScriptToken.RESERVED_WORD_RETURN;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "short" {
    lastToken = JavaScriptToken.RESERVED_WORD_SHORT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "static" {
    lastToken = JavaScriptToken.RESERVED_WORD_STATIC;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "super" {
    lastToken = JavaScriptToken.RESERVED_WORD_SUPER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "switch" {
    lastToken = JavaScriptToken.RESERVED_WORD_SWITCH;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "synchronized" {
    lastToken = JavaScriptToken.RESERVED_WORD_SYNCHRONIZED;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+12, nextState));
    return (t);
}
<YYINITIAL> "this" {
    lastToken = JavaScriptToken.RESERVED_WORD_THIS;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "throw" {
    lastToken = JavaScriptToken.RESERVED_WORD_THROW;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}
<YYINITIAL> "throws" {
    lastToken = JavaScriptToken.RESERVED_WORD_THROWS;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+6, nextState));
    return (t);
}
<YYINITIAL> "while" { 
    lastToken = JavaScriptToken.RESERVED_WORD_TRANSIENT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+9, nextState));
    return (t);
}
<YYINITIAL> "try" {
    lastToken = JavaScriptToken.RESERVED_WORD_TRY;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+3, nextState));
    return (t);
}
<YYINITIAL> "var" {
    lastToken = JavaScriptToken.RESERVED_WORD_VAR;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+3, nextState));
    return (t);
}
<YYINITIAL> "void" {
    lastToken = JavaScriptToken.RESERVED_WORD_VOID;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "with" { 
    lastToken = JavaScriptToken.RESERVED_WORD_WITH;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}


<YYINITIAL> "null" { 
    lastToken = JavaScriptToken.LITERAL_NULL;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}

<YYINITIAL> "true" { 
    lastToken = JavaScriptToken.LITERAL_BOOLEAN;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+4, nextState));
    return (t);
}
<YYINITIAL> "false" { 
    lastToken = JavaScriptToken.LITERAL_BOOLEAN;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar+5, nextState));
    return (t);
}

<YYINITIAL> {Identifier} { 
    lastToken = JavaScriptToken.IDENTIFIER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}

<YYINITIAL> {DecimalNum} {
    /* At this point, the number we found could still be too large.
     * If it is too large, we need to return an error.
     * Java has methods built in that will decode from a string
     * and throw an exception the number is too large 
     */
    try {
        /* bigger negatives are allowed than positives.  Thus
         * we have to be careful to make sure a neg sign is preserved
         */
        if (lastToken == JavaScriptToken.OPERATOR_SUBTRACT){
            Integer.decode('-' + yytext());
        } else {
            Integer.decode(yytext());
        }
        lastToken = JavaScriptToken.LITERAL_INTEGER_DECIMAL;
    } catch (NumberFormatException e){
        lastToken = JavaScriptToken.ERROR_INTEGER_DECIMIAL_SIZE;
    }
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {OctalNum} {
    /* An Octal number cannot be too big.  After removing 
     * initial zeros, It can have 11 digits, the first
     * of which must be 3 or less.
     */
    lastToken = JavaScriptToken.LITERAL_INTEGER_OCTAL;
    int i;
    int length =yytext().length();
    for (i=1 ; i<length-11; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){ 
            lastToken = JavaScriptToken.ERROR_INTEGER_OCTAL_SIZE;
        }
    }
    if (length - i > 11){
        lastToken = JavaScriptToken.ERROR_INTEGER_OCTAL_SIZE;
    } else if (length - i == 11){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' && yytext().charAt(i) != '1' && 
        yytext().charAt(i) != '2' && yytext().charAt(i) != '3'){
            lastToken = JavaScriptToken.ERROR_INTEGER_OCTAL_SIZE;
        }
    }
    // Otherwise, it should be OK   
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {HexNum} {
    /* A Hex number cannot be too big.  After removing 
     * initial zeros, It can have 8 digits
     */
    lastToken = JavaScriptToken.LITERAL_INTEGER_HEXIDECIMAL;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-8; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){ 
            lastToken = JavaScriptToken.ERROR_INTEGER_HEXIDECIMAL_SIZE;
        }
    }
    if (length - i > 8){
        lastToken = JavaScriptToken.ERROR_INTEGER_HEXIDECIMAL_SIZE;
    }
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {DecimalLong} { 
    try {
        if (lastToken == JavaScriptToken.OPERATOR_SUBTRACT){
            Long.decode('-' + yytext().substring(0,yytext().length()-1));
        } else {
            Long.decode(yytext().substring(0,yytext().length()-1));
        }
        lastToken = JavaScriptToken.LITERAL_LONG_DECIMAL;
    } catch (NumberFormatException e){  
        lastToken = JavaScriptToken.ERROR_LONG_DECIMIAL_SIZE;
    }
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {OctalLong} {
    /* An Octal number cannot be too big.  After removing 
     * initial zeros, It can have 23 digits, the first
     * of which must be 1 or less.  The last will be the L or l
     * at the end.
     */
    lastToken = JavaScriptToken.LITERAL_LONG_OCTAL;
    int i;
    int length =yytext().length();
    for (i=1 ; i<length-23; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){ 
            lastToken = JavaScriptToken.ERROR_LONG_OCTAL_SIZE;
        }
    }
    if (length - i > 23){
        lastToken = JavaScriptToken.ERROR_LONG_OCTAL_SIZE;
    } else if (length - i == 23){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (yytext().charAt(i) != '0' && yytext().charAt(i) != '1'){
            lastToken = JavaScriptToken.ERROR_LONG_OCTAL_SIZE;
        }
    }
    // Otherwise, it should be OK   
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {HexLong} {
    /* A Hex long cannot be too big.  After removing 
     * initial zeros, It can have 17 digits, the last of which is
     * the L or l
     */
    lastToken = JavaScriptToken.LITERAL_LONG_HEXIDECIMAL;
    int i;
    int length =yytext().length();
    for (i=2 ; i<length-17; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){ 
            lastToken = JavaScriptToken.ERROR_LONG_HEXIDECIMAL_SIZE;
        }
    }
    if (length - i > 17){
        lastToken = JavaScriptToken.ERROR_LONG_HEXIDECIMAL_SIZE;
    }
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {ZeroFloat} {
    /* catch the case of a zero in parsing, so that we do not incorrectly
     * give an error that a number was rounded to zero
     */
    lastToken = JavaScriptToken.LITERAL_FLOATING_POINT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {ZeroDouble} {
    lastToken = JavaScriptToken.LITERAL_DOUBLE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {Float} {
    /* Sun s java has a few bugs here.  Their MAX_FLOAT and MIN_FLOAT do not
     * quite match the spec.  Its not far off, so we will deal.  If they fix
     * then we are fixed.  So all good.
     */ 
    Float f;
    try {
        f = Float.valueOf(yytext());
        if (f.isInfinite() || f.compareTo(new Float(0f)) == 0){
            lastToken = JavaScriptToken.ERROR_FLOAT_SIZE;
        } else {
            lastToken = JavaScriptToken.LITERAL_FLOATING_POINT;
        }
    } catch (NumberFormatException e){
        lastToken = JavaScriptToken.ERROR_FLOAT_SIZE;
    }
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {Double} {
    Double d;
    try {
        d = Double.valueOf(yytext());
        if (d.isInfinite() || d.compareTo(new Double(0d)) == 0){
            lastToken = JavaScriptToken.ERROR_DOUBLE_SIZE;
        } else {
            lastToken = JavaScriptToken.LITERAL_DOUBLE;
        }
    } catch (NumberFormatException e){
        lastToken = JavaScriptToken.ERROR_DOUBLE_SIZE;
    } 
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}


<YYINITIAL> {Character} { 
    lastToken = JavaScriptToken.LITERAL_CHARACTER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {String} { 
    lastToken = JavaScriptToken.LITERAL_STRING;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}

<YYINITIAL> ({WhiteSpace}+) { 
    lastToken = JavaScriptToken.WHITE_SPACE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}

<YYINITIAL> {Comment} { 
    lastToken = JavaScriptToken.COMMENT_END_OF_LINE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {HTMLComment} { 
    lastToken = JavaScriptToken.COMMENT_END_OF_LINE;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {TradComment} {
    lastToken = JavaScriptToken.COMMENT_TRADITIONAL;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}

<YYINITIAL> {UnclosedString} { 
    /* most of these errors have to be caught down near the end of the file.
     * This way, previous expressions of the same length have precedence.
     * This is really useful for catching anything bad by just allowing it 
     * to slip through the cracks. 
     */ 
    lastToken = JavaScriptToken.ERROR_UNCLOSED_STRING;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {MalformedUnclosedString} { 
    lastToken = JavaScriptToken.ERROR_MALFORMED_UNCLOSED_STRING;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {MalformedString} { 
    lastToken = JavaScriptToken.ERROR_MALFORMED_STRING;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {UnclosedCharacter} { 
    lastToken = JavaScriptToken.ERROR_UNCLOSED_CHARACTER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {MalformedUnclosedCharacter} { 
    lastToken = JavaScriptToken.ERROR_MALFORMED_UNCLOSED_CHARACTER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {MalformedCharacter} { 
    lastToken = JavaScriptToken.ERROR_MALFORMED_CHARACTER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {ErrorFloat} { 
    lastToken = JavaScriptToken.ERROR_FLOAT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {ErrorIdentifier} { 
    lastToken = JavaScriptToken.ERROR_IDENTIFIER;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
<YYINITIAL> {OpenComment} { 
    lastToken = JavaScriptToken.ERROR_UNCLOSED_COMMENT;
    JavaScriptToken t = (new JavaScriptToken(lastToken, yytext(), yyline, yychar, yychar + yytext().length(), nextState));
    return (t);
}
