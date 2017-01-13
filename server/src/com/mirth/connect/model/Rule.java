/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("rule")
public abstract class Rule extends FilterTransformerElement {

    public enum Operator {
        AND, OR, NONE
    }

    public Rule() {}

    public Rule(Rule props) {
        super(props);
        operator = props.getOperator();
    }

    private Operator operator;

    @Override
    public abstract Rule clone();

    public Operator getOperator() {
        return this.operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("operator", operator);
        return purgedProperties;
    }
}