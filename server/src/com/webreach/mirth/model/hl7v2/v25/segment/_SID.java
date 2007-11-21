package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SID extends Segment {
	public _SID(){
		fields = new Class[]{_CE.class, _ST.class, _ST.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Application/Method Identifier", "Substance Lot Number", "Substance Container Identifier", "Substance Manufacturer Identifier"};
		description = "Substance Identifier";
		name = "SID";
	}
}
