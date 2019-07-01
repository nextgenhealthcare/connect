/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;

import com.mirth.connect.connectors.file.FileSystemConnectionOptions;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;

public class WebDavConnection implements FileSystemConnection {
    public class WebDavFileInfo implements FileInfo {
        private String thePath;
        private WebdavFile theFile;

        public WebDavFileInfo(String path, WebdavFile theFile) {
            this.thePath = path;
            this.theFile = theFile;
        }

        public long getLastModified() {
            return theFile.lastModified();
        }

        public String getName() {
            return theFile.getName();
        }

        /** Gets the absolute pathname of the file */
        public String getAbsolutePath() {
            return theFile.getAbsolutePath();
        }

        public String getCanonicalPath() throws IOException {
            return this.theFile.getCanonicalPath();
        }

        /** Gets the absolute pathname of the directory holding the file */
        public String getParent() {
            return this.thePath;
        }

        public long getSize() {
            return theFile.length();
        }

        public boolean isDirectory() {
            return theFile.isDirectory();
        }

        public boolean isFile() {
            return theFile.isFile();
        }

        public boolean isReadable() {
            return theFile.canRead();
        }

        @Override
        public void populateSourceMap(Map<String, Object> sourceMap) {}
    }

    private static transient Log logger = LogFactory.getLog(WebDavConnection.class);

    /** The WebDAV client instance */
    private WebdavResource client = null;
    private boolean secure = false;
    private String username = null;
    private String password = null;

    public WebDavConnection(String host, boolean secure, FileSystemConnectionOptions fileSystemOptions) throws Exception {
        this.secure = secure;
        username = fileSystemOptions.getUsername();
        password = fileSystemOptions.getPassword();

        HttpURL url = null;

        if (secure) {
            url = new HttpsURL("https://" + host);
        } else {
            url = new HttpURL("http://" + host);
        }

        if (!username.equals("null")) {
            url.setUserinfo(username, password);
        }

        client = new WebdavResource(url);
    }

