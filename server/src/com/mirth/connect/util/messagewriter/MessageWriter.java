/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import com.mirth.connect.donkey.model.message.Message;

public interface MessageWriter {
    /**
     * @return True if the message was written, false if the message was not written, because it had
     *         no information relevant to this writer. (false does not mean that an error occurred,
     *         if an error occurs, an exception will be thrown)
     * @throws MessageWriterException
     *             If an error occurred while attempting to write the message.
     */
    public boolean write(Message message) throws MessageWriterException;

    public void close() throws MessageWriterException;
}
