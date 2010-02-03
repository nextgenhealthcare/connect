/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.file;

import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.UUID;
import org.mule.util.Utility;

/**
 * <code>SimpleFilenameParser</code> understands a limited set of tokens,
 * namely
 * <ul>
 * <li>${DATE} : the currrent date in the format dd-MM-yy_HH-mm-ss.SS</li>
 * <li>${DATE:yy-MM-dd} : the current date using the specified format</li>
 * <li>${SYSTIME} : The current system time milliseconds</li>
 * <li>${UUID} : A generated Universally unique id</li>
 * <li>${ORIGINALNAME} : The origial file name if the file being written was
 * read from another location</li>
 * <li>${COUNT} : An incremental counter</li>
 * </ul>
 * 
 * Note that square brackets can be used instead of curl brackets, this is
 * useful when defining the file output pattern in a Mule Url endpointUri where
 * the curl bracket is an invalid character.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */

public class SimpleFilenameParser implements FilenameParser
{
    public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SS";

    private long count = 1;

    public String getFilename(UMOMessageAdapter adaptor, String pattern)
    {
        String result = null;
        if (pattern != null && pattern.indexOf('{') > -1) {
            result = getFilename(adaptor, pattern, '{', '}');
        } else {
            result = getFilename(adaptor, pattern, '[', ']');
        }

        return result;
    }

    protected String getFilename(UMOMessageAdapter adaptor, String pattern, char left, char right)
    {
        String filename = pattern;
        if (pattern == null) {
            filename = System.currentTimeMillis() + ".dat";
        } else {
            int index = pattern.indexOf("$" + left + "DATE" + right);
            if (index > -1) {
                filename = filename.replaceAll("\\$\\" + left + "DATE\\" + right,
                                               Utility.getTimeStamp(DEFAULT_DATE_FORMAT));
            }
            index = pattern.indexOf("$" + left + "DATE:");
            if (index > -1) {
                int curl = pattern.indexOf(right, index);
                if (curl == -1) {
                    filename = filename.replaceAll("\\$\\" + left + "DATE:", Utility.getTimeStamp(DEFAULT_DATE_FORMAT));
                } else {
                    String dateformat = pattern.substring(index + 7, curl);
                    filename = filename.replaceAll("\\$\\" + left + "DATE:" + dateformat + "\\" + right,
                                                   Utility.getTimeStamp(dateformat));
                }
            }
            index = pattern.indexOf("$" + left + "UUID" + right);
            if (index > -1) {
                filename = filename.replaceAll("\\$\\" + left + "UUID\\" + right, new UUID().getUUID());
            }
            index = pattern.indexOf("$" + left + "SYSTIME" + right);
            if (index > -1) {
                filename = filename.replaceAll("\\$\\" + left + "SYSTIME\\" + right,
                                               String.valueOf(System.currentTimeMillis()));
            }
            index = pattern.indexOf("$" + left + "COUNT" + right);
            if (index > -1) {
                filename = filename.replaceAll("\\$\\" + left + "COUNT\\" + right, String.valueOf(getCount()));
            }
            index = pattern.indexOf("$" + left + "ORIGINALNAME" + right);
            if (index > -1 && adaptor != null) {
                String name = (String) adaptor.getProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME);
                if (name != null) {
                    filename = filename.replaceAll("\\$\\" + left + "ORIGINALNAME\\" + right, name);
                }
            }
        }
        return filename;
    }

    protected synchronized long getCount()
    {
        return count++;
    }
}
