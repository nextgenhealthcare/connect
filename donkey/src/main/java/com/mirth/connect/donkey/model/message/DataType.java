/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public class DataType {

    private String type;
    private XmlSerializer serializer;

    public DataType(String type, XmlSerializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public String getType() {
        return type;
    }

    public XmlSerializer getSerializer() {
        return serializer;
    }

}
