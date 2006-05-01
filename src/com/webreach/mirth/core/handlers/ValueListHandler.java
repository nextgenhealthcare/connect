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


package com.webreach.mirth.core.handlers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ValueListHandler implements ValueListIterator {
	protected List list;
	protected ListIterator listIterator;

	public ValueListHandler() {
		
	}

	protected void setList(List list) throws IteratorException {
		this.list = list;

		if (list != null) {
			listIterator = list.listIterator();
		} else {
			throw new IteratorException("List empty");
		}
	}

	public Collection getList() {
		return list;
	}

	public int getSize() throws IteratorException {
		int size = 0;

		if (list != null) {
			size = list.size();
		} else {
			throw new IteratorException(); // No Data
		}

		return size;
	}

	public Object getCurrentElement() throws IteratorException {
		Object object = null;

		// will not advance iterator
		if (list != null) {
			int currIndex = listIterator.nextIndex();
			object = list.get(currIndex);
		} else {
			throw new IteratorException();
		}

		return object;
	}

	public List getPreviousElements(int count) throws IteratorException {
		LinkedList list = new LinkedList();
		Object object = null;
		int i = 0;

		if (listIterator != null) {
			while (listIterator.hasPrevious() && (i < count)) {
				object = listIterator.previous();
				list.add(object);
				i++;
			}
		} else {
			throw new IteratorException(); // No data
		}

		return list;
	}

	public List getNextElements(int count) throws IteratorException {
		LinkedList list = new LinkedList();
		Object object = null;
		int i = 0;

		if (listIterator != null) {
			while (listIterator.hasNext() && (i < count)) {
				object = listIterator.next();
				list.add(object);
				i++;
			}
		} else {
			throw new IteratorException(); // No data
		}

		return list;
	}

	public void resetIndex() throws IteratorException {
		if (listIterator != null) {
			listIterator = list.listIterator();
		} else {
			throw new IteratorException(); // No data
		}
	}
}
