/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Object to hold information about a database table.
 * Table objects will be sorted based on the table name.
 * 
 */
public class Table implements Serializable, Comparable<Table> {
	private String name;	// table name
	private List<Column> columns;	//list of columns for this table
	
	public Table(String name, List<Column> columns) {
		super();
		this.name = name;
		this.columns = columns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (!(obj instanceof Table)) return false;
				
		Table table = (Table) obj;
		if (name != null && !name.equals(table.getName())) return false;
		if (columns != null && !columns.equals(table.getColumns())) return false;		
		
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		hashCode = 31*hashCode + (name == null ? 0 : name.hashCode());
		hashCode = 31*hashCode + (columns == null ? 0 : columns.hashCode()); 
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("name=" + getName() + ", ");
		
		StringBuilder columnsList = new StringBuilder();
		for (Iterator<Column> i = columns.iterator(); i.hasNext(); ) {
			Column col = i.next();
			columnsList.append(col.toString());
			if (i.hasNext()) {
				columnsList.append(", ");
			}		
		}
		builder.append("columns=" + columnsList + ", ");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(Table table) {
		return name.compareTo(table.getName());
	}
}
