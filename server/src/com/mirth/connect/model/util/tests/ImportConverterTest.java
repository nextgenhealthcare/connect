package com.mirth.connect.model.util.tests;

import junit.framework.TestCase;

import com.mirth.connect.model.util.ImportConverter;

public class ImportConverterTest extends TestCase {
    public void testCompareVersions() {
        assertEquals(1, ImportConverter.compareVersions("5", "4"));
        assertEquals(-1, ImportConverter.compareVersions("5", "6"));
        assertEquals(0, ImportConverter.compareVersions("3", "3"));

        assertEquals(1, ImportConverter.compareVersions("1.9", "1.8"));
        assertEquals(-1, ImportConverter.compareVersions("1.8", "1.9"));
        assertEquals(0, ImportConverter.compareVersions("1.8", "1.8"));

        assertEquals(1, ImportConverter.compareVersions("1.8.2", "1.8.0"));
        assertEquals(-1, ImportConverter.compareVersions("1.8.0", "1.8.2"));
        assertEquals(0, ImportConverter.compareVersions("1.8.2", "1.8.2"));

        assertEquals(1, ImportConverter.compareVersions("1.8.2", "1.8"));
        assertEquals(-1, ImportConverter.compareVersions("1.8", "1.8.2"));
    }

    public void testNormalizeVersion() {
        assertEquals("1.8", ImportConverter.normalizeVersion("1.8", -1));
        assertEquals("1.8", ImportConverter.normalizeVersion("1.8", 0));
        assertEquals("1.8", ImportConverter.normalizeVersion("1.8", 1));
        assertEquals("1.8", ImportConverter.normalizeVersion("1.8", 2));
        assertEquals("1.8.0", ImportConverter.normalizeVersion("1.8", 3));
        assertEquals("1.8.0.0", ImportConverter.normalizeVersion("1.8", 4));
    }

}
