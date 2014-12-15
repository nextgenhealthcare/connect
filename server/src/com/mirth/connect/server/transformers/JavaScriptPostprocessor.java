/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.components.PostProcessor;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptPostprocessor implements PostProcessor {

    private JavaScriptPostProcessorTask task = new JavaScriptPostProcessorTask();
    private EventController eventController= ControllerFactory.getFactory().createEventController();

    @Override
    public Response doPostProcess(Message message) throws DonkeyException, InterruptedException {
        try {
            task.setMessage(message);
            return JavaScriptUtil.executeJavaScriptPostProcessorTask(task, message.getChannelId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Throwable t = e;
            if (e instanceof JavaScriptExecutorException) {
                t = e.getCause();
            }

            eventController.dispatchEvent(new ErrorEvent(message.getChannelId(), null, message.getMessageId(), ErrorEventType.POSTPROCESSOR_SCRIPT, null, null, "Error running postprocessor scripts", t));
            throw new DonkeyException(t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.POSTPROCESSOR_SCRIPT.toString(), "Error running postprocessor scripts", t));
        }
    }

    private class JavaScriptPostProcessorTask extends JavaScriptTask<Object> {

        private Message message;

        public void setMessage(Message message) {
            this.message = message;
        }

        @Override
        public Object call() throws Exception {
            return JavaScriptUtil.executePostprocessorScripts(this, message);
        }
    }
}
