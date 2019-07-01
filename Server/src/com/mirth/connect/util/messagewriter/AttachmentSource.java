/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.util.List;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;

public interface AttachmentSource {
    public List<Attachment> getMessageAttachments(Message message) throws ClientException;
}