/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.file;

import java.io.File;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UniqueIdNotSupportedException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.connectors.file.transformers.FileToByteArray;

/**
 * <code>FileMessageAdapter</code> provides a wrapper for a message. Users can
 * obtain the contents of the message through the payload property and can get
 * the filename and directory in the properties using PROPERTY_FILENAME and
 * PROPERTY_DIRECTORY
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class FileContentsMessageAdapter extends AbstractMessageAdapter
{
    private FileToByteArray transformer = new FileToByteArray();

    private byte[] message = null;
    private File file = null;

    public FileContentsMessageAdapter(Object message) throws MessagingException
    {
        if (message instanceof File) {
            setMessage((File) message);
        } else if (message instanceof byte[]){
        	setMessage((byte[])message);
        } else if (message instanceof String){
        	setMessage(((String)message).getBytes());
        }else{
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayload()
     */
    public Object getPayload()
    {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsBytes()
     */
    public byte[] getPayloadAsBytes() throws Exception
    {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception
    {
        return new String(getPayloadAsBytes());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#setMessage(java.lang.Object)
     */
    private void setMessage(File message) throws MessagingException
    {
        try {
            this.file = message;
            this.message = (byte[]) transformer.transform(message);
            properties.put(FileConnector.PROPERTY_ORIGINAL_FILENAME, this.file.getName());
            properties.put(FileConnector.PROPERTY_DIRECTORY, this.file.getParent());
        } catch (TransformerException e) {
            throw new MessagingException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getAbsolutePath()), e);
        }
    }

    private void setMessage(byte[] message) throws MessagingException
    {
      
            this.file = null;
            this.message = (byte[]) message;
           // properties.put(FileConnector.PROPERTY_ORIGINAL_FILENAME, this.file.getName());
           // properties.put(FileConnector.PROPERTY_DIRECTORY, this.file.getParent());
       
    }

    public File getFile()
    {
        return file;
    }

    public String getUniqueId() throws UniqueIdNotSupportedException
    {
    	if (file != null){
    		return file.getAbsolutePath();
    	}else{
    		return "";
    	}
    }
}
