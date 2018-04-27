/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides date/time utility methods.
 */
public class DateUtil {
    private DateUtil() {}

    /**
     * Parses a date string according to the specified pattern and returns a java.util.Date object.
     * 
     * @param pattern
     *            The SimpleDateFormat pattern to use (e.g. "yyyyMMddHHmmss").
     * @param date
     *            The date string to parse.
     * @return A java.util.Date object representing the parsed date.
     * @throws Exception
     *             If the pattern could not be parsed.
     */
    public static Date getDate(String pattern, String date) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.parse(date);
    }

    /**
     * Formats a java.util.Date object into a string according to a specified pattern.
     * 
     * @param pattern
     *            The SimpleDateFormat pattern to use (e.g. "yyyyMMddHHmmss").
     * @param date
     *            The java.util.Date object to format.
     * @return The formatted date string.
     */
    public static String formatDate(String pattern, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * Formats the current date into a string according to a specified pattern.
     * 
     * @param pattern
     *            The SimpleDateFormat pattern to use (e.g. "yyyyMMddHHmmss").
     * @return The current formatted date string.
     */
    public static String getCurrentDate(String pattern) {
        return formatDate(pattern, new Date());
    }

    /**
     * Parses a date string according to a specified input pattern, and formats the date back to a
     * string according to a specified output pattern.
     * 
     * @param inPattern
     *            The SimpleDateFormat pattern to use for parsing the inbound date string (e.g.
     *            "yyyyMMddHHmmss").
     * @param outPattern
     *            The SimpleDateFormat pattern to use for formatting the outbound date string (e.g.
     *            "yyyyMMddHHmmss").
     * @param date
     *            The date string to convert.
     * @return The converted date string.
     * @throws Exception
     *             If the pattern could not be parsed.
     */
    public static String convertDate(String inPattern, String outPattern, String date) throws Exception {
        Date newDate = getDate(inPattern, date);
        return formatDate(outPattern, newDate);
    }
}
