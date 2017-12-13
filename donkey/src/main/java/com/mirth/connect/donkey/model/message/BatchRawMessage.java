/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;

public class BatchRawMessage {
    private BatchMessageSource batchMessageSource;
    protected Map<String, Object> sourceMap = new HashMap<String, Object>();
    private List<Attachment> attachments;

    public BatchRawMessage(BatchMessageSource batchMessageSource) {
        this.batchMessageSource = batchMessageSource;
    }

    public BatchRawMessage(BatchMessageSource batchMessageSource, Map<String, Object> sourceMap) {
        this.batchMessageSource = batchMessageSource;
        this.sourceMap = sourceMap;
    }

    public BatchRawMessage(BatchMessageSource batchMessageSource, Map<String, Object> sourceMap, List<Attachment> attachments) {
        this.batchMessageSource = batchMessageSource;
        this.sourceMap = sourceMap;
        this.attachments = attachments;
    }

    public BatchMessageSource getBatchMessageSource() {
        return batchMessageSource;
    }

    public Map<String, Object> getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Map<String, Object> sourceMap) {
        this.sourceMap = sourceMap;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
