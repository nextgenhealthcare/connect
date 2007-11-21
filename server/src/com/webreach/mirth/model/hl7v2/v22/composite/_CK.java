package com.webreach.mirth.model.hl7v2.v22.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CK extends Composite {
	public _CK(){
		fields = new Class[]{_NM.class, _NM.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"ID Number", "Check Digit"};
		description = "Composite ID with Check Digit";
		name = "CK";
	}
}
