package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OVR extends Segment {
	public _OVR(){
		fields = new Class[]{_CWE.class, _CWE.class, _TX.class, _XCN.class, _XCN.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Business Rule Override Type", "Business Rule Override Code", "Override Comments", "Override Entered By", "Override Authorized By"};
		description = "Override Segment";
		name = "OVR";
	}
}
