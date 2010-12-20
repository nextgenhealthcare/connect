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

/**
 * This allows creation of a FILE.ignore file in order to allow the system to
 * ignore files that we do not want
 * 
 */
public interface FileIgnoring {

    /**
     * Is this file currently being ignored?
     * 
     * @param file
     * @param fromDir
     * @return whether the file is being ignored or not
     */
    public boolean isFileIgnored(File file);

    /**
     * Mark the file as being ignored
     * 
     * @param file
     * @param fromDir
     */
    public void ignoreFile(File file);

}
