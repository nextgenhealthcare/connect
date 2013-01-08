/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.x12;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

public class X12Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			JAXBContext jc = JAXBContext.newInstance("com.mirth.connect.model.x12");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			// TransactionType collection=
			// (TransactionType)((JAXBElement<TransactionType>)unmarshaller.unmarshal(X12Test.class.getResourceAsStream("xml/837.4010.X097.xml"))).getValue();
			TransactionType collection = (TransactionType) ((JAXBElement<TransactionType>) unmarshaller.unmarshal(X12Test.class.getResourceAsStream("xml/997.4010.xml"))).getValue();
			System.out.println(collection.getId() + ": " + collection.getName());

			HashMap<String, String> mappings = new LinkedHashMap<String, String>();
			LoopType loop = collection.getLoop();
			processLoop(loop, mappings);
			for (Iterator iter = mappings.entrySet().iterator(); iter.hasNext();) {
				Entry element = (Entry) iter.next();
				System.out.println(element.getKey() + ": " + element.getValue());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addEntry(String name, String description, Map<String, String> mappings) {
		// hack way to format for our ui
		name = name.replaceAll("_LOOP", "").replaceAll("-", "");
		if (name.length() > 5) {
			name = name.substring(0, name.length() - 4) + "." + trimLeadingZero(name.substring(name.length() - 4, name.length() - 2)) + "." + trimLeadingZero(name.substring(name.length() - 2));
		} else if (name.length() > 3) {
			name = name.substring(0, name.length() - 2) + "." + trimLeadingZero(name.substring(name.length() - 2));
		}
		mappings.put(name.replace('-', '.'), description);
	}

	private static String trimLeadingZero(String name) {
		if (name.startsWith("0")) {
			return name.substring(1);
		} else {
			return name;
		}
	}

	private static void processLoop(LoopType loop, Map<String, String> mappings) {
		addEntry(loop.getXid(), loop.getName(), mappings);
		for (Iterator iter = loop.getSegmentOrLoopOrRepeat().iterator(); iter.hasNext();) {
			Object element = ((JAXBElement<?>) iter.next()).getValue();
			if (element instanceof SegmentType) {
				SegmentType segment = (SegmentType) element;
				processSegment(segment, mappings);
			} else if (element instanceof LoopType) {
				processLoop((LoopType) element, mappings);
			}
		}
	}

	private static void processSegment(SegmentType segment, Map<String, String> mappings) {
		addEntry(segment.getXid(), segment.getName(), mappings);
		for (Iterator iter = segment.getElementOrComposite().iterator(); iter.hasNext();) {
			Object component = iter.next();
			if (component instanceof CompositeType) {
				processComposite((CompositeType) component, mappings);
			} else if (component instanceof ElementType) {
				ElementType element = (ElementType) component;
				addEntry(element.getXid(), element.getName(), mappings);
			}
		}
	}

	private static void processComposite(CompositeType composite, Map<String, String> mappings) {
		addEntry(composite.getDataEle(), composite.getName(), mappings);
		for (Iterator<ElementType> iter = composite.getElement().iterator(); iter.hasNext();) {
			ElementType element = iter.next();
			addEntry(element.getXid(), element.getName(), mappings);

		}
	}
}
