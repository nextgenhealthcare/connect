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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertActionGroup")
public class AlertActionGroup {

    protected List<AlertAction> actions = new ArrayList<AlertAction>();
    protected String subject;
    protected String template;
    
    public AlertActionGroup() {
        
    }
    
    public AlertActionGroup (String subject, String template, List<AlertAction> actions) {
        this.subject = subject;
        this.template = template;
        this.actions = actions;
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
