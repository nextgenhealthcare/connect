/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.export;

import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.model.filters.MessageFilter;

public class MessageExportOptions {
    final public static int SINGLE = 1; // working
    final public static int SINGLE_COMPRESSED = 2; // not working
    final public static int MULTIPLE = 3; // working
    final public static int MULTIPLE_COMPRESSED_EACH = 4; // working
    final public static int MULTIPLE_COMPRESSED_ARCHIVE = 5; // not working
    
    private String channelId;
    private MessageFilter messageFilter;
    private ContentType contentType;
    private int bufferSize = 100;
    private String folder;
    private int outputType = MessageExportOptions.SINGLE;
    private boolean encrypt = false;
    private String charset = "UTF-8";

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(MessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public int getOutputType() {
        return outputType;
    }

    public void setOutputType(int outputType) {
        this.outputType = outputType;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
