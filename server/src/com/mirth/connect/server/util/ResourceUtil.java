/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceUtil {
    /**
     * Returns a resource as a stream by checking:
     * 
     * 1. The classpath for a resource with the specified name
     * 2. For a file with the specified path
     * 
     * @param resourceName
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getResourceStream(Class<?> clazz, String resourceName) throws FileNotFoundException {
        String cpResourceName = null;
        
        if (!resourceName.startsWith("/")) {
            cpResourceName = "/" + resourceName;
        } else {
            cpResourceName = resourceName;
        }
        
        InputStream is = clazz.getResourceAsStream(cpResourceName);

        if (is == null) {
            is = new FileInputStream(resourceName);
        }

        return is;
    }
}
