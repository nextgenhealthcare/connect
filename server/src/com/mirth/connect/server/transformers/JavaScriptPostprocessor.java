/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.components.PostProcessor;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class JavaScriptPostprocessor implements PostProcessor {

    private JavaScriptPostProcessorTask task = new JavaScriptPostProcessorTask();

    @Override
    public Response doPostProcess(Message message) throws Exception {
        task.setMessage(message);
        return JavaScriptUtil.executeJavaScriptPostProcessorTask(task, message.getChannelId());
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
