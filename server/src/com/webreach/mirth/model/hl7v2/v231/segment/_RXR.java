package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXR extends Segment {
	public _RXR(){
		fields = new Class[]{_CE.class, _CE.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Route", "Site", "Administration Device", "Administration Method", "Routing Instruction"};
		description = "Pharmacy/Treatment Route";
		name = "RXR";
	}
}
