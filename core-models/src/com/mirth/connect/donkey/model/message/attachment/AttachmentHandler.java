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

import com.mirth.connect.donkey.model.message.RawMessage;

public interface AttachmentHandler extends Serializable {

    public void initialize(RawMessage message, Object channel) throws AttachmentException;

    public Attachment nextAttachment() throws AttachmentException;

    public String shutdown() throws AttachmentException;
}
