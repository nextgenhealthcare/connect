/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.ChannelGroup;

public class ChannelTableNameEntry implements Comparable<ChannelTableNameEntry> {

    private String name;

    public ChannelTableNameEntry(String name) {
        this.name = StringUtils.defaultString(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(ChannelTableNameEntry entry) {
        if (entry == null) {
            return 1;
        }
        if (StringUtils.equals(name, ChannelGroup.DEFAULT_NAME)) {
            return -1;
        } else if (StringUtils.equals(entry.getName(), ChannelGroup.DEFAULT_NAME)) {
            return 1;
        }
        return name.compareToIgnoreCase(entry.getName());
    }
}