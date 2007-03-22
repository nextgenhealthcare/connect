/* 
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileContentsMessageAdapter.java,v 1.6 2005/09/02 18:08:28 rossmason Exp $
 * $Revision: 1.6 $
 * $Date: 2005/09/02 18:08:28 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package com.webreach.mirth.server.mule.providers.file;

import java.io.File;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UniqueIdNotSupportedException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.server.mule.providers.file.transformers.FileToByteArray;

public class TextLineMessageAdapter extends AbstractMessageAdapter
{
    private FileToByteArray transformer = new FileToByteArray();

    private String message = null;
    private File file;
    public TextLineMessageAdapter(Object message) throws MessagingException
    {
        if (message instanceof String) {
            setMessage((String)message);
        } else if (message instanceof File){
        	//Hackish method
        	this.file = (File)message;
        	setMessage("");
        }else {
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
        return message.getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#getPayloadAsString()
     */
    public String getPayloadAsString() throws Exception
    {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOMessageAdapter#setMessage(java.lang.Object)
     */
    private void setMessage(String message) throws MessagingException
    {
        try {
           
            this.message = message;
            if (this.file != null){
            	properties.put(FileConnector.PROPERTY_ORIGINAL_FILENAME, this.file.getName());
            	properties.put(FileConnector.PROPERTY_DIRECTORY, this.file.getParent());
            }
        } catch (Exception e) {
            throw new MessagingException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getAbsolutePath()), e);
        }
    }

    public File getFile()
    {
        return file;
    }

    public String getUniqueId() throws UniqueIdNotSupportedException
    {
        if (file!=null)
        	return file.getAbsolutePath();
        else
        	throw new UniqueIdNotSupportedException(this);
    }
}
