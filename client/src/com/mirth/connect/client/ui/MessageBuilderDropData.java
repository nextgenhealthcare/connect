/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.tree.TreeNode;

public class MessageBuilderDropData {

    private TreeNode node;
    private String messageSegment;
    private String mapping;

    public MessageBuilderDropData(TreeNode node, String messageSegment, String mapping) {
        setNode(node);
        setMessageSegment(messageSegment);
        setMapping(mapping);
    }

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getMessageSegment() {
        return messageSegment;
    }

    public void setMessageSegment(String messageSegment) {
        this.messageSegment = messageSegment;
    }
}
