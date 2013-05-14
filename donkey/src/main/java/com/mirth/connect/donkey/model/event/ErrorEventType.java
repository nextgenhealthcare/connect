package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

public enum ErrorEventType {
    FILTER, TRANSFORMER, USER_DEFINED_TRANSFORMER, RESPONSE_TRANSFORMER, SOURCE_CONNECTOR, DESTINATION_CONNECTOR, XML_CONVERSION;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
