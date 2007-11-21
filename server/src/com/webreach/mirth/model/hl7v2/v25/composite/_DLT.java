package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DLT extends Composite {
	public _DLT(){
		fields = new Class[]{_NR.class, _NM.class, _ID.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Normal Range", "Numeric Threshold", "Change Computation", "Days Retained"};
		description = "Delta";
		name = "DLT";
	}
}
