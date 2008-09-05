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

public interface MessageObjectController {
    public void initialize();

    public void removeAllFilterTables();

    public void updateMessage(MessageObject incomingMessageObject, boolean checkIfMessageExists);

    public void importMessage(MessageObject messageObject);

    public int createMessagesTempTable(MessageObjectFilter filter, String uid, boolean forceTemp) throws ControllerException;

    public List<MessageObject> getMessagesByPageLimit(int page, int pageSize, int maxMessages, String uid, MessageObjectFilter filter) throws ControllerException;

    public List<MessageObject> getMessagesByPage(int page, int pageSize, int maxMessages, String uid) throws ControllerException;

    public int removeMessages(MessageObjectFilter filter) throws ControllerException;

    public void removeFilterTable(String uid);

    public void clearMessages(String channelId) throws ControllerException;

    public void reprocessMessages(final MessageObjectFilter filter, final boolean replace, final List<String> destinations) throws ControllerException;

    public void processMessage(MessageObject message) throws ControllerException;

    // util methods

    public MessageObject cloneMessageObjectForBroadcast(MessageObject messageObject, String connectorName);

    public MessageObject getMessageObjectFromEvent(UMOEvent event) throws Exception;

    // status

    public void setError(MessageObject messageObject, String errorType, String errorMessage, Throwable e);

    public void setSuccess(MessageObject messageObject, String responseMessage);

    public void setTransformed(MessageObject messageObject);

    public void setQueued(MessageObject messageObject, String responseMessage);

    public void setFiltered(MessageObject messageObject, String responseMessage);

    public void resetQueuedStatus(MessageObject messageObject);

    // attachments

    public Attachment getAttachment(String attachmentId) throws ControllerException;

    public List<Attachment> getAttachmentsByMessageId(String messageId) throws ControllerException;

    public List<Attachment> getAttachmentIdsByMessageId(String messageId) throws ControllerException;

    public void insertAttachment(Attachment attachment);

    public void deleteAttachments(MessageObject message);

    public void deleteUnusedAttachments();

    public Attachment createAttachment(Object data, String type) throws UnsupportedDataTypeException;

    public Attachment createAttachment(Object data, String type, MessageObject messageObject) throws UnsupportedDataTypeException;
}
