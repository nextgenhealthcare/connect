/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.util;

import com.webreach.mirth.model.MessageObject.Protocol;

public class DefaultVocabulary extends MessageVocabulary {

    public DefaultVocabulary(String version, String type) {
        super(version, type);
        // TODO Auto-generated constructor stub
    }

    public String getDescription(String elementId) {
        return new String();
    }

    @Override
    public Protocol getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

}
