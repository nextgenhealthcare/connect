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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A directed acyclic graph. The sink nodes (those that aren't depended on by any other nodes) are
 * kept track of, and each node in the graph keeps track of all direct dependents and dependencies.
 * Each element in the graph is assumed to be unique.
 */
public class DirectedAcyclicGraph<E> {

    private Map<E, DirectedAcyclicGraphNode<E>> sinkNodes = new HashMap<E, DirectedAcyclicGraphNode<E>>();

    protected DirectedAcyclicGraphNode<E> createNode(E element) {
        return new DirectedAcyclicGraphNode<E>(element);
    }

    /**
     * Adds a new directed edge to the graph.
     * 
     * @param dependentElement
     *            - The element that depends upon the other.
     * @param dependencyElement
     *            - The element being depended on by the other.
     * @throws DirectedAcyclicGraphException
     *             if adding the edge would cause an illegal cycle in the graph.
     */
    public void addDependency(E dependentElement, E dependencyElement) throws DirectedAcyclicGraphException {
        // First try to find the dependent node if it already exists in the graph
        DirectedAcyclicGraphNode<E> matchingDependentNode = getNode(dependentElement);

        if (matchingDependentNode != null) {
            /*
             * If the dependent node exists, make sure that the tentative dependency doesn't already
             * exist as a dependent of the dependent node, which would indicate that an illegal
             * cycle is about to be introduced.
             */
            if (matchingDependentNode.findDependentNode(dependencyElement) != null) {
                throw new DirectedAcyclicGraphException();
            }
        } else {
            // The dependent node wasn't found, so create a new one
            matchingDependentNode = createNode(dependentElement);
            // New dependent nodes are always sinks to start with
            sinkNodes.put(dependentElement, matchingDependentNode);
        }

        // First try to find the dependency node if it already exists in the graph
        DirectedAcyclicGraphNode<E> matchingDependencyNode = getNode(dependencyElement);

        if (matchingDependencyNode == null) {
            // The dependency node wasn't found, so create a new one
            matchingDependencyNode = createNode(dependencyElement);
        }

        // No cycles were detected, so link the two nodes to each other
        matchingDependentNode.putDependencyNode(matchingDependencyNode);
        matchingDependencyNode.putDependentNode(matchingDependentNode);

        // Always remove the dependency element from the sink node map
        sinkNodes.remove(dependencyElement);
    }

    /**
     * Searches through the graph and returns the node associated with the given element, or null if
     * no such node exists.
     */
    public DirectedAcyclicGraphNode<E> getNode(E element) {
        DirectedAcyclicGraphNode<E> node = null;
        for (DirectedAcyclicGraphNode<E> sinkNode : sinkNodes.values()) {
            node = sinkNode.findDependencyNode(element);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

    /**
     * Returns a list of multiple sets of elements such that the elements within a single set are
     * independent of each other, but are dependent on the elements in all subsequent sets. It may
     * not be the case that all elements in a set are dependent on all elements in the subsequent
     * set.
     */
    public List<Set<E>> getOrderedElements() {
        List<Set<E>> orderedElements = new ArrayList<Set<E>>();

        for (Set<DirectedAcyclicGraphNode<E>> nodeSet : getOrderedNodes()) {
            Set<E> elementSet = new HashSet<E>();

            for (DirectedAcyclicGraphNode<E> node : nodeSet) {
                elementSet.add(node.getElement());
            }

            orderedElements.add(elementSet);
        }

        return orderedElements;
    }

    /**
     * Returns a list of multiple sets of nodes such that the nodes within a single set are
     * independent of each other, but are dependent on the nodes in all subsequent sets. It may not
     * be the case that all nodes in a set are dependent on all nodes in the subsequent set.
     */
    public List<Set<DirectedAcyclicGraphNode<E>>> getOrderedNodes() {
        List<Set<DirectedAcyclicGraphNode<E>>> orderedNodes = new ArrayList<Set<DirectedAcyclicGraphNode<E>>>();

        // Visit every sink node with a starting depth of zero
        for (DirectedAcyclicGraphNode<E> sinkNode : sinkNodes.values()) {
            visit(sinkNode, orderedNodes, 0);
        }

        return orderedNodes;
    }

    private void visit(DirectedAcyclicGraphNode<E> node, List<Set<DirectedAcyclicGraphNode<E>>> orderedNodes, int depth) {
        boolean foundHigher = false;
        boolean foundLower = false;

        /*
         * First search in the ordered list and remove the node if it is contained at a higher
         * depth. This indicates that multiple sink nodes are dependent on the current node, but one
         * has a longer path than the other. The longer path should take precedence and cause the
         * current node to be pushed into a lower tier.
         */
        for (int i = 0; i < depth; i++) {
            Set<DirectedAcyclicGraphNode<E>> set = orderedNodes.get(i);
            if (set.contains(node)) {
                set.remove(node);
                foundHigher = true;
                break;
            }
        }

        /*
         * If the node wasn't already found in a higher tier, search the lower tiers now. If it's
         * found at a lower depth then that should take precedence, and should not be added at the
         * current depth.
         */
        if (!foundHigher) {
            for (int i = depth; i < orderedNodes.size(); i++) {
                Set<DirectedAcyclicGraphNode<E>> set = orderedNodes.get(i);
                if (set.contains(node)) {
                    foundLower = true;
                    break;
                }
            }
        }

        // If the node wasn't found at a lower depth, add it at the current depth.
        if (!foundLower) {
            while (orderedNodes.size() <= depth) {
                orderedNodes.add(new HashSet<DirectedAcyclicGraphNode<E>>());
            }
            orderedNodes.get(depth).add(node);
        }

        // Visit all dependencies of the current node recursively
        for (DirectedAcyclicGraphNode<E> dependencyNode : node.getDependencyNodes()) {
            visit(dependencyNode, orderedNodes, depth + 1);
        }
    }
}