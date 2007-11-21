package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DLD extends Composite {
	public _DLD(){
		fields = new Class[]{_IS.class, _TS.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Discharge Location", "Effective Date"};
		description = "Discharge Location and Date";
		name = "DLD";
	}
}
