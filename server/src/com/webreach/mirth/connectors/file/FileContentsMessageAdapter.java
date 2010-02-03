/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UniqueIdNotSupportedException;
import org.mule.util.StringMessageHelper;

import com.webreach.mirth.connectors.file.filesystems.FileInfo;

public class FileContentsMessageAdapter extends AbstractMessageAdapter {
    private byte[] message = null;
    private FileInfo file = null;

    public FileContentsMessageAdapter(Object message) throws MessagingException {
        if (message instanceof FileInfo) {
            setMessage((FileInfo) message);
        } else if (message instanceof byte[]) {
            setMessage((byte[]) message);
        } else if (message instanceof String) {
        	 setMessage(StringMessageHelper.getBytes((String)message)); 
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    public Object getPayload() {
        return message;
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return message;
    }

    public String getPayloadAsString() throws Exception {
        return new String(getPayloadAsBytes());
    }

    private void setMessage(FileInfo message) throws MessagingException {
        this.file = message;
        this.message = "".getBytes();
        properties.put(FileConnector.PROPERTY_ORIGINAL_FILENAME, this.file.getName());
        properties.put(FileConnector.PROPERTY_DIRECTORY, this.file.getParent());
    }

    private void setMessage(byte[] message) throws MessagingException {
        this.file = null;
        this.message = (byte[]) message;
    }

    public String getUniqueId() throws UniqueIdNotSupportedException {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return "";
        }
    }
}
