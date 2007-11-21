package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXR extends Segment {
	public _RXR(){
		fields = new Class[]{_CE.class, _CWE.class, _CE.class, _CWE.class, _CE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Route", "Administration Site", "Administration Device", "Administration Method", "Routing Instruction", "Administration Site Modifier"};
		description = "Pharmacy/Treatment Route";
		name = "RXR";
	}
}
