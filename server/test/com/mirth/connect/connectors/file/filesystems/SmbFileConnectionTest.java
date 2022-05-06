package com.mirth.connect.connectors.file.filesystems;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.SmbSchemeProperties;

import jcifs.CIFSContext;
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
    
    /*
     * Tests that when an SmbFileConnection is destroyed, the associated CIFSContext is closed, which allows the CIFSContext to be deallocated.
     */
    @Test
    public void testContextIsClosedOnDestroy() throws Exception {
        SmbSchemeProperties schemeProperties = new SmbSchemeProperties();
        FileSystemConnectionOptions fileSystemOptions = new FileSystemConnectionOptions(false, "localhost/testuser", "testpassword", schemeProperties);
        TestSmbFileConnection smbFileConnection = new TestSmbFileConnection("host1", fileSystemOptions, 1000);
        
        smbFileConnection.destroy();
        
        verify(smbFileConnection.getShareContext(), times(1)).close();
    }
    
    private class TestSmbFileConnection extends SmbFileConnection {
        
        private CIFSContext shareContext = mock(CIFSContext.class);

        public TestSmbFileConnection(String share, FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
            super(share, fileSystemOptions, timeout);
            
            SmbFile smbFileShare = mock(SmbFile.class);
            when(smbFileShare.getContext()).thenReturn(shareContext);
            setShare(smbFileShare);
        }
        
        public CIFSContext getShareContext() {
            return shareContext;
        }
        
    }
}
