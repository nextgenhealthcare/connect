/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertConnectors")
public class AlertConnectors {
    private Set<Integer> enabledConnectors = new HashSet<Integer>();
    private Set<Integer> disabledConnectors = new HashSet<Integer>();

    public Set<Integer> getEnabledConnectors() {
        return enabledConnectors;
    }

    public Set<Integer> getDisabledConnectors() {
        return disabledConnectors;
    }
}