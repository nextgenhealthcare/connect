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