/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.serverlog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class ServerLogItem implements Serializable {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");

    private String serverId;
    private Long id;
    private String level;
    private Date date;
    private String threadName;
    private String category;
    private String lineNumber;
    private String message;
    private String throwableInformation;

    public ServerLogItem() {}

    public ServerLogItem(String message) {
        this(null, null, null, null, null, null, null, message, null);
    }

    public ServerLogItem(String serverId, Long id, String level, Date date, String threadName, String category, String lineNumber, String message, String throwableInformation) {
        this.serverId = serverId;
        this.id = id;
        this.level = level;
        this.date = date;
        this.threadName = threadName;
        this.category = category;
        this.lineNumber = lineNumber;
        this.message = message;
        this.throwableInformation = throwableInformation;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThrowableInformation() {
        return throwableInformation;
    }

    public void setThrowableInformation(String throwableInformation) {
        this.throwableInformation = throwableInformation;
    }

    @Override
    public String toString() {
        if (id != null) {
            StringBuilder builder = new StringBuilder();
            builder.append('[').append(DATE_FORMAT.format(date)).append("]  ");
            builder.append(level);
            builder.append("  (").append(category);
            if (StringUtils.isNotBlank(lineNumber)) {
                builder.append(':').append(lineNumber);
            }
            builder.append("): ").append(message);
            if (StringUtils.isNotBlank(throwableInformation)) {
                builder.append('\n').append(throwableInformation);
            }
            return builder.toString();
        } else {
            return message;
        }
    }
}
