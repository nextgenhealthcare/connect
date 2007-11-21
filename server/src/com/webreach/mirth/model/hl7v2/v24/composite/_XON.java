package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XON extends Composite {
	public _XON(){
		fields = new Class[]{_ST.class, _IS.class, _NM.class, _NM.class, _ID.class, _HD.class, _IS.class, _HD.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Organization Name", "Organization Name Type Code", "ID Number (NM)", "Check Digit", "Code Identifying the Check Digit Scheme Employed", "Assigning Authority", "Identifier Type Code (IS)", "Assigning Facility ID", "Name Representation Code"};
		description = "Extended Composite Name and Identification Number For Organizations";
		name = "XON";
	}
}
