/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.datatype;

import com.mirth.connect.donkey.model.message.SerializationType;

public abstract class DeserializationProperties extends DataTypePropertiesGroup {

    /**
     * If this returns null, the data type default serialization type will be used.
     */
    public SerializationType getSerializationType() {
        return null;
    }

    public void setSerializationType(SerializationType serializationType) {}
}
