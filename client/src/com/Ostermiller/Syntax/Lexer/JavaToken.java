/*
 * This file is part of a syntax highlighting package
 * Copyright (C) 1999, 2000  Stephen Ostermiller
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

/** 
 * A JavaToken is a token that is returned by a lexer that is lexing a java
 * source file.  It has several attributes describing the token:
 * The type of token, the text of the token, the line number on which it
 * occurred, the number of characters into the input at which it started, and
 * similarly, the number of characters into the input at which it ended. <br>
 * The tokens should comply with the 
 * <A Href=http://java.sun.com/docs/books/jls/html/>Java 
 * Language Specification</A>.
 */ 
public class JavaToken extends Token {
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_ABSTRACT = 0x101;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_BOOLEAN = 0x102;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_BREAK = 0x103;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_BYTE = 0x104;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CASE = 0x105;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CATCH = 0x106;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CHAR = 0x107;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CLASS = 0x108;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CONST = 0x109;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_CONTINUE = 0x10A;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_DEFAULT = 0x10B;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_DO = 0x10C;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_DOUBLE = 0x10D;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_ELSE = 0x10E;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_EXTENDS = 0x10F;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_FINAL = 0x110;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_FINALLY = 0x111;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_FLOAT = 0x112;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_FOR = 0x113;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_GOTO = 0x114;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_IF = 0x115;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_IMPLEMENTS = 0x116;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_IMPORT = 0x117;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_INSTANCEOF = 0x118;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_INT = 0x119;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_INTERFACE = 0x11A;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_LONG = 0x11B;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_NATIVE = 0x11C;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_NEW = 0x11D;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_PACKAGE = 0x11E;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_PRIVATE = 0x11F;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_PROTECTED = 0x120;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_PUBLIC = 0x121;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_RETURN = 0x122;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_SHORT = 0x123;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_STATIC = 0x124;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_STRICTFP = 0x130;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_SUPER = 0x125;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_SWITCH = 0x126;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_SYNCHRONIZED = 0x127;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_THIS = 0x128;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_THROW = 0x129;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_THROWS = 0x12A;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_TRANSIENT = 0x12B;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_TRY = 0x12C;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_VOID = 0x12D;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_VOLATILE = 0x12E;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int RESERVED_WORD_WHILE = 0x12F;
  
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int IDENTIFIER = 0x200;
  
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_BOOLEAN = 0x300;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_INTEGER_DECIMAL = 0x310;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_INTEGER_OCTAL = 0x311;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_INTEGER_HEXIDECIMAL = 0x312;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_LONG_DECIMAL = 0x320;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_LONG_OCTAL = 0x321;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_LONG_HEXIDECIMAL = 0x322;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_FLOATING_POINT = 0x330;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_DOUBLE = 0x340;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_CHARACTER = 0x350;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_STRING = 0x360;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int LITERAL_NULL = 0x370;

  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_LPAREN = 0x400;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_RPAREN = 0x401;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_LBRACE = 0x410;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_RBRACE = 0x411;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_LBRACKET = 0x420;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_RBRACKET = 0x421;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_SEMICOLON = 0x430;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_COMMA = 0x440;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int SEPARATOR_PERIOD = 0x450;

  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_GREATER_THAN = 0x500;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_LESS_THAN = 0x501;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_LESS_THAN_OR_EQUAL = 0x502;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_GREATER_THAN_OR_EQUAL = 0x503;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_EQUAL = 0x504;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_NOT_EQUAL = 0x505;  
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_LOGICAL_NOT = 0x510;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_LOGICAL_AND = 0x511;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_LOGICAL_OR = 0x512;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_ADD = 0x520;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SUBTRACT = 0x521;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_MULTIPLY = 0x522;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_DIVIDE = 0x523;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_MOD = 0x524;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_COMPLIMENT = 0x530;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_AND = 0x531;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_OR = 0x532;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_XOR = 0x533;  
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_LEFT = 0x540;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_RIGHT = 0x541;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_RIGHT_UNSIGNED = 0x542;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_ASSIGN = 0x550;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_ADD_ASSIGN = 0x560;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SUBTRACT_ASSIGN = 0x561;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_MULTIPLY_ASSIGN = 0x562;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_DIVIDE_ASSIGN = 0x563;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_MOD_ASSIGN = 0x564;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_AND_ASSIGN = 0x571;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_OR_ASSIGN = 0x572;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_BITWISE_XOR_ASSIGN = 0x573;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_LEFT_ASSIGN = 0x580;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_RIGHT_ASSIGN = 0x581;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_SHIFT_RIGHT_UNSIGNED_ASSIGN = 0x582;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_INCREMENT = 0x590;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_DECREMENT = 0x591;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_QUESTION = 0x5A0;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int OPERATOR_COLON = 0x5A1;
  
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int COMMENT_TRADITIONAL = 0xD00;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int COMMENT_END_OF_LINE = 0xD10;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int COMMENT_DOCUMENTATION = 0xD20;

  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int WHITE_SPACE = 0xE00;

  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_IDENTIFIER = 0xF00;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_UNCLOSED_STRING = 0xF10;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_MALFORMED_STRING = 0xF11;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_MALFORMED_UNCLOSED_STRING = 0xF12;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_UNCLOSED_CHARACTER = 0xF20;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_MALFORMED_CHARACTER = 0xF21;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_MALFORMED_UNCLOSED_CHARACTER = 0xF22;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_INTEGER_DECIMIAL_SIZE = 0xF30;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_INTEGER_OCTAL_SIZE = 0xF31;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_INTEGER_HEXIDECIMAL_SIZE = 0xF32;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_LONG_DECIMIAL_SIZE = 0xF33;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_LONG_OCTAL_SIZE = 0xF34;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_LONG_HEXIDECIMAL_SIZE = 0xF35;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_FLOAT_SIZE = 0xF36;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_DOUBLE_SIZE = 0xF37;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */  
  public final static int ERROR_FLOAT = 0xF38;
  /**
   * <A Href=http://java.sun.com/docs/books/jls/html/>The Java 
   * Language Specification</A> explains this token
   */
  public final static int ERROR_UNCLOSED_COMMENT = 0xF40;
  
