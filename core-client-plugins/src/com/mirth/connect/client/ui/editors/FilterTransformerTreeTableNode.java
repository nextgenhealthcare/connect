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
import java.util.List;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.mirth.connect.client.ui.editors.BaseEditorPaneBase.OperatorNamePair;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorElement;

public abstract class FilterTransformerTreeTableNode<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends AbstractMutableTreeTableNode {

    protected BaseEditorPaneBase<T, C> editorPane;
    protected C element;

    public FilterTransformerTreeTableNode(BaseEditorPaneBase<T, C> editorPane, C element) {
        this.editorPane = editorPane;
        setElement(element);
    }

    public C getElement() {
        return element;
    }

    @SuppressWarnings("unchecked")
    public C getElementWithChildren() {
        if (element instanceof IteratorElement) {
            IteratorElement<C> iteratorElement = (IteratorElement<C>) element;
            List<C> children = new ArrayList<C>();
            for (int i = 0; i < getChildCount(); i++) {
                children.add(((FilterTransformerTreeTableNode<T, C>) getChildAt(i)).getElementWithChildren());
            }
            iteratorElement.getProperties().setChildren(children);
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    public void setElement(C element) {
        this.element = (C) element.clone();
    }

    public boolean isIteratorNode() {
        return element instanceof IteratorElement;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int column) {
        if (column == editorPane.getNumColumn()) {
            return element.getSequenceNumber();
        } else if (column == editorPane.getNameColumn()) {
            return new OperatorNamePair(element.getName());
        } else if (column == editorPane.getTypeColumn()) {
            return element.getType();
        } else if (column == editorPane.getEnabledColumn()) {
            return element.isEnabled();
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int column) {
        if (column == editorPane.getNumColumn()) {
            element.setSequenceNumber((String) value);
        } else if (column == editorPane.getNameColumn()) {
            element.setName(((OperatorNamePair) value).getName());
        } else if (column == editorPane.getEnabledColumn()) {
            element.setEnabled((Boolean) value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FilterTransformerTreeTableNode) {
            try {
                return element.equals(((FilterTransformerTreeTableNode<T, C>) obj).getElement());
            } catch (Exception e) {
            }
        }
        return false;
    }
}