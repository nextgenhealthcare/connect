package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RP extends Composite {
	public _RP(){
		fields = new Class[]{_ST.class, _HD.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Pointer", "Application ID", "Type of Data", "Sub Type"};
		description = "Reference Pointer";
		name = "RP";
	}
}
