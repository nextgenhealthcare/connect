package com.mirth.connect.util.messagewriter;

import com.mirth.connect.donkey.model.message.Message;

public interface MessageWriter {
    /**
     * @return True if the message was written successfully, false otherwise.
     */
    public boolean write(Message message) throws MessageWriterException;

    public void close() throws MessageWriterException;
}
