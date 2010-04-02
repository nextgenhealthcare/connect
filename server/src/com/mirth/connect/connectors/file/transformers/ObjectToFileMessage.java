/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.file.transformers;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>ObjectToFileMessage</code> converts an object to a FileMessage type
 * using the obect as the payload. The filename for the message is either picked
 * up from the FileConnector config or explicitly set in the transformer
 * properties
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 * @deprecated This is no longer needed as there is no longer a FileMessage type
 */
public class ObjectToFileMessage extends AbstractTransformer
{
    protected String outputFilename;
    protected String outputFilePattern;
    protected static long counter;

    /**
     * 
     */
    public ObjectToFileMessage()
    {
        registerSourceType(Object.class);
        setReturnClass(Object.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transformers.AbstractTransformer#doTransform(java.lang.Object)
     */
    public Object doTransform(Object src) throws TransformerException
    {
        return src;
    }
}
