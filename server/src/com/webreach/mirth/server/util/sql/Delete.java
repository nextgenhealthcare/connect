package com.webreach.mirth.server.util.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Delete {
	private List<String> criteria;
	private String table;
	
	public Delete(String table) {
		criteria = new ArrayList<String>();
		this.table = table;
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
		
		for (ListIterator iter = criteria.listIterator(); iter.hasNext();) {
			String criteria = (String) iter.next();
			builder.append("\t");
			builder.append(criteria);
			
			if (iter.hasNext()) {
				builder.append(" AND\n");	
			}
		}
		
		return builder.toString();
	}
}
