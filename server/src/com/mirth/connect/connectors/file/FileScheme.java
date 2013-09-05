/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

public enum FileScheme {
    FILE("file"), FTP("ftp"), SFTP("sftp"), SMB("smb"), WEBDAV("webdav");

    private String displayName;

    private FileScheme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FileScheme fromDisplayName(String displayName) {
        for (FileScheme scheme : FileScheme.values()) {
            if (scheme.getDisplayName().equals(displayName)) {
                return scheme;
            }
        }

        return null;
    }
}