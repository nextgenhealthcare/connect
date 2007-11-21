package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCD extends Segment {
	public _PCD(){
		fields = new Class[]{_IS.class, _DT.class, _ST.class, _IS.class, _PPN.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Date Type", "When", "Comment", "Standing", "Revised By", "Date Reason Code"};
		description = "Date Information Segment";
		name = "PCD";
	}
}
