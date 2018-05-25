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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("driverInfo")
public class DriverInfo implements Serializable {
    private String className;
    private String name;
    private String template;
    private String selectLimit;

    public DriverInfo() {

    }

    public DriverInfo(String name, String className, String template, String selectLimit) {
        this.name = name;
        this.className = className;
        this.template = template;
        this.selectLimit = selectLimit;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(String selectLimit) {
        this.selectLimit = selectLimit;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName() + "[");
        builder.append("name=" + getName() + ", ");
        builder.append("className=" + getClassName() + ", ");
        builder.append("template=" + getTemplate() + ", ");
        builder.append("selectLimit=" + getSelectLimit());
        builder.append("]");
        return builder.toString();
    }
}
