package com.mirth.connect.userutil;

public class XmlUtil {
    public static String prettyPrint(String input) {
        return com.mirth.connect.util.XmlUtil.prettyPrint(input);
    }
    
    public static String decode(String entity) {
        return com.mirth.connect.util.XmlUtil.decode(entity);
    }
    
    public static String encode(char s) {
        return com.mirth.connect.util.XmlUtil.encode(s);
    }
    
    public static String encode(String s) {
        return com.mirth.connect.util.XmlUtil.encode(s);
    }
    
    public static String encode(char[] text, int start, int length) {
        return com.mirth.connect.util.XmlUtil.encode(text, start, length);
    }
}