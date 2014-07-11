/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message.batch;


public interface BatchMessageReceiver extends BatchMessageSource {

    public boolean canRead();

    public byte[] readBytes() throws Exception;

    public String getStringFromBytes(byte[] bytes) throws Exception;

    public void readCompleted();

}
