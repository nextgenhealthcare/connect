/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

public abstract class MessageVocabulary {
    public MessageVocabulary(String version, String type) {}

    public abstract String getDescription(String elementId);

    public abstract String getDataType();
}
