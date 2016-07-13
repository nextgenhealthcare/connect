/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.regex;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class RegexAttachmentHandler implements AttachmentHandler {

    private RegexAttachmentHandlerProvider provider;
    private int currentPattern;
    private Matcher matcher;
    private String message;
    private Map<String, Object> sourceMap;
    private StringBuilder newMessage;
    private int offset;
    private int group;

    public RegexAttachmentHandler(RegexAttachmentHandlerProvider provider) {
        this.provider = provider;
    }

    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {
        try {
            if (message.isBinary()) {
                String messageData = org.apache.commons.codec.binary.StringUtils.newStringUsAscii(Base64Util.encodeBase64(message.getRawBytes()));
                message.clearMessage();
                message = new RawMessage(messageData, message.getDestinationMetaDataIds(), message.getSourceMap());
            }

            this.message = message.getRawData();
            this.sourceMap = message.getSourceMap();
            newMessage = new StringBuilder();
            offset = 0;

            if (CollectionUtils.isNotEmpty(provider.getRegexInfo())) {
                currentPattern = 0;
                matcher = provider.getRegexInfo().get(currentPattern).getPattern().matcher(message.getRawData());
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
                boolean done = false;

                while (!done) {
                    while (matcher.find()) {
                        String uuid = ServerUUIDGenerator.getUUID();
                        String attachmentString = message.substring(matcher.start(group), matcher.end(group));

                        for (Entry<String, String> replacementEntry : provider.getInboundReplacements().entrySet()) {
                            String replaceKey = replacementEntry.getKey();
                            String replaceValue = replacementEntry.getValue();

                            if (replaceKey != null && replaceValue != null) {
                                replaceKey = provider.getReplacer().replaceValues(replaceKey, provider.getChannelId(), provider.getChannelName(), sourceMap);
                                replaceValue = provider.getReplacer().replaceValues(replaceValue, provider.getChannelId(), provider.getChannelName(), sourceMap);

                                attachmentString = attachmentString.replace(replaceKey, replaceValue);
                            }
                        }

                        // Always append the part of the message before the attachment and update the offset
                        newMessage.append(message.substring(offset, matcher.start(group)));
                        offset = matcher.end(group);

                        // Don't store blank attachments.
                        if (StringUtils.isNotBlank(attachmentString)) {
                            String mimeType = provider.getReplacer().replaceValues(provider.getRegexInfo().get(currentPattern).getMimeType(), provider.getChannelId(), provider.getChannelName(), sourceMap);
                            Attachment attachment = new Attachment(uuid, StringUtil.getBytesUncheckedChunked(attachmentString, Constants.ATTACHMENT_CHARSET), mimeType);

                            attachmentString = null;

                            newMessage.append(attachment.getAttachmentId());

                            return attachment;
                        } else {
                            // If a blank attachment was encountered, still append it to the message so nothing is lost
                            newMessage.append(attachmentString);
                        }
                    }

                    if (currentPattern < provider.getRegexInfo().size() - 1) {
                        // Advance to the next pattern
                        currentPattern++;

                        // Reset the message and offset for the next matcher
                        newMessage.append(message.substring(offset));
                        message = newMessage.toString();
                        newMessage = new StringBuilder();
                        offset = 0;

                        // Create the next matcher
                        matcher = provider.getRegexInfo().get(currentPattern).getPattern().matcher(message);
                        group = matcher.groupCount();
                    } else {
                        done = true;
                    }
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