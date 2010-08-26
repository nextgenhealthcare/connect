/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.transformers;

import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.transformer.TransformerException;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.util.JavaScriptUtil;

public class JavaScriptPreprocessor extends AbstractEventAwareTransformer {
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public Object transform(Object src, UMOEventContext muleContext) throws TransformerException {
        String message = new String();

        if (src instanceof MessageObject) {
            return src;
        } else if (src instanceof String) {
            message = (String) src;
        }

        return JavaScriptUtil.getInstance().executePreprocessorScripts(message, muleContext, getChannelId());
    }
}
