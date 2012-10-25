/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.sqlmap.extensions;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class SerializedObjectTypeHandler implements TypeHandlerCallback {
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        setter.setString(serializer.toXML(parameter));
    }

    public Object getResult(ResultGetter getter) throws SQLException {
        return serializer.fromXML(getter.getString());
    }

    public Object valueOf(String source) {
        return source;
    }
}