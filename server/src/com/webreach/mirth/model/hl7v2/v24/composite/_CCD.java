package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CCD extends Composite {
	public _CCD(){
		fields = new Class[]{_ID.class, _TS.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"When to Charge Code", "Date/Time"};
		description = "Charge Time";
		name = "CCD";
	}
}
