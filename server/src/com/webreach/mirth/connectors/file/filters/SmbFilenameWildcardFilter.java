/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file.filters;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.routing.filters.WildcardFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.connectors.file.FileConnector;

public class SmbFilenameWildcardFilter extends WildcardFilter implements SmbFilenameFilter
{
    private static transient Log logger = LogFactory.getLog(SmbFilenameFilter.class);

    public SmbFilenameWildcardFilter()
    {
        super();
    }

    public SmbFilenameWildcardFilter(String pattern)
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
    public boolean accept(SmbFile dir, String name)
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
