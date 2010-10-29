/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.thoughtworks.xstream.converters.basic;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class Base64StringConverter implements Converter {

    private static final Base64Encoder base64 = new Base64Encoder();

    public boolean canConvert(Class type) {
        return type.getName().equals("java.lang.String");
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.addAttribute("encoding", "base64");
        writer.setValue(base64.encode(((String) source).getBytes()));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        String encoding = reader.getAttribute("encoding");
        String data = reader.getValue();

        try {
            if ("base64".equalsIgnoreCase(encoding)) {
                return new String(base64.decode(data));
            } else {
                return data;
            }
        } catch (Exception e) {
            return data;
        }
    }
}
