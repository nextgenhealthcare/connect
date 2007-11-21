package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM4 extends Segment {
	public _OM4(){
		fields = new Class[]{_NM.class, _ID.class, _TX.class, _NM.class, _CE.class, _CE.class, _CE.class, _TX.class, _TX.class, _CQ.class, _CQ.class, _TX.class, _ID.class, _CQ.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/ Observation Master File", "Derived Specimen", "Container Description", "Container Volume", "Container Units", "Specimen", "Additive", "Preparation", "Special Handling Requirements", "Normal Collection Volume", "Minimum Collection Volume", "Specimen Requirements", "Specimen Priorities", "Specimen Retention Time"};
		description = "Observations that Require Specimens";
		name = "OM4";
	}
}
