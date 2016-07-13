/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.mirth.connect.server.util.TemplateValueReplacer;

public class RegexAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private String channelId;
    private String channelName;
    private List<RegexInfo> regexInfoList = new ArrayList<RegexInfo>();
    private Map<String, String> inboundReplacements = new HashMap<String, String>();
    private Map<String, String> outboundReplacements = new HashMap<String, String>();

    TemplateValueReplacer getReplacer() {
        return replacer;
    }

    String getChannelId() {
        return channelId;
    }

    String getChannelName() {
        return channelName;
    }

    List<RegexInfo> getRegexInfo() {
        return regexInfoList;
    }

    Map<String, String> getInboundReplacements() {
        return inboundReplacements;
    }

    Map<String, String> getOutboundReplacements() {
        return outboundReplacements;
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        channelId = channel.getChannelId();
        channelName = channel.getName();

        if (attachmentProperties.getProperties().containsKey("regex.pattern")) {
            String regex = attachmentProperties.getProperties().get("regex.pattern");
            String mimeType = attachmentProperties.getProperties().get("regex.mimetype");
            if (StringUtils.isBlank(mimeType)) {
                mimeType = "text/plain";
            }
            regexInfoList.add(new RegexInfo(Pattern.compile(regex), mimeType));
        }

        int count = 0;
        while (attachmentProperties.getProperties().containsKey("regex.pattern" + count)) {
            String regex = attachmentProperties.getProperties().get("regex.pattern" + count);
            String mimeType = attachmentProperties.getProperties().get("regex.mimetype" + count);
            if (StringUtils.isBlank(mimeType)) {
                mimeType = "text/plain";
            }
            regexInfoList.add(new RegexInfo(Pattern.compile(regex), mimeType));
            count++;
        }

        count = 0;
        while (attachmentProperties.getProperties().containsKey("regex.replaceKey" + count)) {
            inboundReplacements.put(StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceKey" + count)), StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("regex.replaceValue" + count)));
            count++;
        }

        count = 0;
        while (attachmentProperties.getProperties().containsKey("outbound.regex.replaceKey" + count)) {
            outboundReplacements.put(StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("outbound.regex.replaceKey" + count)), StringEscapeUtils.unescapeJava(attachmentProperties.getProperties().get("outbound.regex.replaceValue" + count)));
            count++;
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
                replaceKey = replacer.replaceValues(replaceKey, channelId, channelName);
                replaceValue = replacer.replaceValues(replaceValue, channelId, channelName);

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