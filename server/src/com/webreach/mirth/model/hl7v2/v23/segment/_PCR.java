package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCR extends Segment {
	public _PCR(){
		fields = new Class[]{_XCN.class, _CX.class, _IS.class, _ST.class, _ST.class, _XAD.class, _XTN.class, _IS.class, _PPN.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Reference", "External ID", "Position", "Description", "Is Board Certified", "Address", "Phone", "Specialty", "Revised By", "Comment", "Standing"};
		description = "Peer Reference Information Segment";
		name = "PCR";
	}
}
