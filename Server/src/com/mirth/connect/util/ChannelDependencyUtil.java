/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.model.ChannelDependency;

public class ChannelDependencyUtil {

    public static ChannelDependencyGraph getDependencyGraph(Set<ChannelDependency> dependencies) throws ChannelDependencyException {
        return new ChannelDependencyGraph(new HashSet<ChannelDependency>(CollectionUtils.emptyIfNull(dependencies)));
    }

    public static OrderedChannels getOrderedChannels(Set<ChannelDependency> dependencies, Set<String> channelIds) throws ChannelDependencyException {
        return getOrderedChannels(channelIds, getDependencyGraph(dependencies));
    }

    /**
     * Given a set of channel IDs and a dependency graph, this method splits the IDs into a set of
     * unordered IDs (that have no dependents or dependencies), and a list of multiple sets of
     * ordered IDs. The list is ordered such that the IDs in any given set are not interdependent on
     * each other, but all the IDs in the set are individually dependent on one or more IDs in one
     * of the subsequent sets.
     */
    public static OrderedChannels getOrderedChannels(Set<String> channelIds, ChannelDependencyGraph graph) {
        Set<String> unorderedIds = new HashSet<String>(channelIds);
        List<Set<String>> orderedIds = new ArrayList<Set<String>>();

        for (Set<String> set : graph.getOrderedElements()) {
            // Only include IDs in the set passed in
            set.retainAll(channelIds);

            if (!set.isEmpty()) {
                orderedIds.add(set);
                unorderedIds.removeAll(set);
            }
        }

        return new OrderedChannels(unorderedIds, orderedIds);
    }

    public static class OrderedChannels {

        private Set<String> unorderedIds;
        private List<Set<String>> orderedIds;

        public OrderedChannels(Set<String> unorderedIds, List<Set<String>> orderedIds) {
            this.unorderedIds = unorderedIds;
            this.orderedIds = orderedIds;
        }

        public Set<String> getUnorderedIds() {
            return unorderedIds;
        }

        public List<Set<String>> getOrderedIds() {
            return orderedIds;
        }
    }
}