/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.javascript;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private String scriptId;
    private Set<String> resourceIds;
    private volatile String contextFactoryId;

    public JavaScriptAttachmentHandlerProvider(MessageController messageController) {
        super(messageController);
    }

    MirthContextFactory getContextFactory() throws Exception {
        MirthContextFactory contextFactory = contextFactoryController.getContextFactory(resourceIds);

        if (!contextFactoryId.equals(contextFactory.getId())) {
            synchronized (this) {
                contextFactory = contextFactoryController.getContextFactory(resourceIds);

                if (!contextFactoryId.equals(contextFactory.getId())) {
                    JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                    contextFactoryId = contextFactory.getId();
                }
            }
        }

        return contextFactory;
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {
        String attachmentScript = attachmentProperties.getProperties().get("javascript.script");

        if (attachmentScript != null) {
            scriptId = ScriptController.getScriptId(ScriptController.ATTACHMENT_SCRIPT_KEY, channel.getChannelId());

            try {
                Set<String> scriptOptions = new HashSet<String>();
                scriptOptions.add("useAttachmentList");
                resourceIds = channel.getResourceIds();
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(resourceIds);
                contextFactoryId = contextFactory.getId();
                JavaScriptUtil.compileAndAddScript(channel.getChannelId(), contextFactory, scriptId, attachmentScript, ContextType.CHANNEL_ATTACHMENT, scriptOptions);
            } catch (Exception e) {
                logger.error("Error compiling attachment handler script " + scriptId + ".", e);
            }
        }
    }

    @Override
    public boolean canExtractAttachments() {
        return true;
    }

    @Override
    public byte[] replaceOutboundAttachment(byte[] content) throws Exception {
        return content;
    }

    @Override
    public AttachmentHandler getHandler() {
        return new JavaScriptAttachmentHandler(this);
    }
}