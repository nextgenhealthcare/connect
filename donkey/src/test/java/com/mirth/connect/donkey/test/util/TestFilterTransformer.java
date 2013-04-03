/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;

public class TestFilterTransformer implements FilterTransformer {

    private boolean transformed = false;

    public boolean isTransformed() {
        return transformed;
    }

    public TestFilterTransformer() {}

    @Override
    public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
        transformed = true;
        return new FilterTransformerResult(false, null);
    }

    @Override
    public void dispose() {

    }
}
