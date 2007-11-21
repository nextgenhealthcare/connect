package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TS extends Composite {
	public _TS(){
		fields = new Class[]{_DTM.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Time", "Degree of Precision"};
		description = "Time Stamp";
		name = "TS";
	}
}
