/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Convenience class to allow fluent building of lists.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListBuilder implements List {

    private List delegate;

    ListBuilder() {
        this(new ArrayList());
    }

    ListBuilder(Object e) {
        this(new ArrayList());
        add(e);
    }

    ListBuilder(List list) {
        this.delegate = list;
    }

    /**
     * Adds an element to the list using the {@link #add} method, and returns this builder.
     * 
     * @param e
     *            element to be appended to this list
     * @return This ListBuilder instance.
     */
    public ListBuilder append(Object e) {
        add(e);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator iterator() {
        return delegate.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Object e) {
        return delegate.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection c) {
        return delegate.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection c) {
        return delegate.addAll(index, c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(int index) {
        return delegate.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object set(int index, Object element) {
        return delegate.set(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, Object element) {
        delegate.add(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(int index) {
        return delegate.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator listIterator() {
        return delegate.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator listIterator(int index) {
        return delegate.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return delegate.toString();
    }
}