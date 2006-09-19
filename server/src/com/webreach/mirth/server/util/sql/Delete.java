package com.webreach.mirth.server.util.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Delete {
	private String table;
	private List<String> criteria;
	
	public Delete(String table) {
		this.table = table;
		this.criteria = new ArrayList<String>();
	}
	
	public void addCriteria(String criteria) {
		this.criteria.add(criteria);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM");
		builder.append("\n\t");
		builder.append(table);
		builder.append("\n");
		builder.append("WHERE");
		builder.append("\n");
		
		for (ListIterator<String> iter = criteria.listIterator(); iter.hasNext();) {
			String criteria = iter.next();
			builder.append("\t");
			builder.append(criteria);
			
			if (iter.hasNext()) {
				builder.append(" AND\n");	
			}
		}
		
		return builder.toString();
	}
}
