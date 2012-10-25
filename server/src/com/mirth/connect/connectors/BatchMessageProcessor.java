/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors;

/**
 * The interface that must be implemented by classes that handle message
 * callbacks from BatchAdapters.
 * 
 */
public interface BatchMessageProcessor {

    public void processBatchMessage(String message) throws BatchMessageProcessorException;

    public String getBatchScriptId();
}