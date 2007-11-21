package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SCV extends Composite {
	public _SCV(){
		fields = new Class[]{_IS.class, _IS.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Parameter Class", "Parameter Value"};
		description = "Scheduling Class Value Pair";
		name = "SCV";
	}
}
