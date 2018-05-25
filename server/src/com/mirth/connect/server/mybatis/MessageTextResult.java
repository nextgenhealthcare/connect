/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.mybatis;

public class MessageTextResult {
    private Long messageId;
    private Integer metaDataId;
    private Integer contentType;
    private Boolean textFound;
    private Long importId;
    private Boolean processed;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public Boolean isTextFound() {
        return textFound;
    }

    public void setTextFound(Boolean textFound) {
        this.textFound = textFound;
    }

    public Long getImportId() {
        return importId;
    }

    public void setImportId(Long importId) {
        this.importId = importId;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}
