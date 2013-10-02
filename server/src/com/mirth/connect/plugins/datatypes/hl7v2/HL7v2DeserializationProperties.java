/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class HL7v2DeserializationProperties extends DeserializationProperties {
    private boolean useStrictParser = false;
    private boolean useStrictValidation = false;
    private String segmentDelimiter = "\\r";

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("useStrictParser", new DataTypePropertyDescriptor(useStrictParser, "Use Strict Parser", "Parse messages based upon strict HL7 specifications.", PropertyEditorType.BOOLEAN));
        properties.put("useStrictValidation", new DataTypePropertyDescriptor(useStrictValidation, "Validate in Strict Parser", "Validate messages using HL7 specifications (applies to Strict Parser only).", PropertyEditorType.BOOLEAN));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "This is the delimiter character(s) that will be used after each segment.", PropertyEditorType.STRING));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("useStrictParser") != null) {
                this.useStrictParser = (Boolean) properties.get("useStrictParser");
            }

            if (properties.get("useStrictValidation") != null) {
                this.useStrictValidation = (Boolean) properties.get("useStrictValidation");
            }

            if (properties.get("segmentDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("segmentDelimiter");
            }
        }
    }

    public boolean isUseStrictParser() {
        return useStrictParser;
    }

    public void setUseStrictParser(boolean useStrictParser) {
        this.useStrictParser = useStrictParser;
    }

    public boolean isUseStrictValidation() {
        return useStrictValidation;
    }

    public void setUseStrictValidation(boolean useStrictValidation) {
        this.useStrictValidation = useStrictValidation;
    }

    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }
}
