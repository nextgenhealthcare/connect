package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCW extends Segment {
	public _PCW(){
		fields = new Class[]{_XON.class, _CX.class, _IS.class, _DR.class, _ST.class, _XAD.class, _XTN.class, _PPN.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Organization", "Organization ID", "Position", "Dates", "Practicing", "Address", "Phone", "Revised By", "Comment", "Standing"};
		description = "Work History Information Segment";
		name = "PCW";
	}
}
