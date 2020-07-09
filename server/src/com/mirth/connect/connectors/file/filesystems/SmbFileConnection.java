/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.RuntimeCIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFilenameFilter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.SmbSchemeProperties;
import com.mirth.connect.connectors.file.filters.SmbFilenameWildcardFilter;

/**
 * The SmbFileSystemConnection class for networked files
 * 
 */
public class SmbFileConnection implements FileSystemConnection {
    public class SmbFileFileInfo implements FileInfo {
        private SmbFile theFile;

        public SmbFileFileInfo(SmbFile theFile) {
            this.theFile = theFile;
        }

        public String getName() {
            return this.theFile.getName();
        }

        public String getAbsolutePath() {
            return this.theFile.getPath();
        }

        public String getCanonicalPath() throws IOException {
            return this.theFile.getCanonicalPath();
        }

        public String getParent() {
            return this.theFile.getParent();
        }

        public long getSize() {
            try {
                return this.theFile.length();
            } catch (SmbException e) {
                return 0;
            }
        }

        public long getLastModified() {
            return this.theFile.getLastModified();
        }

        public boolean isDirectory() {
            try {
                return this.theFile.isDirectory();
            } catch (SmbException e) {
                return false;
            }
        }

        public boolean isFile() {
            try {
                return this.theFile.isFile();
            } catch (SmbException e) {
                return false;
            }
        }

        public boolean isReadable() {
            try {
                return this.theFile.canRead();
            } catch (SmbException e) {
                return false;
            }
        }

        @Override
        public void populateSourceMap(Map<String, Object> sourceMap) {}
    }

    private Logger logger = Logger.getLogger(getClass());
    private NtlmPasswordAuthenticator auth = null;
    private SmbFile share = null;
    private int timeout;
    private final AtomicBoolean valid = new AtomicBoolean();

    public SmbFileConnection(String share, FileSystemConnectionOptions fileSystemOptions, int timeout) throws Exception {
        String domainAndUser = fileSystemOptions.getUsername();
        String password = fileSystemOptions.getPassword();

        String[] params = Pattern.compile("[\\\\|/|@|:|;]").split(domainAndUser);
        String domain = null;
        String username = null;

        if (params.length > 1) {
            domain = params[0];
            username = params[1];
        } else {
            username = params[0];
        }

        if ((username != null) && (password != null)) {
            auth = new NtlmPasswordAuthenticator(domain, username, password);
        }
        
        String minVersion = ((SmbSchemeProperties) fileSystemOptions.getSchemeProperties()).getSmbMinVersion();
        String maxVersion = ((SmbSchemeProperties) fileSystemOptions.getSchemeProperties()).getSmbMaxVersion();
        
        Properties jcifsProperties = new Properties();
        jcifsProperties.setProperty("jcifs.smb.client.minVersion", minVersion);
        jcifsProperties.setProperty("jcifs.smb.client.maxVersion", maxVersion);
        jcifsProperties.setProperty("jcifs.smb.client.useExtendedSecurity", "true");
        jcifsProperties.setProperty("jcifs.smb.lmCompatibility", "3");
        
        
        CIFSContext baseContext = new BaseContext(new PropertyConfiguration(jcifsProperties)).withCredentials(auth);
        this.share = new SmbFile("smb://" + share, baseContext);
        this.share.setConnectTimeout(timeout);
        this.timeout = timeout;
        this.valid.set(true);
    }
    
    protected SmbFile getShare() {
        return share;
    }

    protected String getPath(String dir, String name) {
    	if (dir == null) {
    		dir = "";
    	}
    	
        if (dir.endsWith("/")) {
            dir = dir.substring(0, dir.length() - 1);
        }
        
        if (!dir.startsWith("smb://")) {
        	if (dir.length() < 1) {
        		dir = share.getURL().toString();
        	} else if (dir.startsWith("/")) {
        		dir = share.getURL().toString() + dir;
        	} else {
        		dir = share.getURL().toString() + "/" + dir;
        	}
        }
        
        if (name != null) {
            if (name.startsWith("/")) {
                name = name.substring(1, name.length());
            }
            return dir + "/" + name;
        } else {
            return dir + "/";
        }
    }
    
    protected SmbFile getSmbFile(SmbFile share, String name) throws Exception {
        CIFSContext context = share.getContext();
        SmbFile smbFile = new SmbFile(name, context);
        smbFile.setConnectTimeout(timeout);
        return smbFile;
    }

