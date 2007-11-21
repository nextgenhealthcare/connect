package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _STA extends Segment {
	public _STA(){
		fields = new Class[]{_CX.class, _ST.class, _ST.class, _ST.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Question ID", "Response", "Have Documentation", "Comment", "Standing", "Revised By"};
		description = "Study Responses Segment";
		name = "STA";
	}
}
