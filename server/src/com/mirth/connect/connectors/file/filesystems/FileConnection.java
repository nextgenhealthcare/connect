/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.mirth.connect.connectors.file.FileConnectorException;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;

/**
 * The FileSystemConnection class for local files
 * 
 */
public class FileConnection implements FileSystemConnection, FileIgnoring {

    private static final String IGNORE = ".ignore";

    public class FileFileInfo implements FileInfo {

        private File theFile;

        public FileFileInfo(File theFile) {
            this.theFile = theFile;
        }

        /** Gets the name of the file relative to the folder searched */
        public String getName() {

            return this.theFile.getName();
        }

        /** Gets the absolute pathname of the file */
        public String getAbsolutePath() {

            return this.theFile.getAbsolutePath();
        }

        /** Gets the absolute pathname of the directory holding the file */
        public String getParent() {

            return this.theFile.getParent();
        }

        /** Gets the size of the file in bytes */
        public long getSize() {

            return this.theFile.length();
        }

        /**
         * Gets the date and time the file was last modified, in milliseconds
         * since the epoch
         */
        public long getLastModified() {

            return this.theFile.lastModified();
        }

        /** Tests if the file is a directory */
        public boolean isDirectory() {

            return this.theFile.isDirectory();

        }

        /** Tests if the file is a plain file */
        public boolean isFile() {
            return this.theFile.isFile();
        }

        /** Tests if the file exists and is readable */
        public boolean isReadable() {

            return this.theFile.canRead();
        }
    }

    public FileConnection() {

        // That was easy
    }

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        FilenameFilter filenameFilter;

        if (isRegex) {
            filenameFilter = new RegexFilenameFilter(filenamePattern);
        } else {
            filenameFilter = new WildcardFileFilter(filenamePattern.trim().split("\\s*,\\s*"));
        }

        File readDirectory = null;
        try {
            readDirectory = new File(fromDir);
        } catch (Exception e) {
            throw new FileConnectorException("Read directory does not exist: " + fromDir, e);
        }

        try {
            File[] todoFiles = readDirectory.listFiles(filenameFilter);
            if (todoFiles == null) {

                return new ArrayList<FileInfo>();
            } else {
                List<FileInfo> result = new ArrayList<FileInfo>(todoFiles.length);
                for (File f : todoFiles) {

                    if (!f.getName().endsWith(IGNORE) && !isFileIgnored(f) && !(ignoreDot && f.getName().startsWith("."))) {
                        result.add(new FileFileInfo(f));
                    }
                }
                return result;
            }
        } catch (Exception e) {
            throw new FileConnectorException("Error listing files from [" + fromDir + "] for pattern [" + filenamePattern + "]", e);
        }
    }

    @Override
    public boolean exists(String file, String path) {
        File src = new File(path, file);
        return src.exists();
    }

    @Override
    public boolean canRead(String readDir) {
        File readDirectory = new File(readDir);
        return readDirectory.isDirectory() && readDirectory.canRead();
    }

    @Override
    public boolean canWrite(String writeDir) {
        File writeDirectory = new File(writeDir);
        return writeDirectory.isDirectory() && writeDirectory.canWrite();
    }

    @Override
    public InputStream readFile(String file, String fromDir) throws FileConnectorException {
        try {
            File src = new File(fromDir, file);
            return new FileInputStream(src);
        } catch (Exception e) {
            throw new FileConnectorException("Error reading file [" + file + "] from dir [" + fromDir + "]", e);
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
    public void writeFile(String file, String toDir, boolean append, InputStream is) throws Exception {
        OutputStream os = null;
        File dstDir = new File(toDir);

        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }

        File dst = new File(dstDir, file);

        try {
            os = new FileOutputStream(dst, append);

            IOUtils.copy(is, os);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public void delete(String file, String fromDir, boolean mayNotExist) throws FileConnectorException {
        File src = new File(fromDir, file);

        if (!src.delete()) {

            if (!mayNotExist) {
                throw new FileConnectorException("File should not exist after deleting: " + src.getAbsolutePath());
            }
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws FileConnectorException {
        File src = new File(fromDir, fromName);
        File dst = new File(toDir, toName);

        dst.delete();

        // File.renameTo operation doesn't work across file systems. So we will
        // attempt to do a File.renameTo for efficiency and atomicity, if this
        // fails then we will use the Commons-IO moveFile operation which
        // does a "copy and delete"
        if (!src.renameTo(dst)) {
            try {
                // Copy the file
                FileUtils.copyFile(src, dst);

                // This will NOT throw any exceptions, this only return
                // true/false
                if (!FileUtils.deleteQuietly(src)) {
                    // We had a problem, so now we should ignore it
                    ignoreFile(src);
                }
            } catch (IOException e) {
                throw new FileConnectorException("Error from file from [" + src.getAbsolutePath() + "] to [" + dst.getAbsolutePath() + "]", e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void activate() {}

    @Override
    public void passivate() {}

    @Override
    public void destroy() {}

    @Override
    public boolean isValid() {
        return true;
    }

    // ///// Ignoring stuff

    @Override
    public boolean isFileIgnored(File file) {
        File f = new File(file.getAbsolutePath() + IGNORE);
        return f.exists();
    }

    @Override
    public void ignoreFile(File file) {
        try {
            File f = new File(file.getAbsolutePath() + IGNORE);
            f.createNewFile();
        } catch (IOException e) {
            // handle exceptions
        }
    }
}