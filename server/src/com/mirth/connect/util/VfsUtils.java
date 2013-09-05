/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

public class VfsUtils {
    private static Map<String, String> uriExtensionMap;
    private static FileSystemManager fileSystemManager;

    static {
        /*
         * Map of file extensions to Apache VFS file-system types (see
         * http://commons.apache.org/proper/commons-vfs/filesystems.html). The import dialog will
         * detect files with these extensions and send the appropriate uri format to the
         * MessageWriterVfs class.
         */
        uriExtensionMap = new HashMap<String, String>();
        uriExtensionMap.put("zip", "zip://");
        uriExtensionMap.put("tar.gz", "tgz://");
        uriExtensionMap.put("tar.bz2", "tbz2://");
        uriExtensionMap.put("tar", "tar://");
    }

    /**
     * Automatically prepend the appropriate URI prefix to the given path.
     */
    public static String pathToUri(String path) {
        for (Entry<String, String> entry : uriExtensionMap.entrySet()) {
            String ext = entry.getKey();
            String prefix = entry.getValue();

            // if the path has this extension and does not have the corresponding uri prefix, then return the path with the uri prefix prepended
            if (path.length() >= ext.length() && path.substring(path.length() - ext.length()).toLowerCase().equals(ext) && (path.length() < prefix.length() || !path.substring(0, prefix.length()).equals(prefix))) {
                return prefix + path;
            }
        }

        return path;
    }

    /**
     * This method is a replacement for VFS.getManager() and provides a workaround for an existing
     * bug in Commons VFS 2.0 (https://issues.apache.org/jira/browse/VFS-228). The
     * ClassNotFoundException described in apache's jira issue occurs in the CLI (works fine in the
     * administrator).
     * 
     * If/when the issue gets resolved in a future version of Commons VFS, then we can revert to
     * using VFS.getManager().
     */
    public static FileSystemManager getManager() throws FileSystemException {
        if (fileSystemManager == null) {
            StandardFileSystemManager stdFileSystemManager = new StandardFileSystemManager();
            stdFileSystemManager.setClassLoader(VfsUtils.class.getClassLoader());
            stdFileSystemManager.init();
            fileSystemManager = stdFileSystemManager;
        }

        return fileSystemManager;
    }
}
