/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.model.CalendarToStringStyle;
import com.mirth.connect.model.Parameter;

public class CodeTemplateFunctionDefinition {

    private String name;
    private List<Parameter> parameters;
    private String returnType;
    private String returnDescription;

    public CodeTemplateFunctionDefinition() {
        this(null);
    }

    public CodeTemplateFunctionDefinition(String name) {
        this(name, null);
    }

    public CodeTemplateFunctionDefinition(String name, List<Parameter> parameters) {
        this(name, parameters, null, null);
    }

    public CodeTemplateFunctionDefinition(String name, List<Parameter> parameters, String returnType, String returnDescription) {
        this.name = name;
        setParameters(parameters);
        this.returnType = returnType;
        this.returnDescription = returnDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters != null ? new ArrayList<Parameter>(parameters) : new ArrayList<Parameter>();
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public void setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
    }

    public String getTransferData() {
        if (StringUtils.isNotBlank(name)) {
            StringBuilder builder = new StringBuilder(name).append('(');

            if (CollectionUtils.isNotEmpty(parameters)) {
                for (Iterator<Parameter> it = parameters.iterator(); it.hasNext();) {
                    builder.append(it.next().getName());
                    if (it.hasNext()) {
                        builder.append(", ");
                    }
                }
            }

            return builder.append(')').toString();
        } else {
            return "Bad Function Definition!";
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
