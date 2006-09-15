/*
 * Static String formatting and query routines.
 * Copyright (C) 2001-2005 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
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

package com.Ostermiller.util;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Utilities for String formatting, manipulation, and queries.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/StringHelper.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class StringHelper {

	/**
	 * Pad the beginning of the given String with spaces until
	 * the String is of the given length.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String prepad(String s, int length){
		return prepad(s, length, ' ');
	}

	/**
	 * Pre-pend the given character to the String until
	 * the result is the desired length.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @param c padding character.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String prepad(String s, int length, char c){
		int needed = length - s.length();
		if (needed <= 0){
			return s;
		}
		StringBuffer sb = new StringBuffer(length);
		for (int i=0; i<needed; i++){
			sb.append(c);
		}
		sb.append(s);
		return (sb.toString());
	}

	/**
	 * Pad the end of the given String with spaces until
	 * the String is of the given length.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String postpad(String s, int length){
		return postpad(s, length, ' ');
	}

	/**
	 * Append the given character to the String until
	 * the result is  the desired length.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @param c padding character.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String postpad(String s, int length, char c){
		int needed = length - s.length();
		if (needed <= 0){
			return s;
		}
		StringBuffer sb = new StringBuffer(length);
		sb.append(s);
		for (int i=0; i<needed; i++){
			sb.append(c);
		}
		return (sb.toString());
	}

	/**
	 * Pad the beginning and end of the given String with spaces until
	 * the String is of the given length.  The result is that the original
	 * String is centered in the middle of the new string.
	 * <p>
	 * If the number of characters to pad is even, then the padding
	 * will be split evenly between the beginning and end, otherwise,
	 * the extra character will be added to the end.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String midpad(String s, int length){
		return midpad(s, length, ' ');
	}

	/**
	 * Pad the beginning and end of the given String with the given character
	 * until the result is  the desired length.  The result is that the original
	 * String is centered in the middle of the new string.
	 * <p>
	 * If the number of characters to pad is even, then the padding
	 * will be split evenly between the beginning and end, otherwise,
	 * the extra character will be added to the end.
	 * <p>
	 * If a String is longer than the desired length,
	 * it will not be truncated, however no padding
	 * will be added.
	 *
	 * @param s String to be padded.
	 * @param length desired length of result.
	 * @param c padding character.
	 * @return padded String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String midpad(String s, int length, char c){
		int needed = length - s.length();
		if (needed <= 0){
			return s;
		}
		int beginning = needed / 2;
		int end = beginning + needed % 2;
		StringBuffer sb = new StringBuffer(length);
		for (int i=0; i<beginning; i++){
			sb.append(c);
		}
		sb.append(s);
		for (int i=0; i<end; i++){
			sb.append(c);
		}
		return (sb.toString());
	}

	/**
	 * Split the given String into tokens.
	 * <P>
	 * This method is meant to be similar to the split
	 * function in other programming languages but it does
	 * not use regular expressions.  Rather the String is
	 * split on a single String literal.
	 * <P>
	 * Unlike java.util.StringTokenizer which accepts
	 * multiple character tokens as delimiters, the delimiter
	 * here is a single String literal.
	 * <P>
	 * Each null token is returned as an empty String.
	 * Delimiters are never returned as tokens.
	 * <P>
	 * If there is no delimiter because it is either empty or
	 * null, the only element in the result is the original String.
	 * <P>
	 * StringHelper.split("1-2-3", "-");<br>
	 * result: {"1","2","3"}<br>
	 * StringHelper.split("-1--2-", "-");<br>
	 * result: {"","1","","2",""}<br>
	 * StringHelper.split("123", "");<br>
	 * result: {"123"}<br>
	 * StringHelper.split("1-2---3----4", "--");<br>
	 * result: {"1-2","-3","","4"}<br>
	 *
	 * @param s String to be split.
	 * @param delimiter String literal on which to split.
	 * @return an array of tokens.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String[] split(String s, String delimiter){
		int delimiterLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (delimiter == null || (delimiterLength = delimiter.length()) == 0){
			// it is not inherently clear what to do if there is no delimiter
			// On one hand it would make sense to return each character because
			// the null String can be found between each pair of characters in
			// a String.  However, it can be found many times there and we don'
			// want to be returning multiple null tokens.
			// returning the whole String will be defined as the correct behavior
			// in this instance.
			return new String[] {s};
		}

		// a two pass solution is used because a one pass solution would
		// require the possible resizing and copying of memory structures
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.

		int count;
		int start;
		int end;

		// Scan s and count the tokens.
		count = 0;
		start = 0;
		while((end = s.indexOf(delimiter, start)) != -1){
			count++;
			start = end + delimiterLength;
		}
		count++;

		// allocate an array to return the tokens,
		// we now know how big it should be
		String[] result = new String[count];

		// Scan s again, but this time pick out the tokens
		count = 0;
		start = 0;
		while((end = s.indexOf(delimiter, start)) != -1){
			result[count] = (s.substring(start, end));
			count++;
			start = end + delimiterLength;
		}
		end = stringLength;
		result[count] = s.substring(start, end);

		return (result);
	}

	/**
	 * Split the given String into tokens.  Delimiters will
	 * be returned as tokens.
	 * <P>
	 * This method is meant to be similar to the split
	 * function in other programming languages but it does
	 * not use regular expressions.  Rather the String is
	 * split on a single String literal.
	 * <P>
	 * Unlike java.util.StringTokenizer which accepts
	 * multiple character tokens as delimiters, the delimiter
	 * here is a single String literal.
	 * <P>
	 * Each null token is returned as an empty String.
	 * Delimiters are never returned as tokens.
	 * <P>
	 * If there is no delimiter because it is either empty or
	 * null, the only element in the result is the original String.
	 * <P>
	 * StringHelper.split("1-2-3", "-");<br>
	 * result: {"1","-","2","-","3"}<br>
	 * StringHelper.split("-1--2-", "-");<br>
	 * result: {"","-","1","-","","-","2","-",""}<br>
	 * StringHelper.split("123", "");<br>
	 * result: {"123"}<br>
	 * StringHelper.split("1-2--3---4----5", "--");<br>
	 * result: {"1-2","--","3","--","-4","--","","--","5"}<br>
	 *
	 * @param s String to be split.
	 * @param delimiter String literal on which to split.
	 * @return an array of tokens.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.05.00
	 */
	public static String[] splitIncludeDelimiters(String s, String delimiter){
		int delimiterLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (delimiter == null || (delimiterLength = delimiter.length()) == 0){
			// it is not inherently clear what to do if there is no delimiter
			// On one hand it would make sense to return each character because
			// the null String can be found between each pair of characters in
			// a String.  However, it can be found many times there and we don'
			// want to be returning multiple null tokens.
			// returning the whole String will be defined as the correct behavior
			// in this instance.
			return new String[] {s};
		}

		// a two pass solution is used because a one pass solution would
		// require the possible resizing and copying of memory structures
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.

		int count;
		int start;
		int end;

		// Scan s and count the tokens.
		count = 0;
		start = 0;
		while((end = s.indexOf(delimiter, start)) != -1){
			count+=2;
			start = end + delimiterLength;
		}
		count++;

		// allocate an array to return the tokens,
		// we now know how big it should be
		String[] result = new String[count];

		// Scan s again, but this time pick out the tokens
		count = 0;
		start = 0;
		while((end = s.indexOf(delimiter, start)) != -1){
			result[count] = (s.substring(start, end));
			count++;
			result[count] = delimiter;
			count++;
			start = end + delimiterLength;
		}
		end = stringLength;
		result[count] = s.substring(start, end);

		return (result);
	}

	/**
	 * Join all the elements of a string array into a single
	 * String.
	 * <p>
	 * If the given array empty an empty string
	 * will be returned.  Null elements of the array are allowed
	 * and will be treated like empty Strings.
	 *
	 * @param array Array to be joined into a string.
	 * @return Concatenation of all the elements of the given array.
	 * @throws NullPointerException if array is null.
	 *
	 * @since ostermillerutils 1.05.00
	 */
	public static String join(String[] array){
		return join(array, "");
	}

	/**
	 * Join all the elements of a string array into a single
	 * String.
	 * <p>
	 * If the given array empty an empty string
	 * will be returned.  Null elements of the array are allowed
	 * and will be treated like empty Strings.
	 *
	 * @param array Array to be joined into a string.
	 * @param delimiter String to place between array elements.
	 * @return Concatenation of all the elements of the given array with the the delimiter in between.
	 * @throws NullPointerException if array or delimiter is null.
	 *
	 * @since ostermillerutils 1.05.00
	 */
	public static String join(String[] array, String delimiter){
		// Cache the length of the delimiter
		// has the side effect of throwing a NullPointerException if
		// the delimiter is null.
		int delimiterLength = delimiter.length();

		// Nothing in the array return empty string
		// has the side effect of throwing a NullPointerException if
		// the array is null.
		if (array.length == 0) return "";

		// Only one thing in the array, return it.
		if (array.length == 1){
			if (array[0] == null) return "";
			return array[0];
		}

		// Make a pass through and determine the size
		// of the resulting string.
		int length = 0;
		for (int i=0; i<array.length; i++){
			if (array[i] != null) length+=array[i].length();
			if (i<array.length-1) length+=delimiterLength;
		}

		// Make a second pass through and concatenate everything
		// into a string buffer.
		StringBuffer result = new StringBuffer(length);
		for (int i=0; i<array.length; i++){
			if (array[i] != null) result.append(array[i]);
			if (i<array.length-1) result.append(delimiter);
		}

		return result.toString();
	}

	/**
	 * Replace occurrences of a substring.
	 *
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 *
	 * @param s String to be modified.
	 * @param find String to find.
	 * @param replace String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String replace(String s, String find, String replace){
		int findLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (find == null || (findLength = find.length()) == 0){
			// If there is nothing to find, we won't try and find it.
			return s;
		}
		if (replace == null){
			// a null string and an empty string are the same
			// for replacement purposes.
			replace = "";
		}
		int replaceLength = replace.length();

		// We need to figure out how long our resulting string will be.
		// This is required because without it, the possible resizing
		// and copying of memory structures could lead to an unacceptable runtime.
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.
		int length;
		if (findLength == replaceLength){
			// special case in which we don't need to count the replacements
			// because the count falls out of the length formula.
			length = stringLength;
		} else {
			int count;
			int start;
			int end;

			// Scan s and count the number of times we find our target.
			count = 0;
			start = 0;
			while((end = s.indexOf(find, start)) != -1){
				count++;
				start = end + findLength;
			}
			if (count == 0){
				// special case in which on first pass, we find there is nothing
				// to be replaced.  No need to do a second pass or create a string buffer.
				return s;
			}
			length = stringLength - (count * (findLength - replaceLength));
		}

		int start = 0;
		int end = s.indexOf(find, start);
		if (end == -1){
			// nothing was found in the string to replace.
			// we can get this if the find and replace strings
			// are the same length because we didn't check before.
			// in this case, we will return the original string
			return s;
		}
		// it looks like we actually have something to replace
		// *sigh* allocate memory for it.
		StringBuffer sb = new StringBuffer(length);

		// Scan s and do the replacements
		while (end != -1){
			sb.append(s.substring(start, end));
			sb.append(replace);
			start = end + findLength;
			end = s.indexOf(find, start);
		}
		end = stringLength;
		sb.append(s.substring(start, end));

		return (sb.toString());
	}

	/**
	 * Replaces characters that may be confused by a HTML
	 * parser with their equivalent character entity references.
	 * <p>
	 * Any data that will appear as text on a web page should
	 * be be escaped.  This is especially important for data
	 * that comes from untrusted sources such as Internet users.
	 * A common mistake in CGI programming is to ask a user for
	 * data and then put that data on a web page.  For example:<pre>
	 * Server: What is your name?
	 * User: &lt;b&gt;Joe&lt;b&gt;
	 * Server: Hello <b>Joe</b>, Welcome</pre>
	 * If the name is put on the page without checking that it doesn't
	 * contain HTML code or without sanitizing that HTML code, the user
	 * could reformat the page, insert scripts, and control the the
	 * content on your web server.
	 * <p>
	 * This method will replace HTML characters such as &gt; with their
	 * HTML entity reference (&amp;gt;) so that the html parser will
	 * be sure to interpret them as plain text rather than HTML or script.
	 * <p>
	 * This method should be used for both data to be displayed in text
	 * in the html document, and data put in form elements. For example:<br>
	 * <code>&lt;html&gt;&lt;body&gt;<i>This in not a &amp;lt;tag&amp;gt;
	 * in HTML</i>&lt;/body&gt;&lt;/html&gt;</code><br>
	 * and<br>
	 * <code>&lt;form&gt;&lt;input type="hidden" name="date" value="<i>This data could
	 * be &amp;quot;malicious&amp;quot;</i>"&gt;&lt;/form&gt;</code><br>
	 * In the second example, the form data would be properly be resubmitted
	 * to your cgi script in the URLEncoded format:<br>
	 * <code><i>This data could be %22malicious%22</i></code>
	 *
	 * @param s String to be escaped
	 * @return escaped String
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeHTML(String s){
		int length = s.length();
		int newLength = length;
		boolean someCharacterEscaped = false;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32){
				switch(c){
					case '\r':
					case '\n':
					case '\t':
					case '\f':{
					} break;
					default: {
						newLength -= 1;
						someCharacterEscaped = true;
					}
				}
			} else {
				switch(c){
					case '\"':{
						newLength += 5;
						someCharacterEscaped = true;
					} break;
					case '&':
					case '\'':{
						newLength += 4;
						someCharacterEscaped = true;
					} break;
					case '<':
					case '>':{
						newLength += 3;
						someCharacterEscaped = true;
					} break;
				}
			}
		}
		if (!someCharacterEscaped){
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32){
				switch(c){
					case '\r':
					case '\n':
					case '\t':
					case '\f':{
						sb.append(c);
					} break;
					default: {
						// Remove this character
					}
				}
			} else {
				switch(c){
					case '\"':{
						sb.append("&quot;");
					} break;
					case '\'':{
						sb.append("&#39;");
					} break;
					case '&':{
						sb.append("&amp;");
					} break;
					case '<':{
						sb.append("&lt;");
					} break;
					case '>':{
						sb.append("&gt;");
					} break;
					default: {
						sb.append(c);
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Replaces characters that may be confused by an SQL
	 * parser with their equivalent escape characters.
	 * <p>
	 * Any data that will be put in an SQL query should
	 * be be escaped.  This is especially important for data
	 * that comes from untrusted sources such as Internet users.
	 * <p>
	 * For example if you had the following SQL query:<br>
	 * <code>"SELECT * FROM addresses WHERE name='" + name + "' AND private='N'"</code><br>
	 * Without this function a user could give <code>" OR 1=1 OR ''='"</code>
	 * as their name causing the query to be:<br>
	 * <code>"SELECT * FROM addresses WHERE name='' OR 1=1 OR ''='' AND private='N'"</code><br>
	 * which will give all addresses, including private ones.<br>
	 * Correct usage would be:<br>
	 * <code>"SELECT * FROM addresses WHERE name='" + StringHelper.escapeSQL(name) + "' AND private='N'"</code><br>
	 * <p>
	 * Another way to avoid this problem is to use a PreparedStatement
	 * with appropriate placeholders.
	 *
	 * @param s String to be escaped
	 * @return escaped String
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeSQL(String s){
		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			switch(c){
				case '\\':
				case '\"':
				case '\'':
				case '\0':{
					newLength += 1;
				} break;
			}
		}
		if (length == newLength){
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			switch(c){
				case '\\':{
					sb.append("\\\\");
				} break;
				case '\"':{
					sb.append("\\\"");
				} break;
				case '\'':{
					sb.append("\\\'");
				} break;
				case '\0':{
					sb.append("\\0");
				} break;
				default: {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Replaces characters that are not allowed in a Java style
	 * string literal with their escape characters.  Specifically
	 * quote ("), single quote ('), new line (\n), carriage return (\r),
	 * and backslash (\), and tab (\t) are escaped.
	 *
	 * @param s String to be escaped
	 * @return escaped String
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeJavaLiteral(String s){
		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			switch(c){
				case '\"':
				case '\'':
				case '\n':
				case '\r':
				case '\t':
				case '\\':{
					newLength += 1;
				} break;
			}
		}
		if (length == newLength){
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			switch(c){
				case '\"':{
					sb.append("\\\"");
				} break;
				case '\'':{
					sb.append("\\\'");
				} break;
				case '\n':{
					sb.append("\\n");
				} break;
				case '\r':{
					sb.append("\\r");
				} break;
				case '\t':{
					sb.append("\\t");
				} break;
				case '\\':{
					sb.append("\\\\");
				} break;
				default: {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Trim any of the characters contained in the second
	 * string from the beginning and end of the first.
	 *
	 * @param s String to be trimmed.
	 * @param c list of characters to trim from s.
	 * @return trimmed String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String trim(String s, String c){
		int length = s.length();
		if (c == null){
			return s;
		}
		int cLength = c.length();
		if (c.length() == 0){
			return s;
		}
		int start = 0;
		int end = length;
		boolean found; // trim-able character found.
		int i;
		// Start from the beginning and find the
		// first non-trim-able character.
		found = false;
		for (i=0; !found && i<length; i++){
			char ch = s.charAt(i);
			found = true;
			for (int j=0; found && j<cLength; j++){
				if (c.charAt(j) == ch) found = false;
			}
		}
		// if all characters are trim-able.
		if (!found) return "";
		start = i-1;
		// Start from the end and find the
		// last non-trim-able character.
		found = false;
		for (i=length-1; !found && i>=0; i--){
			char ch = s.charAt(i);
			found = true;
			for (int j=0; found && j<cLength; j++){
				if (c.charAt(j) == ch) found = false;
			}
		}
		end = i+2;
		return s.substring(start, end);
	}

	private static HashMap<String,Integer> htmlEntities = new HashMap<String,Integer>();
	static {
		htmlEntities.put("nbsp", new Integer(160));
		htmlEntities.put("iexcl", new Integer(161));
		htmlEntities.put("cent", new Integer(162));
		htmlEntities.put("pound", new Integer(163));
		htmlEntities.put("curren", new Integer(164));
		htmlEntities.put("yen", new Integer(165));
		htmlEntities.put("brvbar", new Integer(166));
		htmlEntities.put("sect", new Integer(167));
		htmlEntities.put("uml", new Integer(168));
		htmlEntities.put("copy", new Integer(169));
		htmlEntities.put("ordf", new Integer(170));
		htmlEntities.put("laquo", new Integer(171));
		htmlEntities.put("not", new Integer(172));
		htmlEntities.put("shy", new Integer(173));
		htmlEntities.put("reg", new Integer(174));
		htmlEntities.put("macr", new Integer(175));
		htmlEntities.put("deg", new Integer(176));
		htmlEntities.put("plusmn", new Integer(177));
		htmlEntities.put("sup2", new Integer(178));
		htmlEntities.put("sup3", new Integer(179));
		htmlEntities.put("acute", new Integer(180));
		htmlEntities.put("micro", new Integer(181));
		htmlEntities.put("para", new Integer(182));
		htmlEntities.put("middot", new Integer(183));
		htmlEntities.put("cedil", new Integer(184));
		htmlEntities.put("sup1", new Integer(185));
		htmlEntities.put("ordm", new Integer(186));
		htmlEntities.put("raquo", new Integer(187));
		htmlEntities.put("frac14", new Integer(188));
		htmlEntities.put("frac12", new Integer(189));
		htmlEntities.put("frac34", new Integer(190));
		htmlEntities.put("iquest", new Integer(191));
		htmlEntities.put("Agrave", new Integer(192));
		htmlEntities.put("Aacute", new Integer(193));
		htmlEntities.put("Acirc", new Integer(194));
		htmlEntities.put("Atilde", new Integer(195));
		htmlEntities.put("Auml", new Integer(196));
		htmlEntities.put("Aring", new Integer(197));
		htmlEntities.put("AElig", new Integer(198));
		htmlEntities.put("Ccedil", new Integer(199));
		htmlEntities.put("Egrave", new Integer(200));
		htmlEntities.put("Eacute", new Integer(201));
		htmlEntities.put("Ecirc", new Integer(202));
		htmlEntities.put("Euml", new Integer(203));
		htmlEntities.put("Igrave", new Integer(204));
		htmlEntities.put("Iacute", new Integer(205));
		htmlEntities.put("Icirc", new Integer(206));
		htmlEntities.put("Iuml", new Integer(207));
		htmlEntities.put("ETH", new Integer(208));
		htmlEntities.put("Ntilde", new Integer(209));
		htmlEntities.put("Ograve", new Integer(210));
		htmlEntities.put("Oacute", new Integer(211));
		htmlEntities.put("Ocirc", new Integer(212));
		htmlEntities.put("Otilde", new Integer(213));
		htmlEntities.put("Ouml", new Integer(214));
		htmlEntities.put("times", new Integer(215));
		htmlEntities.put("Oslash", new Integer(216));
		htmlEntities.put("Ugrave", new Integer(217));
		htmlEntities.put("Uacute", new Integer(218));
		htmlEntities.put("Ucirc", new Integer(219));
		htmlEntities.put("Uuml", new Integer(220));
		htmlEntities.put("Yacute", new Integer(221));
		htmlEntities.put("THORN", new Integer(222));
		htmlEntities.put("szlig", new Integer(223));
		htmlEntities.put("agrave", new Integer(224));
		htmlEntities.put("aacute", new Integer(225));
		htmlEntities.put("acirc", new Integer(226));
		htmlEntities.put("atilde", new Integer(227));
		htmlEntities.put("auml", new Integer(228));
		htmlEntities.put("aring", new Integer(229));
		htmlEntities.put("aelig", new Integer(230));
		htmlEntities.put("ccedil", new Integer(231));
		htmlEntities.put("egrave", new Integer(232));
		htmlEntities.put("eacute", new Integer(233));
		htmlEntities.put("ecirc", new Integer(234));
		htmlEntities.put("euml", new Integer(235));
		htmlEntities.put("igrave", new Integer(236));
		htmlEntities.put("iacute", new Integer(237));
		htmlEntities.put("icirc", new Integer(238));
		htmlEntities.put("iuml", new Integer(239));
		htmlEntities.put("eth", new Integer(240));
		htmlEntities.put("ntilde", new Integer(241));
		htmlEntities.put("ograve", new Integer(242));
		htmlEntities.put("oacute", new Integer(243));
		htmlEntities.put("ocirc", new Integer(244));
		htmlEntities.put("otilde", new Integer(245));
		htmlEntities.put("ouml", new Integer(246));
		htmlEntities.put("divide", new Integer(247));
		htmlEntities.put("oslash", new Integer(248));
		htmlEntities.put("ugrave", new Integer(249));
		htmlEntities.put("uacute", new Integer(250));
		htmlEntities.put("ucirc", new Integer(251));
		htmlEntities.put("uuml", new Integer(252));
		htmlEntities.put("yacute", new Integer(253));
		htmlEntities.put("thorn", new Integer(254));
		htmlEntities.put("yuml", new Integer(255));
		htmlEntities.put("fnof", new Integer(402));
		htmlEntities.put("Alpha", new Integer(913));
		htmlEntities.put("Beta", new Integer(914));
		htmlEntities.put("Gamma", new Integer(915));
		htmlEntities.put("Delta", new Integer(916));
		htmlEntities.put("Epsilon", new Integer(917));
		htmlEntities.put("Zeta", new Integer(918));
		htmlEntities.put("Eta", new Integer(919));
		htmlEntities.put("Theta", new Integer(920));
		htmlEntities.put("Iota", new Integer(921));
		htmlEntities.put("Kappa", new Integer(922));
		htmlEntities.put("Lambda", new Integer(923));
		htmlEntities.put("Mu", new Integer(924));
		htmlEntities.put("Nu", new Integer(925));
		htmlEntities.put("Xi", new Integer(926));
		htmlEntities.put("Omicron", new Integer(927));
		htmlEntities.put("Pi", new Integer(928));
		htmlEntities.put("Rho", new Integer(929));
		htmlEntities.put("Sigma", new Integer(931));
		htmlEntities.put("Tau", new Integer(932));
		htmlEntities.put("Upsilon", new Integer(933));
		htmlEntities.put("Phi", new Integer(934));
		htmlEntities.put("Chi", new Integer(935));
		htmlEntities.put("Psi", new Integer(936));
		htmlEntities.put("Omega", new Integer(937));
		htmlEntities.put("alpha", new Integer(945));
		htmlEntities.put("beta", new Integer(946));
		htmlEntities.put("gamma", new Integer(947));
		htmlEntities.put("delta", new Integer(948));
		htmlEntities.put("epsilon", new Integer(949));
		htmlEntities.put("zeta", new Integer(950));
		htmlEntities.put("eta", new Integer(951));
		htmlEntities.put("theta", new Integer(952));
		htmlEntities.put("iota", new Integer(953));
		htmlEntities.put("kappa", new Integer(954));
		htmlEntities.put("lambda", new Integer(955));
		htmlEntities.put("mu", new Integer(956));
		htmlEntities.put("nu", new Integer(957));
		htmlEntities.put("xi", new Integer(958));
		htmlEntities.put("omicron", new Integer(959));
		htmlEntities.put("pi", new Integer(960));
		htmlEntities.put("rho", new Integer(961));
		htmlEntities.put("sigmaf", new Integer(962));
		htmlEntities.put("sigma", new Integer(963));
		htmlEntities.put("tau", new Integer(964));
		htmlEntities.put("upsilon", new Integer(965));
		htmlEntities.put("phi", new Integer(966));
		htmlEntities.put("chi", new Integer(967));
		htmlEntities.put("psi", new Integer(968));
		htmlEntities.put("omega", new Integer(969));
		htmlEntities.put("thetasym", new Integer(977));
		htmlEntities.put("upsih", new Integer(978));
		htmlEntities.put("piv", new Integer(982));
		htmlEntities.put("bull", new Integer(8226));
		htmlEntities.put("hellip", new Integer(8230));
		htmlEntities.put("prime", new Integer(8242));
		htmlEntities.put("Prime", new Integer(8243));
		htmlEntities.put("oline", new Integer(8254));
		htmlEntities.put("frasl", new Integer(8260));
		htmlEntities.put("weierp", new Integer(8472));
		htmlEntities.put("image", new Integer(8465));
		htmlEntities.put("real", new Integer(8476));
		htmlEntities.put("trade", new Integer(8482));
		htmlEntities.put("alefsym", new Integer(8501));
		htmlEntities.put("larr", new Integer(8592));
		htmlEntities.put("uarr", new Integer(8593));
		htmlEntities.put("rarr", new Integer(8594));
		htmlEntities.put("darr", new Integer(8595));
		htmlEntities.put("harr", new Integer(8596));
		htmlEntities.put("crarr", new Integer(8629));
		htmlEntities.put("lArr", new Integer(8656));
		htmlEntities.put("uArr", new Integer(8657));
		htmlEntities.put("rArr", new Integer(8658));
		htmlEntities.put("dArr", new Integer(8659));
		htmlEntities.put("hArr", new Integer(8660));
		htmlEntities.put("forall", new Integer(8704));
		htmlEntities.put("part", new Integer(8706));
		htmlEntities.put("exist", new Integer(8707));
		htmlEntities.put("empty", new Integer(8709));
		htmlEntities.put("nabla", new Integer(8711));
		htmlEntities.put("isin", new Integer(8712));
		htmlEntities.put("notin", new Integer(8713));
		htmlEntities.put("ni", new Integer(8715));
		htmlEntities.put("prod", new Integer(8719));
		htmlEntities.put("sum", new Integer(8721));
		htmlEntities.put("minus", new Integer(8722));
		htmlEntities.put("lowast", new Integer(8727));
		htmlEntities.put("radic", new Integer(8730));
		htmlEntities.put("prop", new Integer(8733));
		htmlEntities.put("infin", new Integer(8734));
		htmlEntities.put("ang", new Integer(8736));
		htmlEntities.put("and", new Integer(8743));
		htmlEntities.put("or", new Integer(8744));
		htmlEntities.put("cap", new Integer(8745));
		htmlEntities.put("cup", new Integer(8746));
		htmlEntities.put("int", new Integer(8747));
		htmlEntities.put("there4", new Integer(8756));
		htmlEntities.put("sim", new Integer(8764));
		htmlEntities.put("cong", new Integer(8773));
		htmlEntities.put("asymp", new Integer(8776));
		htmlEntities.put("ne", new Integer(8800));
		htmlEntities.put("equiv", new Integer(8801));
		htmlEntities.put("le", new Integer(8804));
		htmlEntities.put("ge", new Integer(8805));
		htmlEntities.put("sub", new Integer(8834));
		htmlEntities.put("sup", new Integer(8835));
		htmlEntities.put("nsub", new Integer(8836));
		htmlEntities.put("sube", new Integer(8838));
		htmlEntities.put("supe", new Integer(8839));
		htmlEntities.put("oplus", new Integer(8853));
		htmlEntities.put("otimes", new Integer(8855));
		htmlEntities.put("perp", new Integer(8869));
		htmlEntities.put("sdot", new Integer(8901));
		htmlEntities.put("lceil", new Integer(8968));
		htmlEntities.put("rceil", new Integer(8969));
		htmlEntities.put("lfloor", new Integer(8970));
		htmlEntities.put("rfloor", new Integer(8971));
		htmlEntities.put("lang", new Integer(9001));
		htmlEntities.put("rang", new Integer(9002));
		htmlEntities.put("loz", new Integer(9674));
		htmlEntities.put("spades", new Integer(9824));
		htmlEntities.put("clubs", new Integer(9827));
		htmlEntities.put("hearts", new Integer(9829));
		htmlEntities.put("diams", new Integer(9830));
		htmlEntities.put("quot", new Integer(34));
		htmlEntities.put("amp", new Integer(38));
		htmlEntities.put("lt", new Integer(60));
		htmlEntities.put("gt", new Integer(62));
		htmlEntities.put("OElig", new Integer(338));
		htmlEntities.put("oelig", new Integer(339));
		htmlEntities.put("Scaron", new Integer(352));
		htmlEntities.put("scaron", new Integer(353));
		htmlEntities.put("Yuml", new Integer(376));
		htmlEntities.put("circ", new Integer(710));
		htmlEntities.put("tilde", new Integer(732));
		htmlEntities.put("ensp", new Integer(8194));
		htmlEntities.put("emsp", new Integer(8195));
		htmlEntities.put("thinsp", new Integer(8201));
		htmlEntities.put("zwnj", new Integer(8204));
		htmlEntities.put("zwj", new Integer(8205));
		htmlEntities.put("lrm", new Integer(8206));
		htmlEntities.put("rlm", new Integer(8207));
		htmlEntities.put("ndash", new Integer(8211));
		htmlEntities.put("mdash", new Integer(8212));
		htmlEntities.put("lsquo", new Integer(8216));
		htmlEntities.put("rsquo", new Integer(8217));
		htmlEntities.put("sbquo", new Integer(8218));
		htmlEntities.put("ldquo", new Integer(8220));
		htmlEntities.put("rdquo", new Integer(8221));
		htmlEntities.put("bdquo", new Integer(8222));
		htmlEntities.put("dagger", new Integer(8224));
		htmlEntities.put("Dagger", new Integer(8225));
		htmlEntities.put("permil", new Integer(8240));
		htmlEntities.put("lsaquo", new Integer(8249));
		htmlEntities.put("rsaquo", new Integer(8250));
		htmlEntities.put("euro", new Integer(8364));
	}

	/**
	 * Turn any HTML escape entities in the string into
	 * characters and return the resulting string.
	 *
	 * @param s String to be unescaped.
	 * @return unescaped String.
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static String unescapeHTML(String s){
		StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0){
			int nextAmp = s.indexOf("&", ampInd+1);
			int nextSemi = s.indexOf(";", ampInd+1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)){
				int value = -1;
				String escape = s.substring(ampInd+1,nextSemi);
				try {
					if (escape.startsWith("#")){
						value = Integer.parseInt(escape.substring(1), 10);
					} else {
						if (htmlEntities.containsKey(escape)){
							value = ((Integer)(htmlEntities.get(escape))).intValue();
						}
					}
				} catch (NumberFormatException x){
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff){
					result.append((char)value);
				} else {
					result.append("&").append(escape).append(";");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));
		return result.toString();
	}

	/**
	 * Escapes characters that have special meaning to
	 * regular expressions
	 *
	 * @param s String to be escaped
	 * @return escaped String
	 * @throws NullPointerException if s is null.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static String escapeRegularExpressionLiteral(String s){
		// According to the documentation in the Pattern class:
		//
		// The backslash character ('\') serves to introduce escaped constructs,
		// as defined in the table above, as well as to quote characters that
		// otherwise would be interpreted as unescaped constructs. Thus the
		// expression \\ matches a single backslash and \{ matches a left brace.
		//
		// It is an error to use a backslash prior to any alphabetic character
		// that does not denote an escaped construct; these are reserved for future
		// extensions to the regular-expression language. A backslash may be used
		// prior to a non-alphabetic character regardless of whether that character
		// is part of an unescaped construct.
		//
		// As a result, escape everything except [0-9a-zA-Z]

		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			if (!((c>='0' && c<='9') || (c>='A' && c<='Z') || (c>='a' && c<='z'))){
				newLength += 1;
			}
		}
		if (length == newLength){
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i=0; i<length; i++){
			char c = s.charAt(i);
			if (!((c>='0' && c<='9') || (c>='A' && c<='Z') || (c>='a' && c<='z'))){
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Build a regular expression that is each of the terms or'd together.
	 *
	 * @param terms a list of search terms.
	 * @param sb place to build the regular expression.
	 * @throws IllegalArgumentException if the length of terms is zero.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	private static void buildFindAnyPattern(String[] terms, StringBuffer sb){
		if (terms.length == 0) throw new IllegalArgumentException("There must be at least one term to find.");
		sb.append("(?:");
		for (int i=0; i<terms.length; i++){
			if (i>0) sb.append("|");
			sb.append("(?:");
			sb.append(escapeRegularExpressionLiteral(terms[i]));
			sb.append(")");
		}
		sb.append(")");
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * contains any of the given terms.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getContainsAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it contains any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static Pattern getContainsAnyPattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?s).*");
		buildFindAnyPattern(terms, sb);
		sb.append(".*");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * equals any of the given terms.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getEqualsAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it equals any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	 public static Pattern getEqualsAnyPattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?s)\\A");
		buildFindAnyPattern(terms, sb);
		sb.append("\\z");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * starts with any of the given terms.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getStartsWithAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it starts with any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	 public static Pattern getStartsWithAnyPattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?s)\\A");
		buildFindAnyPattern(terms, sb);
		sb.append(".*");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * ends with any of the given terms.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getEndsWithAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it ends with any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static Pattern getEndsWithAnyPattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?s).*");
		buildFindAnyPattern(terms, sb);
		sb.append("\\z");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * contains any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getContainsAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it contains any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static Pattern getContainsAnyIgnoreCasePattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?i)(?u)(?s).*");
		buildFindAnyPattern(terms, sb);
		sb.append(".*");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * equals any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getEqualsAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it equals any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	 public static Pattern getEqualsAnyIgnoreCasePattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?i)(?u)(?s)\\A");
		buildFindAnyPattern(terms, sb);
		sb.append("\\z");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * starts with any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getStartsWithAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it starts with any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	 public static Pattern getStartsWithAnyIgnoreCasePattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?i)(?u)(?s)\\A");
		buildFindAnyPattern(terms, sb);
		sb.append(".*");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Compile a pattern that can will match a string if the string
	 * ends with any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * Usage:<br>
	 * <code>boolean b = getEndsWithAnyPattern(terms).matcher(s).matches();</code>
	 * <p>
	 * If multiple strings are matched against the same set of terms,
	 * it is more efficient to reuse the pattern returned by this function.
	 *
	 * @param terms Array of search strings.
	 * @return Compiled pattern that can be used to match a string to see if it ends with any of the terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static Pattern getEndsWithAnyIgnoreCasePattern(String[] terms){
		StringBuffer sb = new StringBuffer();
		sb.append("(?i)(?u)(?s).*");
		buildFindAnyPattern(terms, sb);
		sb.append("\\z");
		return Pattern.compile(sb.toString());
	}

	/**
	 * Tests to see if the given string contains any of the given terms.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getContainsAnyPattern(String[])
	 *
	 * @param s String that may contain any of the given terms.
	 * @param terms list of substrings that may be contained in the given string.
	 * @return true iff one of the terms is a substring of the given string.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean containsAny(String s, String[] terms){
		return getContainsAnyPattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string equals any of the given terms.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getEqualsAnyPattern(String[])
	 *
	 * @param s String that may equal any of the given terms.
	 * @param terms list of strings that may equal the given string.
	 * @return true iff one of the terms is equal to the given string.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean equalsAny(String s, String[] terms){
		return getEqualsAnyPattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string starts with any of the given terms.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getStartsWithAnyPattern(String[])
	 *
	 * @param s String that may start with any of the given terms.
	 * @param terms list of strings that may start with the given string.
	 * @return true iff the given string starts with one of the given terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean startsWithAny(String s, String[] terms){
		return getStartsWithAnyPattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string ends with any of the given terms.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getEndsWithAnyPattern(String[])
	 *
	 * @param s String that may end with any of the given terms.
	 * @param terms list of strings that may end with the given string.
	 * @return true iff the given string ends with one of the given terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean endsWithAny(String s, String[] terms){
		return getEndsWithAnyPattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string contains any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getContainsAnyIgnoreCasePattern(String[])
	 *
	 * @param s String that may contain any of the given terms.
	 * @param terms list of substrings that may be contained in the given string.
	 * @return true iff one of the terms is a substring of the given string.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean containsAnyIgnoreCase(String s, String[] terms){
		return getContainsAnyIgnoreCasePattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string equals any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getEqualsAnyIgnoreCasePattern(String[])
	 *
	 * @param s String that may equal any of the given terms.
	 * @param terms list of strings that may equal the given string.
	 * @return true iff one of the terms is equal to the given string.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean equalsAnyIgnoreCase(String s, String[] terms){
		return getEqualsAnyIgnoreCasePattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string starts with any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getStartsWithAnyIgnoreCasePattern(String[])
	 *
	 * @param s String that may start with any of the given terms.
	 * @param terms list of strings that may start with the given string.
	 * @return true iff the given string starts with one of the given terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean startsWithAnyIgnoreCase(String s, String[] terms){
		return getStartsWithAnyIgnoreCasePattern(terms).matcher(s).matches();
	}

	/**
	 * Tests to see if the given string ends with any of the given terms.
	 * <p>
	 * Case is ignored when matching using Unicode case rules.
	 * <p>
	 * This implementation is more efficient than the brute force approach
	 * of testing the string against each of the terms.  It instead compiles
	 * a single regular expression that can test all the terms at once, and
	 * uses that expression against the string.
	 * <p>
	 * This is a convenience method.  If multiple strings are tested against
	 * the same set of terms, it is more efficient not to compile the regular
	 * expression multiple times.
	 * @see #getEndsWithAnyIgnoreCasePattern(String[])
	 *
	 * @param s String that may end with any of the given terms.
	 * @param terms list of strings that may end with the given string.
	 * @return true iff the given string ends with one of the given terms.
	 *
	 * @since ostermillerutils 1.02.25
	 */
	public static boolean endsWithAnyIgnoreCase(String s, String[] terms){
		return getEndsWithAnyIgnoreCasePattern(terms).matcher(s).matches();
	}
}
