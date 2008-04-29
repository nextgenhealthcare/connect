package com.webreach.mirth.util;

public class StringUtil {
	public static String convertLFtoCR(String str) {
		return str.replaceAll("\\r\\n|\\n", "\r");
	}
}
