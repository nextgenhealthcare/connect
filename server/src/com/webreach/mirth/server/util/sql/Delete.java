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
