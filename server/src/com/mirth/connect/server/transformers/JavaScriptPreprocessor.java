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
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.util.JavaScriptUtil;

public class JavaScriptPreprocessor implements PreProcessor {
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String doPreProcess(ConnectorMessage message) throws DonkeyException, InterruptedException {
        try {
            return JavaScriptUtil.executePreprocessorScripts(message, getChannelId());
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new DonkeyException(e, ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_000, "Error running preprocessor scripts", e));
        }
    }
}
