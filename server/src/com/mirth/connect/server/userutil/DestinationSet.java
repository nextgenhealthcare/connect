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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mozilla.javascript.Context;

import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Utility class used in the preprocessor or source filter/transformer to prevent the message from
 * being sent to specific destinations.
 */
public class DestinationSet implements Set<Integer> {

    private Map<String, Integer> destinationIdMap = Collections.emptyMap();
    private Set<Integer> metaDataIds;

    /**
     * DestinationSet instances should NOT be constructed manually. The instance "destinationSet"
     * provided in the scope should be used.
     * 
     * @param connectorMessage
     *            The delegate ImmutableConnectorMessage object.
     */
    public DestinationSet(ImmutableConnectorMessage connectorMessage) {
        try {
            if (connectorMessage.getSourceMap().containsKey(Constants.DESTINATION_SET_KEY)) {
                this.destinationIdMap = connectorMessage.getDestinationIdMap();
                this.metaDataIds = (Set<Integer>) connectorMessage.getSourceMap().get(Constants.DESTINATION_SET_KEY);
            }
        } catch (Exception e) {
            metaDataIds = new HashSet<>();
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
        return remove(Collections.singleton(metaDataIdOrConnectorName));

        // Optional<Integer> metaDataId = convertToMetaDataId(metaDataIdOrConnectorName);

        // if (metaDataId.isPresent()) {
        //     return metaDataIds.remove(metaDataId.get());
        // }

        // return false;
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
        if(metaDataIdOrConnectorNames == null) { return false; }

        return metaDataIdOrConnectorNames.stream()
            .map(this::convertToMetaDataId)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(metaDataIds::remove)
            .filter(Boolean::booleanValue)
            .count() > 0;

        // boolean removed = false;
        // for(Object item : metaDataIdOrConnectorNames) {
        //     Optional<Integer> m = convertToMetaDataId(item);
        //     if(m.isPresent() && metaDataIds.remove(m.get())) {
        //         removed = true;
        //     }
        // }
        // return removed;

        //working
        // boolean removed = false;

        // for (Object metaDataIdOrConnectorName : metaDataIdOrConnectorNames) {
        //     if (remove(metaDataIdOrConnectorName)) {
        //         removed = true;
        //     }
        // }

        // return removed;
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
        //Optional<Integer> metaDataId = convertToMetaDataId(metaDataIdOrConnectorName);
        //return metaDataId.isPresent() && metaDataIds.retainAll(Collections.singleton(metaDataId.get()));
        return removeAllExcept(Collections.singleton(metaDataIdOrConnectorName));
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
        if(metaDataIdOrConnectorNames == null) { return false; }
        
        Set<Integer> set = metaDataIdOrConnectorNames.stream()
            .map(this::convertToMetaDataId)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());

        return metaDataIds.retainAll(set);
    }

    /**
     * Stop all destinations from being processed for this message. This does NOT mark the source
     * message as FILTERED.
     * 
     * @return A boolean indicating whether at least one destination connector was actually removed
     *         from processing for this message.
     */
    public boolean removeAll() {
        int origSize = size();
        clear();
        return origSize > 0;
    }

    private Optional<Integer> convertToMetaDataId(Object metaDataIdOrConnectorName) {
        Integer result = null;

        if (metaDataIdOrConnectorName != null) {
            if (metaDataIdOrConnectorName instanceof Number) {
                result = Integer.valueOf(((Number) metaDataIdOrConnectorName).intValue());
            } else if (metaDataIdOrConnectorName.getClass().getName().equals("org.mozilla.javascript.NativeNumber")) {
                result = (Integer) Context.jsToJava(metaDataIdOrConnectorName, int.class);
            } else {
                result = destinationIdMap.get(metaDataIdOrConnectorName.toString());
            }
        }

        return Optional.ofNullable(result);
    }

    @Override
    public int size() {
        return metaDataIds.size();
    }

    @Override
    public boolean isEmpty() {
        return metaDataIds.isEmpty();
    }

    @Override
    public boolean contains(Object metaDataIdOrConnectorName) {
        Optional<Integer> m = convertToMetaDataId(metaDataIdOrConnectorName);

        return m.isPresent() && metaDataIds.contains(m.get());
    }

    @Override
    public Iterator<Integer> iterator() {
        return Collections.unmodifiableSet(metaDataIds).iterator();
    }

    @Override
    public Object[] toArray() {
        return metaDataIds.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return metaDataIds.toArray(a);
    }

    @Override
    public boolean add(Integer metaDataId) {
        return metaDataId != null && metaDataIds.add(metaDataId);
    }

    @Override
    public boolean containsAll(Collection<?> metaDataIdOrConnectorNames) {
        if(metaDataIdOrConnectorNames == null) { return false; }

        return metaDataIdOrConnectorNames.stream()
        .map(this::contains)
        .allMatch(Boolean::booleanValue);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> metaDataIdOrConnectorNames) {
        boolean changed = false;

        if(metaDataIdOrConnectorNames != null) {
            for(Object item : metaDataIdOrConnectorNames) {
                Optional<Integer> m = convertToMetaDataId(item);

                if(m.isPresent() && metaDataIds.add(m.get())) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> metaDataIdOrConnectorNames) {
        //List<Object> objList = metaDataIdOrConnectorNames.stream().map(m -> (Object)m).collect(Collectors.toList());
        //return removeAllExcept(objList);

        return removeAllExcept((Collection<Object>)metaDataIdOrConnectorNames);
    }

    @Override
    public boolean removeAll(Collection<?> metaDataIdOrConnectorNames) {
        //List<Object> list = metaDataIdOrConnectorNames.stream().map(m -> (Object)m).collect(Collectors.toList());
        //return remove(list);

        return remove((Collection<Object>)metaDataIdOrConnectorNames);
    }

    @Override
    public void clear() {
        metaDataIds.clear();
    }
}