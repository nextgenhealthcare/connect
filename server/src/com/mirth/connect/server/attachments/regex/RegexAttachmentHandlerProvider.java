/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;

public class RegexAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    private Pattern pattern;
    private String mimeType;
    private Map<String, String> inboundReplacements = new HashMap<String, String>();
    private Map<String, String> outboundReplacements = new HashMap<String, String>();

    Pattern getPattern() {
        return pattern;
    }

    String getMimeType() {
        return mimeType;
    }

    Map<String, String> getInboundReplacements() {
        return inboundReplacements;
    }

    Map<String, String> getOutboundReplacements() {
        return outboundReplacements;
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        String regex = attachmentProperties.getProperties().get("regex.pattern");
        mimeType = attachmentProperties.getProperties().get("regex.mimetype");

        int count = 0;
        while (attachmentProperties.getProperties().containsKey("regex.replaceKey" + count)) {
            inboundReplacements.put(StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceKey" + count)), StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceValue" + count)));
            count++;
        }

        count = 0;
        while (attachmentProperties.getProperties().containsKey("outbound.regex.replaceKey" + count)) {
            outboundReplacements.put(StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("outbound.regex.replaceKey" + count)), StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("outbound.regex.replaceValue" + count)));
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

    @Override
    public byte[] replaceOutboundAttachment(byte[] content) throws Exception {
        String attachmentString = "";
        attachmentString = new String(content, Constants.ATTACHMENT_CHARSET);

        for (Entry<String, String> replacementEntry : outboundReplacements.entrySet()) {
            String replaceKey = replacementEntry.getKey();
            String replaceValue = replacementEntry.getValue();

            if (replaceKey != null && replaceValue != null) {
                attachmentString = attachmentString.replace(replaceKey, replaceValue);
            }
        }

        return attachmentString.getBytes(Constants.ATTACHMENT_CHARSET);
    }

    @Override
    public AttachmentHandler getHandler() {
        return new RegexAttachmentHandler(this);
    }
}