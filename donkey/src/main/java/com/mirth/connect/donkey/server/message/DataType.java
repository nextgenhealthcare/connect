/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message;

import com.mirth.connect.donkey.model.message.XmlSerializer;

public class DataType {

    private String type;
    private XmlSerializer serializer;
    private BatchAdaptor batchAdaptor;
    private AutoResponder autoResponder;

    public DataType(String type, XmlSerializer serializer, BatchAdaptor batchAdaptor, AutoResponder autoResponder) {
        this.type = type;
        this.serializer = serializer;
        this.batchAdaptor = batchAdaptor;
        this.autoResponder = autoResponder;
    }

    public String getType() {
        return type;
    }

    public XmlSerializer getSerializer() {
        return serializer;
    }
    
    public BatchAdaptor getBatchAdaptor() {
    	return batchAdaptor;
    }

    public AutoResponder getAutoResponder() {
        return autoResponder;
    }
}