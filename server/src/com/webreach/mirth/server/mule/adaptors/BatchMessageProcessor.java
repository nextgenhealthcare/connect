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
