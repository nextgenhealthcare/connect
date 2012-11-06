/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DateParser {
    // @formatter:off
    final private static String[] DEFAULT_PATTERNS = new String[] { 
        "yyyy-MM-dd HH:mm:ss",
        "EEE MMM dd HH:mm:ss zzz yyyy",
        "EEE MMM dd zzz yyyy",
        "yyyy-MM-dd",
        "yyyy MM dd",
        "yyyy.MM.dd",
        "MM-dd-yyyy",
        "MM dd yyyy",
        "MM.dd.yyyy",
        "HH:mm:ss",
        "yyyyMMddHHmm",
        "yyyyMMddHHmmss",};
    // @formatter:on

    private List<String> patterns = Arrays.asList(DEFAULT_PATTERNS);

    public void addPattern(String pattern) {
        patterns.add(pattern);
    }

    public Calendar parse(String dateString) throws DateParserException {
        Calendar date = Calendar.getInstance();

        for (String pattern : patterns) {
            SimpleDateFormat format = new SimpleDateFormat(pattern);

            try {
                date.setTime(format.parse(dateString));
                return date;
            } catch (ParseException e) {
            }
        }

        throw new DateParserException("Unrecognized date format");
    }

    public class DateParserException extends Exception {
        public DateParserException(String message) {
            super(message);
        }
    }
}
