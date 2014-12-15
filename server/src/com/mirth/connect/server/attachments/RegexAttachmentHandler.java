/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.util.StringUtil;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class RegexAttachmentHandler extends MirthAttachmentHandler {

    private Pattern pattern;
    private Matcher matcher;
    private String mimeType;
    private String message;
    private StringBuilder newMessage;
    private Map<String, String> replacements = new HashMap<String, String>();
    private int offset;
    private int group;

    public RegexAttachmentHandler() {

    }

    @Override
    public void initialize(String message, Channel channel) throws AttachmentException {
        try {
            this.message = message;
            newMessage = new StringBuilder();
            offset = 0;

            if (pattern != null) {
                matcher = pattern.matcher(message);
                //TODO Validate number of groups that the user can provide
                group = matcher.groupCount();
            }
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public void initialize(byte[] bytes, Channel channel) throws AttachmentException {
        throw new AttachmentException("Binary data not supported for Regex attachment handler");
    }

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        try {
            if (matcher != null) {
                while (matcher.find()) {

                    String uuid = ServerUUIDGenerator.getUUID();
                    String attachmentString = message.substring(matcher.start(group), matcher.end(group));

                    for (Entry<String, String> replacementEntry : replacements.entrySet()) {
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

                    Attachment attachment = new Attachment(uuid, StringUtil.getBytesUncheckedChunked(attachmentString, Constants.ATTACHMENT_CHARSET), mimeType);

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

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        String regex = attachmentProperties.getProperties().get("regex.pattern");
        mimeType = attachmentProperties.getProperties().get("regex.mimetype");

        int count = 0;
        while (attachmentProperties.getProperties().containsKey("regex.replaceKey" + count)) {
            replacements.put(StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceKey" + count)), StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceValue" + count)));
            count++;
        }

        if (StringUtils.isNotEmpty(regex)) {
            pattern = Pattern.compile(regex);
        } else {
            pattern = null;
        }
    }

    @Override
    public boolean canExtractAttachments() {
        return true;
    }
}
