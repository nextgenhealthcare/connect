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
import com.mirth.connect.donkey.server.channel.components.FilterResponse;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;

public class TestFilterTransformer implements FilterTransformer {
    @Override
    public boolean doFilterTransform(ConnectorMessage message) {
        FilterResponse filterResponse = new FilterResponse(false, message.getRaw().getContent());
        return filterResponse.isFiltered();
    }

    @Override
    public void dispose() {

    }
}
