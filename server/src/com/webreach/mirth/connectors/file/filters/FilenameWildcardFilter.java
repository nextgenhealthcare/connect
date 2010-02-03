/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.file.filters;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.routing.filters.WildcardFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.connectors.file.FileConnector;

/**
 * <code>FilenameWildcardFilter</code> Filters the incoming files from the
 * read From directory, based on file patterns
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class FilenameWildcardFilter extends WildcardFilter implements FilenameFilter
{
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(FilenameWildcardFilter.class);

    public FilenameWildcardFilter()
    {
        super();
    }

    public FilenameWildcardFilter(String pattern)
    {
        super(pattern);
    }

    /**
     * UMOFilter condition decider method. <p/> Returns
     * <code>boolean</code> <code>TRUE</code> if the file conforms to an
     * acceptable pattern or <code>FALSE</code> otherwise.
     * 
     * @param dir The directory to apply the filter to.
     * @param name The name of the file to apply the filter to.
     * @return indication of acceptance as boolean.
     */
    public boolean accept(File dir, String name)
    {
        if (name == null) {
            logger.warn("The filename and or directory was null");
            return false;
        } else {
            return accept(name);
        }
    }
    
    public boolean accept(UMOMessage message)
    {
        return accept(message.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME));
    }
}
