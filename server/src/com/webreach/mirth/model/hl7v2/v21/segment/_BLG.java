package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BLG extends Segment {
	public _BLG(){
		fields = new Class[]{_CM.class, _CM.class, _CM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"When to Charge", "Value Type", "Observation Identifier"};
		description = "Billing";
		name = "BLG";
	}
}
