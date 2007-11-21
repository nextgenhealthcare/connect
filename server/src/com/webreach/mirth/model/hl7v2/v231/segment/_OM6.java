package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM6 extends Segment {
	public _OM6(){
		fields = new Class[]{_NM.class, _TX.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/Observation Master File", "Derivation Rule"};
		description = "Observations that Are Calculated From Other Observations Segment";
		name = "OM6";
	}
}
