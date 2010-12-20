/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.file;

import org.mule.umo.provider.UMOMessageAdapter;

/**
 * <code>FilenameParser</code> is a simple expression parser interface for
 * processing filenames
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public interface FilenameParser {
    public String getFilename(UMOMessageAdapter adaptor, String pattern);
}
