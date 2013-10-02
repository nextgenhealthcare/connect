/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;

public class TestDestinationChain extends DestinationChain {
    private Map<Integer, FilterTransformerExecutor> filterTransformerExecutors = new HashMap<Integer, FilterTransformerExecutor>();

    public Map<Integer, FilterTransformerExecutor> getFilterTransformerExecutors() {
        return filterTransformerExecutors;
    }

    @Override
    public void addDestination(int metaDataId, FilterTransformerExecutor filterTransformerExecutor, DestinationConnector connector) {
        filterTransformerExecutors.put(metaDataId, filterTransformerExecutor);
        super.addDestination(metaDataId, filterTransformerExecutor, connector);
    }
}
