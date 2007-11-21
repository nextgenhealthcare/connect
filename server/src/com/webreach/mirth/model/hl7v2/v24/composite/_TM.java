package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TM extends Composite {
	public _TM(){
		fields = new Class[]{_Time.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"Value"};
		description = "Time";
		name = "TM";
	}
}
