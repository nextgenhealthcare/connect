/* SQLLexer.java is a generated file.  You probably want to
 * edit SQLLexer.lex to make changes.  Use JFlex to generate it.
 * To generate SQLLexer.java
 * Install <a href="http://jflex.de/">JFlex</a> v1.3.2 or later.
 * Once JFlex is in your classpath run<br>
 * <code>java JFlex.Main SQLLexer.lex</code><br>
 * You will then have a file called SQLLexer.java
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
 * SQLLexer is a SQL language lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  SQLLexer shredder = new SQLLexer(System.in);
 *  SQLToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *      System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 * 
 * @see SQLToken
 */ 

%%

%public
%class SQLLexer
%implements Lexer
%function getNextToken
%type Token 

%{
    private int lastToken;
    private int nextState=YYINITIAL;
	private StringBuffer commentBuffer = new StringBuffer();
	private int commentNestCount = 0;
	private int commentStartLine = 0;
	private int commentStartChar = 0;
    
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
            SQLLexer shredder = new SQLLexer(in);
            Token t;
            while ((t = shredder.getNextToken()) != null) {
                if (t.getID() != SQLToken.WHITE_SPACE){
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
%ignorecase

%state COMMENT

keyword=("ABORT"|"ABS"|"ABSOLUTE"|"ACCESS"|"ACTION"|"ADA"|"ADD"|"ADMIN"|"AFTER"|"AGGREGATE"|"ALIAS"|"ALL"|"ALLOCATE"|"ALTER"|"ANALYSE"|"ANALYZE"|"AND"|"ANY"|"ARE"|"ARRAY"|"AS"|"ASC"|"ASENSITIVE"|"ASSERTION"|"ASSIGNMENT"|"ASYMMETRIC"|"AT"|"ATOMIC"|"AUTHORIZATION"|"AVG"|"BACKWARD"|"BEFORE"|"BEGIN"|"BETWEEN"|"BINARY"|"BIT"|"BITVAR"|"BIT_LENGTH"|"BLOB"|"BOOLEAN"|"BOTH"|"BREADTH"|"BY"|"C"|"CACHE"|"CALL"|"CALLED"|"CARDINALITY"|"CASCADE"|"CASCADED"|"CASE"|"CAST"|"CATALOG"|"CATALOG_NAME"|"CHAIN"|"CHAR"|"CHARACTER"|"CHARACTERISTICS"|"CHARACTER_LENGTH"|"CHARACTER_SET_CATALOG"|"CHARACTER_SET_NAME"|"CHARACTER_SET_SCHEMA"|"CHAR_LENGTH"|"CHECK"|"CHECKED"|"CHECKPOINT"|"CLASS"|"CLASS_ORIGIN"|"CLOB"|"CLOSE"|"CLUSTER"|"COALESCE"|"COBOL"|"COLLATE"|"COLLATION"|"COLLATION_CATALOG"|"COLLATION_NAME"|"COLLATION_SCHEMA"|"COLUMN"|"COLUMN_NAME"|"COMMAND_FUNCTION"|"COMMAND_FUNCTION_CODE"|"COMMENT"|"COMMIT"|"COMMITTED"|"COMPLETION"|"CONDITION_NUMBER"|"CONNECT"|"CONNECTION"|"CONNECTION_NAME"|"CONSTRAINT"|"CONSTRAINTS"|"CONSTRAINT_CATALOG"|"CONSTRAINT_NAME"|"CONSTRAINT_SCHEMA"|"CONSTRUCTOR"|"CONTAINS"|"CONTINUE"|"CONVERT"|"COPY"|"CORRESPONDING"|"COUNT"|"CREATE"|"CREATEDB"|"CREATEUSER"|"CROSS"|"CUBE"|"CURRENT"|"CURRENT_DATE"|"CURRENT_PATH"|"CURRENT_ROLE"|"CURRENT_TIME"|"CURRENT_TIMESTAMP"|"CURRENT_USER"|"CURSOR"|"CURSOR_NAME"|"CYCLE"|"DATA"|"DATABASE"|"DATE"|"DATETIME_INTERVAL_CODE"|"DATETIME_INTERVAL_PRECISION"|"DAY"|"DEALLOCATE"|"DEC"|"DECIMAL"|"DECLARE"|"DEFAULT"|"DEFERRABLE"|"DEFERRED"|"DEFINED"|"DEFINER"|"DELETE"|"DELIMITER"|"DELIMITERS"|"DEPTH"|"DEREF"|"DESC"|"DESCRIBE"|"DESCRIPTOR"|"DESTROY"|"DESTRUCTOR"|"DETERMINISTIC"|"DIAGNOSTICS"|"DICTIONARY"|"DISCONNECT"|"DISPATCH"|"DISTINCT"|"DO"|"DOMAIN"|"DOUBLE"|"DROP"|"DYNAMIC"|"DYNAMIC_FUNCTION"|"DYNAMIC_FUNCTION_CODE"|"EACH"|"ELSE"|"ENCODING"|"ENCRYPTED"|"END"|"END-EXEC"|"EQUALS"|"ESCAPE"|"EVERY"|"EXCEPT"|"EXCEPTION"|"EXCLUSIVE"|"EXEC"|"EXECUTE"|"EXISTING"|"EXISTS"|"EXPLAIN"|"EXTERNAL"|"EXTRACT"|"FALSE"|"FETCH"|"FINAL"|"FIRST"|"FLOAT"|"FOR"|"FORCE"|"FOREIGN"|"FORTRAN"|"FORWARD"|"FOUND"|"FREE"|"FREEZE"|"FROM"|"FULL"|"FUNCTION"|"G"|"GENERAL"|"GENERATED"|"GET"|"GLOBAL"|"GO"|"GOTO"|"GRANT"|"GRANTED"|"GROUP"|"GROUPING"|"HANDLER"|"HAVING"|"HIERARCHY"|"HOLD"|"HOST"|"HOUR"|"IDENTITY"|"IGNORE"|"ILIKE"|"IMMEDIATE"|"IMPLEMENTATION"|"IN"|"INCREMENT"|"INDEX"|"INDICATOR"|"INFIX"|"INHERITS"|"INITIALIZE"|"INITIALLY"|"INNER"|"INOUT"|"INPUT"|"INSENSITIVE"|"INSERT"|"INSTANCE"|"INSTANTIABLE"|"INSTEAD"|"INT"|"INTEGER"|"INTERSECT"|"INTERVAL"|"INTO"|"INVOKER"|"IS"|"ISNULL"|"ISOLATION"|"ITERATE"|"JOIN"|"K"|"KEY"|"KEY_MEMBER"|"KEY_TYPE"|"LANCOMPILER"|"LANGUAGE"|"LARGE"|"LAST"|"LATERAL"|"LEADING"|"LEFT"|"LENGTH"|"LESS"|"LEVEL"|"LIKE"|"LIMIT"|"LISTEN"|"LOAD"|"LOCAL"|"LOCALTIME"|"LOCALTIMESTAMP"|"LOCATION"|"LOCATOR"|"LOCK"|"LOWER"|"M"|"MAP"|"MATCH"|"MAX"|"MAXVALUE"|"MESSAGE_LENGTH"|"MESSAGE_OCTET_LENGTH"|"MESSAGE_TEXT"|"METHOD"|"MIN"|"MINUTE"|"MINVALUE"|"MOD"|"MODE"|"MODIFIES"|"MODIFY"|"MODULE"|"MONTH"|"MORE"|"MOVE"|"MUMPS"|"NAME"|"NAMES"|"NATIONAL"|"NATURAL"|"NCHAR"|"NCLOB"|"NEW"|"NEXT"|"NO"|"NOCREATEDB"|"NOCREATEUSER"|"NONE"|"NOT"|"NOTHING"|"NOTIFY"|"NOTNULL"|"NULL"|"NULLABLE"|"NULLIF"|"NUMBER"|"NUMERIC"|"OBJECT"|"OCTET_LENGTH"|"OF"|"OFF"|"OFFSET"|"OIDS"|"OLD"|"ON"|"ONLY"|"OPEN"|"OPERATION"|"OPERATOR"|"OPTION"|"OPTIONS"|"OR"|"ORDER"|"ORDINALITY"|"OUT"|"OUTER"|"OUTPUT"|"OVERLAPS"|"OVERLAY"|"OVERRIDING"|"OWNER"|"PAD"|"PARAMETER"|"PARAMETERS"|"PARAMETER_MODE"|"PARAMETER_NAME"|"PARAMETER_ORDINAL_POSITION"|"PARAMETER_SPECIFIC_CATALOG"|"PARAMETER_SPECIFIC_NAME"|"PARAMETER_SPECIFIC_SCHEMA"|"PARTIAL"|"PASCAL"|"PASSWORD"|"PATH"|"PENDANT"|"PLI"|"POSITION"|"POSTFIX"|"PRECISION"|"PREFIX"|"PREORDER"|"PREPARE"|"PRESERVE"|"PRIMARY"|"PRIOR"|"PRIVILEGES"|"PROCEDURAL"|"PROCEDURE"|"PUBLIC"|"READ"|"READS"|"REAL"|"RECURSIVE"|"REF"|"REFERENCES"|"REFERENCING"|"REINDEX"|"RELATIVE"|"RENAME"|"REPEATABLE"|"REPLACE"|"RESET"|"RESTRICT"|"RESULT"|"RETURN"|"RETURNED_LENGTH"|"RETURNED_OCTET_LENGTH"|"RETURNED_SQLSTATE"|"RETURNS"|"REVOKE"|"RIGHT"|"ROLE"|"ROLLBACK"|"ROLLUP"|"ROUTINE"|"ROUTINE_CATALOG"|"ROUTINE_NAME"|"ROUTINE_SCHEMA"|"ROW"|"ROWS"|"ROW_COUNT"|"RULE"|"SAVEPOINT"|"SCALE"|"SCHEMA"|"SCHEMA_NAME"|"SCOPE"|"SCROLL"|"SEARCH"|"SECOND"|"SECTION"|"SECURITY"|"SELECT"|"SELF"|"SENSITIVE"|"SEQUENCE"|"SERIALIZABLE"|"SERVER_NAME"|"SESSION"|"SESSION_USER"|"SET"|"SETOF"|"SETS"|"SHARE"|"SHOW"|"SIMILAR"|"SIMPLE"|"SIZE"|"SMALLINT"|"SOME"|"SOURCE"|"SPACE"|"SPECIFIC"|"SPECIFICTYPE"|"SPECIFIC_NAME"|"SQL"|"SQLCODE"|"SQLERROR"|"SQLEXCEPTION"|"SQLSTATE"|"SQLWARNING"|"START"|"STATE"|"STATEMENT"|"STATIC"|"STATISTICS"|"STDIN"|"STDOUT"|"STRUCTURE"|"STYLE"|"SUBCLASS_ORIGIN"|"SUBLIST"|"SUBSTRING"|"SUM"|"SYMMETRIC"|"SYSID"|"SYSTEM"|"SYSTEM_USER"|"TABLE"|"TABLE_NAME"|"TEMP"|"TEMPLATE"|"TEMPORARY"|"TERMINATE"|"THAN"|"THEN"|"TIME"|"TIMESTAMP"|"TIMEZONE_HOUR"|"TIMEZONE_MINUTE"|"TO"|"TOAST"|"TRAILING"|"TRANSACTION"|"TRANSACTIONS_COMMITTED"|"TRANSACTIONS_ROLLED_BACK"|"TRANSACTION_ACTIVE"|"TRANSFORM"|"TRANSFORMS"|"TRANSLATE"|"TRANSLATION"|"TREAT"|"TRIGGER"|"TRIGGER_CATALOG"|"TRIGGER_NAME"|"TRIGGER_SCHEMA"|"TRIM"|"TRUE"|"TRUNCATE"|"TRUSTED"|"TYPE"|"UNCOMMITTED"|"UNDER"|"UNENCRYPTED"|"UNION"|"UNIQUE"|"UNKNOWN"|"UNLISTEN"|"UNNAMED"|"UNNEST"|"UNTIL"|"UPDATE"|"UPPER"|"USAGE"|"USER"|"USER_DEFINED_TYPE_CATALOG"|"USER_DEFINED_TYPE_NAME"|"USER_DEFINED_TYPE_SCHEMA"|"USING"|"VACUUM"|"VALID"|"VALUE"|"VALUES"|"VARCHAR"|"VARIABLE"|"VARYING"|"VERBOSE"|"VERSION"|"VIEW"|"WHEN"|"WHENEVER"|"WHERE"|"WITH"|"WITHOUT"|"WORK"|"WRITE"|"YEAR"|"ZONE")
whitespace=([ \r\n\t\f])
identifier=([^ \r\n\t\f\+\-\*\/\<\>\=\~\!\@\#\%\^\&\|\`\'\"\~\?\$\(\)\[\]\,\;\:\*\.\_0-9][^ \r\n\t\f\+\-\*\/\<\>\=\~\!\@\#\%\^\&\|\`\'\"\~\?\$\(\)\[\]\,\;\:\*\.]*)
digit=([0-9])
digits=({digit}+)
positionalparams=("$"{digits})
separator=([\(\)\[\]\,\;\:\*\.]|{positionalparams})
operator=([\+\-\*\/\<\>\=\~\!\@\#\%\^\&\|\`\?\$])
integer=({digits})
string=([\'](([^\r\n\']|[\\][\'])*)[\'])
bitstring=("B"[\']([01]+)[\'])
stringerror=([\'](([^\r\n\']|[\\][\'])*)[\r\n])
bitstringerror1=("B"[\']([^01\r\n]*)[\'])
bitstringerror2=("B"[\'](([^\r\n\']|[\\][\'])*)[\r\n])
floatpoint=(({digits}"."({digits}?)("E"[+-]{digits})?)|(({digits}?)"."{digits}("E"[+-]{digits})?)|({digits}"E"[+-]{digits}))
linecomment=("--"[^\r\n]*)
commentstart="/*"
commenttext=(([^\*\/]|([\*]+[^\*\/])|([\/]+[^\*\/]))*)
commentend=(([\*]*)"/")
%% 

<YYINITIAL> {linecomment} {
    nextState = YYINITIAL;
	lastToken = SQLToken.COMMENT_END_OF_LINE;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {commentstart} {
    nextState = COMMENT;
	commentBuffer.setLength(0);
    commentBuffer.append(yytext());
	commentNestCount = 1;
	commentStartLine = yyline;
	commentStartChar = yychar;
	yybegin(nextState);
}

<COMMENT> {commentstart} {
    nextState = COMMENT;
    commentBuffer.append(yytext());
	commentNestCount++;
	yybegin(nextState);
}

<COMMENT> {commenttext} {
    nextState = COMMENT;
    commentBuffer.append(yytext());
	yybegin(nextState);
}

<COMMENT> {commentend} {
	commentNestCount--;    
    commentBuffer.append(yytext());
	if (commentNestCount == 0){
		nextState = YYINITIAL;	
		lastToken = SQLToken.COMMENT_TRADITIONAL;
		SQLToken t = (new SQLToken(lastToken,commentBuffer.toString(),commentStartLine,commentStartChar,commentStartChar+commentBuffer.length(),nextState));
		yybegin(nextState);
		return(t);
	} 
}

<COMMENT> <<EOF>> {
	nextState = YYINITIAL;	
	lastToken = SQLToken.ERROR_UNCLOSED_COMMENT;
	SQLToken t = (new SQLToken(lastToken,commentBuffer.toString(),commentStartLine,commentStartChar,commentStartChar+commentBuffer.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {keyword} {
    nextState = YYINITIAL;
	lastToken = SQLToken.RESERVED_WORD;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {separator} {
    nextState = YYINITIAL;
	lastToken = SQLToken.SEPARATOR;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {operator} {
    nextState = YYINITIAL;
	lastToken = SQLToken.OPERATOR;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {bitstring} {
    nextState = YYINITIAL;
	lastToken = SQLToken.LITERAL_BIT_STRING;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {bitstringerror1} {
    nextState = YYINITIAL;
	lastToken = SQLToken.ERROR_UNCLOSED_BIT_STRING;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {bitstringerror2} {
    nextState = YYINITIAL;
	lastToken = SQLToken.ERROR_BAD_BIT_STRING;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {identifier} {
    nextState = YYINITIAL;
	lastToken = SQLToken.IDENTIFIER;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {integer} {
    nextState = YYINITIAL;
	lastToken = SQLToken.LITERAL_INTEGER;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {string} {
    nextState = YYINITIAL;
	lastToken = SQLToken.LITERAL_STRING;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}


<YYINITIAL> {stringerror} {
    nextState = YYINITIAL;
	lastToken = SQLToken.ERROR_UNCLOSED_STRING;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {floatpoint} {
    nextState = YYINITIAL;
	lastToken = SQLToken.LITERAL_FLOAT;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL> {whitespace}* {
    nextState = YYINITIAL;
	lastToken = SQLToken.WHITE_SPACE;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}

<YYINITIAL, COMMENT> [^] {
    nextState = YYINITIAL;
	lastToken = SQLToken.ERROR;
    String text = yytext();
	SQLToken t = (new SQLToken(lastToken,text,yyline,yychar,yychar+text.length(),nextState));
	yybegin(nextState);
	return(t);
}
