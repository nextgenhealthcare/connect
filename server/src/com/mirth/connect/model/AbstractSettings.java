/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class AbstractSettings {

    public abstract Properties getProperties();

    public abstract void setProperties(Properties properties);

    /**
     * Takes a String and returns a Boolean Object.
     * "1" = true
     * "0" = false
     * null or not a number = null
     * 
     * @param str
     * @return
     */
    protected Boolean intToBooleanObject(String str) {
        return intToBooleanObject(str, null);
    }
    
    /**
     * Takes a String and returns a Boolean Object.
     * "1" = true
     * "0" = false
     * null or not a number = defaultValue
     * 
     * @param str
     * @param defaultValue
     * @return
     */
    protected Boolean intToBooleanObject(String str, Boolean defaultValue) {
        int i = NumberUtils.toInt(str, -1);
        
        if (i == -1) {
            // Must return null explicitly to avoid Java NPE due to autoboxing
            if (defaultValue == null) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            return BooleanUtils.toBooleanObject(i);
        }
    }

    /**
     * Takes a String and returns an Integer Object.
     * "1" = 1
     * null or not a number = null
     * 
     * @param str
     * @return
     */
    protected Integer toIntegerObject(String str) {
        return toIntegerObject(str, null);
    }
    
    /**
     * Takes a String and returns an Integer Object.
     * "1" = 1
     * null or not a number = defaultValue
     * 
     * @param str
     * @param defaultValue
     * @return
     */
    protected Integer toIntegerObject(String str, Integer defaultValue) {
        int i = NumberUtils.toInt(str, -1);
        
        if (i == -1) {
            // Must return null explicitly to avoid Java NPE due to autoboxing
            if (defaultValue == null) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            return i;
        }
    }
    
    /**
     * Takes a String and returns a Long Object.
     * "1" = 1
     * null or not a number = null
     * 
     * @param str
     * @return
     */
    protected Long toLongObject(String str) {
        return toLongObject(str, null);
    }
    
    /**
     * Takes a String and returns a Long Object.
     * "1" = 1
     * null or not a number = defaultValue
     * 
     * @param str
     * @param defaultValue
     * @return
     */
    protected Long toLongObject(String str, Long defaultValue) {
        long i = NumberUtils.toLong(str, -1);
        
        if (i == -1) {
            // Must return null explicitly to avoid Java NPE due to autoboxing
            if (defaultValue == null) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            return i;
        }
    }
}
