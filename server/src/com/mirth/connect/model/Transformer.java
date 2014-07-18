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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.donkey.util.xstream.Base64StringConverter;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * A Transformer represents a script which is executed on each message passing
 * through the Connector with which the transformer is associated.
 * 
 */

@XStreamAlias("transformer")
public class Transformer implements Serializable, Migratable, Purgable {
    private List<Step> steps;

    @XStreamConverter(Base64StringConverter.class)
    private String inboundTemplate;

    @XStreamConverter(Base64StringConverter.class)
    private String outboundTemplate;

    private String inboundDataType;
    private String outboundDataType;
    private DataTypeProperties inboundProperties;
    private DataTypeProperties outboundProperties;

    public Transformer() {
        this.steps = new ArrayList<Step>();
    }

    public String getInboundDataType() {
        return this.inboundDataType;
    }

    public void setInboundDataType(String inboundDataType) {
        this.inboundDataType = inboundDataType;
    }

    public String getInboundTemplate() {
        return inboundTemplate;
    }

    public void setInboundTemplate(String inboundTemplate) {
        this.inboundTemplate = inboundTemplate;
    }

    public String getOutboundDataType() {
        return outboundDataType;
    }

    public void setOutboundDataType(String outboundDataType) {
        this.outboundDataType = outboundDataType;
    }

    public String getOutboundTemplate() {
        return outboundTemplate;
    }

    public void setOutboundTemplate(String outboundTemplate) {
        this.outboundTemplate = outboundTemplate;
    }

    public List<Step> getSteps() {
        return this.steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof Transformer)) {
            return false;
        }

        Transformer transformer = (Transformer) that;

        return ObjectUtils.equals(this.getSteps(), transformer.getSteps()) && ObjectUtils.equals(this.getInboundTemplate(), transformer.getInboundTemplate()) && ObjectUtils.equals(this.getOutboundTemplate(), transformer.getOutboundTemplate()) && ObjectUtils.equals(this.getInboundDataType(), transformer.getInboundDataType()) && ObjectUtils.equals(this.getOutboundDataType(), transformer.getOutboundDataType()) && ObjectUtils.equals(this.getInboundProperties(), transformer.getInboundProperties()) && ObjectUtils.equals(this.getOutboundProperties(), transformer.getOutboundProperties());
    }

    public DataTypeProperties getInboundProperties() {
        return inboundProperties;
    }

    public void setInboundProperties(DataTypeProperties inboundProperties) {
        this.inboundProperties = inboundProperties;
    }

    public DataTypeProperties getOutboundProperties() {
        return outboundProperties;
    }

    public void setOutboundProperties(DataTypeProperties outboundProperties) {
        this.outboundProperties = outboundProperties;
    }

    public void migrate3_0_1(DonkeyElement transformer) {
        for (DonkeyElement step : transformer.getChildElement("steps").getChildElements()) {
            step.getChildElement("data").removeAttribute("class");
        }
    }

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("inboundTemplateChars", PurgeUtil.countChars(inboundTemplate));
        purgedProperties.put("outboundTemplateChars", PurgeUtil.countChars(outboundTemplate));
        purgedProperties.put("inboundDataType", inboundDataType);
        purgedProperties.put("outboundDataType", outboundDataType);
        purgedProperties.put("inboundProperties", inboundProperties.getPurgedProperties());
        purgedProperties.put("outboundProperties", outboundProperties.getPurgedProperties());
        purgedProperties.put("steps", PurgeUtil.purgeList(steps));
        return purgedProperties;
    }
}
