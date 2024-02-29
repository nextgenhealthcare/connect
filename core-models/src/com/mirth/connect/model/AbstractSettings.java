/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.donkey.util.xstream.SerializerException;

public abstract class AbstractSettings {

    public abstract Properties getProperties(Serializer serializer);

    public abstract void setProperties(Properties properties, Serializer serializer);

    /**
     * Takes a String and returns a Boolean Object. "1" = true "0" = false null or not a number =
     * null
     * 
     * @param str
     * @return
     */
    protected Boolean intToBooleanObject(String str) {
        return intToBooleanObject(str, null);
    }

    /**
     * Takes a String and returns a Boolean Object. "1" = true "0" = false null or not a number =
     * defaultValue
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
     * Takes a String and returns an Integer Object. "1" = 1 null or not a number = null
     * 
     * @param str
     * @return
     */
    protected Integer toIntegerObject(String str) {
        return toIntegerObject(str, null);
    }

    /**
     * Takes a String and returns an Integer Object. "1" = 1 null or not a number = defaultValue
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
     * Takes a String and returns a Long Object. "1" = 1 null or not a number = null
     * 
     * @param str
     * @return
     */
    protected Long toLongObject(String str) {
        return toLongObject(str, null);
    }

    /**
     * Takes a String and returns a Long Object. "1" = 1 null or not a number = defaultValue
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

    protected <T> T deserialize(String str, Serializer serializer, Class<T> expectedClass, T defaultValue) {
        if (str != null) {
            try {
                return serializer.deserialize(str, expectedClass);
            } catch (SerializerException e) {
            }
        }
        return defaultValue;
    }

    protected <T> List<T> toList(String str, Serializer serializer, Class<T> expectedListItemClass, List<T> defaultValue) {
        if (str != null) {
            try {
                return serializer.deserializeList(str, expectedListItemClass);
            } catch (SerializerException e) {
            }
        }
        return defaultValue;
    }
}