    @Override
    public List<FileInfo> listFiles(String dir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        SmbFile readDirectory = null;
        SmbFilenameFilter filenameFilter = new SmbFilenameWildcardFilter(filenamePattern, isRegex);

        try {
            readDirectory = getSmbFile(share, getPath(dir, null));
        } catch (Exception e) {
            throw new FileConnectorException("Directory does not exist: " + dir, e);
        }

        try {
            SmbFile[] todoFiles = readDirectory.listFiles(filenameFilter);

            if (todoFiles == null) {
                return new ArrayList<FileInfo>();
            } else {
                List<FileInfo> result = new ArrayList<FileInfo>(todoFiles.length);

                for (SmbFile f : todoFiles) {
                    if (f.isFile() && !(ignoreDot && f.getName().startsWith("."))) {
                        result.add(new SmbFileFileInfo(f));
                    }
                }

                return result;
            }
        } catch (Exception e) {
            throw new FileConnectorException("Error listing files in dir [" + dir + "] for pattern [" + filenamePattern + "]", e);
        }
    }

    @Override
    public List<String> listDirectories(String fromDir) throws Exception {
        List<String> directories = new ArrayList<String>();
        SmbFile readDirectory = null;

        try {
            readDirectory = getSmbFile(share, getPath(fromDir, null));
        } catch (Exception e) {
            throw new FileConnectorException("Directory does not exist: " + fromDir, e);
        }

        SmbFileFilter fileFilter = new SmbFileFilter() {
            @Override
            public boolean accept(SmbFile file) throws SmbException {
                return file.isDirectory();
            }
        };

        for (SmbFile directory : readDirectory.listFiles(fileFilter)) {
            directories.add(directory.getCanonicalPath());
        }

        return directories;
    }

    @Override
    public boolean exists(String file, String path) {
        try {
            return getSmbFile(share, getPath(path, file)).exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canRead(String readDir) {
        try {
            return getSmbFile(share, getPath(readDir, null)).canRead();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canWrite(String writeDir) {
        OutputStream os = null;
        SmbFile dst = null;

        // MIRTH-1113: No longer using jcif's canWrite() method since it's pretty much useless.
        // It only checks the "read only" file attribute instead of a file's ACL. 
        // The only way to tell if you can write to a file is to actually try and open 
        // it for writing with new SmbFileOutputStream.
        try {
            dst = getSmbFile(share, getPath(writeDir, UUID.randomUUID().toString()));
            os = new SmbFileOutputStream(dst, false);

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (os != null) {
                IOUtils.closeQuietly(os);
            }

            try {
                // Delete the temp file.
                if (dst.exists()) {
                    dst.delete();
                }
            } catch (Exception e) {
                logger.warn("Failed to delete test write file.", e);
            }
        }
    }

    @Override
    public InputStream readFile(String name, String dir, Map<String, Object> sourceMap) throws FileConnectorException {
        SmbFile src = null;

        try {
            src = getSmbFile(share, getPath(dir, name));
            return new SmbFileInputStream(src);
        } catch (Exception e) {
            throw new FileConnectorException("Error reading file: " + src.getPath(), e);
        }
    }

    /** Must be called after readFile when reading is complete */
    @Override
    public void closeReadFile() throws Exception {
        // nothing
    }

    @Override
    public boolean canAppend() {
        return true;
    }

    @Override
    public void writeFile(String name, String dir, boolean append, InputStream is, long contentLength, Map<String, Object> connectorMap) throws Exception {
        OutputStream os = null;
        SmbFile dst = null;
        SmbFile dstDir = null;

        try {
            dstDir = getSmbFile(share, getPath(dir, null));

            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }

            dst = getSmbFile(share, getPath(dir, name));
            os = new SmbFileOutputStream(dst, append);

            IOUtils.copy(is, os);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public void delete(String name, String dir, boolean mayNotExist) throws FileConnectorException {
        SmbFile src = null;

        try {
            src = getSmbFile(share, getPath(dir, name));
            src.delete();

            if (src.exists()) {
                if (!mayNotExist) {
                    throw new FileConnectorException("Source file was deleted, should not exist: " + src.getPath());
                }
            }
        } catch (Exception e) {
            throw new FileConnectorException("Error deleting file: " + src.getPath(), e);
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws FileConnectorException {
        SmbFile src = null;
        SmbFile dst = null;
        SmbFile dstDir = null;

        try {
            src = getSmbFile(share, getPath(fromDir, fromName));
            dstDir = getSmbFile(share, getPath(toDir, null));

            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }

            dst = getSmbFile(share, getPath(toDir, toName));

            try {
                dst.delete();
            } catch (Exception e) {
                // ignore if file alread doesn't exist
            }

            src.renameTo(dst);
        } catch (Exception e) {
            throw new FileConnectorException("Error moving file from [" + src.getPath() + "] to [" + dst.getPath() + "]", e);
        }
    }

    @Override
    public boolean isConnected() {
        return valid.get();
    }

    @Override
    public void disconnect() {}

    @Override
    public void activate() {}

    @Override
    public void passivate() {}

    @Override
    public void destroy () {
        if(valid.compareAndSet(true, false)) {
            try {
                share.getContext().close();
            } catch (CIFSException e) {
                throw new RuntimeCIFSException(e);
            }
        }
    }

    @Override
    public boolean isValid() {
        return valid.get();
    }
}