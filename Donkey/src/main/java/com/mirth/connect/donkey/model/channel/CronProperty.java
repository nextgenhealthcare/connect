/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("cronProperty")
public class CronProperty implements Serializable {
    private String description;
    private String expression;

    public CronProperty(String description, String expression) {
        this.description = description;
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}