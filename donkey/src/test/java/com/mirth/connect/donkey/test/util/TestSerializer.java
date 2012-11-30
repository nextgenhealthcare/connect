/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;

public class TestSerializer implements XmlSerializer {
	
    @Override
    public boolean isTransformerRequired() {
        return false;
    }
    
    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return message;
    }

    @Override
    public String toXML(String message) throws SerializerException {
        return message;
    }

    @Override
    public String fromXML(String message) throws SerializerException {
        return message;
    }

}
