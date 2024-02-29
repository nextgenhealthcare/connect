/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class MirthTestUtil {

    public static void assertMapEquals(Map<?, ?> o1, Map<?, ?> o2) {
        if (o1 != null || o2 != null) {
            if (o1 == null || o2 == null || o1.size() != o2.size()) {
                fail();
            }
            for (Entry<?, ?> entry : o1.entrySet()) {
                if (!o2.containsKey(entry.getKey()) || !EqualsBuilder.reflectionEquals(entry.getValue(), o2.get(entry.getKey()))) {
                    fail();
                }
            }
        }
    }
}