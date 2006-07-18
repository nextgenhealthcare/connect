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


package com.webreach.mirth.managers.types;

public class MirthPropertyType {
	private String name;
	private String displayName;

	public MirthPropertyType(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if (o instanceof MirthPropertyType) {
			MirthPropertyType rhs = (MirthPropertyType) o;
			return (this.getName().equals(rhs.getName()) && this.getDisplayName().equals(rhs.getDisplayName()));
		}
		
		return false;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getClass().getSimpleName() + "[");
		buffer.append(getName() + ",");
		buffer.append(getDisplayName() + "]");
		return buffer.toString();
	}
}
