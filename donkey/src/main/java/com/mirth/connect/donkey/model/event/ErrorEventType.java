/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("errorEventType")
public enum ErrorEventType {
    PREPROCESSOR, FILTER, TRANSFORMER, USER_DEFINED_TRANSFORMER, RESPONSE_TRANSFORMER, SOURCE_CONNECTOR, DESTINATION_CONNECTOR, XML_CONVERSION;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
