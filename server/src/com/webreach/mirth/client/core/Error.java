/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.core;

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
