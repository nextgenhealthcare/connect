/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.model.ChannelDependency;

/**
 * A directed acyclic graph constructed from a set of channel dependencies. The sink nodes (those
 * that aren't depended on by any other nodes) are kept track of, and each node in the graph keeps
 * track of all direct dependents and dependencies.
 */
public class ChannelDependencyGraph extends DirectedAcyclicGraph<String> {

    public ChannelDependencyGraph(Set<ChannelDependency> dependencies) throws ChannelDependencyException {
        if (CollectionUtils.isNotEmpty(dependencies)) {
            for (ChannelDependency dependency : dependencies) {
                addDependency(dependency);
            }
        }
    }

    public void addDependency(ChannelDependency dependency) throws ChannelDependencyException {
        try {
            addDependency(dependency.getDependentId(), dependency.getDependencyId());
        } catch (DirectedAcyclicGraphException e) {
            throw new ChannelDependencyException(dependency, e);
        }
    }
}