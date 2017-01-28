/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.LinkedList;

import com.mirth.connect.util.ScriptBuilderException;

public interface FilterTransformerIterable<C extends FilterTransformerElement> {

    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException;

    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException;

    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException;
}