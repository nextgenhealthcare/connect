package com.mirth.connect.model.alert;

import java.util.HashSet;
import java.util.Set;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("defaultTrigger")
public class DefaultTrigger extends ChannelTrigger implements AlertTrigger {

    private Set<ErrorEventType> errorAlertTypes = new HashSet<ErrorEventType>();
    private String regex;

    public DefaultTrigger(Set<ErrorEventType> errorAlertTypes, String regex) {
        this.errorAlertTypes = errorAlertTypes;
        this.regex = regex;
    }

    public Set<ErrorEventType> getErrorAlertTypes() {
        return errorAlertTypes;
    }

    public void setErrorAlertTypes(Set<ErrorEventType> errorAlertTypes) {
        this.errorAlertTypes = errorAlertTypes;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

}
