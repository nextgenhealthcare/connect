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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.webreach.mirth.util.EqualsUtil;

@XStreamAlias("archiveMetaData")
public class ArchiveMetaData implements Serializable {
	public enum Type {
		PLUGIN, CONNECTOR
	};


	private Type type;
	
	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof ArchiveMetaData)) {
			return false;
		}

		ArchiveMetaData transport = (ArchiveMetaData) that;

		return
			EqualsUtil.areEqual(this.getType(), transport.getType());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("type=" + getType().toString() + ", ");
		builder.append("]");
		return builder.toString();
	}
}
