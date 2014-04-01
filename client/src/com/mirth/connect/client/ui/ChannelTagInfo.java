/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChannelTagInfo {

    private boolean enabled;
    private Set<String> tags = new HashSet<String>();
    private Set<String> visibleTags = new HashSet<String>();

    public ChannelTagInfo() {}

    public ChannelTagInfo(ChannelTagInfo channelTagInfo) {
        enabled = channelTagInfo.isEnabled();
        tags.addAll(channelTagInfo.getTags());
        visibleTags.addAll(channelTagInfo.getVisibleTags());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
        updateVisibleTags();
    }

    public Set<String> getVisibleTags() {
        return visibleTags;
    }

    public void setVisibleTags(Set<String> visibleTags) {
        this.visibleTags = visibleTags;
        updateVisibleTags();
    }

    private void updateVisibleTags() {
        for (Iterator<String> it = visibleTags.iterator(); it.hasNext();) {
            if (!tags.contains(it.next())) {
                it.remove();
            }
        }

        if (visibleTags.isEmpty()) {
            enabled = false;
        }
    }
}