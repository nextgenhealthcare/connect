package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM5 extends Segment {
	public _OM5(){
		fields = new Class[]{_NM.class, _CE.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/Observation Master File", "Test/Observations Included Within An Ordered Test Battery", "Observation ID Suffixes"};
		description = "Observation Batteries (sets)";
		name = "OM5";
	}
}
