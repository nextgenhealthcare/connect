/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.mule.umo.UMOEvent;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.filters.MessageObjectFilter;

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

    public abstract List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException;

    public abstract List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException;

    public abstract void insertAttachment(Attachment attachment);

    public abstract void deleteAttachments(MessageObject message);

    public abstract void deleteUnusedAttachments();

    public abstract Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException;

    public abstract Attachment createAttachment(Object data, String type, MessageObject messageObject) throws UnsupportedDataTypeException;
}
