/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.Map;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.server.util.GlobalChannelVariableStoreFactory;
import com.mirth.connect.server.util.GlobalVariableStore;

public class MirthMetaDataReplacer extends MetaDataReplacer {
    
    @Override
    protected Object getMetaDataValue(ConnectorMessage connectorMessage, MetaDataColumn column) {
        Object value = super.getMetaDataValue(connectorMessage, column);
        
        // Check the global channel and global maps if nothing was found in the connector or channel maps
        if (value == null) {
            Map<String, Object> globalChannelMap = GlobalChannelVariableStoreFactory.getInstance().get(connectorMessage.getChannelId()).getVariables();
            Map<String, Object> globalMap = GlobalVariableStore.getInstance().getVariables();
            
            if (globalChannelMap.containsKey(column.getMappingName())) {
                value = globalChannelMap.get(column.getMappingName());
            } else if (globalMap.containsKey(column.getMappingName())) {
                value = globalMap.get(column.getMappingName());
            }
        }
        
        return value;
    }
}
