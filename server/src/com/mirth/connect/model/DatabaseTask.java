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
import java.util.Calendar;

import org.apache.commons.lang3.text.WordUtils;

public class DatabaseTask implements Serializable {

    public enum Status {
        IDLE, RUNNING;

        @Override
        public String toString() {
            return WordUtils.capitalizeFully(super.toString());
        }
    }

    private String id;
    private Status status;
    private String name;
    private String description;
    private String confirmationMessage;
    private Calendar startDateTime;

    public DatabaseTask(String id, String name, String description) {
        this(id, name, description, null);
    }

    public DatabaseTask(String id, String name, String description, String confirmationMessage) {
        this.id = id;
        this.status = Status.IDLE;
        this.name = name;
        this.description = description;
        this.confirmationMessage = confirmationMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public Calendar getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Calendar startDateTime) {
        this.startDateTime = startDateTime;
    }

    @Override
    public String toString() {
        return name;
    }
}