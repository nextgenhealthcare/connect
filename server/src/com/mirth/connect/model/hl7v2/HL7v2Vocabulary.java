/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.hl7v2;

import com.mirth.connect.model.converters.DataTypeFactory;
import com.mirth.connect.model.util.MessageVocabulary;

public class HL7v2Vocabulary extends MessageVocabulary {
    private String version;

    public HL7v2Vocabulary(String version, String type) {
        super(version, type);
        this.version = version.replaceAll("\\.", "");
    }

    // For now we are going to use the large hashmap
    // TODO: 1.4.1 - Use hl7 model XML from JAXB to generate vocab in real-time
    public String getDescription(String elementId) {
        try {
            if (elementId.indexOf('.') < 0) {
                if (elementId.length() < 4) {
                    // we have a segment
                    return Component.getSegmentDescription(version, elementId);
                } else {
                    // We have a message (ADTA01)
                    return Component.getMessageDescription(version, elementId);
                }

            } else {
                String[] parts = elementId.split("\\.");
                if (parts.length == 3) {
                    // we have a complete node, PID.5.1
                    return Component.getCompositeFieldDescriptionWithSegment(version, elementId, false);
                } else if (parts.length == 2) {
                    // coule either be a segment or composite
                    // PID.5 or XPN.1
                    // Try segment first then composite
                    String description = "";
                    try {
                        description = Component.getSegmentFieldDescription(version, elementId, false);
                    } catch (Exception e) {
                        description = Component.getCompositeFieldDescription(version, elementId, false);
                    }
                    return description;

                } else {
                    return "";
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return "";
        }
        // return reference.getDescription(elementId, version);
    }

    public String getDataType() {
        return DataTypeFactory.HL7V2;
    }
}
