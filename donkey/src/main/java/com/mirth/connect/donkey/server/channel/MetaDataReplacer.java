/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;

public class MetaDataReplacer {
    private Logger logger = Logger.getLogger(getClass());

    /*
     * Extracts any custom columns from the connector message and adds them to
     * the custom column map.
     */
    public void setMetaDataMap(ConnectorMessage connectorMessage, List<MetaDataColumn> metaDataColumns) {
        for (MetaDataColumn column : metaDataColumns) {
            if (StringUtils.isNotEmpty(column.getMappingName())) {
                Object value = getMetaDataValue(connectorMessage, column);
                try {
                    castAndSetValue(connectorMessage, column, value);
                } catch (MetaDataColumnException e) {
                    // If there is an error casting the value, log a warning but continue with processing because
                    // the metadata values are not essential for processing
                    logger.warn("Could not cast value '" + value.toString() + "' to " + column.getType().toString(), e);
                }
            }
        }
    }

    protected Object getMetaDataValue(ConnectorMessage connectorMessage, MetaDataColumn column) {
        Object value = null;

        if (connectorMessage.getConnectorMap().containsKey(column.getMappingName())) {
            value = connectorMessage.getConnectorMap().get(column.getMappingName());
        } else if (connectorMessage.getChannelMap().containsKey(column.getMappingName())) {
            value = connectorMessage.getChannelMap().get(column.getMappingName());
        }

        return value;
    }

    private void castAndSetValue(ConnectorMessage connectorMessage, MetaDataColumn column, Object value) throws MetaDataColumnException {
        if (value != null) {
            try {
                connectorMessage.getMetaDataMap().put(column.getName(), column.getType().castMetaDataFromString(value.toString()));
            } catch (MetaDataColumnException e) {
                throw new MetaDataColumnException(e, column);
            }
        }
    }
}
