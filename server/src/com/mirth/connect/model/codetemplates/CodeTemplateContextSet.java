/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CodeTemplateContextSet implements Set<ContextType>, Serializable {

    private Set<ContextType> delegate;

    public CodeTemplateContextSet(ContextType... contextTypes) {
        this(Arrays.asList(contextTypes));
    }

    public CodeTemplateContextSet(Collection<ContextType> contextTypes) {
        delegate = new HashSet<ContextType>(contextTypes);
    }

    public CodeTemplateContextSet addContext(ContextType... contextTypes) {
        addAll(Arrays.asList(contextTypes));
        return this;
    }

    public static CodeTemplateContextSet getGlobalContextSet() {
        return new CodeTemplateContextSet(ContextType.values());
    }

    public static CodeTemplateContextSet getChannelContextSet() {
        return getConnectorContextSet().addContext(ContextType.CHANNEL_DEPLOY, ContextType.CHANNEL_UNDEPLOY, ContextType.CHANNEL_PREPROCESSOR, ContextType.CHANNEL_POSTPROCESSOR, ContextType.CHANNEL_ATTACHMENT, ContextType.CHANNEL_BATCH);
    }

    public static CodeTemplateContextSet getConnectorContextSet() {
        return new CodeTemplateContextSet(ContextType.SOURCE_RECEIVER, ContextType.SOURCE_FILTER_TRANSFORMER, ContextType.DESTINATION_FILTER_TRANSFORMER, ContextType.DESTINATION_DISPATCHER, ContextType.DESTINATION_RESPONSE_TRANSFORMER);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<ContextType> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(ContextType e) {
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends ContextType> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Set) {
            return delegate.equals(obj);
        }
        return false;
    }
}