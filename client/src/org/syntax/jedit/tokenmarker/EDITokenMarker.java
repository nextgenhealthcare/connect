package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

/**
 * HL7 token marker.
 * 
 * @author Chris Lang
 */
public class EDITokenMarker extends CTokenMarker {
	public EDITokenMarker() {
		super(true, getKeywords());
	}

	public static KeywordMap getKeywords() {
		if (ccKeywords == null) {
			
		}
		return ccKeywords;
	}

	// private members
	private static KeywordMap ccKeywords;
}