  private int ID;
  private String contents;
  private int lineNumber;
  private int charBegin;
  private int charEnd;
  private int state;

  /**
   * Create a new token.
   * The constructor is typically called by the lexer
   *
   * @param ID the id number of the token
   * @param contents A string representing the text of the token
   * @param lineNumber the line number of the input on which this token started
   * @param charBegin the offset into the input in characters at which this token started
   * @param charEnd the offset into the input in characters at which this token ended
   */
  public JavaToken(int ID, String contents, int lineNumber, int charBegin, int charEnd){
    this (ID, contents, lineNumber, charBegin, charEnd, Token.UNDEFINED_STATE);
  }

  /**
   * Create a new token.
   * The constructor is typically called by the lexer
   *
   * @param ID the id number of the token
   * @param contents A string representing the text of the token
   * @param lineNumber the line number of the input on which this token started
   * @param charBegin the offset into the input in characters at which this token started
   * @param charEnd the offset into the input in characters at which this token ended
   * @param state the state the tokenizer is in after returning this token.
   */
  public JavaToken(int ID, String contents, int lineNumber, int charBegin, int charEnd, int state){
	this.ID = ID;
	this.contents = new String(contents);
	this.lineNumber = lineNumber;
	this.charBegin = charBegin;
	this.charEnd = charEnd;
    this.state = state;
  }

  /**
     * Get an integer representing the state the tokenizer is in after
     * returning this token.
     * Those who are interested in incremental tokenizing for performance
     * reasons will want to use this method to figure out where the tokenizer
     * may be restarted.  The tokenizer starts in Token.INITIAL_STATE, so
     * any time that it reports that it has returned to this state, the
     * tokenizer may be restarted from there.
     */
  public int getState(){
    return state;
  }

  /** 
   * get the ID number of this token
   * 
   * @return the id number of the token
   */
  public int getID(){
  	return ID;
  }

  /** 
   * get the contents of this token
   * 
   * @return A string representing the text of the token
   */
  public String getContents(){
  	return (new String(contents));
  }

  /** 
   * get the line number of the input on which this token started
   * 
   * @return the line number of the input on which this token started
   */
  public int getLineNumber(){
  	return lineNumber;
  }

  /** 
   * get the offset into the input in characters at which this token started
   *
   * @return the offset into the input in characters at which this token started
   */
  public int getCharBegin(){
  	return charBegin;
  }

  /** 
   * get the offset into the input in characters at which this token ended
   *
   * @return the offset into the input in characters at which this token ended
   */
  public int getCharEnd(){
 	return charEnd;
  }

  /** 
   * Checks this token to see if it is a reserved word.
   * Reserved words are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java 
   * Language Specification</A>.
   *
   * @return true if this token is a reserved word, false otherwise
   */
  public boolean isReservedWord(){
  	return((ID >> 8) == 0x1);
  }

  /** 
   * Checks this token to see if it is an identifier.
   * Identifiers are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java 
   * Language Specification</A>.
   *
   * @return true if this token is an identifier, false otherwise
   */
  public boolean isIdentifier(){
  	return((ID >> 8) == 0x2);
  }

