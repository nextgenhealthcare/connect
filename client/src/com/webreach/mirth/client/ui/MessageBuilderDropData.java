package com.webreach.mirth.client.ui;

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
