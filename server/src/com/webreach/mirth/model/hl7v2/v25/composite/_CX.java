package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CX extends Composite {
	public _CX(){
		fields = new Class[]{_ST.class, _ST.class, _ID.class, _HD.class, _ID.class, _HD.class, _DT.class, _DT.class, _CWE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Check Digit", "Check Digit Scheme", "Assigning Authority", "Identifier Type Code", "Assigning Facility", "Effective Date", "Expiration Date", "Assigning Jurisdiction", "Assigning Agency or Department"};
		description = "Extended Composite ID with Check Digit";
		name = "CX";
	}
}
