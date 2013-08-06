/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

public class XmlUtil {
    public static String prettyPrint(String input) {
        return com.mirth.connect.util.MirthXmlUtil.prettyPrint(input);
    }

    public static String decode(String entity) {
        return com.mirth.connect.util.MirthXmlUtil.decode(entity);
    }

    public static String encode(char s) {
        return com.mirth.connect.util.MirthXmlUtil.encode(s);
    }

    public static String encode(String s) {
        return com.mirth.connect.util.MirthXmlUtil.encode(s);
    }

    public static String encode(char[] text, int start, int length) {
        return com.mirth.connect.util.MirthXmlUtil.encode(text, start, length);
    }
}