package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CSP extends Segment {
	public _CSP(){
		fields = new Class[]{_CE.class, _TS.class, _TS.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Study Phase Identifier", "Date/Time Study Phase Began", "Date/Time Study Phase Ended", "Study Phase Evaluability"};
		description = "Clinical Study Phase";
		name = "CSP";
	}
}
