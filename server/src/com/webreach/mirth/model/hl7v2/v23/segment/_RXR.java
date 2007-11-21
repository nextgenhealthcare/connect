package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RXR extends Segment {
	public _RXR(){
		fields = new Class[]{_CE.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Route", "Site", "Administration Device", "Administration Method"};
		description = "Pharmacy/Treatment route";
		name = "RXR";
	}
}
