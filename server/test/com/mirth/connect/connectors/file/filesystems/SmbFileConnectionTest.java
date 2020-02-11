package com.mirth.connect.connectors.file.filesystems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.SmbSchemeProperties;

import jcifs.DialectVersion;
import jcifs.config.PropertyConfiguration;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;

public class SmbFileConnectionTest {

    @Test
    public void testShareConfiguration() throws Exception {
        SmbSchemeProperties schemeProperties = new SmbSchemeProperties();
        schemeProperties.setSmbMinVersion(DialectVersion.SMB1.toString());
        schemeProperties.setSmbMaxVersion(DialectVersion.SMB1.toString());
        FileSystemConnectionOptions fileSystemOptions = new FileSystemConnectionOptions(false, "localhost/testuser", "testpassword", schemeProperties);
        SmbFileConnection smbFileConnection = new SmbFileConnection("host1", fileSystemOptions, 1000);
        SmbFile smbFile = smbFileConnection.getShare();
        PropertyConfiguration config = (PropertyConfiguration) smbFile.getContext().getConfig();
        assertEquals(3, config.getLanManCompatibility());
        assertEquals(DialectVersion.SMB1, config.getMinimumVersion());
        assertEquals(DialectVersion.SMB1, config.getMaximumVersion());
        assertEquals("localhost", smbFile.getContext().getCredentials().getUserDomain());
        
        schemeProperties = new SmbSchemeProperties();
        schemeProperties.setSmbMinVersion(DialectVersion.SMB1.toString());
        schemeProperties.setSmbMaxVersion(DialectVersion.SMB202.toString());
        fileSystemOptions = new FileSystemConnectionOptions(false, "testuser", "testpassword", schemeProperties);
        smbFileConnection = new SmbFileConnection("host1", fileSystemOptions, 1000);
        smbFile = smbFileConnection.getShare();
        config = (PropertyConfiguration) smbFile.getContext().getConfig();
        assertEquals(3, config.getLanManCompatibility());
        assertEquals(DialectVersion.SMB1, config.getMinimumVersion());
        assertEquals(DialectVersion.SMB202, config.getMaximumVersion());
    }
    
    @Test
    public void testGetPath() throws Exception {
        SmbSchemeProperties schemeProperties = new SmbSchemeProperties();
        FileSystemConnectionOptions fileSystemOptions = new FileSystemConnectionOptions(false, "testuser", "testpassword", schemeProperties);
        SmbFileConnection smbFileConnection = new SmbFileConnection("host1", fileSystemOptions, 1000);
        
        assertEquals("smb://host1/dir1/", smbFileConnection.getPath("/dir1", null));
        assertEquals("smb://host1/dir1/", smbFileConnection.getPath("/dir1/", null));
        assertEquals("smb://host2/dir1/", smbFileConnection.getPath("smb://host2/dir1", null));
        assertEquals("smb://host2/dir1/", smbFileConnection.getPath("smb://host2/dir1/", null));
        assertEquals("smb://host1/dir1/name1", smbFileConnection.getPath("/dir1", "name1"));
        assertEquals("smb://host1/dir1/name1", smbFileConnection.getPath("/dir1", "/name1"));
        assertEquals("smb://host1/dir1/name1", smbFileConnection.getPath("/dir1/", "name1"));
        assertEquals("smb://host1/dir1/name1", smbFileConnection.getPath("/dir1/", "/name1"));
        assertEquals("smb://host2/dir1/name1", smbFileConnection.getPath("smb://host2/dir1", "name1"));
        assertEquals("smb://host2/dir1/name1", smbFileConnection.getPath("smb://host2/dir1", "/name1"));
        assertEquals("smb://host2/dir1/name1", smbFileConnection.getPath("smb://host2/dir1/", "name1"));
        assertEquals("smb://host2/dir1/name1", smbFileConnection.getPath("smb://host2/dir1/", "/name1"));
        assertEquals("smb://host1/dir1/dir2/dir3/name1", smbFileConnection.getPath("/dir1/dir2/dir3", "name1"));
        
        // dir does not start with /
        assertEquals("smb://host1/dir1/name1", smbFileConnection.getPath("dir1", "name1"));
        
        // dir is null
        assertEquals("smb://host1/name1", smbFileConnection.getPath(null, "name1"));
    }
    
    @Test
    public void testGetSmbFile() throws Exception {
    	 SmbSchemeProperties schemeProperties = new SmbSchemeProperties();
         FileSystemConnectionOptions fileSystemOptions = new FileSystemConnectionOptions(false, "localhost/testuser", "testpassword", schemeProperties);
         SmbFileConnection smbFileConnection = new SmbFileConnection("host1", fileSystemOptions, 1000);
         SmbFile share = smbFileConnection.getShare();
         
         SmbFile smbFile = smbFileConnection.getSmbFile(share, "smb://host1");
    	 NtlmPasswordAuthenticator auth = (NtlmPasswordAuthenticator) smbFile.getContext().getCredentials();
    	 
    	 assertEquals("localhost", auth.getUserDomain());
    	 assertEquals("testuser", auth.getUsername());
    	 assertEquals("testpassword", auth.getPassword());
    }
}
