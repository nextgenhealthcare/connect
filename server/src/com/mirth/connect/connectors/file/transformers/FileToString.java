/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.file.transformers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>FileToString</code> reads a file's contents intor a string.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */
public class FileToString extends AbstractTransformer
{
    public FileToString()
    {
        registerSourceType(File.class);
        registerSourceType(String.class);
        registerSourceType(byte[].class);
        setReturnClass(String.class);
    }

    public Object doTransform(Object src) throws TransformerException
    {
        if (src instanceof byte[])
            return new String((byte[]) src);
        if (src instanceof String)
            return src.toString();

        StringBuffer sb = new StringBuffer(new Long(((File) src).length()).intValue());
        char[] buf = new char[1024 * 8];
        FileReader fr = null;
        try {
            fr = new FileReader((File) src);
            int read = 0;
            while ((read = fr.read(buf)) >= 0) {
                sb.append(buf, 0, read);
            }
        } catch (IOException e) {
            throw new TransformerException(this, e);
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                logger.debug("Failed to close reader in transformer: " + e.getMessage());
            }
        }

        return sb.toString();
    }
}
