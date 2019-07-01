/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import com.mirth.connect.model.ChannelDependency;

public class ChannelDependencyException extends DirectedAcyclicGraphException {

    private ChannelDependency dependency;

    public ChannelDependencyException(ChannelDependency dependency) {
        this(dependency, null);
    }

    public ChannelDependencyException(ChannelDependency dependency, Throwable cause) {
        super("Conflicting dependencies found for IDs: " + dependency.getDependentId() + ", " + dependency.getDependencyId(), cause);
        this.dependency = dependency;
    }

    public ChannelDependency getDependency() {
        return dependency;
    }

    public void setDependency(ChannelDependency dependency) {
        this.dependency = dependency;
    }
}