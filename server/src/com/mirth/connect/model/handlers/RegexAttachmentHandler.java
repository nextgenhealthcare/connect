/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.util.UUIDGenerator;

public class RegexAttachmentHandler extends AttachmentHandler {

    private Pattern pattern;
    private Matcher matcher;
    private String mimeType;
    private String message;
    private String newMessage;
    private Map<String, String> replacements = new HashMap<String, String>();
    private int offset;
    private int group;

    public RegexAttachmentHandler() {

    }

    @Override
    public void initialize(String message, Channel channel) throws AttachmentException {
        try {
            this.message = message;
            newMessage = "";
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
    
                    String uuid = UUIDGenerator.getUUID();
                    String attachmentString = message.substring(matcher.start(group), matcher.end(group));
                    
                    for (Entry<String, String> replacementEntry : replacements.entrySet()) {
                    	String replaceKey = replacementEntry.getKey();
                    	String replaceValue = replacementEntry.getValue();
                    	
                    	if (replaceKey != null && replaceValue != null) {
                    		attachmentString = attachmentString.replace(replaceKey, replaceValue);
                    	}
                    }
                    Attachment attachment = new Attachment(uuid, attachmentString.getBytes(), mimeType);
                    
                    attachmentString = null;
    
                    newMessage += message.substring(offset, matcher.start(group)) + attachment.getAttachmentId();
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
            String finalMessage = newMessage + message.substring(offset);
            
            newMessage = null;
            message = null;
            mimeType = null;
            matcher = null;
            replacements.clear();
            
            return finalMessage;
        } catch (Throwable t) {
            throw new AttachmentException(t);
        }
    }

    @Override
    public void setProperties(AttachmentHandlerProperties attachmentProperties) {
        String regex = attachmentProperties.getProperties().get("regex.pattern");
        mimeType = attachmentProperties.getProperties().get("regex.mimetype");
        
        int count = 0;
        while (attachmentProperties.getProperties().containsKey("regex.replaceKey" + count)) {
        	replacements.put(attachmentProperties.getProperties().get("regex.replaceKey" + count), attachmentProperties.getProperties().get("regex.replaceValue" + count));
        	count++;
        }

        if (StringUtils.isNotEmpty(regex)) {
            pattern = Pattern.compile(regex);
        } else {
            pattern = null;
        }
    }

}
