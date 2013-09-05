/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class MirthFileFilter extends javax.swing.filechooser.FileFilter {
    private String fileExtension;

    public MirthFileFilter(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Returns true if the file is a directory, or the extension matches the one
     * specified in the filter.
     * 
     */
    @Override
    public boolean accept(File file) {
        return (file.isDirectory() || FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(fileExtension));
    }

    @Override
    public String getDescription() {
        if (fileExtension.equalsIgnoreCase("xml")) {
            return "XML files";
        } else if (fileExtension.equalsIgnoreCase("html")) {
            return "HTML files";
        } else if (fileExtension.equalsIgnoreCase("txt")) {
            return "Text files";
        } else if (fileExtension.equalsIgnoreCase("zip")) {
            return "ZIP files";
        } else if (fileExtension.equalsIgnoreCase("wsdl")) {
            return "WSDL files";
        } else {
            return "ERROR";
        }
    }
}
