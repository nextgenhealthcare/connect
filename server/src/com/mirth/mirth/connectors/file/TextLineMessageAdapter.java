/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.file;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UniqueIdNotSupportedException;

import com.webreach.mirth.connectors.file.filesystems.FileInfo;

public class TextLineMessageAdapter extends AbstractMessageAdapter {
    private String message = null;
    private FileInfo file;

    public TextLineMessageAdapter(Object message) throws MessagingException {
        if (message instanceof String) {
            setMessage((String) message);
        } else if (message instanceof FileInfo) {
            // Hackish method
            this.file = (FileInfo) message;
            setMessage("");
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayload()
     */
    public Object getPayload() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsBytes()
     */
    public byte[] getPayloadAsBytes() throws Exception {
        return message.getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#setMessage(java.lang.Object)
     */
    private void setMessage(String message) throws MessagingException {
        try {

            this.message = message;
            if (this.file != null) {
                properties.put(FileConnector.PROPERTY_ORIGINAL_FILENAME, this.file.getName());
                properties.put(FileConnector.PROPERTY_DIRECTORY, this.file.getParent());
            }
        } catch (Exception e) {
            throw new MessagingException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getAbsolutePath()), e);
        }
    }

    public FileInfo getFile() {
        return file;
    }

    public String getUniqueId() throws UniqueIdNotSupportedException {
        if (file != null)
            return file.getAbsolutePath();
        else
            throw new UniqueIdNotSupportedException(this);
    }
}
