/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("attachment")
public class Attachment implements Serializable {
    private String id;
    private byte[] content;
    private String type;
    private boolean encrypt;
    private String encryptionHeader;

    public Attachment() {

    }

    public Attachment(String id, byte[] content, String type) {
        this(id, content, type, null);
    }

    public Attachment(String id, byte[] content, String type, String encryptionHeader) {
        this.id = id;
        this.content = content;
        this.setType(type);
        this.encryptionHeader = encryptionHeader;
        this.encrypt = encryptionHeader != null;
    }

    public String getAttachmentId() {
        return "${ATTACH:" + id + "}";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEncrypted() {
        return encrypt;
    }

    public void setEncrypted(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getEncryptionHeader() {
        return encryptionHeader;
    }

    public void setEncryptionHeader(String encryptionHeader) {
        this.encryptionHeader = encryptionHeader;
    }
}
