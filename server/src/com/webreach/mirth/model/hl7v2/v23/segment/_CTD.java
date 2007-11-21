package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CTD extends Segment {
	public _CTD(){
		fields = new Class[]{_CE.class, _XPN.class, _XAD.class, _PL.class, _XTN.class, _CE.class, _CM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Role", "Contact Name", "Contact Address", "Contact Location", "Contact Communication Information", "Preferred Method of Contact", "Contact Identifiers"};
		description = "Contact Data";
		name = "CTD";
	}
}
