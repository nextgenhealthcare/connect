/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import org.apache.commons.lang3.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

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
		    ObjectUtils.equals(this.getType(), transport.getType());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("type=" + getType().toString() + ", ");
		builder.append("]");
		return builder.toString();
	}
}
