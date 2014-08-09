/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import java.util.Map;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptPreprocessor implements PreProcessor {

    private JavaScriptPreProcessorTask task = new JavaScriptPreProcessorTask();
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    private Map<String, Integer> destinationIdMap;

    public JavaScriptPreprocessor(Map<String, Integer> destinationIdMap) {
        this.destinationIdMap = destinationIdMap;
    }

    @Override
    public String doPreProcess(ConnectorMessage message) throws DonkeyException, InterruptedException {
        try {
            task.setMessage(message);
            return JavaScriptUtil.executeJavaScriptPreProcessorTask(task, message.getChannelId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Throwable t = e;
            if (e instanceof JavaScriptExecutorException) {
                t = e.getCause();
            }

            eventController.dispatchEvent(new ErrorEvent(message.getChannelId(), null, ErrorEventType.PREPROCESSOR_SCRIPT, null, null, "Error running preprocessor scripts", t));
            throw new DonkeyException(t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.PREPROCESSOR_SCRIPT.toString(), "Error running preprocessor scripts", t));
        }
    }

    private class JavaScriptPreProcessorTask extends JavaScriptTask<Object> {

        private ConnectorMessage message;

        public void setMessage(ConnectorMessage message) {
            this.message = message;
        }

        @Override
        public Object call() throws Exception {
            return JavaScriptUtil.executePreprocessorScripts(this, message, destinationIdMap);
        }
    }
}
