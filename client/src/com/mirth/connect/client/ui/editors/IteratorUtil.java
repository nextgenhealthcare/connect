/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorElement;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class IteratorUtil {

    public static <T extends FilterTransformer<C>, C extends FilterTransformerElement> String replaceOrRemoveIteratorVariables(String expression, TreeTableNode parent, boolean replace) {
        if (replace) {
            return replaceIteratorVariables(expression, parent);
        } else {
            return removeIteratorVariables(expression, parent);
        }
    }

    public static String replaceIteratorVariables(String expression, MirthTreeTable treeTable) {
        if (StringUtils.isNotBlank(expression)) {
            int selectedRow = treeTable.getSelectedRow();
            if (selectedRow >= 0) {
                TreePath path = treeTable.getPathForRow(selectedRow);
                if (path != null) {
                    return replaceIteratorVariables(expression, ((TreeTableNode) path.getLastPathComponent()).getParent());
                }
            }
        }
        return expression;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FilterTransformer<C>, C extends FilterTransformerElement> String replaceIteratorVariables(String expression, TreeTableNode parent) {
        if (StringUtils.isNotBlank(expression) && parent instanceof FilterTransformerTreeTableNode) {
            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) parent;
            String replaced = replaceIteratorVariables(expression, node.getParent());

            if (node.getElement() instanceof IteratorElement) {
                replaced = replaceIteratorVariables(replaced, (IteratorElement<C>) node.getElement());
            }

            return replaced;
        }

        return expression;
    }

    public static <C extends FilterTransformerElement> String replaceIteratorVariables(String expression, IteratorElement<C> element) {
        if (StringUtils.isNotBlank(expression)) {
            for (String prefix : element.getProperties().getPrefixSubstitutions()) {
                String indexVariable = "[" + element.getProperties().getIndexVariable() + "]";

                if (StringUtils.startsWith(expression, prefix) && !StringUtils.startsWith(expression, prefix + indexVariable)) {
                    String suffix = "msg" + StringUtils.removeStart(expression, prefix);
                    suffix = JavaScriptSharedUtil.removeNumberLiterals(suffix);
                    suffix = StringUtils.removeStart(suffix, "msg");

                    expression = prefix + indexVariable + suffix;
                }
            }
        }
        return expression;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FilterTransformer<C>, C extends FilterTransformerElement> String removeIteratorVariables(String expression, TreeTableNode parent) {
        if (StringUtils.isNotBlank(expression)) {
            while (parent != null && parent instanceof FilterTransformerTreeTableNode) {
                FilterTransformerTreeTableNode<T, C> parentIterator = (FilterTransformerTreeTableNode<T, C>) parent;

                if (parentIterator.isIteratorNode()) {
                    IteratorElement<C> iterator = (IteratorElement<C>) parentIterator.getElement();
                    String indexVariable = "[" + iterator.getProperties().getIndexVariable() + "]";

                    for (String prefix : iterator.getProperties().getPrefixSubstitutions()) {
                        if (StringUtils.startsWith(expression, prefix + indexVariable)) {
                            expression = prefix + StringUtils.removeStart(expression, prefix + indexVariable);
                        }
                    }
                }

                parent = parent.getParent();
            }
        }
        return expression;
    }

    public static List<String> getAncestorIndexVariables(TreeTableNode parent) {
        List<String> list = new ArrayList<String>();
        getAncestorIndexVariables(parent, list);
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <T extends FilterTransformer<C>, C extends FilterTransformerElement> void getAncestorIndexVariables(TreeTableNode parent, List<String> list) {
        if (parent != null && parent instanceof FilterTransformerTreeTableNode) {
            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) parent;
            getAncestorIndexVariables(node.getParent(), list);
            if (node.getElement() instanceof IteratorElement) {
                list.add(((IteratorElement<C>) node.getElement()).getProperties().getIndexVariable());
            }
        }
    }

    public static List<String> getDescendantIndexVariables(TreeTableNode node) {
        List<String> list = new ArrayList<String>();
        getDescendantIndexVariables(node, list);
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <T extends FilterTransformer<C>, C extends FilterTransformerElement> void getDescendantIndexVariables(TreeTableNode parent, List<String> list) {
        if (parent != null && parent instanceof FilterTransformerTreeTableNode) {
            FilterTransformerTreeTableNode<T, C> node = (FilterTransformerTreeTableNode<T, C>) parent;
            if (node.getElement() instanceof IteratorElement) {
                list.add(((IteratorElement<C>) node.getElement()).getProperties().getIndexVariable());
                for (Enumeration<? extends MutableTreeTableNode> en = node.children(); en.hasMoreElements();) {
                    getDescendantIndexVariables(en.nextElement(), list);
                }
            }
        }
    }

    public static String getValidIndexVariable(TreeTableNode parent, TreeTableNode node) {
        return getValidIndexVariable(getAncestorIndexVariables(parent), getDescendantIndexVariables(node));
    }

    public static String getValidIndexVariable(List<String> ancestorIndexVariables, List<String> descendantIndexVariables) {
        String indexVariable = "i";
        while (ancestorIndexVariables.contains(indexVariable) || descendantIndexVariables.contains(indexVariable)) {
            char ch = indexVariable.charAt(0);
            int len = indexVariable.length();
            ch++;
            if (ch > 'z') {
                ch = 'i';
                len++;
            }
            indexVariable = StringUtils.repeat(ch, len);
        }
        return indexVariable;
    }
}