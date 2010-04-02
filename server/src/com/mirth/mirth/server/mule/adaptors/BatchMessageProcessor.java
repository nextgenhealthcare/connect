/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.mule.adaptors;

import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;

/**
 * The interface that must be implemented by classes that handle message
 * callbacks from BatchAdapters.
 * 
 * @author Erik Horstkotte (erikh@webreachinc.com)
 */ 
public interface BatchMessageProcessor {

	public void processBatchMessage(String message)
		throws MessagingException, UMOException;
}