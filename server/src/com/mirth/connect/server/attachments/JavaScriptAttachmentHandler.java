/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAttachmentHandler extends MirthAttachmentHandler {

    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private String scriptId;
    private String contextFactoryId;
    private String newMessage;
    private List<com.mirth.connect.server.userutil.Attachment> attachments;
    private int index;

    public JavaScriptAttachmentHandler() {

    }

    @Override
    public void initialize(String message, Channel channel) throws AttachmentException {
        index = 0;
        attachments = new ArrayList<com.mirth.connect.server.userutil.Attachment>();
        try {
            MirthContextFactory contextFactory = contextFactoryController.getContextFactory(channel.getResourceIds());
            if (!contextFactoryId.equals(contextFactory.getId())) {
                JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                contextFactoryId = contextFactory.getId();
            }
            
            newMessage = JavaScriptUtil.executeAttachmentScript(contextFactory, message, channel.getChannelId(), attachments);
        } catch (Throwable t) {
            if (t instanceof JavaScriptExecutorException) {
                t = t.getCause();
            }

            throw new AttachmentException("Error running javascript attachment handler script", t);
        }
    }

    @Override
    public void initialize(byte[] bytes, Channel channel) throws AttachmentException {
        throw new AttachmentException("Binary data not supported for Javascript attachment handler");
    }
    
    @Override
    public Attachment nextAttachment() {
        if (index < attachments.size()) {
            com.mirth.connect.server.userutil.Attachment attachment = attachments.get(index++);
            return new Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
        }

        return null;
    }

    @Override
    public String shutdown() {
        String finalMessage = newMessage;

        newMessage = null;
        attachments = null;

        return finalMessage;
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        String attachmentScript = attachmentProperties.getProperties().get("javascript.script");

        if (attachmentScript != null) {
            scriptId = ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channel.getChannelId());
            
            try {
                Set<String> scriptOptions = new HashSet<String>();
                scriptOptions.add("useAttachmentList");
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(channel.getResourceIds());
                contextFactoryId = contextFactory.getId();
                JavaScriptUtil.compileAndAddScript(contextFactory, scriptId, attachmentScript, ContextType.CHANNEL_CONTEXT, scriptOptions);
            } catch (Exception e) {
                logger.error("Error compiling attachment handler script " + scriptId + ".", e);
            }
        }
    }

    @Override
    public boolean canExtractAttachments() {
        return true;
    }
}
