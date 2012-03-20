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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ibatis.sqlmap.client.extensions.ParameterSetter;
import com.ibatis.sqlmap.client.extensions.ResultGetter;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MapTypeHandler implements TypeHandlerCallback {
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

    public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
        Map parameterMap = (Map) parameter;
        Map map = new HashMap();
        
        // convert the values in the variable map to strings
        for (Iterator iter = parameterMap.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            String keyName = new String();
            if (entry.getKey() != null){
                keyName = entry.getKey().toString();
                String keyValue = new String();
                if (entry.getValue() == null) {
                    keyValue = "";
                }
                else {
                    keyValue = entry.getValue().toString();
                }
                map.put(keyName, keyValue);
            }
        }

        setter.setString(serializer.toXML(map));
    }

    public Object getResult(ResultGetter getter) throws SQLException {
        return (Map) serializer.fromXML(getter.getString());
    }

    public Object valueOf(String source) {
        return source;
    }
}