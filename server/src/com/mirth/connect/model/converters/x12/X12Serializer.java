/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.x12;

import java.util.Map;

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.model.converters.edi.EDISerializer;

public class X12Serializer extends EDISerializer {

	private boolean inferX12Delimiters = true;
	
	public static Map<String, String> getDefaultProperties() {
		Map<String, String> map = EDISerializer.getDefaultProperties();
		map.put("inferX12Delimiters", "true");
		return map;
	}

	public X12Serializer(Map x12Properties) {
		super(x12Properties);
		if (x12Properties == null) {
			return;
		}
		if (x12Properties.get("inferX12Delimiters") != null && ((String) x12Properties.get("inferX12Delimiters")).equals("false")) {
			this.inferX12Delimiters = false;
		}
	}

	public X12Serializer(boolean inferX12Delimiters) {
		super();
		this.inferX12Delimiters = inferX12Delimiters;
	}

	@Override
	public String toXML(String source) throws SerializerException {
		//TODO: Investigate this replace all - see if needed with new 1.7 fixes
		source = source.replaceAll("\\r\\n", "\n").replaceAll("\\n\\n", "\n");

		if (this.inferX12Delimiters) {
			String x12message = source;
			if (x12message.startsWith("ISA")) {
				super.setElementDelim(x12message.charAt(3) + "");
				super.setSubelementDelim(x12message.charAt(104) + "");
				super.setSegmentDelim(x12message.charAt(105) + "");
				// hack to handle newlines
				if (x12message.charAt(106) == '\n') {
					setSegmentDelim(getSegmentDelim() + x12message.charAt(106));
				}
			}
		}
		return super.toXML(source);
	}
}
