package org.syntax.jedit.tokenmarker;

import org.syntax.jedit.KeywordMap;

public class NCPDPTokenMarker extends CTokenMarker {
	public NCPDPTokenMarker() {
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
