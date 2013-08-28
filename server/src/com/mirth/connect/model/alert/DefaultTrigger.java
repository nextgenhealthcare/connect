/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.HashSet;
import java.util.Set;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("defaultTrigger")
public class DefaultTrigger extends ChannelTrigger implements AlertTrigger, Migratable {

    private Set<ErrorEventType> errorEventTypes;
    private String regex;

    public DefaultTrigger() {
        this(new HashSet<ErrorEventType>(), "");
    }

    public DefaultTrigger(Set<ErrorEventType> errorAlertTypes, String regex) {
        this.errorEventTypes = errorAlertTypes;
        this.regex = regex;
    }

    public Set<ErrorEventType> getErrorEventTypes() {
        return errorEventTypes;
    }

    public void setErrorEventTypes(Set<ErrorEventType> errorEventTypes) {
        this.errorEventTypes = errorEventTypes;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

}
