/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.util;

import java.util.Formatter;
import java.util.Locale;

public class DisplayUtil {

	/**
	 * This returns a formatted string that shows the number based on the locale
	 * turns 1000 into 1,000
	 * @param number
	 * @return
	 */
	public static String formatNumber(int number) {
		StringBuilder str = new StringBuilder(); 
		Formatter f = new Formatter(str, Locale.getDefault()); 
		f.format("%,d", number); 
		return str.toString();
	}
	
	/**
     * This returns a formatted string that shows the number based on the locale
     * turns 1000 into 1,000
     * @param number
     * @return
     */
    public static String formatNumber(long number) {
        StringBuilder str = new StringBuilder(); 
        Formatter f = new Formatter(str, Locale.getDefault()); 
        f.format("%,d", number); 
        return str.toString();
    }
	
	/**
	 * Formats a number according to the locale
	 * turns 1000.0 to 1,000
	 * @param number
	 * @return
	 */
	public static String formatNumber(float number) {
		StringBuilder str = new StringBuilder(); 
		Formatter f = new Formatter(str, Locale.getDefault()); 
		f.format("%,.0f", number); 
		return str.toString();
    }

}