  /** 
   * Checks this token to see if it is a literal.
   * Literals are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java 
   * Language Specification</A>.
   *
   * @return true if this token is a literal, false otherwise
   */
  public boolean isLiteral(){
  	return((ID >> 8) == 0x3);
  }
  
  /** 
   * Checks this token to see if it is a Separator.
   * Separators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java 
   * Language Specification</A>.
   *
   * @return true if this token is a Separator, false otherwise
   */
  public boolean isSeparator(){
  	return((ID >> 8) == 0x4);
  }

  /** 
   * Checks this token to see if it is a Operator.
   * Operators are explained in <A Href=http://java.sun.com/docs/books/jls/html/>Java 
   * Language Specification</A>.
   *
   * @return true if this token is a Operator, false otherwise
   */
  public boolean isOperator(){
  	return((ID >> 8) == 0x5);
  }

  /** 
   * Checks this token to see if it is a comment.
   * 
   * @return true if this token is a comment, false otherwise
   */
  public boolean isComment(){
  	return((ID >> 8) == 0xD);
  }

  /** 
   * Checks this token to see if it is White Space.
   * Usually tabs, line breaks, form feed, spaces, etc.
   * 
   * @return true if this token is White Space, false otherwise
   */
  public boolean isWhiteSpace(){
  	return((ID >> 8) == 0xE);
  }

  /** 
   * Checks this token to see if it is an Error.
   * Unfinished comments, numbers that are too big, unclosed strings, etc.
   * 
   * @return true if this token is an Error, false otherwise
   */
  public boolean isError(){
  	return((ID >> 8) == 0xF);
  }

	/**
	 * A description of this token.  The description should
	 * be appropriate for syntax highlighting.  For example
	 * "comment" is returned for a comment.
     *
	 * @return a description of this token.
	 */
	public String getDescription(){
		if (isReservedWord()){
			return("reservedWord");
		} else if (isIdentifier()){
			return("identifier");
		} else if (isLiteral()){
			return("literal");
		} else if (isSeparator()){
			return("separator");
		} else if (isOperator()){
			return("operator");
		} else if (isComment()){
			return("comment");
		} else if (isWhiteSpace()){
			return("whitespace");
		} else if (isError()){
		 	return("error");
		} else {
			return("unknown");
		}
	}

  /**
   * get a String that explains the error, if this token is an error.
   * 
   * @return a  String that explains the error, if this token is an error, null otherwise.
   */
  public String errorString(){
  	String s;
  	if (isError()){
  		s = "Error on line " + lineNumber + ": ";
  		switch (ID){
  		case ERROR_IDENTIFIER:
  			s += "Unrecognized Identifier: " + contents;
  		break; 
		case ERROR_UNCLOSED_STRING:
  			s += "'\"' expected after " + contents;
  		break; 		
		case ERROR_MALFORMED_STRING:
		case ERROR_MALFORMED_UNCLOSED_STRING:
  			s += "Illegal character in " + contents;
  		break;
		case ERROR_UNCLOSED_CHARACTER:
  			s += "\"'\" expected after " + contents;
  		break; 		
		case ERROR_MALFORMED_CHARACTER:
		case ERROR_MALFORMED_UNCLOSED_CHARACTER:
  			s += "Illegal character in " + contents;
  		break;
		case ERROR_INTEGER_DECIMIAL_SIZE:
		case ERROR_INTEGER_OCTAL_SIZE:
		case ERROR_FLOAT:  			
  			s += "Illegal character in " + contents;
  		break;
		case ERROR_INTEGER_HEXIDECIMAL_SIZE:
		case ERROR_LONG_DECIMIAL_SIZE:
		case ERROR_LONG_OCTAL_SIZE:
		case ERROR_LONG_HEXIDECIMAL_SIZE:
		case ERROR_FLOAT_SIZE:
		case ERROR_DOUBLE_SIZE:
  			s += "Literal out of bounds: " + contents;
  		break;
		case ERROR_UNCLOSED_COMMENT:
  			s += "*/ expected after " + contents;
  		break;
		}
  			
  	} else {
  		s = null;
  	}
  	return (s);
  }

  /** 
   * get a representation of this token as a human readable string.
   * The format of this string is subject to change and should only be used
   * for debugging purposes.
   *
   * @return a string representation of this token
   */  
  public String toString() {
      return ("Token #" + Integer.toHexString(ID) + ": " + getDescription() + " Line " + 
      	lineNumber + " from " +charBegin + " to " + charEnd + " : " + contents);
  }
  
}
