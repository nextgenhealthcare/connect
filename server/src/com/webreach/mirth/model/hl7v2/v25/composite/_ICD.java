package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _ICD extends Composite {
	public _ICD(){
		fields = new Class[]{_IS.class, _ID.class, _TS.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Certification Patient Type", "Certification Required", "Date/Time Certification Required"};
		description = "Insurance Certification Definition";
		name = "ICD";
	}
}
