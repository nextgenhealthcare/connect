package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XON extends Composite {
	public _XON(){
		fields = new Class[]{_ST.class, _IS.class, _NM.class, _NM.class, _ID.class, _HD.class, _ID.class, _HD.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Organization Name", "Organization Name Type Code", "ID Number", "Check Digit", "Check Digit Scheme", "Assigning Authority", "Identifier Type Code", "Assigning Facility", "Name Representation Code", "Organization Identifier"};
		description = "Extended Composite Name and Identification Number For Organizations";
		name = "XON";
	}
}
