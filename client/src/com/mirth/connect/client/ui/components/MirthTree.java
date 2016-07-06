/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTree;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.MapperDropData;
import com.mirth.connect.client.ui.MessageBuilderDropData;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TreeTransferable;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.MessageTreePanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.donkey.model.message.SerializationType;

public class MirthTree extends JXTree implements DropTargetListener {

    private Frame parent;
    private MyFilter mf;
    private FilterTreeModel ftm;
    private String prefix;
    private String suffix;
    private DropTarget dropTarget;
    private DataFlavor supportedDropFlavor;

    // Default construct to allow viewing in netbeans
    public MirthTree() {
        this(null, null, null);
    }

    public MirthTree(MirthTreeNode root, String prefix, String suffix) {
        this.parent = PlatformUI.MIRTH_FRAME;

        setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                setFont(UIConstants.MONOSPACED_FONT);
                putClientProperty("html.disable", Boolean.TRUE);
                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }
        });

        mf = new MyFilter();
        ftm = new FilterTreeModel(root, mf);
        this.setModel(ftm);
        dropTarget = new DropTarget(this, this);
        this.prefix = prefix;
        this.suffix = suffix;

        if (prefix != null) {
            if (prefix.equals(MessageTreePanel.MAPPER_PREFIX)) {
                this.supportedDropFlavor = TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR;
            } else if (prefix.equals(MessageTreePanel.MESSAGE_BUILDER_PREFIX)) {
                this.supportedDropFlavor = TreeTransferable.MAPPER_DATA_FLAVOR;
            }
        }
    }

    public class FilterTreeModel extends DefaultTreeModel {

        Filter filter;

        public TreeNode getRoot() {
            return root;
        }

        public FilterTreeModel(TreeNode root, Filter filter) {
            super(root);
            this.filter = filter;
        }

        public void setFiltered(boolean pass) {
            this.filter.setFiltered(!pass);
        }

        public void updateTreeStructure() {
            if (this.getRoot() != null) {
                TreeNode tn = this.getRoot();
                int count = tn.getChildCount();
                Object[] path = { tn };
                int[] childIndices = new int[count];
                Object[] children = new Object[count];
                this.fireTreeStructureChanged(tn, path, childIndices, children);
            }
        }

        public boolean performFilter(TreeNode tn, String text, boolean exact, boolean ignoreChildren) {
            if (tn == null) {
                return false;
            }

            int realCount = super.getChildCount(tn);

            boolean passed = filter.pass(tn, text, exact) || ignoreChildren;
            boolean originalPassed = passed;

            for (int i = 0; i < realCount; i++) {
                boolean childPassed = performFilter(tn.getChildAt(i), text, exact, originalPassed);
                passed = passed || childPassed;
            }

            ((MirthTreeNode) tn).setFiltered(!passed);

            int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];

            return passed;
        }

        public int getChildCount(Object parent) {
            int realCount = super.getChildCount(parent);
            int visibleCount = 0;
            for (int i = 0; i < realCount; i++) {
                MirthTreeNode mtn = (MirthTreeNode) super.getChild(parent, i);
                if (!mtn.isFiltered()) {
                    visibleCount++;
                }
            }

            return visibleCount;
        }

        public Object getChild(Object parent, int index) {
            int cnt = -1;
            for (int i = 0; i < super.getChildCount(parent); i++) {
                MirthTreeNode child = (MirthTreeNode) super.getChild(parent, i);
                if (!child.isFiltered()) {
                    cnt++;
                }

                if (cnt == index) {
                    return child;
                }
            }
            return null;
        }
    }

    interface Filter {

        public boolean pass(Object obj, String text, boolean exact);

        public void setFiltered(boolean pass);

        public boolean isFiltered();
    }

    class MyFilter implements Filter {

        boolean pass = true;

        public boolean pass(Object obj, String text, boolean exact) {
            if (pass) {
                return true;
            }

            String s = obj.toString();
            if (exact) {
                return s.equals(text);
            } else {
                return s.toLowerCase().indexOf(text.toLowerCase()) != -1;
            }
        }

        public void setFiltered(boolean pass) {
            this.pass = pass;
        }

        public boolean isFiltered() {
            return pass;
        }
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();

            if (parent.currentContentPage == parent.channelEditPanel.transformerPane && tr.isDataFlavorSupported(supportedDropFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } catch (Exception e) {
            dtde.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent dte) {}

    public void dragOver(DropTargetDragEvent dtde) {
        if (!dtde.isDataFlavorSupported(supportedDropFlavor)) {
            return;
        }

        Point cursorLocationBis = dtde.getLocation();
        TreePath destinationPath = getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
        if (destinationPath != null) {
            if (((MirthTreeNode) destinationPath.getLastPathComponent()).isLeaf()) {
                this.setSelectionPath(destinationPath);
            }
        }
    }

    public void drop(DropTargetDropEvent dtde) {
        if (parent.currentContentPage != parent.channelEditPanel.transformerPane) {
            return;
        }

        try {
            TreeNode selectedNode = (TreeNode) this.getLastSelectedPathComponent();

            if (selectedNode == null) {
                return;
            }

            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable tr = dtde.getTransferable();

            if (supportedDropFlavor == TreeTransferable.MAPPER_DATA_FLAVOR) {
                Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                MapperDropData data = (MapperDropData) transferData;

                parent.channelEditPanel.transformerPane.addNewStep(constructMessageBuilderStepName(data.getNode(), selectedNode), constructPath(selectedNode, prefix, suffix).toString(), data.getMapping(), TransformerPane.MESSAGE_BUILDER);
            } else if (supportedDropFlavor == TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR) {
                Object transferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                MessageBuilderDropData data = (MessageBuilderDropData) transferData;
                parent.channelEditPanel.transformerPane.addNewStep(constructMessageBuilderStepName(selectedNode, data.getNode()), data.getMessageSegment(), constructPath(selectedNode, prefix, suffix).toString(), TransformerPane.MESSAGE_BUILDER);
            } else {
                dtde.rejectDrop();
            }
        } catch (Exception e) {
            dtde.rejectDrop();
        }

    }

    public void dropActionChanged(DropTargetDragEvent dtde) {}

    /**
     * Get the index of a node in relation to other nodes with the same name. Returns -1 if there
     * isn't an index.
     * 
     * @param node
     * @return
     */
    public static int getIndexOfNode(TreeNode node) {
        String nodeName = node.toString().replaceAll(" \\(.*\\)", ""); // Get the node name without the vocab
        TreeNode parent = node.getParent();

        // The parent will be null for the root node
        if (parent != null) {
            Enumeration children = parent.children();
            int indexCounter = 0;
            int foundIndex = -1;

            // Look through all of the children of the parent to see if there
            // are multiple children with the same name.
            while (children.hasMoreElements()) {
                TreeNode child = (TreeNode) children.nextElement();

                // Ignore the draggable nodes that have no children
                if (child.getChildCount() > 0) {
                    if (nodeName.equals(child.toString().replaceAll(" \\(.*\\)", ""))) {
                        if (child != node) {
                            indexCounter++;
                        } else {
                            foundIndex = indexCounter;
                            indexCounter++;
                        }
                    }
                }
            }

            // If there were multiple children, add the index to the nodeQ.
            if (indexCounter > 1) {
                return foundIndex;
            }
        }

        return -1;
    }

    /**
     * Construct a path for a specific node.
     * 
     * @param parent
     * @param prefix
     * @param suffix
     * @return
     */
    public static StringBuilder constructPath(TreeNode parent, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.insert(0, prefix);

        if (parent == null) {
            return sb.append(suffix);
        }

        MirthTreeNode node = (MirthTreeNode) parent;
        SerializationType serializationType = node.getSerializationType();

        // Get the parent if the leaf was actually passed in instead of the parent.
        if (node.isLeaf()) {
            node = (MirthTreeNode) node.getParent();
        }

        /*
         * MIRTH-3196 - We now assign nodes types as we iterate from child to parent to add more
         * versatility to namespace drag and drop. The drag and drop can drag nodes, attribute or
         * namespace attributes so the user should be able to correctly do these now. If a node has
         * a namespace then will wildcard it. If an ancestor of the node has an implicit namespace
         * then we will also append a wildcard. The only exception to this is if the implicit
         * namespace is on the root node, we actually set this to the default namespace in
         * JavaScriptBuilder.
         */

        LinkedList<PathNode> nodeQ = new LinkedList<PathNode>();
        while (node != null && node.getParent() != null) {
            if (serializationType.equals(SerializationType.JSON) && node.isArrayElement()) {
                nodeQ.add(new PathNode(String.valueOf(node.getParent().getIndex(node) - 1), PathNode.NodeType.ARRAY_CHILD));
            } else {

                PathNode.NodeType type = PathNode.NodeType.OTHER;
                if (serializationType.equals(SerializationType.XML)) {
                    type = getXmlNodeType(node);
                }

                String nodeValue = node.getValue().replaceAll(" \\(.*\\)", "");
                nodeQ.add(new PathNode(nodeValue, type));

                if (serializationType.equals(SerializationType.XML)) {
                    int parentIndexValue = getIndexOfNode(node);
                    if (parentIndexValue != -1) {
                        nodeQ.add(nodeQ.size() - 1, new PathNode(String.valueOf(parentIndexValue), PathNode.NodeType.ARRAY_CHILD));
                    }
                }
            }
            node = (MirthTreeNode) node.getParent();
        }

        boolean foundImplicitNamespace = false;

        while (!nodeQ.isEmpty()) {
            PathNode nodeValue = nodeQ.removeLast();

            //We start at the parent so if any implicit namespaces are reached then the rest of the nodes should wildcard the namespace
            boolean includeNamespace = false;
            PathNode.NodeType type = nodeValue.getType();

            //We don't want to include a wildcard for attributes, ns definitions or array indices
            if (serializationType.equals(SerializationType.XML) && !Arrays.asList(PathNode.NodeType.XML_ATTRIBUTE, PathNode.NodeType.XMLNS_DEFINITION, PathNode.NodeType.XML_PREFIX_DEFINITION, PathNode.NodeType.ARRAY_CHILD).contains(type)) {
                if (foundImplicitNamespace) {
                    includeNamespace = true;
                } else if (type == PathNode.NodeType.XML_XMLNS_NODE) {
                    foundImplicitNamespace = true;
                    includeNamespace = true;
                } else if (type == PathNode.NodeType.XML_PREFIXED_NODE) {
                    includeNamespace = true;
                }
            }

            if (includeNamespace) {
                int colonIndex = nodeValue.getValue().indexOf(':') + 1;
                sb.append(".*::['" + StringUtils.substring(nodeValue.getValue(), colonIndex) + "']");
            } else if (serializationType.equals(SerializationType.XML) && type == PathNode.NodeType.XMLNS_DEFINITION) {
                sb.append(".namespace('')");
            } else if (serializationType.equals(SerializationType.XML) && type == PathNode.NodeType.XML_PREFIX_DEFINITION) {
                sb.append(".namespace('" + StringUtils.substringAfter(nodeValue.getValue(), "@xmlns:") + "')");
            } else if(type == PathNode.NodeType.XML_PREFIXED_ATTRIBUTE){
                sb.append(".@*::['" + StringUtils.substringAfter(nodeValue.getValue(), ":") + "']");
            }else if (type == PathNode.NodeType.ARRAY_CHILD) {
                sb.append("[" + nodeValue.getValue() + "]");
            } else {
                sb.append("['" + nodeValue.getValue() + "']");
            }
        }

        if (!serializationType.equals(SerializationType.JSON)) {
            sb.append(suffix);
        }

        return sb;
    }

    private static class PathNode {

        public enum NodeType {
            ARRAY_CHILD, XML_PREFIXED_NODE, XML_XMLNS_NODE, XML_PREFIX_DEFINITION, XMLNS_DEFINITION, XML_ATTRIBUTE, XML_PREFIXED_ATTRIBUTE, OTHER
        }

        private String value;
        private NodeType type;

        public PathNode(String value, NodeType type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public NodeType getType() {
            return type;
        }
    }

    private static boolean nodeContainImplicitNamespace(MirthTreeNode treeNode) {
        MirthTreeNode childNode = null;
        Enumeration<TreeNode> children = treeNode.children();
        while (children.hasMoreElements()) {
            childNode = (MirthTreeNode) children.nextElement();
            if (StringUtils.equals(childNode.getValue().replaceAll(" \\(.*\\)", ""), "@xmlns")) {
                //this means that the node had a non prefixed namespace defined
                return true;
            }
        }
        return false;
    }

    private static PathNode.NodeType getXmlNodeType(MirthTreeNode node) {
        PathNode.NodeType type = null;
        String nodeValue = node.getValue().replaceAll(" \\(.*\\)", "");

        if (nodeContainImplicitNamespace(node)) {
            type = PathNode.NodeType.XML_XMLNS_NODE;
        } else if (StringUtils.startsWith(nodeValue, "@xmlns")) {
            if (StringUtils.contains(nodeValue, ":")) {
                type = PathNode.NodeType.XML_PREFIX_DEFINITION;
            } else {
                type = PathNode.NodeType.XMLNS_DEFINITION;
            }
        } else if (StringUtils.contains(nodeValue, "@")) {
            if (StringUtils.contains(nodeValue, ":")) {
                type = PathNode.NodeType.XML_PREFIXED_ATTRIBUTE;
            } else {
                type = PathNode.NodeType.XML_ATTRIBUTE;
            }
        } else if (StringUtils.contains(nodeValue, ":")) {
            type = PathNode.NodeType.XML_PREFIXED_NODE;
        }
        return type;
    }

    /**
     * Construct a variable for a specfic node.
     * 
     * @param parent
     * @return
     */
    public static String constructVariable(TreeNode parent) {
        String variable = "";

        if (parent == null) {
            return MessageTreePanel.MAPPER_PREFIX;
        }

        MirthTreeNode node = (MirthTreeNode) parent;
        SerializationType serializationType = node.getSerializationType();

        // Get the parent if the leaf was actually passed in instead of the parent.
        if (node.isLeaf()) {
            node = (MirthTreeNode) node.getParent();
        }

        boolean wasArrayElement = false;

        // Stop the loop as soon as the parent or grandparent is null,
        // because we don't want to include the root node.
        while (node != null && node.getParent() != null) {
            String parentName = node.getValue();
            Pattern pattern = Pattern.compile(" (\\(.*\\))");
            Matcher matcher = pattern.matcher(parentName.toString());

            if (serializationType.equals(SerializationType.JSON) && node.isArrayElement()) {
                if (variable.length() != 0) {
                    variable = "_" + variable;
                }
                variable = (node.getParent().getIndex(node) - 1) + variable; // JSON array
                wasArrayElement = true;
            } else {
                String parentIndex = "";
                if (serializationType.equals(SerializationType.XML)) {
                    // Get the index of the parent about to be added.
                    int parentIndexValue = MirthTree.getIndexOfNode(node);
                    if (parentIndexValue != -1) {
                        parentIndex += parentIndexValue;
                    }
                }

                // If something has already been added and last node processed wasn't an array element, then prepend it with an "_"
                if (variable.length() != 0 && !wasArrayElement) {
                    variable = "_" + variable;
                }

                // Add either the vocab (if there is one) or the name.
                if (matcher.find()) {
                    variable = removeInvalidVariableCharacters(matcher.group(1)) + parentIndex + variable;
                } else {
                    variable = removeInvalidVariableCharacters(node.getValue().replaceAll(" \\(.*\\)", "")) + parentIndex + variable;
                }

                wasArrayElement = false;
            }

            node = (MirthTreeNode) node.getParent();
        }

        return variable;
    }

    public static String constructMessageBuilderStepName(TreeNode in, TreeNode out) {
        if (in != null) {
            return constructNodeDescription(out) + " (out) <-- " + constructNodeDescription(in) + " (in)";
        } else {
            return constructNodeDescription(out) + " (out)";
        }
    }

    public static String constructNodeDescription(TreeNode parent) {
        String description = "";

        MirthTreeNode node = (MirthTreeNode) parent;
        SerializationType serializationType = node.getSerializationType();

        // Get the parent if the leaf was actually passed in instead of the parent.
        if (node.isLeaf()) {
            node = (MirthTreeNode) node.getParent();
        }

        boolean wasArrayElement = false;
        // Stop the loop as soon as the parent or grandparent is null,
        // because we don't want to include the root node.
        while (node != null && node.getParent() != null) {
            String parentName = node.getValue();
            Pattern pattern = Pattern.compile(" (\\(.*\\))");
            Matcher matcher = pattern.matcher(parentName.toString());

            // Get the index of the parent about to be added.
            String parentIndex = "";
            if (serializationType.equals(SerializationType.JSON) && node.isArrayElement()) {
                // Prepend " - " to description if last node processed was not an array element.
                if (!wasArrayElement) {
                    description = " - " + description;
                }
                description = " [" + (node.getParent().getIndex(node) - 1) + "]" + description; // JSON array
                wasArrayElement = true;
            } else {
                if (serializationType.equals(SerializationType.XML)) {
                    int parentIndexValue = MirthTree.getIndexOfNode(node);
                    if (parentIndexValue != -1) {
                        parentIndex = " [" + parentIndexValue + "]";
                    }
                }

                // Add either the vocab (if there is one) or the name.
                if (matcher.find()) {
                    String matchDescription = matcher.group(1);
                    matchDescription = matchDescription.substring(1, matchDescription.length() - 1);
                    // Also add the segment name for the last node if vocab was used.
                    description = matchDescription + parentIndex + (description.length() == 0 ? " (" + node.getValue().replaceAll(" \\(.*\\)", "") + ")" : " - ") + description;
                } else {
                    description = node.getValue() + parentIndex + (description.length() != 0 && !wasArrayElement ? " - " : "") + description;
                }

                wasArrayElement = false;
            }

            node = (MirthTreeNode) node.getParent();
        }

        return description;
    }

    /**
     * Remove invalid characters for variables and fix capitalization.
     * 
     * @param source
     * @return
     */
    private static String removeInvalidVariableCharacters(String source) {
        source = source.toLowerCase();
        source = source.replaceAll("\\/", " or ");
        source = source.replaceAll(" - ", "_");
        source = source.replaceAll("&", " and ");
        source = source.replace("@", "att ");
        source = source.replaceAll("[^a-zA-Z0-9_\\s]", ""); // get rid of everything not a letter, number, underscore, or space

        // Trim all whitespace and '.' from the variable
        source = source.trim();
        while (source.charAt(source.length() - 1) == '.' || source.charAt(0) == '.') {
            if (source.charAt(source.length() - 1) == '.') {
                source = source.substring(0, source.length() - 1);
            }
            if (source.charAt(0) == '.') {
                source = source.substring(1);
            }
            source = source.trim();
        }

        // camelCase the variable while removing spaces and periods.
        while (source.indexOf(' ') != -1 || source.indexOf('.') != -1) {
            int index = source.indexOf(' ');
            int index2 = source.indexOf('.');
            // If . before ' ' 
            if (index2 != -1 && index2 < index) {
                index = index2;
            }
            // if no space ' ' handle .  
            if (index == -1) {
                index = index2;
            }
            source = source.replaceFirst(" |\\.", "");
            source = source.substring(0, index) + source.substring(index, index + 1).toUpperCase() + source.substring(index + 1);
        }

//        if (source.equalsIgnoreCase("value"))
//        	source = "";

        return source;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }
}
