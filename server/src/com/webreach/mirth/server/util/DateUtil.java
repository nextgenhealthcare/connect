package com.webreach.mirth.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static Date getDate(String pattern, String date) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.parse(date);
	}

	public static String formatDate(String pattern, Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}
	
	public static String getCurrentDate(String pattern){
		return formatDate(pattern, new Date());
	}
	public static String convertDate(String inPattern, String outPattern, String date) throws Exception{
		Date newDate = getDate(inPattern, date);
		return formatDate(outPattern, newDate);
	}
}
