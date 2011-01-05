/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;

/** The base for classes to be implemented for Protocols that need to support
 *  batch processing - i.e., the processing of multiple messages per source
 *  (file).
 *  
 */
public interface BatchAdaptor {

	public void processBatch(Reader src, Map properties, BatchMessageProcessor dest, UMOEndpoint endpoint)
		throws MessagingException, UMOException, IOException;
}
