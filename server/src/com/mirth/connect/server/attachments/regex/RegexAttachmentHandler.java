/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.regex;

import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class RegexAttachmentHandler implements AttachmentHandler {

    private RegexAttachmentHandlerProvider provider;
    private Matcher matcher;
    private String message;
    private StringBuilder newMessage;
    private int offset;
    private int group;

    public RegexAttachmentHandler(RegexAttachmentHandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {
        if (message.isBinary()) {
            throw new AttachmentException("Binary data not supported for Regex attachment handler");
        }

        try {
            this.message = message.getRawData();
            newMessage = new StringBuilder();
            offset = 0;

            if (provider.getPattern() != null) {
                matcher = provider.getPattern().matcher(message.getRawData());
                //TODO Validate number of groups that the user can provide
                group = matcher.groupCount();
            }
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        try {
            if (matcher != null) {
                while (matcher.find()) {

                    String uuid = ServerUUIDGenerator.getUUID();
                    String attachmentString = message.substring(matcher.start(group), matcher.end(group));

                    for (Entry<String, String> replacementEntry : provider.getInboundReplacements().entrySet()) {
                        String replaceKey = replacementEntry.getKey();
                        String replaceValue = replacementEntry.getValue();

                        if (replaceKey != null && replaceValue != null) {
                            attachmentString = attachmentString.replace(replaceKey, replaceValue);
                        }
                    }

                    // Don't store blank attachments.
                    if (StringUtils.isBlank(attachmentString)) {
                        return null;
                    }

                    Attachment attachment = new Attachment(uuid, StringUtil.getBytesUncheckedChunked(attachmentString, Constants.ATTACHMENT_CHARSET), provider.getMimeType());

                    attachmentString = null;

                    newMessage.append(message.substring(offset, matcher.start(group)));
                    newMessage.append(attachment.getAttachmentId());

                    offset = matcher.end(group);

                    return attachment;
                }
            }

            return null;
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public String shutdown() throws AttachmentException {
        try {
            newMessage.append(message.substring(offset));
            // We are finished with the matcher and message now so we can free their memory
            matcher = null;
            message = null;

            String finalMessage = newMessage.toString();
            // We are finished with the new message buffer now so we can free its memory
            newMessage = null;

            return finalMessage;
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }
}