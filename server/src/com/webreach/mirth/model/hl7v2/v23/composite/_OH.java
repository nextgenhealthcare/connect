package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _OH extends Composite {
	public _OH(){
		fields = new Class[]{_String.class, _TM.class, _TM.class, _TM.class, _TM.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Office Day", "Office Start Time", "Office End Time", "Provider Start Time", "Provider End Time"};
		description = "Office Hours";
		name = "OH";
	}
}
