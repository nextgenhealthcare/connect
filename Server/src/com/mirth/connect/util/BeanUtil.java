/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

public class BeanUtil {
    private static Logger logger = Logger.getLogger(BeanUtil.class);

    public static void setProperties(Object bean, Map<String, String> properties) {
        for (Entry<String, String> entry : properties.entrySet()) {
            try {
                BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
            } catch (Exception e) {
                logger.error("Failed to set object property '" + entry.getKey() + "'", e);
            }
        }
    }

    public static void setPropertiesQuietly(Object bean, Map<String, String> properties) {
        for (Entry<String, String> entry : properties.entrySet()) {
            try {
                BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
            } catch (Exception e) {
            }
        }
    }
}
