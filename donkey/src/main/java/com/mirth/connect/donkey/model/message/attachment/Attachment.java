/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("attachment")
public class Attachment {
    private String id;
    private byte[] content;
    private String type;

    public Attachment() {

    }

    public Attachment(String id, byte[] content, String type) {
        this.id = id;
        this.content = content;
        this.setType(type);
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

}
