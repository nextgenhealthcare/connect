/* JavaLexer.java is a generated file.  You probably want to
 * edit JavaLexer.lex to make changes.  Use JFlex to generate it.
 * To generate JavaLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main JavaLexer.lex</code><br>
 * You will then have a file called JavaLexer.java
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
 * JavaLexer is a java lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  JavaLexer shredder = new JavaLexer(System.in);
 *  JavaToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 * The tokens returned should comply with the 
 * <A Href=http://java.sun.com/docs/books/jls/html/>Java 
 * Language Specification</A>.
 * @see JavaToken
 */ 

%%

%public
%class JavaLexer
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
            JavaLexer shredder = new JavaLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != JavaToken.WHITE_SPACE){
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
    lastToken = JavaToken.SEPARATOR_LPAREN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
    }
<YYINITIAL> ")" {
    lastToken = JavaToken.SEPARATOR_RPAREN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "{" {
    lastToken = JavaToken.SEPARATOR_LBRACE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "}" {
    lastToken = JavaToken.SEPARATOR_RBRACE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "[" {
    lastToken = JavaToken.SEPARATOR_LBRACKET;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "]" {
    lastToken = JavaToken.SEPARATOR_RBRACKET;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ";" {
    lastToken = JavaToken.SEPARATOR_SEMICOLON;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "," {
    lastToken = JavaToken.SEPARATOR_COMMA;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "." {
    lastToken = JavaToken.SEPARATOR_PERIOD;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> "=" {
    lastToken = JavaToken.OPERATOR_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">" {
    lastToken = JavaToken.OPERATOR_GREATER_THAN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "<" {
    lastToken = JavaToken.OPERATOR_LESS_THAN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "!" {
    lastToken = JavaToken.OPERATOR_LOGICAL_NOT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "~" {
    lastToken = JavaToken.OPERATOR_BITWISE_COMPLIMENT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "?" {
    lastToken = JavaToken.OPERATOR_QUESTION;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ":" {
    lastToken = JavaToken.OPERATOR_COLON;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "+" {
    lastToken = JavaToken.OPERATOR_ADD;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "-" {
    lastToken = JavaToken.OPERATOR_SUBTRACT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "*" {
    lastToken = JavaToken.OPERATOR_MULTIPLY;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "/" {
    lastToken = JavaToken.OPERATOR_DIVIDE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "&" {
    lastToken = JavaToken.OPERATOR_BITWISE_AND;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "|" {
    lastToken = JavaToken.OPERATOR_BITWISE_OR;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "^" {
    lastToken = JavaToken.OPERATOR_BITWISE_XOR;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "%" {
    lastToken = JavaToken.OPERATOR_MOD;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> "==" {
    lastToken = JavaToken.OPERATOR_EQUAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "<=" { 
    lastToken = JavaToken.OPERATOR_LESS_THAN_OR_EQUAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">=" { 
    lastToken = JavaToken.OPERATOR_GREATER_THAN_OR_EQUAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "!=" { 
    lastToken = JavaToken.OPERATOR_NOT_EQUAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "||" { 
    lastToken = JavaToken.OPERATOR_LOGICAL_OR;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "&&" { 
    lastToken = JavaToken.OPERATOR_LOGICAL_AND;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "++" { 
    lastToken = JavaToken.OPERATOR_INCREMENT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "--" { 
    lastToken = JavaToken.OPERATOR_DECREMENT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">>" { 
    lastToken = JavaToken.OPERATOR_SHIFT_RIGHT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "<<" { 
    lastToken = JavaToken.OPERATOR_SHIFT_LEFT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">>>" { 
    lastToken = JavaToken.OPERATOR_SHIFT_RIGHT_UNSIGNED;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "+=" { 
    lastToken = JavaToken.OPERATOR_ADD_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "-=" { 
    lastToken = JavaToken.OPERATOR_SUBTRACT_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "*=" { 
    lastToken = JavaToken.OPERATOR_MULTIPLY_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "/=" { 
    lastToken = JavaToken.OPERATOR_DIVIDE_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "&=" { 
    lastToken = JavaToken.OPERATOR_BITWISE_AND_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "|=" { 
    lastToken = JavaToken.OPERATOR_BITWISE_OR_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "^=" { 
    lastToken = JavaToken.OPERATOR_BITWISE_XOR_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "%=" { 
    lastToken = JavaToken.OPERATOR_MOD_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "<<=" { 
    lastToken = JavaToken.OPERATOR_SHIFT_LEFT_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">>=" { 
    lastToken = JavaToken.OPERATOR_SHIFT_RIGHT_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> ">>>=" { 
    lastToken = JavaToken.OPERATOR_SHIFT_RIGHT_UNSIGNED_ASSIGN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> "abstract" { 
    lastToken = JavaToken.RESERVED_WORD_ABSTRACT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "boolean" { 
    lastToken = JavaToken.RESERVED_WORD_BOOLEAN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "break" { 
    lastToken = JavaToken.RESERVED_WORD_BREAK;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "byte" { 
    lastToken = JavaToken.RESERVED_WORD_BYTE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "case" { 
    lastToken = JavaToken.RESERVED_WORD_CASE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "catch" { 
    lastToken = JavaToken.RESERVED_WORD_CATCH;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "char" { 
    lastToken = JavaToken.RESERVED_WORD_CHAR;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "class" { 
    lastToken = JavaToken.RESERVED_WORD_CLASS;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "const" { 
    lastToken = JavaToken.RESERVED_WORD_CONST;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "continue" { 
    lastToken = JavaToken.RESERVED_WORD_CONTINUE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "default" { 
    lastToken = JavaToken.RESERVED_WORD_DEFAULT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "do" { 
    lastToken = JavaToken.RESERVED_WORD_DO;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "double" { 
    lastToken = JavaToken.RESERVED_WORD_DOUBLE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "else" { 
    lastToken = JavaToken.RESERVED_WORD_ELSE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "extends" { 
    lastToken = JavaToken.RESERVED_WORD_EXTENDS;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "final" { 
    lastToken = JavaToken.RESERVED_WORD_FINAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "finally" { 
    lastToken = JavaToken.RESERVED_WORD_FINALLY;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "float" { 
    lastToken = JavaToken.RESERVED_WORD_FLOAT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "for" {
    lastToken = JavaToken.RESERVED_WORD_FOR;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "goto" { 
    lastToken = JavaToken.RESERVED_WORD_GOTO;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "if" { 
    lastToken = JavaToken.RESERVED_WORD_IF;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "implements" { 
    lastToken = JavaToken.RESERVED_WORD_IMPLEMENTS;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "import" { 
    lastToken = JavaToken.RESERVED_WORD_IMPORT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "instanceof" { 
    lastToken = JavaToken.RESERVED_WORD_INSTANCEOF;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "int" { 
    lastToken = JavaToken.RESERVED_WORD_INT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "interface" { 
    lastToken = JavaToken.RESERVED_WORD_INTERFACE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "long" { 
    lastToken = JavaToken.RESERVED_WORD_LONG;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "native" { 
    lastToken = JavaToken.RESERVED_WORD_NATIVE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "new" { 
    lastToken = JavaToken.RESERVED_WORD_NEW;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "package" { 
    lastToken = JavaToken.RESERVED_WORD_PACKAGE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "private" { 
    lastToken = JavaToken.RESERVED_WORD_PRIVATE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "protected" { 
    lastToken = JavaToken.RESERVED_WORD_PROTECTED;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "public" { 
    lastToken = JavaToken.RESERVED_WORD_PUBLIC;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "return" { 
    lastToken = JavaToken.RESERVED_WORD_RETURN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "short" { 
    lastToken = JavaToken.RESERVED_WORD_SHORT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "static" {
    lastToken = JavaToken.RESERVED_WORD_STATIC;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "strictfp" {
    lastToken = JavaToken.RESERVED_WORD_STRICTFP;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "super" { 
    lastToken = JavaToken.RESERVED_WORD_SUPER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "switch" { 
    lastToken = JavaToken.RESERVED_WORD_SWITCH;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "synchronized" { 
    lastToken = JavaToken.RESERVED_WORD_SYNCHRONIZED;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "this" { 
    lastToken = JavaToken.RESERVED_WORD_THIS;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "throw" { 
    lastToken = JavaToken.RESERVED_WORD_THROW;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "throws" { 
    lastToken = JavaToken.RESERVED_WORD_THROWS;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "transient" { 
    lastToken = JavaToken.RESERVED_WORD_TRANSIENT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "try" { 
    lastToken = JavaToken.RESERVED_WORD_TRY;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "void" { 
    lastToken = JavaToken.RESERVED_WORD_VOID;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "volatile" { 
    lastToken = JavaToken.RESERVED_WORD_VOLATILE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "while" { 
    lastToken = JavaToken.RESERVED_WORD_WHILE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> "null" { 
    lastToken = JavaToken.LITERAL_NULL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> "true" { 
    lastToken = JavaToken.LITERAL_BOOLEAN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> "false" { 
    lastToken = JavaToken.LITERAL_BOOLEAN;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> {Identifier} { 
    lastToken = JavaToken.IDENTIFIER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> {DecimalNum} {
    /* At this point, the number we found could still be too large.
     * If it is too large, we need to return an error.
     * Java has methods built in that will decode from a string
     * and throw an exception the number is too large 
     */     
    String text = yytext();
    try {
        /* bigger negatives are allowed than positives.  Thus
         * we have to be careful to make sure a neg sign is preserved
         */
        if (lastToken == JavaToken.OPERATOR_SUBTRACT){
            Integer.decode('-' + text);
        } else {
            Integer.decode(text);
        }
        lastToken = JavaToken.LITERAL_INTEGER_DECIMAL;
    } catch (NumberFormatException e){
        lastToken = JavaToken.ERROR_INTEGER_DECIMIAL_SIZE;
    }
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {OctalNum} {
    /* An Octal number cannot be too big.  After removing 
     * initial zeros, It can have 11 digits, the first
     * of which must be 3 or less.
     */
    lastToken = JavaToken.LITERAL_INTEGER_OCTAL;
    int i;     
    String text = yytext();
    int length = text.length();
    for (i=1 ; i<length-11; i++){
        //check for initial zeros
        if (yytext().charAt(i) != '0'){ 
            lastToken = JavaToken.ERROR_INTEGER_OCTAL_SIZE;
        }
    }
    if (length - i > 11){
        lastToken = JavaToken.ERROR_INTEGER_OCTAL_SIZE;
    } else if (length - i == 11){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (text.charAt(i) != '0' && text.charAt(i) != '1' && 
        text.charAt(i) != '2' && text.charAt(i) != '3'){
            lastToken = JavaToken.ERROR_INTEGER_OCTAL_SIZE;
        }
    }
    // Otherwise, it should be OK  
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {HexNum} {
    /* A Hex number cannot be too big.  After removing 
     * initial zeros, It can have 8 digits
     */
    lastToken = JavaToken.LITERAL_INTEGER_HEXIDECIMAL;
    int i;    
    String text = yytext();
    int length = text.length();
    for (i=2 ; i<length-8; i++){
        //check for initial zeros
        if (text.charAt(i) != '0'){ 
            lastToken = JavaToken.ERROR_INTEGER_HEXIDECIMAL_SIZE;
        }
    }
    if (length - i > 8){
        lastToken = JavaToken.ERROR_INTEGER_HEXIDECIMAL_SIZE;
    }
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {DecimalLong} { 
    String text = yytext();
    try {
        if (lastToken == JavaToken.OPERATOR_SUBTRACT){
            Long.decode('-' + text.substring(0,text.length()-1));
        } else {
            Long.decode(text.substring(0,text.length()-1));
        }
        lastToken = JavaToken.LITERAL_LONG_DECIMAL;
    } catch (NumberFormatException e){  
        lastToken = JavaToken.ERROR_LONG_DECIMIAL_SIZE;
    }
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {OctalLong} {
    /* An Octal number cannot be too big.  After removing 
     * initial zeros, It can have 23 digits, the first
     * of which must be 1 or less.  The last will be the L or l
     * at the end.
     */
    lastToken = JavaToken.LITERAL_LONG_OCTAL;
    int i; 
    String text = yytext();
    int length = text.length();
    for (i=1 ; i<length-23; i++){
        //check for initial zeros
        if (text.charAt(i) != '0'){ 
            lastToken = JavaToken.ERROR_LONG_OCTAL_SIZE;
        }
    }
    if (length - i > 23){
        lastToken = JavaToken.ERROR_LONG_OCTAL_SIZE;
    } else if (length - i == 23){
        // if the rest of the number is as big as possible
        // the first digit can only be 3 or less
        if (text.charAt(i) != '0' && text.charAt(i) != '1'){
            lastToken = JavaToken.ERROR_LONG_OCTAL_SIZE;
        }
    }
    // Otherwise, it should be OK  
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {HexLong} {
    /* A Hex long cannot be too big.  After removing 
     * initial zeros, It can have 17 digits, the last of which is
     * the L or l
     */
    lastToken = JavaToken.LITERAL_LONG_HEXIDECIMAL;
    int i;
    String text = yytext();
    int length = text.length();
    for (i=2 ; i<length-17; i++){
        //check for initial zeros
        if (text.charAt(i) != '0'){ 
            lastToken = JavaToken.ERROR_LONG_HEXIDECIMAL_SIZE;
        }
    }
    if (length - i > 17){
        lastToken = JavaToken.ERROR_LONG_HEXIDECIMAL_SIZE;
    }
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {ZeroFloat} {
    /* catch the case of a zero in parsing, so that we do not incorrectly
     * give an error that a number was rounded to zero
     */
    lastToken = JavaToken.LITERAL_FLOATING_POINT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {ZeroDouble} {
    lastToken = JavaToken.LITERAL_DOUBLE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
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
            lastToken = JavaToken.ERROR_FLOAT_SIZE;
        } else {
            lastToken = JavaToken.LITERAL_FLOATING_POINT;
        }
    } catch (NumberFormatException e){
        lastToken = JavaToken.ERROR_FLOAT_SIZE;
    }
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {Double} {
    Double d;
    try {
        d = Double.valueOf(yytext());
        if (d.isInfinite() || d.compareTo(new Double(0d)) == 0){
            lastToken = JavaToken.ERROR_DOUBLE_SIZE;
        } else {
            lastToken = JavaToken.LITERAL_DOUBLE;
        }
    } catch (NumberFormatException e){
        lastToken = JavaToken.ERROR_DOUBLE_SIZE;
    } 
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}


<YYINITIAL> {Character} { 
    lastToken = JavaToken.LITERAL_CHARACTER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {String} { 
    lastToken = JavaToken.LITERAL_STRING;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> ({WhiteSpace}+) { 
    lastToken = JavaToken.WHITE_SPACE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> {Comment} { 
    lastToken = JavaToken.COMMENT_END_OF_LINE;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {DocComment} {
    lastToken = JavaToken.COMMENT_DOCUMENTATION;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {TradComment} {
    lastToken = JavaToken.COMMENT_TRADITIONAL;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}

<YYINITIAL> {UnclosedString} { 
    /* most of these errors have to be caught down near the end of the file.
     * This way, previous expressions of the same length have precedence.
     * This is really useful for catching anything bad by just allowing it 
     * to slip through the cracks. 
     */ 
    lastToken = JavaToken.ERROR_UNCLOSED_STRING;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {MalformedUnclosedString} { 
    lastToken = JavaToken.ERROR_MALFORMED_UNCLOSED_STRING;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {MalformedString} { 
    lastToken = JavaToken.ERROR_MALFORMED_STRING;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {UnclosedCharacter} { 
    lastToken = JavaToken.ERROR_UNCLOSED_CHARACTER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {MalformedUnclosedCharacter} { 
    lastToken = JavaToken.ERROR_MALFORMED_UNCLOSED_CHARACTER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {MalformedCharacter} { 
    lastToken = JavaToken.ERROR_MALFORMED_CHARACTER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {ErrorFloat} { 
    lastToken = JavaToken.ERROR_FLOAT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {ErrorIdentifier} { 
    lastToken = JavaToken.ERROR_IDENTIFIER;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
<YYINITIAL> {OpenComment} { 
    lastToken = JavaToken.ERROR_UNCLOSED_COMMENT;
    String text = yytext();
    JavaToken t = (new JavaToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
    return (t);
}
