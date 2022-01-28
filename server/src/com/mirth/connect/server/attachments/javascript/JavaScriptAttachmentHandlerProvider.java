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
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    private Logger logger = Logger.getLogger(getClass());
    private ContextFactoryController contextFactoryController = getContextFactoryController();
    private String scriptId;
    private Set<String> resourceIds;
    private volatile String contextFactoryId;
    private boolean debug = false;
    private MirthMain debugger;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();

    public JavaScriptAttachmentHandlerProvider() {
        // default constructor
    }
    
    public JavaScriptAttachmentHandlerProvider(MessageController messageController) {
        super(messageController);
    }

    MirthContextFactory getContextFactory(Channel channel) throws Exception {
        MirthContextFactory contextFactory = debug ? contextFactoryController.getDebugContextFactory(channel.getResourceIds(), channel.getChannelId(), scriptId) : contextFactoryController.getContextFactory(resourceIds);
        
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
                MirthContextFactory contextFactory;
                this.debug = channel.getDebugOptions() != null && channel.getDebugOptions().isAttachmentBatchScripts();
                
                Set<String> scriptOptions = new HashSet<String>();
                scriptOptions.add("useAttachmentList");
                resourceIds = channel.getResourceIds();
                
                if (debug) {
                    contextFactory = contextFactoryController.getDebugContextFactory(channel.getResourceIds(), channel.getChannelId(), scriptId);
                    contextFactory.setContextType(ContextType.CHANNEL_ATTACHMENT);
                    contextFactory.setScriptText(attachmentScript);
                    contextFactory.setDebugType(true);
                    debugger = getDebugger(contextFactory, channel);
                } else {
                    // default case
                    contextFactory = contextFactoryController.getContextFactory(resourceIds);
                }
                
                contextFactoryId = contextFactory.getId();
                compileAndAddScript(channel, contextFactory, scriptId, attachmentScript, scriptOptions);
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
    
    protected MirthMain getDebugger(MirthContextFactory contextFactory, Channel channel) {
        if (debug) {
            return MirthMain.mirthMainEmbedded(contextFactory, scopeProvider, channel.getName() + "-" + channel.getChannelId(), scriptId);
        } else {
            return null;
        }
    }
    
    protected void showDebugger() {
        if (debug) {
            if (debugger != null) {
                debugger.doBreak();

                if (!debugger.isVisible()) {
                    debugger.setVisible(true);
                }
            }
        }
    }

    protected ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }

    protected void compileAndAddScript(Channel channel, MirthContextFactory contextFactory, String scriptId, String attachmentScript, Set<String> scriptOptions) throws Exception {
        JavaScriptUtil.compileAndAddScript(channel.getChannelId(), contextFactory, scriptId, attachmentScript, ContextType.CHANNEL_ATTACHMENT, scriptOptions);
    }

}