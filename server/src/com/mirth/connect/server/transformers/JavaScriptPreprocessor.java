/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptPreprocessor implements PreProcessor {

    private JavaScriptPreProcessorTask task = new JavaScriptPreProcessorTask();

    @Override
    public String doPreProcess(ConnectorMessage message) throws DonkeyException, InterruptedException {
        try {
            task.setMessage(message);
            return JavaScriptUtil.executeJavaScriptPreProcessorTask(task, message.getChannelId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new DonkeyException(e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_000, "Error running preprocessor scripts", e));
        }
    }

    private class JavaScriptPreProcessorTask extends JavaScriptTask<Object> {

        private ConnectorMessage message;

        public void setMessage(ConnectorMessage message) {
            this.message = message;
        }

        @Override
        public Object call() throws Exception {
            return JavaScriptUtil.executePreprocessorScripts(this, message);
        }
    }
}
