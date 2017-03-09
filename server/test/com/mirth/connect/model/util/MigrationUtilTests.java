/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

import junit.framework.TestCase;

import com.mirth.connect.util.MigrationUtil;

public class MigrationUtilTests extends TestCase {
    public void testCompareVersions() {
        assertEquals(1, MigrationUtil.compareVersions("5", "4"));
        assertEquals(-1, MigrationUtil.compareVersions("5", "6"));
        assertEquals(0, MigrationUtil.compareVersions("3", "3"));

        assertEquals(1, MigrationUtil.compareVersions("1.9", "1.8"));
        assertEquals(-1, MigrationUtil.compareVersions("1.8", "1.9"));
        assertEquals(0, MigrationUtil.compareVersions("1.8", "1.8"));

        assertEquals(1, MigrationUtil.compareVersions("1.8.2", "1.8.0"));
        assertEquals(-1, MigrationUtil.compareVersions("1.8.0", "1.8.2"));
        assertEquals(0, MigrationUtil.compareVersions("1.8.2", "1.8.2"));

        assertEquals(1, MigrationUtil.compareVersions("1.8.2", "1.8"));
        assertEquals(-1, MigrationUtil.compareVersions("1.8", "1.8.2"));
    }

    public void testNormalizeVersion() {
        assertEquals("1.8", MigrationUtil.normalizeVersion("1.8", -1));
        assertEquals("1.8", MigrationUtil.normalizeVersion("1.8", 0));
        assertEquals("1.8", MigrationUtil.normalizeVersion("1.8", 1));
        assertEquals("1.8", MigrationUtil.normalizeVersion("1.8", 2));
        assertEquals("1.8.0", MigrationUtil.normalizeVersion("1.8", 3));
        assertEquals("1.8.0.0", MigrationUtil.normalizeVersion("1.8", 4));
        assertEquals("1.8.0", MigrationUtil.normalizeVersion("1.8.0.0", 3));
    }

}
