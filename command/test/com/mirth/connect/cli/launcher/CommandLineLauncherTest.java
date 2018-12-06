/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.cli.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandLineLauncherTest {

    @BeforeClass
    public static void setup() {
        CommandLineLauncher.logger = Logger.getLogger(CommandLineLauncher.class);
    }

    @Test
    public void testLoadEmptyExtensionFolder() throws Exception {
        File f = new File("../command/tests/extensionsTest/emptyExtension");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(0, libs.size());
    }

    @Test
    public void testLoadExtensionWithZeroSharedLibs() throws Exception {
        File f = new File("../command/tests/extensionsTest/extensionWithNoSharedLibs");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(0, libs.size());
    }

    @Test
    public void testLoadExtensionWithLibsAtRoot() throws Exception {
        File f = new File("../command/tests/extensionsTest/extensionWithLibsAtRoot");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(1, libs.size());
        assertTrue(libs.contains("libAtRoot.jar"));
    }

    @Test
    public void testLoadExtensionWithLibsAtRootAndSubfolder() throws Exception {
        File f = new File("../command/tests/extensionsTest/extensionWithLibsAtRootAndSubfolder");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(2, libs.size());
        assertTrue(libs.contains("rootLib.jar"));
        assertTrue(libs.contains("libs/subdirLib.jar"));

    }

    @Test
    public void testLoadExtensionWithMultileSharedLibsFromDifferentXml() throws Exception {
        File f = new File("../command/tests/extensionsTest/extensionWithMultileSharedLibsFromDifferentXml");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(3, libs.size());
        assertTrue(libs.contains("libs/libFromSourceXml.jar"));
        assertTrue(libs.contains("libs/libFromDestinationXml.jar"));
        assertTrue(libs.contains("libs/libFromPluginXml.jar"));

    }

    @Test
    public void testLoadExtensionWithSameLibReferencedFromDifferentXml() throws Exception {
        File f = new File("../command/tests/extensionsTest/extensionWithSameLibReferencedFromDifferentXml");
        Set<String> libs = CommandLineLauncher.getSharedLibsForExtension(f);
        assertEquals(1, libs.size());
        assertTrue(libs.contains("libs/sameRef.jar"));
    }
}
