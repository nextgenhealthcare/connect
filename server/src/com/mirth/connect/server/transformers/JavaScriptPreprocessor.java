/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import org.apache.log4j.Logger;
import org.mozilla.javascript.RhinoException;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptPreprocessor implements PreProcessor {

    private Logger logger = Logger.getLogger(getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private Channel channel;
    private String scriptId;
    private String contextFactoryId;
    private JavaScriptPreProcessorTask task;

    public JavaScriptPreprocessor(Channel channel, String preProcessingScript) throws JavaScriptInitializationException {
        this.channel = channel;

        scriptId = ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, channel.getChannelId());

        try {
            MirthContextFactory contextFactory = contextFactoryController.getContextFactory(channel.getResourceIds());
            contextFactoryId = contextFactory.getId();
            JavaScriptUtil.compileAndAddScript(contextFactory, scriptId, preProcessingScript, ContextType.CHANNEL_CONTEXT);
            task = new JavaScriptPreProcessorTask(contextFactory);
        } catch (Exception e) {
            logger.error("Error compiling preprocessor script " + scriptId + ".", e);

            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channel.getChannelId(), null, 0, ErrorEventType.PREPROCESSOR_SCRIPT.toString(), null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(ErrorEventType.PREPROCESSOR_SCRIPT.toString(), null, e));
            throw new JavaScriptInitializationException("Error initializing JavaScript Preprocessor", e);
        }
    }

    @Override
    public String doPreProcess(ConnectorMessage message) throws DonkeyException, InterruptedException {
        try {
            MirthContextFactory contextFactory = contextFactoryController.getContextFactory(channel.getResourceIds());
            if (!contextFactoryId.equals(contextFactory.getId())) {
                JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                contextFactoryId = contextFactory.getId();
                task.setContextFactory(contextFactory);
            }

            task.setMessage(message);
            return JavaScriptUtil.executeJavaScriptPreProcessorTask(task, message.getChannelId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Throwable t = e;
            if (e instanceof JavaScriptExecutorException) {
                t = e.getCause();
            }

            eventController.dispatchEvent(new ErrorEvent(message.getChannelId(), null, message.getMessageId(), ErrorEventType.PREPROCESSOR_SCRIPT, null, null, "Error running preprocessor scripts", t));
            throw new DonkeyException(t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.PREPROCESSOR_SCRIPT.toString(), "Error running preprocessor scripts", t));
        }
    }

    private class JavaScriptPreProcessorTask extends JavaScriptTask<Object> {

        private ConnectorMessage message;

        public JavaScriptPreProcessorTask(MirthContextFactory contextFactory) {
            super(contextFactory);
        }

        public void setMessage(ConnectorMessage message) {
            this.message = message;
        }

        @Override
        public Object call() throws Exception {
            return JavaScriptUtil.executePreprocessorScripts(this, message, channel.getSourceConnector().getDestinationIdMap());
        }
    }
}
