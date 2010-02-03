/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.core;

import java.util.Calendar;

import org.apache.commons.httpclient.NameValuePair;

public class ErrorDate {
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;

	public ErrorDate(Calendar calendar) {
		this.year = calendar.get(Calendar.YEAR);
		this.month = calendar.get(Calendar.MONTH);
		this.day = calendar.get(Calendar.DATE);
		this.hour = calendar.get(Calendar.HOUR_OF_DAY);
		this.minute = calendar.get(Calendar.MINUTE);
	}

	public ErrorDate() {
		this(Calendar.getInstance());
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public NameValuePair[] getAsParams() {
		return new NameValuePair[] { new NameValuePair("error[date_entered(1i)]", Integer.toString(year)), new NameValuePair("error[date_entered(2i)]", Integer.toString(month)), new NameValuePair("error[date_entered(3i)]", Integer.toString(day)), new NameValuePair("error[date_entered(4i)]", Integer.toString(hour)), new NameValuePair("error[date_entered(5i)]", Integer.toString(minute)) };
	}

}
