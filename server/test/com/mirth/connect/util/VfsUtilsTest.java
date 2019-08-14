package com.mirth.connect.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VfsUtilsTest {
    @Test(expected = NullPointerException.class)
    public void testPathToUriNull() {
        VfsUtils.pathToUri(null);
    }

    @Test
    public void testPathToUriBlank() {
        assertEquals("", VfsUtils.pathToUri(""));
    }

    @Test
    public void testTarGz() {
        assertEquals("tgz://test.tar.gz", VfsUtils.pathToUri("test.tar.gz"));
    }

    @Test
    public void testZip() {
        assertEquals("zip://test.zip", VfsUtils.pathToUri("test.zip"));
    }

    @Test
    public void testBzip2() {
        assertEquals("tbz2://test.tar.bz2", VfsUtils.pathToUri("test.tar.bz2"));
    }

    @Test
    public void testGzipNoTar() {
        assertEquals("test.gz", VfsUtils.pathToUri("test.gz"));
    }

    @Test
    public void testBzip2NoTar() {
        assertEquals("test.bz2", VfsUtils.pathToUri("test.bz2"));
    }

    @Test
    public void testTar() {
        assertEquals("tar://test.tar", VfsUtils.pathToUri("test.tar"));
    }
}
