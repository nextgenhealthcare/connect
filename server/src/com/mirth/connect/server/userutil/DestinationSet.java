/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Utility class used in the preprocessor or source filter/transformer to prevent the message from
 * being sent to specific destinations.
 */
public class DestinationSet {

    private Map<String, Integer> destinationIdMap;
    private Set<Integer> metaDataIds;

    /**
     * DestinationSet instances should NOT be constructed manually. The instance "destinationSet"
     * provided in the scope should be used.
     */
    public DestinationSet(ImmutableConnectorMessage connectorMessage) {
        try {
            if (connectorMessage.getSourceMap().containsKey(Constants.DESTINATION_SET_KEY)) {
                this.destinationIdMap = connectorMessage.getDestinationIdMap();
                this.metaDataIds = (Set<Integer>) connectorMessage.getSourceMap().get(Constants.DESTINATION_SET_KEY);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Stop a destination from being processed for this message.
     * 
     * @param metaDataIdOrConnectorName
     *            An integer representing the metaDataId of a destination connector, or the actual
     *            destination connector name.
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean remove(Object metaDataIdOrConnectorName) {
        if (metaDataIds != null) {
            Integer metaDataId = convertToMetaDataId(metaDataIdOrConnectorName);

            if (metaDataId != null) {
                return metaDataIds.remove(metaDataId);
            }
        }

        return false;
    }

    /**
     * Stop a destination from being processed for this message.
     * 
     * @param metaDataIdOrConnectorNames
     *            A collection of integers representing the metaDataId of a destination connectors,
     *            or the actual destination connector names. JavaScript arrays can be used.
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean remove(Collection<Object> metaDataIdOrConnectorNames) {
        boolean removed = false;

        for (Object metaDataIdOrConnectorName : metaDataIdOrConnectorNames) {
            if (remove(metaDataIdOrConnectorName)) {
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Stop all except one destination from being processed for this message.
     * 
     * @param metaDataIdOrConnectorName
     *            An integer representing the metaDataId of a destination connector, or the actual
     *            destination connector name.
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean removeAllExcept(Object metaDataIdOrConnectorName) {
        if (metaDataIds != null) {
            Integer metaDataId = convertToMetaDataId(metaDataIdOrConnectorName);

            if (metaDataId != null) {
                return metaDataIds.retainAll(Collections.singleton(metaDataId));
            }
        }

        return false;
    }

    /**
     * Stop all except one destination from being processed for this message.
     * 
     * @param metaDataIdOrConnectorNames
     *            A collection of integers representing the metaDataId of a destination connectors,
     *            or the actual destination connector names. JavaScript arrays can be used.
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean removeAllExcept(Collection<Object> metaDataIdOrConnectorNames) {
        if (metaDataIds != null) {
            Set<Integer> set = new HashSet<Integer>();

            for (Object metaDataIdOrConnectorName : metaDataIdOrConnectorNames) {
                Integer metaDataId = convertToMetaDataId(metaDataIdOrConnectorName);

                if (metaDataId != null) {
                    set.add(metaDataId);
                }
            }

            return metaDataIds.retainAll(set);
        }

        return false;
    }

    /**
     * Stop all destinations from being processed for this message. This does NOT mark the source
     * message as FILTERED.
     * 
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean removeAll() {
        if (metaDataIds != null && metaDataIds.size() > 0) {
            metaDataIds.clear();
            return true;
        }

        return false;
    }

    private Integer convertToMetaDataId(Object metaDataIdOrConnectorName) {
        if (metaDataIdOrConnectorName != null) {
            if (metaDataIdOrConnectorName instanceof Number) {
                return ((Number) metaDataIdOrConnectorName).intValue();
            } else if (metaDataIdOrConnectorName.getClass().getName().equals("org.mozilla.javascript.NativeNumber")) {
                return (Integer) Context.jsToJava(metaDataIdOrConnectorName, int.class);
            } else if (destinationIdMap != null) {
                return destinationIdMap.get(metaDataIdOrConnectorName.toString());
            }
        }

        return null;
    }
}