/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("step")
public abstract class Step extends FilterTransformerElement {

    public Step() {}

    public Step(Step props) {
        super(props);
    }

    @Override
    public abstract Step clone();
}