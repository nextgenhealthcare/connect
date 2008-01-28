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


package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.Calendar;

import com.webreach.mirth.util.EqualsUtil;

public class User implements Serializable {
	private Integer id;
	private String username;
	private String email;
	private String fullName;
	private String description;
	private String phoneNumber;
	private Calendar lastLogin;
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public Calendar getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Calendar lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof User)) {
			return false;
		}
		
		User user = (User) that;
		
		return
			EqualsUtil.areEqual(this.getId(), user.getId()) &&
			EqualsUtil.areEqual(this.getUsername(), user.getUsername()) &&
			EqualsUtil.areEqual(this.getEmail(), user.getEmail()) &&
			EqualsUtil.areEqual(this.getFullName(), user.getFullName()) &&
			EqualsUtil.areEqual(this.getDescription(), user.getDescription()) &&
			EqualsUtil.areEqual(this.getLastLogin(), user.getLastLogin()) &&
			EqualsUtil.areEqual(this.getPhoneNumber(), user.getPhoneNumber());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("username=" + getUsername() + ", ");
		builder.append("email=" + getEmail() + ", ");
		builder.append("fullname=" + getFullName() + ", ");
		builder.append("description=" + getDescription() + ", ");
		builder.append("lastLogin=" + getLastLogin() + ", ");
		builder.append("phonenumber=" + getPhoneNumber());
		builder.append("]");
		return builder.toString();
	}
}
