package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NPU extends Segment {
	public _NPU(){
		fields = new Class[]{_ID.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Bed Location", "Bed Status"};
		description = "Bed Status Update";
		name = "NPU";
	}
}
