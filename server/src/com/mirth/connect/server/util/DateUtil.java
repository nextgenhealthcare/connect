/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static Date getDate(String pattern, String date) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.parse(date);
    }

    public static String formatDate(String pattern, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static String getCurrentDate(String pattern) {
        return formatDate(pattern, new Date());
    }

    public static String convertDate(String inPattern, String outPattern, String date) throws Exception {
        Date newDate = getDate(inPattern, date);
        return formatDate(outPattern, newDate);
    }
}
