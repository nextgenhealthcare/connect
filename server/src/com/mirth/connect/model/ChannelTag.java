/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelTag")
public class ChannelTag implements Serializable, Purgable {

    public static final int MAX_NAME_LENGTH = 24;
    public static final Pattern INVALID_NAME_PATTERN = Pattern.compile("[^a-zA-Z_0-9\\-\\s]");

    private String id;
    private String name;
    private Set<String> channelIds;
    private Color backgroundColor;

    public ChannelTag(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    public ChannelTag(String id, String name) {
        this(id, name, new HashSet<String>());
    }

    public ChannelTag(String id, String name, Set<String> channelIds) {
        this(id, name, channelIds, Color.lightGray);
    }

    public ChannelTag(String id, String name, Set<String> channelIds, Color backgroundColor) {
        this.id = id;
        this.name = fixName(name);
        this.channelIds = channelIds;
        this.backgroundColor = backgroundColor;
    }

    public ChannelTag(ChannelTag tag) {
        id = tag.getId();
        name = fixName(tag.getName());
        channelIds = new HashSet<String>(tag.getChannelIds());
        backgroundColor = tag.getBackgroundColor();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = fixName(name);
    }

    public Set<String> getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(Set<String> channelIds) {
        this.channelIds = channelIds;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public static String fixName(String name) {
        name = StringUtils.substring(INVALID_NAME_PATTERN.matcher(name).replaceAll(""), 0, MAX_NAME_LENGTH);
        if (StringUtils.isBlank(name)) {
            name = "_";
        }
        return name;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", getId());
        purgedProperties.put("nameChars", PurgeUtil.countChars(getName()));
        purgedProperties.put("channelCount", channelIds.size());
        purgedProperties.put("backgroundColor", getBackgroundColor());
        return purgedProperties;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, CalendarToStringStyle.instance()).toString();
    }
}
