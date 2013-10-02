/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message;

import java.io.Reader;


/**
 * The base for classes to be implemented for Protocols that need to support
 * batch processing - i.e., the processing of multiple messages per source
 * (file).
 * 
 */
public interface BatchAdaptor {

    // TODO: Improve exception handling or throw a custom exception type
    public void processBatch(Reader src, BatchMessageProcessor dest) throws Exception;
}
