/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelGroup")
public class ChannelGroup implements Serializable, Migratable, Purgable, Cacheable<ChannelGroup> {

    public static final String DEFAULT_ID = "Default Group";
    public static final String DEFAULT_NAME = "[Default Group]";

    private String id;
    private String name;
    private Integer revision;
    private Calendar lastModified;
    private String description;
    private List<Channel> channels;

    public ChannelGroup() {
        this("", "");
    }

    public ChannelGroup(String name, String description) {
        this(UUID.randomUUID().toString(), name, description);
    }

    public ChannelGroup(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.revision = 1;
        this.lastModified = Calendar.getInstance();
        this.description = description;
        this.channels = new ArrayList<Channel>();
    }

    public ChannelGroup(ChannelGroup group) {
        id = group.getId();
        name = group.getName();
        revision = group.getRevision();
        lastModified = group.getLastModified();
        description = group.getDescription();
        channels = new ArrayList<Channel>();
        for (Channel channel : group.getChannels()) {
            channels.add(new Channel(channel.getId()));
        }
    }

    public static ChannelGroup getDefaultGroup() {
        ChannelGroup defaultGroup = new ChannelGroup(DEFAULT_ID, DEFAULT_NAME, "Channels not part of a group will appear here.");
        defaultGroup.setRevision(null);
        defaultGroup.setLastModified(null);
        return defaultGroup;
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
        this.name = name;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public void replaceChannelsWithIds() {
        if (CollectionUtils.isNotEmpty(channels)) {
            List<Channel> list = new ArrayList<Channel>();
            for (Channel channel : channels) {
                list.add(new Channel(channel.getId()));
            }
            channels = list;
        }
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}
    
    @Override
    public void migrate3_6_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("descriptionChars", PurgeUtil.countChars(description));
        purgedProperties.put("lastModified", lastModified);
        purgedProperties.put("channelCount", channels.size());
        return purgedProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false, null, "DEFAULT_ID", "DEFAULT_NAME");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("id=").append(id).append(", ");
        builder.append("name=").append(name).append(", ");
        builder.append("revision=").append(revision).append(", ");
        builder.append("lastModified=").append(lastModified).append(", ");
        builder.append("description=").append(description).append(']');
        return builder.toString();
    }

    @Override
    public ChannelGroup cloneIfNeeded() {
        return new ChannelGroup(this);
    }
}