package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XON extends Composite {
	public _XON(){
		fields = new Class[]{_ST.class, _IS.class, _NM.class, _NM.class, _ID.class, _HD.class, _IS.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Organization Name", "Organization Name Type Code", "ID Number", "Check Digit", "Code Identifying the Check Digit", "Assigning Authority", "Identifier Type Code", "Assigned Facility"};
		description = "Extended Composite Name and ID Number";
		name = "XON";
	}
}
