package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DLN extends Composite {
	public _DLN(){
		fields = new Class[]{_ST.class, _IS.class, _DT.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"License Number", "Issuing State, Province, Country", "Expiration Date"};
		description = "Driver's License Number";
		name = "DLN";
	}
}
