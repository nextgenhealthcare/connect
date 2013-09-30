/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class FilenameUtils {
    public static String getAbsolutePath(File baseDir, String path) {
        path = StringUtils.trim(path);
        File file = new File(path);

        if (file.isAbsolute()) {
            return file.getPath();
        }

        char firstChar = path.charAt(0);

        /*
         * For Windows systems, if the path begins with a forward or back slash, extract the drive
         * letter from baseDir and use the drive's root directory as the new baseDir.
         */
        if (firstChar == '/' || firstChar == '\\') {
            File parent = baseDir.getParentFile();

            while (parent != null) {
                baseDir = parent;
                parent = baseDir.getParentFile();
            }

            return new File(baseDir, path).getAbsolutePath();
        }

        return new File(baseDir, path).getAbsolutePath();
    }
}
