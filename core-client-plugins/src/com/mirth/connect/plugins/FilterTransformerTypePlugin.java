/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.FilterTransformer;
import com.mirth.connect.model.FilterTransformerElement;

public abstract class FilterTransformerTypePlugin<T extends FilterTransformer<C>, C extends FilterTransformerElement> extends ClientPlugin {

    public FilterTransformerTypePlugin(String name) {
        super(name);
    }

    public abstract boolean isNameEditable();

    public abstract C newObject(String variable, String mapping);

    public abstract EditorPanel<C> getPanel();

    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(null, null);
    }

    public Pair<String, String> getIteratorInfo(C element) {
        return new ImmutablePair<String, String>(null, null);
    }

    public void setIteratorInfo(C element, String target, String outbound) {}

    public final void replaceIteratorVariables(C element, FilterTransformerTreeTableNode<T, C> parent) {
        replaceOrRemoveIteratorVariables(element, parent, true);
    }

    public final void removeIteratorVariables(C element, FilterTransformerTreeTableNode<T, C> parent) {
        replaceOrRemoveIteratorVariables(element, parent, false);
    }

    public void replaceOrRemoveIteratorVariables(C element, FilterTransformerTreeTableNode<T, C> parent, boolean replace) {}

    public boolean includesScrollPane() {
        return false;
    }

    public boolean onlySourceConnector() {
        return false;
    }

    public C getDefaults() {
        return getPanel().getDefaults();
    }

    public C getProperties() {
        return getPanel().getProperties();
    }

    public void setProperties(Mode mode, boolean response, C properties) {
        getPanel().resetInvalidProperties();
        getPanel().setProperties(properties);
    }

    public String checkProperties(C properties, boolean highlight) {
        getPanel().resetInvalidProperties();
        return getPanel().checkProperties(properties, highlight);
    }

    public void resetInvalidProperties() {
        getPanel().resetInvalidProperties();
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}