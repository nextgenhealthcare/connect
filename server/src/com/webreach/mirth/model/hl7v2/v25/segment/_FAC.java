package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _FAC extends Segment {
	public _FAC(){
		fields = new Class[]{_EI.class, _ID.class, _XAD.class, _XTN.class, _XCN.class, _ST.class, _XAD.class, _XTN.class, _XCN.class, _ST.class, _XAD.class, _XTN.class};
		repeats = new int[]{0, 0, -1, 0, -1, -1, -1, -1, -1, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Facility ID", "Facility Type", "Facility Address", "Facility Telecommunication", "Contact Person", "Contact Title", "Contact Address", "Contact Telecommunication", "Signature Authority", "Signature Authority Title", "Signature Authority Address", "Signature Authority Telecommunication"};
		description = "Facility";
		name = "FAC";
	}
}
