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
