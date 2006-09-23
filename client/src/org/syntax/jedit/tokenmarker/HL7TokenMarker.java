/*
 * PatchTokenMarker.java - DIFF patch token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package org.syntax.jedit.tokenmarker;

import javax.swing.text.Segment;

/**
 * Patch/diff token marker.
 *
 * @author Slava Pestov
 * @version $Id: PatchTokenMarker.java,v 1.7 1999/12/13 03:40:30 sp Exp $
 */
public class HL7TokenMarker extends TokenMarker
{
	public static final byte VALUE = Token.INTERNAL_FIRST;

	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		addToken(3, Token.KEYWORD1);
		lastOffset = offset + 3;
loop:		for(int i = offset + 3; i < length; i++)
		{
			int i1 = (i+1);

			switch(token)
			{
			case Token.NULL:
				switch(array[i])
				{
				case '|':
					addToken(i - lastOffset,token);
					addToken(1,Token.OPERATOR);
					lastOffset =  i1;
					break;				
				case '^': case '~': case '&': case '\\':
					addToken(i - lastOffset,token);
					addToken(1,Token.LITERAL1);
					lastOffset =  i1;
					break;
				}
				break;
			default:
				throw new InternalError("Invalid state: "
					+ token);
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,Token.NULL);
		return Token.NULL;
	}

	public boolean supportsMultilineTokens()
	{
		return false;
	}
}
