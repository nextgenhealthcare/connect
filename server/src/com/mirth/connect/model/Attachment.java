/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Arrays;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("attachment")
public class Attachment implements Serializable {
    private String attachmentId;
    private String messageId;
    private byte[] data;
    private int size;
    private String type;

    public Attachment(){
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    public String toString() {
        return "Attachment{" +
                "attachmentId='" + attachmentId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", data=" + data +
                ", size=" + size +
                ", type='" + type + '\'' +
                '}';
    }

    protected Object clone() {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(this.getAttachmentId());
        attachment.setData(this.getData());
        attachment.setMessageId(this.getMessageId());
        attachment.setSize(this.getSize());
        attachment.setType(this.getType());
        return attachment;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Attachment that = (Attachment) o;

        if (!attachmentId.equals(that.attachmentId)) return false;
        if (!Arrays.equals(data, that.data)) return false;
        if (!messageId.equals(that.messageId)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = attachmentId.hashCode();
        result = 29 * result + messageId.hashCode();
        return result;
    }
}
