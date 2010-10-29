/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.mule.umo.UMOEvent;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.filters.MessageObjectFilter;

public abstract class MessageObjectController extends Controller {
    public static MessageObjectController getInstance() {
        return ControllerFactory.getFactory().createMessageObjectController();
    }

    public abstract void removeAllFilterTables();

    public abstract void updateMessage(MessageObject incomingMessageObject, boolean checkIfMessageExists);

    public abstract void updateMessageStatus(String channelId, String messageId, MessageObject.Status newStatus);

    public abstract void importMessage(MessageObject messageObject);

    public abstract int createMessagesTempTable(MessageObjectFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public abstract List<MessageObject> getMessagesByPageLimit(int page, int pageSize, int maxMessages, String uid, MessageObjectFilter filter) throws ControllerException;

    public abstract List<MessageObject> getMessagesByPage(int page, int pageSize, int maxMessages, String uid, boolean descending) throws ControllerException;

    public abstract int removeMessages(MessageObjectFilter filter) throws ControllerException;

    public abstract int pruneMessages(MessageObjectFilter filter, int limit) throws ControllerException;

    public abstract void removeFilterTable(String uid);

    public abstract void clearMessages(String channelId) throws ControllerException;

    public abstract void reprocessMessages(final MessageObjectFilter filter, final boolean replace, final List<String> destinations) throws ControllerException;

    public abstract void processMessage(MessageObject message) throws ControllerException;

    // util methods

    public abstract MessageObject cloneMessageObjectForBroadcast(MessageObject messageObject, String connectorName);

    public abstract MessageObject getMessageObjectFromEvent(UMOEvent event) throws Exception;

    // status

    public abstract void setError(MessageObject messageObject, String errorType, String errorMessage, Throwable e, Object payload);

    public abstract void setSuccess(MessageObject messageObject, String responseMessage, Object payload);

    public abstract void setTransformed(MessageObject messageObject, Object payload);

    public abstract void setQueued(MessageObject messageObject, String responseMessage, Object payload);

    public abstract void setFiltered(MessageObject messageObject, String responseMessage, Object payload);

    public abstract void resetQueuedStatus(MessageObject messageObject);

    // attachments

    public abstract Attachment getAttachment(String attachmentId) throws ControllerException;

    public abstract List<Attachment> getAttachmentsByMessage(MessageObject messageObject) throws ControllerException;

    public abstract List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException;

    public abstract List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException;

    public abstract void insertAttachment(Attachment attachment);

    public abstract void deleteAttachments(MessageObject message);

    public abstract void deleteUnusedAttachments();

    public abstract Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException;

    public abstract Attachment createAttachment(Object data, String type, MessageObject messageObject) throws UnsupportedDataTypeException;

    public abstract void setAttachmentMessageId(MessageObject messageObject, Attachment attachment);
}
