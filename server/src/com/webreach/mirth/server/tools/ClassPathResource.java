package com.webreach.mirth.server.tools;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

public class ClassPathResource {
	private static Logger logger = Logger.getLogger(ClassPathResource.class);
	
    public static URI getResourceURI(String resource) {    	
    	// If nothing found, null is returned
    	URI uri = null;
    	try
		{
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
		}
		catch (Exception e) {
			logger.error("Could not load resource.", e);
		}
		return uri;
    }
}

