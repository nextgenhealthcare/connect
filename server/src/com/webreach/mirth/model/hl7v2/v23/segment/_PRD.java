package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PRD extends Segment {
	public _PRD(){
		fields = new Class[]{_CE.class, _XPN.class, _XAD.class, _PL.class, _XTN.class, _CE.class, _CM.class, _TS.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Role", "Provider Name", "Provider Address", "Provider Location", "Provider Communication Information", "Preferred Method of Contact", "Provider Identifiers", "Effective Start Date of Role", "Effective End Date of Role"};
		description = "Provider Data";
		name = "PRD";
	}
}
