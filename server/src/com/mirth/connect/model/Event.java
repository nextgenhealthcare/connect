/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("event")
public class Event implements Serializable, Exportable {
    public static final String ATTR_EXCEPTION = "Exception";

    public enum Level {
        INFORMATION, WARNING, ERROR
    }

    public enum Outcome {
        SUCCESS, FAILURE
    }

    private int id;
    private Calendar dateTime;
    private Level level = Level.INFORMATION;
    private String name;
    private Map<String, String> attributes = new LinkedHashMap<String, String>();
    private Outcome outcome = Outcome.SUCCESS;
    private int userId = 0;
    private String ipAddress;

    public Event() {

    }

    public Event(String name) {
        this.name = name;
    }
    
    public Event(String name, Level level, Outcome outcome, Map<String, String> attributes) {
        this.name = name;
        this.level = level;
        this.outcome = outcome;
        this.attributes = attributes;
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

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }

        attributes.put(name, value);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }

    public static String getExportHeader() {
        return "ID, Date and Time, Level, Outcome, Operation, Name, User ID, IP Address, Attributes";
    }

    public String toExportString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id + ", ");
        builder.append(new SimpleDateFormat(Exportable.DATE_TIME_FORMAT).format(dateTime.getTime()) + ", ");
        builder.append(level + ", ");
        builder.append(outcome + ", ");
        builder.append(name + ", ");
        builder.append(userId + ", ");
        builder.append(ipAddress + ", ");

        /*
         * Print out the attributes and Base64 encode them in case there are
         * newlines.
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        MapUtils.verbosePrint(ps, "attributes", attributes);
        builder.append(Base64.encodeBase64URLSafeString(baos.toByteArray()));
        builder.append(System.getProperty("line.separator"));
        IOUtils.closeQuietly(ps);

        return builder.toString();
    }
}
