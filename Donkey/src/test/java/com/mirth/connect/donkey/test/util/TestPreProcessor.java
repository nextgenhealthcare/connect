/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.components.PreProcessor;

public class TestPreProcessor implements PreProcessor {

    private boolean processed = false;

    public boolean isProcessed() {
        return processed;
    }

    @Override
    public String doPreProcess(ConnectorMessage message) throws DonkeyException {
        processed = true;
        return message.getRaw().getContent();
    }

}
