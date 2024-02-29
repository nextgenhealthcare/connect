/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.tree.DefaultMutableTreeNode;

import com.mirth.connect.donkey.model.message.SerializationType;

public class MirthTreeNode extends DefaultMutableTreeNode {

    private boolean filtered = false;
    private SerializationType serializationType = SerializationType.XML;
    private JSONType jsonType = JSONType.VALUE;
    private boolean isArrayElement = false;

    public MirthTreeNode(String nodeValue) {
        super(nodeValue);
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public SerializationType getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    public JSONType getJSONType() {
        return jsonType;
    }

    public void setJSONType(JSONType jsonType) {
        this.jsonType = jsonType;
    }

    public boolean isArrayElement() {
        return isArrayElement;
    }

    public void setArrayElement(boolean isArrayElement) {
        this.isArrayElement = isArrayElement;
    }

    public String getValue() {
        return String.valueOf(super.getUserObject());
    }

    @Override
    public String toString() {
        if (serializationType.equals(SerializationType.JSON)) {
            if (jsonType.equals(JSONType.ARRAY)) {
                return "[] " + super.toString();
            } else if (jsonType.equals(JSONType.OBJECT)) {
                return "{} " + super.toString();
            }
        }
        return super.toString();
    }

    public enum JSONType {
        VALUE, OBJECT, ARRAY;
    }
}
