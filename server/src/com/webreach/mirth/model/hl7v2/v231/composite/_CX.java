package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CX extends Composite {
	public _CX(){
		fields = new Class[]{_ID.class, _NM.class, _ID.class, _HD.class, _IS.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID", "Check Digit", "Code Identifying the Check Digit Scheme Employed", "Assigning Authority", "Identifier Type Code", "Assigning Facility"};
		description = "Extended Composite ID with Check Digit";
		name = "CX";
	}
}
