/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.alert;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.donkey.util.migration.Migratable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertActionGroup")
public class AlertActionGroup implements Migratable {

    protected List<AlertAction> actions;
    protected String subject;
    protected String template;

    public AlertActionGroup() {
        actions = new ArrayList<AlertAction>();
    }

    public List<AlertAction> getActions() {
        return actions;
    }

    public void setActions(List<AlertAction> actions) {
        this.actions = actions;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

}