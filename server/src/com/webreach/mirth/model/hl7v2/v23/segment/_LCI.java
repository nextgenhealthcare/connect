package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LCI extends Segment {
	public _LCI(){
		fields = new Class[]{_XON.class, _CX.class, _XAD.class, _XTN.class, _ST.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, -1, -1, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Location ID (internal)", "External ID", "Address", "Phone", "Comment", "Standing ID", "Revised By"};
		description = "Location (Sites) Information Segment";
		name = "LCI";
	}
}
