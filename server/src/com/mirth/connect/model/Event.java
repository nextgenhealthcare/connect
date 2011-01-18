/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("event")
public class Event implements Serializable {
    public enum Level {
        NORMAL, HIGH
    }

    private int id;
    private Calendar date;
    private Level level;
    private String event;
    private String description;
    private Properties attributes;

    public Event() {
        this.level = Level.NORMAL;
        this.description = new String();
        this.attributes = new Properties();
    }

    public Event(String event) {
        this.event = event;
        this.level = Level.NORMAL;
        this.description = new String();
        this.attributes = new Properties();
    }

    public Calendar getDate() {
        return this.date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String event) {
        this.description = event;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Properties getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Properties attributes) {
        this.attributes = attributes;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof Event)) {
            return false;
        }

        Event event = (Event) that;

        return ObjectUtils.equals(this.getId(), event.getId()) && ObjectUtils.equals(this.getLevel(), event.getLevel()) && ObjectUtils.equals(this.getEvent(), event.getEvent()) && ObjectUtils.equals(this.getDescription(), event.getDescription()) && ObjectUtils.equals(this.getDate(), event.getDate()) && ObjectUtils.equals(this.getAttributes(), event.getAttributes());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName() + "[");
        builder.append("id=" + getId() + ", ");
        builder.append("level=" + getLevel() + ", ");
        builder.append("event=" + getEvent() + ", ");
        builder.append("description=" + getDescription() + ", ");
        builder.append("date=" + getDate());
        builder.append("]");
        return builder.toString();
    }
}
