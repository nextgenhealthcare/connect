package com.webreach.mirth.model.hl7v2.v21.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TS extends Composite {
	public _TS(){
		fields = new Class[]{_Time.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Value"};
		description = "Time Stamp";
		name = "TS";
	}
}
