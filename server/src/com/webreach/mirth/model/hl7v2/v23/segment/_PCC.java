package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCC extends Segment {
	public _PCC(){
		fields = new Class[]{_XON.class, _CX.class, _ST.class, _ST.class, _ST.class, _ST.class, _XAD.class, _XTN.class, _ST.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Party", "External ID", "Description", "Nature", "Size", "Conflict", "Address", "Phone", "Comment", "Standing", "Revised By"};
		description = "Conflict of Information Segment";
		name = "PCC";
	}
}
