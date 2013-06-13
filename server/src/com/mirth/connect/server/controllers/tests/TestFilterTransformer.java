/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;

public class TestFilterTransformer implements FilterTransformer {
    @Override
    public FilterTransformerResult doFilterTransform(ConnectorMessage message) {
        if (message != null && message.getTransformed() != null) {
            return new FilterTransformerResult(true, message.getTransformed().getContent());
        }

        return new FilterTransformerResult(true, "");
    }

    @Override
    public void dispose() {

    }
}
