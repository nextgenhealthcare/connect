package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SFT extends Segment {
	public _SFT(){
		fields = new Class[]{_XON.class, _ST.class, _ST.class, _ST.class, _TX.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Software Vendor Organization", "Software Certified Version or Release Number", "Software Product Name", "Software Binary ID", "Software Product Information", "Software Install Date"};
		description = "Software Segment";
		name = "SFT";
	}
}
