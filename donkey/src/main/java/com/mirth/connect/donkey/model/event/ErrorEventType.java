/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang3.text.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("errorEventType")
public enum ErrorEventType {
    ANY, SOURCE_CONNECTOR, DESTINATION_CONNECTOR, SERIALIZER, FILTER, TRANSFORMER, USER_DEFINED_TRANSFORMER, RESPONSE_VALIDATION, RESPONSE_TRANSFORMER, ATTACHMENT_HANDLER, DEPLOY_SCRIPT, PREPROCESSOR_SCRIPT, POSTPROCESSOR_SCRIPT, SHUTDOWN_SCRIPT;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
