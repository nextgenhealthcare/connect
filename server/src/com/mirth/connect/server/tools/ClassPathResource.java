/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.tools;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

public class ClassPathResource {
    private static Logger logger = Logger.getLogger(ClassPathResource.class);

    public static URI getResourceURI(String resource) {
        // If nothing found, null is returned
        URI uri = null;
        try {
            logger.debug("Loading: " + resource);
            URL url = ClassPathResource.class.getResource(resource);

            // If nothing is found, try it with or without the '/' in front.
            // Without a '/' in front java prepends the full package name.
            if (url == null) {
                if (resource.charAt(0) == '/') {
                    resource = resource.substring(1);
                } else {
                    resource = "/" + resource;
                }
                url = ClassPathResource.class.getResource(resource);
            }
            if (url != null)
                uri = url.toURI();
        } catch (Exception e) {
            logger.error("Could not load resource.", e);
        }
        return uri;
    }
}
