package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RI extends Composite {
	public _RI(){
		fields = new Class[]{_IS.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Repeat Pattern", "Explicit Time Interval"};
		description = "Repeat Interval";
		name = "RI";
	}
}
