/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.ArrayList;
import java.util.Collection;

public class Parameters extends ArrayList<Parameter> {

    public Parameters() {}

    public Parameters(Collection<? extends Parameter> c) {
        super(c);
    }

    public Parameters(String name, String type, String description) {
        add(new Parameter(name, type, description));
    }

    public Parameters add(String name, String type, String description) {
        add(new Parameter(name, type, description));
        return this;
    }
}