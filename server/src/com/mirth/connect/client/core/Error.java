/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

public class Error {
	private String stackTrace;
	private ErrorDate date;
	private String osName;
	private String osArchitecture;
	private String osVersion;
	private String javaVersion;

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public ErrorDate getDate() {
		return date;
	}

	public void setDate(ErrorDate date) {
		this.date = date;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsArchitecture() {
		return osArchitecture;
	}

	public void setOsArchitecture(String osArchitecture) {
		this.osArchitecture = osArchitecture;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public NameValuePair[] getAsParams() {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new NameValuePair("error[stack_trace]", stackTrace));
		list.add(new NameValuePair("error[os_name]", osName));
		list.add(new NameValuePair("error[os_architecture]", osArchitecture));
		list.add(new NameValuePair("error[os_version]", osVersion));
		list.add(new NameValuePair("error[java_version]", javaVersion));
		list.addAll(Arrays.asList(date.getAsParams()));

		return list.toArray(new NameValuePair[] {});
	}

}
