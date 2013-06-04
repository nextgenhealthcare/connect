package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("errorEventType")
public enum ErrorEventType {
    FILTER, TRANSFORMER, USER_DEFINED_TRANSFORMER, RESPONSE_TRANSFORMER, SOURCE_CONNECTOR, DESTINATION_CONNECTOR, XML_CONVERSION;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
