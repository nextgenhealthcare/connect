package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CK extends Composite {
	public _CK(){
		fields = new Class[]{_NM.class, _NM.class, _ID.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"ID Number (NM)", "Check Digit", "Code Identifying the Check Digit Scheme Employed", "Assigning Authority"};
		description = "Composite ID with Check Digit";
		name = "CK";
	}
}
