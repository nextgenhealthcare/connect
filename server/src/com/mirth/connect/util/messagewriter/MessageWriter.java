/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import com.mirth.connect.donkey.model.message.Message;

public interface MessageWriter {
    /**
     * @return True if the message was written successfully, false otherwise.
     */
    public boolean write(Message message) throws MessageWriterException;

    public void close() throws MessageWriterException;
}