    @Override
    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex, boolean ignoreDot) throws Exception {
        FilenameFilter filenameFilter;

        if (isRegex) {
            filenameFilter = new RegexFilenameFilter(filenamePattern);
        } else {
            filenameFilter = new WildcardFileFilter(filenamePattern.trim().split("\\s*,\\s*"));
        }

        return list(fromDir, true, filenameFilter, ignoreDot);
    }

    @Override
    public List<String> listDirectories(String fromDir) throws Exception {
        List<String> directories = new ArrayList<String>();
        for (FileInfo directory : list(fromDir, false, null, false)) {
            directories.add(directory.getCanonicalPath());
        }
        return directories;
    }

    private List<FileInfo> list(String fromDir, boolean files, FilenameFilter filenameFilter, boolean ignoreDot) throws Exception {
        client.setPath(fromDir);
        WebdavResource[] resources = client.listWebdavResources();

        if (resources == null || resources.length == 0) {
            return new ArrayList<FileInfo>();
        }

        List<FileInfo> fileInfoList = new ArrayList<FileInfo>(resources.length);
        for (int i = 0; i < resources.length; i++) {

            WebdavFile file = null;
            String filePath = getFullPath(fromDir, resources[i].getPath());

            if (secure) {

                HttpsURL hrl = new HttpsURL("https://" + client.getHost() + filePath);
                if (!username.equals("null")) {
                    hrl.setUserinfo(username, password);
                }
                file = new WebdavFile(hrl);

            } else {

                HttpURL hrl = new HttpURL("http://" + client.getHost() + filePath);
                if (!username.equals("null")) {
                    hrl.setUserinfo(username, password);
                }
                file = new WebdavFile(hrl);

            }

            if (files) {
                if (file.isFile() && filenameFilter.accept(null, file.getName()) && !(ignoreDot && file.getName().startsWith("."))) {
                    fileInfoList.add(new WebDavFileInfo(fromDir, file));
                }
            } else if (file.isDirectory()) {
                fileInfoList.add(new WebDavFileInfo(fromDir, file));
            }
        }

        return fileInfoList;
    }

    @Override
    public boolean exists(String file, String path) {
        try {
            client.setPath(getFullPath(path, file));
            return client.exists();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public InputStream readFile(String file, String fromDir, Map<String, Object> sourceMap) throws Exception {
        String fullPath = getFullPath(fromDir, file);

        client.setPath(fullPath);
        if (client.isCollection()) {
            logger.error("Invalid filepath: " + fullPath);
            throw new Exception("Invalid Path");
        }

        return client.getMethodData();
    }

    @Override
    public void closeReadFile() throws Exception {
        // irrelevant
    }

    @Override
    public boolean canAppend() {
        return false;
    }

    @Override
    public void writeFile(String file, String toDir, boolean append, InputStream is, Map<String, Object> connectorMap) throws Exception {
        String fullPath = getFullPath(toDir, file);

        // first check if the toDir exists.
        client.setPath(toDir);

        if (!client.exists()) {

            // create the directory.
            client.mkcolMethod(toDir);
            logger.info("Destination directory does not exist. Creating directory: '" + toDir + "'");

            if (!client.putMethod(fullPath, is)) {
                logger.error("Unable to write file: '" + fullPath);
            }

        } else {

            // make sure it's a directory, not a file.
            if (!client.isCollection()) {
                throw new Exception("The destination directory path is invalid: '" + client.getPath() + "'");
            } else {
                // valid directory. now write the file.
                if (!client.putMethod(fullPath, is)) {
                    logger.error("Unable to write file: '" + fullPath);
                }
            }
        }
    }

    @Override
    public void delete(String file, String fromDir, boolean mayNotExist) throws Exception {
        String fullPath = getFullPath(fromDir, file);

        if (!client.deleteMethod(fullPath)) {
            if (!mayNotExist) {
                logger.error("Unable to delete file: '" + fullPath + "'");
            }
        }
    }

    @Override
    public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {
        String sourcePath = getFullPath(fromDir, fromName);
        String targetPath = getFullPath(toDir, toName);

        // first check if the toDir exists.
        client.setPath(toDir);

        if (!client.exists()) {

            // create the directory. and then move.
            client.mkcolMethod(toDir);
            logger.info("Move-To directory does not exist. Creating directory: '" + toDir + "'");

            if (!client.moveMethod(sourcePath, targetPath)) {
                logger.error("Unable to move file: '" + sourcePath + "' to '" + targetPath + "'");
            }

        } else {

            // make sure it's a directory, not a file.
            if (!client.isCollection()) {
                throw new Exception("The move-to directory path is invalid: '" + client.getPath() + "'");
            } else {
                // valid directory. now move the file.
                if (!client.moveMethod(sourcePath, targetPath)) {
                    logger.error("Unable to move file: '" + sourcePath + "' to '" + targetPath + "'");
                }
            }
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.exists();
    }

    @Override
    public void disconnect() {}

    @Override
    public void activate() {
        // irrelevant
    }

    @Override
    public void passivate() {
        // irrelevant
    }

    @Override
    public void destroy() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    @Override
    public boolean isValid() {
        return client != null && client.exists();
    }

    @Override
    public boolean canRead(String readDir) {
        try {
            client.setPath(readDir);
            return client.exists() && client.isCollection();
        } catch (IOException e) {
            logger.debug(e);
            return false;
        }
    }

    @Override
    public boolean canWrite(String writeDir) {
        try {
            client.setPath(writeDir);
            return client.exists() && client.isCollection() && !client.isLocked();
        } catch (IOException e) {
            logger.debug(e);
            return false;
        }
    }

    private String getFullPath(String dir, String file) {
        return ("/" + dir + "/" + file).replaceAll("//", "/");
    }
}
