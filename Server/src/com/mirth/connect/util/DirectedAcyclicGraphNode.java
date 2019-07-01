/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A directed acyclic graph node. The node contains an element, and all direct dependents and
 * dependencies are kept track of.
 */
public class DirectedAcyclicGraphNode<E> {

    private E element;
    private Map<E, DirectedAcyclicGraphNode<E>> dependentNodes = new HashMap<E, DirectedAcyclicGraphNode<E>>();
    private Map<E, DirectedAcyclicGraphNode<E>> dependencyNodes = new HashMap<E, DirectedAcyclicGraphNode<E>>();

    public DirectedAcyclicGraphNode(E element) {
        this.element = element;
    }

    /**
     * Returns the unique element associated with this node.
     */
    public E getElement() {
        return element;
    }

    /**
     * Returns true if this node is not depended on by any other node.
     */
    public boolean isSink() {
        return dependentNodes.isEmpty();
    }

    /**
     * Returns true if this node is not dependent on any other node.
     */
    public boolean isSource() {
        return dependencyNodes.isEmpty();
    }

    /**
     * Returns a set of all directly dependent nodes.
     */
    Set<DirectedAcyclicGraphNode<E>> getDependentNodes() {
        return new HashSet<DirectedAcyclicGraphNode<E>>(dependentNodes.values());
    }

    /**
     * Searches through this and all descendant dependents to find the node associated with the
     * given element. Returns null if no such node was found.
     */
    public DirectedAcyclicGraphNode<E> findDependentNode(E element) {
        if (Objects.equals(getElement(), element)) {
            return this;
        }

        DirectedAcyclicGraphNode<E> dependentNode = dependentNodes.get(element);

        if (dependentNode == null) {
            for (DirectedAcyclicGraphNode<E> node : dependentNodes.values()) {
                dependentNode = node.findDependentNode(element);
                if (dependentNode != null) {
                    break;
                }
            }
        }

        return dependentNode;
    }

    /**
     * Returns a set of all directly dependent elements of this node.
     */
    public Set<E> getDirectDependentElements() {
        return new LinkedHashSet<E>(dependentNodes.keySet());
    }

    /**
     * Returns a set of all directly dependent nodes of this node.
     */
    public Set<DirectedAcyclicGraphNode<E>> getDirectDependentNodes() {
        return new LinkedHashSet<DirectedAcyclicGraphNode<E>>(dependentNodes.values());
    }

    /**
     * Returns a set of all dependent elements of this node, both direct and indirect.
     */
    public Set<E> getAllDependentElements() {
        Set<E> dependentElements = new LinkedHashSet<E>();
        getAllDependentElements(dependentElements);
        return dependentElements;
    }

    private void getAllDependentElements(Set<E> set) {
        for (DirectedAcyclicGraphNode<E> node : dependentNodes.values()) {
            set.add(node.getElement());
            node.getAllDependentElements(set);
        }
    }

    /**
     * Injects the given node as a dependent of this node.
     */
    void putDependentNode(DirectedAcyclicGraphNode<E> dependentNode) {
        dependentNodes.put(dependentNode.getElement(), dependentNode);
    }

    /**
     * Returns a set of all direct dependencies of this node.
     */
    Set<DirectedAcyclicGraphNode<E>> getDependencyNodes() {
        return new HashSet<DirectedAcyclicGraphNode<E>>(dependencyNodes.values());
    }

    /**
     * Searches through this and all descendant dependencies to find the node associated with the
     * given element. Returns null if no such node was found.
     */
    public DirectedAcyclicGraphNode<E> findDependencyNode(E element) {
        if (Objects.equals(getElement(), element)) {
            return this;
        }

        DirectedAcyclicGraphNode<E> dependencyNode = dependencyNodes.get(element);

        if (dependencyNode == null) {
            for (DirectedAcyclicGraphNode<E> node : dependencyNodes.values()) {
                dependencyNode = node.findDependencyNode(element);
                if (dependencyNode != null) {
                    break;
                }
            }
        }

        return dependencyNode;
    }

    /**
     * Returns a set of all direct dependency elements of this node.
     */
    public Set<E> getDirectDependencyElements() {
        return new LinkedHashSet<E>(dependencyNodes.keySet());
    }

    /**
     * Returns a set of all direct dependency nodes of this node.
     */
    public Set<DirectedAcyclicGraphNode<E>> getDirectDependencyNodes() {
        return new LinkedHashSet<DirectedAcyclicGraphNode<E>>(dependencyNodes.values());
    }

    /**
     * Returns a set of all dependency elements of this node, both direct and indirect.
     */
    public Set<E> getAllDependencyElements() {
        Set<E> dependencyIds = new LinkedHashSet<E>();
        getAllDependencyElements(dependencyIds);
        return dependencyIds;
    }

    private void getAllDependencyElements(Set<E> set) {
        for (DirectedAcyclicGraphNode<E> node : dependencyNodes.values()) {
            set.add(node.getElement());
            node.getAllDependencyElements(set);
        }
    }

    /**
     * Injects the given node as a dependency of this node.
     */
    void putDependencyNode(DirectedAcyclicGraphNode<E> dependencyNode) {
        dependencyNodes.put(dependencyNode.getElement(), dependencyNode);
    }

    /**
     * Returns a set containing this node if it's a sink, or all sink nodes dependent on this node.
     */
    Set<DirectedAcyclicGraphNode<E>> getSinkNodes() {
        Set<DirectedAcyclicGraphNode<E>> sinkNodes = new HashSet<DirectedAcyclicGraphNode<E>>();

        if (isSink()) {
            sinkNodes.add(this);
        } else {
            for (DirectedAcyclicGraphNode<E> node : dependentNodes.values()) {
                sinkNodes.addAll(node.getSinkNodes());
            }
        }

        return sinkNodes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof DirectedAcyclicGraphNode) {
            return Objects.equals(element, ((DirectedAcyclicGraphNode<?>) obj).getElement());
        }
        return false;
    }
}