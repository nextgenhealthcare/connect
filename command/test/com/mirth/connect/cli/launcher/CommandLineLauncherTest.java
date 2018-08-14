package com.mirth.connect.cli.launcher;

import java.io.File;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandLineLauncherTest {

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
