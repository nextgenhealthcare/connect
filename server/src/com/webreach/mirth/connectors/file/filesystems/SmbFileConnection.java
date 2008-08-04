package com.webreach.mirth.connectors.file.filesystems;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbFilenameFilter;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;

import com.webreach.mirth.connectors.file.filters.SmbFilenameWildcardFilter;

/**
 * The SmbFileSystemConnection class for networked files
 * 
 * @author Gerald Bortis
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

        public String getParent() {
            return this.theFile.getParent();
        }

        public long getSize() {
            return this.theFile.getContentLength();
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
    }

    private NtlmPasswordAuthentication auth = null;
    private String share = null;

    public SmbFileConnection(String share, String domainAndUser, String password) {
        String[] params = Pattern.compile("[\\|/|@|:|;]").split(domainAndUser);
        String domain = null;
        String username = null;

        if (params.length > 1) {
            domain = params[0];
            username = params[1];
        } else {
            username = params[0];
        }

        if ((username != null) && (password != null)) {
            auth = new NtlmPasswordAuthentication(domain, username, password);
        }

        this.share = "smb://" + share;
    }

    private String getPath(String dir, String name) {
        if (name != null) {
            return dir + "/" + name;
        } else {
            return dir + "/";
        }
    }

    private SmbFile getSmbFile(String context, String name, NtlmPasswordAuthentication auth) throws Exception {
        if (auth != null) {
            return new SmbFile(context, name, auth);
        } else {
            return new SmbFile(context, name);
        }
    }

    public List<FileInfo> listFiles(String dir, String filenamePattern) throws Exception {
        SmbFile readDirectory = null;
        SmbFilenameFilter filenameFilter = new SmbFilenameWildcardFilter(filenamePattern);

        try {
            readDirectory = getSmbFile(share, getPath(dir, null), auth);
        } catch (Exception e) {
            throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, dir), e);
        }

        try {
            SmbFile[] todoFiles = readDirectory.listFiles(filenameFilter);

            if (todoFiles == null) {
                return new ArrayList<FileInfo>();
            } else {
                List<FileInfo> result = new ArrayList<FileInfo>(todoFiles.length);

                for (SmbFile f : todoFiles) {
                    result.add(new SmbFileFileInfo(f));
                }

                return result;
            }
        } catch (Exception e) {
            throw new MuleException(new Message("file", 1), e);
        }
    }

    public InputStream readFile(String name, String dir) throws MuleException {
        SmbFile src = null;

        try {
            src = getSmbFile(share, getPath(dir, name), auth);
            return new SmbFileInputStream(src);
        } catch (Exception e) {
            throw new MuleException(new Message("file", 1, src.getPath()), e);
        }
    }

    /** Must be called after readFile when reading is complete */
    public void closeReadFile() throws Exception {
    // nothing
    }

    public boolean canAppend() {
        return true;
    }

    public void writeFile(String name, String dir, boolean append, byte[] message) throws Exception {
        OutputStream os = null;
        SmbFile dst = null;
        SmbFile dstDir = null;

        try {
            dstDir = getSmbFile(share, getPath(dir, null), auth);

            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }
            
            dst = getSmbFile(share, getPath(dir, name), auth);
            os = new SmbFileOutputStream(dst, append);
            os.write(message);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public void delete(String name, String dir, boolean mayNotExist) throws MuleException {
        SmbFile src = null;

        try {
            src = getSmbFile(share, getPath(dir, name), auth);
            src.delete();

            if (src.exists()) {
                if (!mayNotExist) {
                    throw new MuleException(new Message("file", 3, src.getPath()));
                }
            }
        } catch (Exception e) {
            throw new MuleException(new Message("file", 3, src.getPath()), e);
        }
    }

    public void move(String fromName, String fromDir, String toName, String toDir) throws MuleException {
        SmbFile src = null;
        SmbFile dst = null;
        SmbFile dstDir = null;
        
        try {
            src = getSmbFile(share, getPath(fromDir, fromName), auth);
            dstDir = getSmbFile(share, getPath(toDir, null), auth);
            
            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }
            
            dst = getSmbFile(share, getPath(toDir, toName), auth);
            
            try {
                dst.delete();    
            } catch (Exception e) {
                // ignore if file alread doesn't exist
            }
            
            src.renameTo(dst);
        } catch (Exception e) {
            throw new MuleException(new Message("file", 4, src.getPath(), dst.getPath()), e);
        }
    }

    public boolean isConnected() {
        return true;
    }

    public void activate() {}

    public void passivate() {}

    public void destroy() {}

    public boolean isValid() {
        return true;
    }
}