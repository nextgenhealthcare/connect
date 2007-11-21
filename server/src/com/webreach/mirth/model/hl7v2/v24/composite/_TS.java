package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TS extends Composite {
	public _TS(){
		fields = new Class[]{_Time.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Time of An Event", "Degree of Precision"};
		description = "Time";
		name = "TS";
	}
}
