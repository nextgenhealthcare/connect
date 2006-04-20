package com.webreach.mirth.core.util;

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