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
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class HL7v2SerializationProperties extends SerializationProperties {
    private boolean handleRepetitions = true;
    private boolean handleSubcomponents = true;
    private boolean useStrictParser = false;
    private boolean useStrictValidation = false;
    private boolean stripNamespaces = true;
    private String segmentDelimiter = "\\r";
    private boolean convertLineBreaks = true;

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("handleRepetitions", new DataTypePropertyDescriptor(handleRepetitions, "Parse Field Repetitions", "Parse field repetitions (applies to Non-Strict Parser only).", PropertyEditorType.BOOLEAN));
        properties.put("handleSubcomponents", new DataTypePropertyDescriptor(handleSubcomponents, "Parse Subcomponents", "Parse subcomponents (applies to Non-Strict Parser only).", PropertyEditorType.BOOLEAN));
        properties.put("useStrictParser", new DataTypePropertyDescriptor(useStrictParser, "Use Strict Parser", "Parse messages based upon strict HL7 specifications.", PropertyEditorType.BOOLEAN));
        properties.put("useStrictValidation", new DataTypePropertyDescriptor(useStrictValidation, "Validate in Strict Parser", "Validate messages using HL7 specifications (applies to Strict Parser only).", PropertyEditorType.BOOLEAN));
        properties.put("stripNamespaces", new DataTypePropertyDescriptor(stripNamespaces, "Strip Namespaces", "Strips namespace definitions from the transformed XML message (applies to Strict Parser only).", PropertyEditorType.BOOLEAN));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "This is the input delimiter character(s) expected to occur after each segment.", PropertyEditorType.STRING));
        properties.put("convertLineBreaks", new DataTypePropertyDescriptor(convertLineBreaks, "Convert Line Breaks", "Convert all styles of line breaks (CRLF, CR, LF) in the raw message to the segment delimiter. ", PropertyEditorType.BOOLEAN));
        
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("handleRepetitions") != null) {
                this.handleRepetitions = (Boolean) properties.get("handleRepetitions");
            }

            if (properties.get("handleSubcomponents") != null) {
                this.handleSubcomponents = (Boolean) properties.get("handleSubcomponents");
            }

            if (properties.get("useStrictParser") != null) {
                this.useStrictParser = (Boolean) properties.get("useStrictParser");
            }

            if (properties.get("useStrictValidation") != null) {
                this.useStrictValidation = (Boolean) properties.get("useStrictValidation");
            }

            if (properties.get("stripNamespaces") != null) {
                this.stripNamespaces = (Boolean) properties.get("stripNamespaces");
            }

            if (properties.get("segmentDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("segmentDelimiter");
            }

            if (properties.get("convertLineBreaks") != null) {
                this.convertLineBreaks = (Boolean) properties.get("convertLineBreaks");
            }
        }
    }

    public boolean isHandleRepetitions() {
        return handleRepetitions;
    }

    public void setHandleRepetitions(boolean handleRepetitions) {
        this.handleRepetitions = handleRepetitions;
    }

    public boolean isHandleSubcomponents() {
        return handleSubcomponents;
    }

    public void setHandleSubcomponents(boolean handleSubcomponents) {
        this.handleSubcomponents = handleSubcomponents;
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

    public boolean isStripNamespaces() {
        return stripNamespaces;
    }

    public void setStripNamespaces(boolean stripNamespaces) {
        this.stripNamespaces = stripNamespaces;
    }

    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }

    public boolean isConvertLineBreaks() {
        return convertLineBreaks;
    }

    public void setConvertLineBreaks(boolean convertLineBreaks) {
        this.convertLineBreaks = convertLineBreaks;
    }
}
