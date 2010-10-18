/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;

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
    }

    private static transient Log logger = LogFactory.getLog(WebDavConnection.class);

    /** The WebDAV client instance */
    private WebdavResource client = null;
    private boolean secure = false;
    private String username = null;
    private String password = null;

    public WebDavConnection(String host, boolean secure, String username, String password) throws Exception {
        this.secure = secure;
        this.username = username;
        this.password = password;

        HttpURL hrl = null;

        if (secure) {
            hrl = new HttpsURL("https://" + host);
        } else {
            hrl = new HttpURL("http://" + host);
        }

        if (!username.equals("null")) {
            hrl.setUserinfo(username, password);
        }

        client = new WebdavResource(hrl);
    }

    public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex) throws Exception {
        FilenameFilter filenameFilter;

        if (isRegex) {
            filenameFilter = new RegexFilenameFilter(filenamePattern);
        } else {
            filenameFilter = new WildcardFileFilter(filenamePattern);
        }

        client.setPath(fromDir);
        if (!client.isCollection()) {
            throw new Exception("Path is currently a file: '" + client.getHost() + client.getPath() + "'");
        }

        String files[] = client.list();
        if (files == null || files.length == 0) {
            return new ArrayList<FileInfo>();
        }

        List<FileInfo> v = new ArrayList<FileInfo>(files.length);
        for (int i = 0; i < files.length; i++) {

            WebdavFile file = null;
            String filePath = ("/" + fromDir + "/" + files[i]).replaceAll("//", "/");

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

            if (file.isFile()) {
                if (filenameFilter.accept(null, file.getName())) {
                    v.add(new WebDavFileInfo(fromDir, file));
                }
            }
        }
        return v;
    }

    public InputStream readFile(String file, String fromDir) throws Exception {

        String fullPath = ("/" + fromDir + "/" + file).replaceAll("//", "/");

        client.setPath(fullPath);
        if (client.isCollection()) {
            logger.error("Invalid filepath: " + fullPath);
            throw new Exception("Invalid Path");
        }

        return client.getMethodData();
    }

    public void closeReadFile() throws Exception {
        // irrelevant
    }

    public boolean canAppend() {
        return false;
    }

    public void writeFile(String file, String toDir, boolean append, byte[] message) throws Exception {

        String fullPath = ("/" + toDir + "/" + file).replaceAll("//", "/");

        // first check if the toDir exists.
        client.setPath(toDir);

        if (!client.exists()) {

            // create the directory.
            client.mkcolMethod(toDir);
            logger.info("Destination directory does not exist. Creating directory: '" + toDir + "'");

            if (!client.putMethod(fullPath, message)) {
                logger.error("Unable to write file: '" + fullPath);
            }

        } else {

            // make sure it's a directory, not a file.
            if (!client.isCollection()) {
                throw new Exception("The destination directory path is invalid: '" + client.getPath() + "'");
            } else {
                // valid directory. now write the file.
                if (!client.putMethod(fullPath, message)) {
                    logger.error("Unable to write file: '" + fullPath);
                }
            }
        }
    }

    public void delete(String file, String fromDir, boolean mayNotExist) throws Exception {

        String fullPath = ("/" + fromDir + "/" + file).replaceAll("//", "/");

        if (!client.deleteMethod(fullPath)) {
            if (!mayNotExist) {
                logger.error("Unable to delete file: '" + fullPath + "'");
            }
        }
    }

    public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {

        String sourcePath = ("/" + fromDir + "/" + fromName).replaceAll("//", "/");
        String targetPath = ("/" + toDir + "/" + toName).replaceAll("//", "/");

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

    public boolean isConnected() {
        return client != null && client.exists();
    }

    public void activate() {
        // irrelevant
    }

    public void passivate() {
        // irrelevant
    }

    public void destroy() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    public boolean isValid() {
        return client != null && client.exists();
    }

    public boolean canRead(String readDir) {
        try {
            client.setPath(readDir);
            return client.exists() && client.isCollection();
        } catch (IOException e) {
            logger.debug(e);
            return false;
        }
    }

    public boolean canWrite(String writeDir) {
        try {
            client.setPath(writeDir);
            return client.exists() && client.isCollection() && !client.isLocked();
        } catch (IOException e) {
            logger.debug(e);
            return false;
        }
    }
}
